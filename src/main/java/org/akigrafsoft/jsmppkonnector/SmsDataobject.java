package org.akigrafsoft.jsmppkonnector;

import org.jsmpp.bean.DataCoding;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.util.MessageId;

import com.akigrafsoft.knetthreads.Message;
import com.akigrafsoft.knetthreads.konnector.KonnectorDataobject;

public class SmsDataobject extends KonnectorDataobject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -642990612822534381L;
	
	public String serviceType;
	public TypeOfNumber sourceAddrTon;
	public NumberingPlanIndicator sourceAddrNpi;
	public String sourceAddr;
	public TypeOfNumber destAddrTon;
	public NumberingPlanIndicator destAddrNpi;
	public String destinationAddr;
	public ESMClass esmClass;
	public byte protocolId;
	public byte priorityFlag;
	public String scheduleDeliveryTime;
	public String validityPeriod;
	public RegisteredDelivery registeredDelivery;
	public byte replaceIfPresentFlag;
	public DataCoding dataCoding;
	public byte smDefaultMsgId;
	
	// we will use the outboundBuffer
	//public byte[] shortMessage;
	
	public OptionalParameter[] optionalParameters;
	
	//
	transient public MessageId messageId;
	
	// carry inbound session
	transient org.jsmpp.session.Session inboundSession;
	transient SubmitSm submitSm;
	
	public SmsDataobject(Message message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
