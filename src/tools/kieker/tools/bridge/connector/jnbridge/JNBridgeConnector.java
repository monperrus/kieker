/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.bridge.connector.jnbridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import com.jnbridge.jnbcore.server.ServerException;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.JNBridgeMonitoringController;
import kieker.tools.bridge.LookupEntity;
import kieker.tools.bridge.connector.AbstractConnector;
import kieker.tools.bridge.connector.ConnectorDataTransmissionException;
import kieker.tools.bridge.connector.ConnectorEndOfDataException;

/**
 * @author Rexhep Hamiti
 *
 * @since 1.11
 */
public class JNBridgeConnector extends AbstractConnector {

	private final JNBridgeMonitoringController controller;
	private final Properties props;

	/**
	 * Constructor
	 * 
	 * @param configuration
	 *            , lookupEnityMap
	 */
	public JNBridgeConnector(final Configuration configuration, final ConcurrentMap<Integer, LookupEntity> lookupEntityMap) {
		super(configuration, lookupEntityMap);
		this.props = new Properties();
		this.controller = JNBridgeMonitoringController.getInstance();
	}

	/**
	 * Initialize the Conntroller
	 * 
	 * @throws ConnectorDataTransmissionException
	 */
	@Override
	public void initialize() throws ConnectorDataTransmissionException {

		try {
			try {
				this.loadProps();
			} catch (final IOException e) {
				//
			}
			// this.props.setProperty("JNBCore", "/Kieker/resources/jnbcore_tcp.properties");
			com.jnbridge.jnbcore.JNBMain.start(this.props);

		} catch (final ServerException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}

	/**
	 * Load the JNBridge Properties
	 */
	public void loadProps() throws IOException {
		// final InputStream in = this.getClass().getResourceAsStream("C:\\kieker\\resources\\jnbcore_tcp.properties");
		// this.props.load(in);
		final InputStream input = new FileInputStream("/Kieker/resources/jnbcore_tcp.properties");
		this.props.load(input);
	}

	/**
	 * Close
	 */
	@Override
	public void close() throws ConnectorDataTransmissionException {
		try {
			com.jnbridge.jnbcore.JNBMain.stop();
		} catch (final ServerException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}

	/**
	 * DeserializeNextRecord
	 */
	@Override
	public IMonitoringRecord deserializeNextRecord() throws ConnectorDataTransmissionException, ConnectorEndOfDataException {
		try {
			return this.controller.getQueue().take();
		} catch (final InterruptedException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}
}
