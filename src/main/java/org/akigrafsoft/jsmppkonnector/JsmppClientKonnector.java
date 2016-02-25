/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.jsmppkonnector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.MessageId;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.ExceptionCreateSessionFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnector;

public class JsmppClientKonnector extends SessionBasedClientKonnector {

	public JsmppClientKonnector(String name) throws ExceptionDuplicate {
		super(name);
	}

	@Override
	public Class<? extends KonnectorConfiguration> getConfigurationClass() {
		return JsmppClientConfiguration.class;
	}

	@Override
	protected void doLoadConfig(KonnectorConfiguration config) {
		super.doLoadConfig(config);
	}

	@Override
	protected void execute(KonnectorDataobject dataobject, Session session) {
		SmsDataobject sms = (SmsDataobject) dataobject;
		final SMPPSession smppSession = (SMPPSession) session.getUserObject();
		try {
			String messageId = smppSession.submitShortMessage(sms.serviceType, sms.sourceAddrTon, sms.sourceAddrNpi,
					sms.sourceAddr, sms.destAddrTon, sms.destAddrNpi, sms.destinationAddr, sms.esmClass, sms.protocolId,
					sms.priorityFlag, sms.scheduleDeliveryTime, sms.validityPeriod, sms.registeredDelivery,
					sms.replaceIfPresentFlag, sms.dataCoding, sms.smDefaultMsgId, sms.outboundBuffer.getBytes());
			// sms.optionalParameters);

			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(buildActivityLog(dataobject.getMessage(),
						"Sent sms <" + sms.outboundBuffer.getBytes() + ">(" + messageId + ")"));
			sms.messageId = new MessageId(messageId);
		} catch (PDUException | InvalidResponseException | NegativeResponseException e) {
			this.notifyFunctionalError(dataobject, e.getMessage());
			return;
		} catch (ResponseTimeoutException | IOException e) {
			this.notifyNetworkError(dataobject, session, e.getMessage());
			return;
		}

		notifyExecuteCompleted(dataobject);
	}

	@Override
	protected void createSession(Session session) throws ExceptionCreateSessionFailed {
		session.setUserObject(new SMPPSession());
	}

	class SessionMessageReceiverListener implements MessageReceiverListener {
		SMPPSession m_session;

		SessionMessageReceiverListener(SMPPSession session) {
			m_session = session;
		}

		@Override
		public DataSmResult onAcceptDataSm(DataSm i_sms, org.jsmpp.session.Session i_session)
				throws ProcessRequestException {
			System.out.println("MessageReceiverListener::onAcceptDataSm");
			return null;
		}

		@Override
		public void onAcceptAlertNotification(AlertNotification arg0) {
			System.out.println("MessageReceiverListener::onAcceptAlertNotification");
		}

		@Override
		public void onAcceptDeliverSm(DeliverSm i_sms) throws ProcessRequestException {
			System.out.println("MessageReceiverListener::onAcceptDeliverSm");
			Message message = new Message();
			SmsDataobject l_dataobject = new SmsDataobject(message);

			try {
				l_dataobject.inboundBuffer = new String(i_sms.getShortMessage(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			l_dataobject.inboundSession = m_session;

			injectMessageInApplication(message, l_dataobject);
		}

	}

	@Override
	public void async_startSession(Session session) {
		SMPPSession smppSession = (SMPPSession) session.getUserObject();

		JsmppClientConfiguration l_config = (JsmppClientConfiguration) this.getConfiguration();

		try {

			BindType l_bindType = BindType.BIND_TX;
			if (l_config.getMode().equalsIgnoreCase(JsmppClientConfiguration.MODE_DUPLEX)) {
				l_bindType = BindType.BIND_TRX;
			}

			smppSession.connectAndBind(l_config.getHost(), l_config.getPort(), l_bindType, l_config.getSystemId(),
					l_config.getPassword(), l_config.getSystemType(), l_config.getTypeOfNumber(),
					l_config.getNumberingPlanIndicator(), l_config.getAdressRange());

			if (AdminLogger.isInfoEnabled())
				AdminLogger.info(buildAdminLog("Bound to " + l_config.getHost() + ":" + l_config.getPort()));

			if (l_config.getMode().equalsIgnoreCase(JsmppClientConfiguration.MODE_DUPLEX)) {
				smppSession.setMessageReceiverListener(new SessionMessageReceiverListener(smppSession));
			}

			this.sessionStarted(session);
		} catch (IOException e) {
			AdminLogger.warn(buildAdminLog(
					"Failed to bind to " + l_config.getHost() + ":" + l_config.getPort() + " : " + e.getMessage()));
			this.sessionDied(session);
		}
	}

	@Override
	protected void async_stopSession(Session session) {
		SMPPSession smppSession = (SMPPSession) session.getUserObject();
		smppSession.unbindAndClose();
		this.sessionStopped(session);
	}

}
