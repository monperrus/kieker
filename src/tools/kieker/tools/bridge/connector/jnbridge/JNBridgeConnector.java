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

	private JNBridgeMonitoringController controller;

	public JNBridgeConnector(final Configuration configuration, final ConcurrentMap<Integer, LookupEntity> lookupEntityMap) {
		super(configuration, lookupEntityMap);
	}

	@Override
	public void initialize() throws ConnectorDataTransmissionException {
		try {
			com.jnbridge.jnbcore.JNBMain.start("C:/Program Files/JNBridge/JNBridgePro v5.1 x64/jnbcore/jnbcore_tcp.properties");
			this.controller = new JNBridgeMonitoringController();
		} catch (final ServerException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}

	@Override
	public void close() throws ConnectorDataTransmissionException {
		try {
			com.jnbridge.jnbcore.JNBMain.stop();
		} catch (final ServerException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}
	}

	@Override
	public IMonitoringRecord deserializeNextRecord() throws ConnectorDataTransmissionException, ConnectorEndOfDataException {
		return this.controller.getQueue().poll();
	}
}
