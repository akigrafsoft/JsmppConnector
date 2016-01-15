package org.akigrafsoft.jsmppkonnector;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

public class JsmppServerConfiguration extends KonnectorConfiguration {

	/**
	 * Minimum port number - used for audit of configuration
	 */
	public static final int MIN_PORT_NUMBER = 0;
	/**
	 * Maximum port number - used for audit of configuration
	 */
	public static final int MAX_PORT_NUMBER = 65535;

	/**
	 * Listener port
	 */
	public int port;

	/**
	 * SystemId
	 */
	public String systemId;

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		if ((port < MIN_PORT_NUMBER) || (port > MAX_PORT_NUMBER)) {
			throw new ExceptionAuditFailed("port must be provided and > 0");
		}
		if ((systemId == null) || systemId.equals("")) {
			throw new ExceptionAuditFailed(
					"systemId must be provided and non empty");
		}
	}
}
