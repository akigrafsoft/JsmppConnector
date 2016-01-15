package org.akigrafsoft.jsmppkonnector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.CancelSm;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.QuerySm;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.ReplaceSm;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;

import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class JsmppServerKonnector extends Konnector {

	private final MessageIDGenerator m_messageIdGenerator = new RandomMessageIDGenerator();

	private JsmppServerConfiguration m_config = null;
	private SMPPServerSessionListener m_sessionListener;
	private boolean m_shouldStop = false;

	private ServerMessageReceiverListener m_messageReceiverListener = new ServerMessageReceiverListener() {
		@Override
		public DataSmResult onAcceptDataSm(DataSm arg0, Session arg1)
				throws ProcessRequestException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void onAcceptCancelSm(CancelSm arg0, SMPPServerSession arg1)
				throws ProcessRequestException {
			// TODO Auto-generated method stub

		}

		@Override
		public QuerySmResult onAcceptQuerySm(QuerySm arg0,
				SMPPServerSession arg1) throws ProcessRequestException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void onAcceptReplaceSm(ReplaceSm arg0, SMPPServerSession arg1)
				throws ProcessRequestException {
			// TODO Auto-generated method stub

		}

		@Override
		public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti arg0,
				SMPPServerSession arg1) throws ProcessRequestException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MessageId onAcceptSubmitSm(SubmitSm sms,
				SMPPServerSession serverSession) throws ProcessRequestException {
			Message message = new Message();
			SmsDataobject l_dataobject = new SmsDataobject(message);

			if (ActivityLogger.isDebugEnabled())
				ActivityLogger.debug(buildActivityLog(message,
						"onAcceptSubmitSm<" + sms.getShortMessage() + ">"));

			try {
				l_dataobject.inboundBuffer = new String(sms.getShortMessage(),
						"UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			l_dataobject.submitSm = sms;

			// save session into message
			l_dataobject.inboundSession = serverSession;

			l_dataobject.messageId = m_messageIdGenerator.newMessageId();

			injectMessageInApplication(message, l_dataobject);

			return l_dataobject.messageId;
		}
	};

	public JsmppServerKonnector(String name) throws ExceptionDuplicate {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doLoadConfig(KonnectorConfiguration config) {
		m_config = (JsmppServerConfiguration) config;
	}

	private class WaitBindTask implements Runnable {
		private final SMPPServerSession m_serverSession;

		public WaitBindTask(SMPPServerSession serverSession) {
			m_serverSession = serverSession;
		}

		public void run() {
			try {
				BindRequest bindRequest = m_serverSession.waitForBind(1000);

				if (AdminLogger.isDebugEnabled())
					AdminLogger
							.debug(buildAdminLog("Accepting bind for session "
									+ m_serverSession.getSessionId()));

				try {
					bindRequest.accept(m_config.systemId);
				} catch (PDUStringException e) {
					AdminLogger.warn(buildAdminLog("PDUStringException "
							+ e.getMessage()));
					bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
				}
			} catch (IllegalStateException e) {
				AdminLogger.warn(buildAdminLog("IllegalStateException "
						+ e.getMessage()));
			} catch (TimeoutException e) {
				AdminLogger.warn(buildAdminLog("TimeoutException "
						+ e.getMessage()));
			} catch (IOException e) {
				AdminLogger
						.warn(buildAdminLog("IOException " + e.getMessage()));
			}
		}
	}

	@Override
	protected CommandResult doStart() {

		try {
			m_sessionListener = new SMPPServerSessionListener(m_config.port);
		} catch (IOException e) {
			return CommandResult.Fail;
		}

		m_shouldStop = false;

		Runnable thread = new Runnable() {
			@Override
			public void run() {
				setStarted();
				while (!m_shouldStop) {
					SMPPServerSession serverSession;
					try {
						serverSession = m_sessionListener.accept();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					// serverSession.getSessionId());
					serverSession
							.setMessageReceiverListener(m_messageReceiverListener);
					executeInNetworkThread(new WaitBindTask(serverSession));
				}

				try {
					m_sessionListener.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setStopped();
			}
		};

		executeInNetworkThread(thread);

		return CommandResult.Success;
	}

	@Override
	public void doHandle(KonnectorDataobject dataobject) {
		SmsDataobject l_dataobject = (SmsDataobject) dataobject;

		SMPPServerSession l_serverSession = (SMPPServerSession) l_dataobject.inboundSession;

		String stringValue = Integer.valueOf(l_dataobject.messageId.getValue(),
				16).toString();
		//
		DeliveryReceipt delRec = new DeliveryReceipt(stringValue, 1, 1,
				new Date(), new Date(), DeliveryReceiptState.DELIVRD, null,
				new String(l_dataobject.submitSm.getShortMessage()));
		try {
			l_serverSession.deliverShortMessage(l_dataobject.submitSm
					.getServiceType(), org.jsmpp.bean.TypeOfNumber
					.valueOf(l_dataobject.submitSm.getDestAddrTon()),
					org.jsmpp.bean.NumberingPlanIndicator
							.valueOf(l_dataobject.submitSm.getDestAddrNpi()),
					l_dataobject.submitSm.getDestAddress(),
					org.jsmpp.bean.TypeOfNumber.valueOf(l_dataobject.submitSm
							.getSourceAddrTon()),
					org.jsmpp.bean.NumberingPlanIndicator
							.valueOf(l_dataobject.submitSm.getSourceAddrNpi()),
					l_dataobject.submitSm.getSourceAddr(), new ESMClass(
							org.jsmpp.bean.MessageMode.DEFAULT,
							org.jsmpp.bean.MessageType.SMSC_DEL_RECEIPT,
							org.jsmpp.bean.GSMSpecificFeature.DEFAULT),
					(byte) 0, (byte) 0, new RegisteredDelivery(0),
					org.jsmpp.bean.DataCodings.newInstance((byte) 0), delRec
							.toString().getBytes());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		} catch (PDUException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		} catch (ResponseTimeoutException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		} catch (InvalidResponseException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		} catch (NegativeResponseException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		} catch (IOException e) {
			e.printStackTrace();
			this.resumeWithNetworkError(l_dataobject, e.getMessage());
			return;
		}

		if (ActivityLogger.isDebugEnabled())
			ActivityLogger.debug(buildActivityLog(dataobject.getMessage(),
					"Sending delivery receipt for message id "
							+ l_dataobject.messageId + ":" + stringValue));

		resumeWithExecutionComplete(dataobject);
	}

	@Override
	protected CommandResult doStop() {
		m_shouldStop = true;
		return CommandResult.Success;
	}

}
