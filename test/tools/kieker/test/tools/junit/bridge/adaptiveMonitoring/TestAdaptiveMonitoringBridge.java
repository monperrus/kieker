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
package kieker.test.tools.junit.bridge.adaptiveMonitoring;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Assert;
import org.junit.Test;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.tools.bridge.LookupEntity;
import kieker.tools.bridge.ServiceContainer;
import kieker.tools.bridge.connector.ConnectorDataTransmissionException;
import kieker.tools.bridge.connector.IServiceConnector;
import kieker.tools.bridge.connector.ServiceConnectorFactory;
import kieker.tools.bridge.connector.tcp.TCPSingleServerConnector;

import kieker.test.tools.junit.bridge.ConfigurationParameters;

/**
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */
public class TestAdaptiveMonitoringBridge {

	private ServiceContainer kdb;
	private IServiceConnector connector;
	private int recordCount; // default initialization is 0
	List<String> opList;

	public TestAdaptiveMonitoringBridge() {
		// empty constructor
	}

	@Test
	public void testAdaptiveMonitoringKDB() throws ConnectorDataTransmissionException, IOException { // NOPMD
		final TCPClientForKDB client = new TCPClientForKDB(ConfigurationParameters.TCP_SINGLE_PORT);
		final TCPClientForKDB client2 = new TCPClientForKDB(ConfigurationParameters.TCP_SINGLE_PORT);
		// Three client Threads
		// FIRST RUN should get cache misses on all ProbeActivation-Checks (Three different OpSignatures)
		// SECOND RUN should not have any cache misses due to the local cache update from FIRST RUN
		// THIRD RUN test activateProbe and deactivateProbe

		final Thread clientThread = new Thread(client, "T1");
		final Thread clientThread2 = new Thread(client, "T2");
		final Thread clientThread3 = new Thread(client2, "T3");

		// configuration of the used connector and monitoring controller
		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		configuration.setProperty(TCPSingleServerConnector.PORT, String.valueOf(ConfigurationParameters.TCP_SINGLE_PORT));

		final Configuration mconfig = ConfigurationFactory.createDefaultConfiguration();
		mconfig.setProperty(ConfigurationFactory.ADAPTIVE_MONITORING_ENABLED, "true");
		mconfig.setProperty(ConfigurationFactory.ADAPTIVE_MONITORING_CONFIG_FILE_UPDATE, "true");
		// TCPSingleServerConnector
		this.connector = new TCPSingleServerConnector(configuration, this.createLookupEntityMap());

		// FIRST RUN ----------------------------
		clientThread.start();
		this.kdb = new ServiceContainer(mconfig, this.connector, false);
		this.kdb.run();

		Assert.assertTrue("Unexpected amount of cache misses. Should have been " + this.kdb.getCacheMisses(), this.kdb.getCacheMisses() == 3);

		// SECOND RUN ----------------------------
		clientThread2.start();
		this.kdb = new ServiceContainer(ConfigurationFactory.createDefaultConfiguration(), this.connector, false); // reset ServiceContainer
		this.kdb.run();

		Assert.assertTrue("Unexpected, should have been 0 cache misses", this.kdb.getCacheMisses() == 0);

		// THIRD RUN ----------------------------

		// clientThread3.start(); // new client object
		// this.kdb = new ServiceContainer(ConfigurationFactory.createDefaultConfiguration(), this.connector, false); // reset ServiceContainer
		// this.kdb.deactivateProbe(ConfigurationParameters.TEST_OPERATION_SIGNATURE); // not sure if it works like this
		// this.kdb.run();
		// System.out.println(this.kdb.getRecordCount());

	}

	protected final ConcurrentMap<Integer, LookupEntity> createLookupEntityMap() throws ConnectorDataTransmissionException {
		final ConcurrentMap<Integer, Class<? extends IMonitoringRecord>> map = new ConcurrentHashMap<Integer, Class<? extends IMonitoringRecord>>();
		map.put(ConfigurationParameters.TEST_RECORD_ID, OperationExecutionRecord.class);
		map.put(-ConfigurationParameters.TEST_RECORD_ID, SignatureActivationCheckRecord.class);

		return ServiceConnectorFactory.createLookupEntityMap(map);
	}

	public int getRecordCount() {
		return this.recordCount;
	}
}
