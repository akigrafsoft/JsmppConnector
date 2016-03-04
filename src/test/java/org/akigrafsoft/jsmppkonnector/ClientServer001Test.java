package org.akigrafsoft.jsmppkonnector;

import static org.junit.Assert.fail;

import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.TypeOfNumber;
import org.junit.Test;

import com.akigrafsoft.knetthreads.Dispatcher;
import com.akigrafsoft.knetthreads.Endpoint;
import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.ExceptionDuplicate;
import com.akigrafsoft.knetthreads.FlowProcessContext;
import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.RequestEnum;
import com.akigrafsoft.knetthreads.konnector.Konnector;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;
import com.akigrafsoft.knetthreads.routing.EndpointRouter;
import com.akigrafsoft.knetthreads.routing.KonnectorRouter;

public class ClientServer001Test {

	private void doSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {

		/*
		 * Properties prop = new Properties(); InputStream in =
		 * Test001.class.getResourceAsStream
		 * ("//home/kmoyse/workspace/JmsConnector/jndi.properties"); try {
		 * prop.load(in); in.close(); } catch (IOException e1) { // TODO
		 * Auto-generated catch block e1.printStackTrace(); }
		 */
		// server
		Konnector server_konnector;
		{
			try {
				server_konnector = new JsmppServerKonnector("TEST_SERVER");
			} catch (ExceptionDuplicate e) {
				e.printStackTrace();
				fail("creation failed");
				return;
			}

			JsmppServerConfiguration config = new JsmppServerConfiguration();
			config.setPort(8542);
			config.setSystemId("sys");

			try {
				server_konnector.configure(config);
			} catch (ExceptionAuditFailed e) {
				e.printStackTrace();
				fail("configuration failed");
				return;
			}

			try {
				final Endpoint l_ep = new Endpoint("TESTNAP") {
					@Override
					public KonnectorRouter getKonnectorRouter(Message message, KonnectorDataobject dataobject) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public RequestEnum classifyInboundMessage(Message message, KonnectorDataobject dataobject) {
						SmsDataobject do_sms = (SmsDataobject) dataobject;
						System.out.println("received" + do_sms.inboundBuffer);
						return null;
					}
				};
				l_ep.setDispatcher(new Dispatcher<RequestEnum>("foo") {

					@Override
					public FlowProcessContext getContext(Message message, KonnectorDataobject dataobject,
							RequestEnum request) {
						// TODO Auto-generated method stub
						return null;
					}
				});

				server_konnector.setEndpointRouter(new EndpointRouter() {
					@Override
					public Endpoint resolveKonnector(Message message, KonnectorDataobject dataobject) {
						return l_ep;
					}
				});
			} catch (ExceptionDuplicate e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		server_konnector.start();

		// Client
		Konnector client_konnector;
		{
			try {
				client_konnector = new JsmppClientKonnector("TEST_CLIENT");
			} catch (ExceptionDuplicate e) {
				e.printStackTrace();
				fail("creation failed");
				return;
			}

			JsmppClientConfiguration config = new JsmppClientConfiguration();
			config.setNumberOfSessions(5);
			config.setHost("localhost");
			config.setPort(8542);
			config.setSystemId("sys");

			try {
				client_konnector.configure(config);
			} catch (ExceptionAuditFailed e) {
				e.printStackTrace();
				fail("configuration failed");
				return;
			}
		}

		client_konnector.start();

		doSleep(500);

		Message message = new Message();
		SmsDataobject do_sms = new SmsDataobject(message);
		do_sms.operationMode = KonnectorDataobject.OperationMode.TWOWAY;
		do_sms.operationSyncMode = KonnectorDataobject.SyncMode.ASYNC;

		do_sms.serviceType = "";
		do_sms.sourceAddrTon = TypeOfNumber.UNKNOWN;
		do_sms.sourceAddrNpi = NumberingPlanIndicator.UNKNOWN;
		do_sms.sourceAddr = "";
		do_sms.destAddrTon = TypeOfNumber.UNKNOWN;
		do_sms.destAddrNpi = NumberingPlanIndicator.UNKNOWN;
		do_sms.destinationAddr = "";
		do_sms.esmClass = new ESMClass();
		do_sms.protocolId = 0;
		do_sms.priorityFlag = 0;
		do_sms.scheduleDeliveryTime = "";
		do_sms.validityPeriod = "";
		do_sms.registeredDelivery = new RegisteredDelivery(0);
		do_sms.replaceIfPresentFlag = 0;
		do_sms.dataCoding = org.jsmpp.bean.DataCodings.newInstance((byte) 0);
		// new GeneralDataCoding(true, true,
		// MessageClass.CLASS1, Alphabet.ALPHA_DEFAULT);
		do_sms.smDefaultMsgId = 1;

		do_sms.outboundBuffer = "CreateBalance,EMSTest:Id01,100,10,10000,20,balance,EUR,2";

		client_konnector.handle(do_sms);

		doSleep(500);

		System.out.println(do_sms.messageId);

		client_konnector.stop();
	}

}
