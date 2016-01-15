package org.akigrafsoft.jsmppkonnector;

import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.SessionBasedClientKonnectorConfiguration;

public class JsmppClientConfiguration extends
		SessionBasedClientKonnectorConfiguration {

	private static String DEFAULT_host = "localhost";

	public static String MODE_SIMPLEX = "Simplex";
	public static String MODE_DUPLEX = "Duplex";

	public String host = DEFAULT_host;
	public int port;

	public String systemId;
	public String password;

	public String systemType;

	public TypeOfNumber typeOfNumber = TypeOfNumber.UNKNOWN;
	public NumberingPlanIndicator numberingPlanIndicator = NumberingPlanIndicator.UNKNOWN;

	public String adressRange;

	public String mode = MODE_SIMPLEX;

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
