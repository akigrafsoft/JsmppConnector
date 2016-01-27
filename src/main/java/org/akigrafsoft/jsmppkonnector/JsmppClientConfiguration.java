package org.akigrafsoft.jsmppkonnector;

import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

/**
 * Configuration class for {@link JsmppClientKonnector}
 * <p>
 * <b>This MUST be a Java bean</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class JsmppClientConfiguration extends
		SessionBasedClientKonnectorConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8176171909923036731L;

	private static String DEFAULT_host = "localhost";

	public static String MODE_SIMPLEX = "Simplex";
	public static String MODE_DUPLEX = "Duplex";

	private String host = DEFAULT_host;
	private int port;

	private String systemId;
	private String password;

	private String systemType;

	private TypeOfNumber typeOfNumber = TypeOfNumber.UNKNOWN;
	private NumberingPlanIndicator numberingPlanIndicator = NumberingPlanIndicator.UNKNOWN;

	private String adressRange;

	private String mode = MODE_SIMPLEX;

	// ------------------------------------------------------------------------
	// Java Bean

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public TypeOfNumber getTypeOfNumber() {
		return typeOfNumber;
	}

	public void setTypeOfNumber(TypeOfNumber typeOfNumber) {
		this.typeOfNumber = typeOfNumber;
	}

	public NumberingPlanIndicator getNumberingPlanIndicator() {
		return numberingPlanIndicator;
	}

	public void setNumberingPlanIndicator(
			NumberingPlanIndicator numberingPlanIndicator) {
		this.numberingPlanIndicator = numberingPlanIndicator;
	}

	public String getAdressRange() {
		return adressRange;
	}

	public void setAdressRange(String adressRange) {
		this.adressRange = adressRange;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	// ------------------------------------------------------------------------
	// Configuration

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		if ((host == null) || host.equals("")) {
			throw new ExceptionAuditFailed(
					"host must be provided and non empty, default="
							+ DEFAULT_host);
		}
		if (port <= 0) {
			throw new ExceptionAuditFailed("port must be provided and > 0");
		}

		if ((systemId == null) || systemId.equals("")) {
			throw new ExceptionAuditFailed(
					"systemId must be provided and non empty");
		}

		if (!mode.equalsIgnoreCase(MODE_SIMPLEX)
				&& !mode.equalsIgnoreCase(MODE_DUPLEX)) {
			throw new ExceptionAuditFailed("mode must be provided"
					+ MODE_SIMPLEX + "or " + MODE_DUPLEX);
		}
	}

}
