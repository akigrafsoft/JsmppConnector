/**
 * Open-source, by AkiGrafSoft.
 *
 * $Id:  $
 *
 **/
package org.akigrafsoft.jsmppkonnector;

import com.akigrafsoft.knetthreads.ExceptionAuditFailed;
import com.akigrafsoft.knetthreads.konnector.KonnectorConfiguration;

/**
 * Configuration class for {@link JsmppServerKonnector}
 * <p>
 * <b>This MUST be a Java bean</b>
 * </p>
 * 
 * @author kmoyse
 * 
 */
public class JsmppServerConfiguration extends KonnectorConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3031072538862986043L;
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
	private int port;

	/**
	 * SystemId
	 */
	private String systemId;

	// ------------------------------------------------------------------------
	// Java Bean

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

	// ------------------------------------------------------------------------
	// Configuration

	@Override
	public void audit() throws ExceptionAuditFailed {
		super.audit();
		if ((port < MIN_PORT_NUMBER) || (port > MAX_PORT_NUMBER)) {
			throw new ExceptionAuditFailed("port must be provided and comprised between " + MIN_PORT_NUMBER + " and "
					+ MAX_PORT_NUMBER + " (included)");
		}
		if ((systemId == null) || systemId.equals("")) {
			throw new ExceptionAuditFailed("systemId must be provided and non empty");
		}
	}
}
