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
import java.util.Deque;
import java.util.LinkedList;
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
 * JUnit test, which tests the probe-activation-request and -answer between Probe-Side and KDB-Side.
 * Furthermore related to that, the working local signature-cache on Probe-Side.
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */
public class TestAdaptiveMonitoringKDBwithoutUpdateComponent {

	private ServiceContainer kdb;
	private IServiceConnector connector;

	// private List<String> opList;

	public TestAdaptiveMonitoringKDBwithoutUpdateComponent() {
		// empty constructor
	}

	@Test
	public void testAdaptiveMonitoringKDB() throws ConnectorDataTransmissionException, IOException { // NOPMD

		final Deque<Integer> recordReceiveOrder = this.initializeRecordReceiveOrder();

		final TCPClientForKDB client = new TCPClientForKDB(ConfigurationParameters.TCP_SINGLE_PORT);
		// Two client Threads
		// FIRST RUN should get cache misses on all ProbeActivation-Checks on probe-side (Three different OpSignatures = 3 Cache-Misses)
		// SECOND RUN should not have any cache miss due to the local cache update from FIRST RUN
		// With this Test it is guaranteed, that the signature-cache mechanism on probe-side and the probe-activation-check on KDB-side
		// is working correctly

		final Thread clientThread = new Thread(client, "T1");
		final Thread clientThread2 = new Thread(client, "T2");

		// configuration of the used connector and monitoring controller
		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		configuration.setProperty(TCPSingleServerConnector.PORT, String.valueOf(ConfigurationParameters.TCP_SINGLE_PORT));

		final Configuration mconfig = ConfigurationFactory.createDefaultConfiguration();
		mconfig.setProperty(ConfigurationFactory.ADAPTIVE_MONITORING_ENABLED, "true");

		// INIT
		this.connector = new TCPSingleServerConnector(configuration, this.createLookupEntityMap());
		this.kdb = new ServiceContainer(mconfig, this.connector, false);

		// SetPatternList
		final List<String> patternList = new LinkedList<String>();
		patternList.add("+" + ConfigurationParameters.TEST_OPERATION_SIGNATURE); // activated
		patternList.add("+" + ConfigurationParameters.TEST_OPERATION_SIGNATURE_2); // activated
		patternList.add("+" + ConfigurationParameters.TEST_OPERATION_SIGNATURE_3); // activated
		this.kdb.getKiekerMonitoringController().setProbePatternList(patternList);

		// FIRST RUN ----------------------------
		clientThread.start();
		this.kdb.run();

		Assert.assertTrue("Unexpected order of records. ", recordReceiveOrder.toString().equals(this.kdb.getRecordOrder().toString()));
		Assert.assertTrue("Unexpected amount of cache misses. Should have been " + this.kdb.getCacheMisses(), this.kdb.getCacheMisses() == 3);

		this.kdb.setRecordOrder(); // deletes the received record Order in KDB
		this.kdb.setCacheMisses(); // deletes the Cache-Misses

		// SECOND RUN ----------------------------
		clientThread2.start();
		this.kdb.run();

		Assert.assertTrue("Unexpected order of records. ", recordReceiveOrder.toString().equals(this.kdb.getRecordOrder().toString()));
		Assert.assertTrue("Unexpected amount of cache misses. Should have been 0 but were" + this.kdb.getCacheMisses(), (this.kdb.getCacheMisses()) == 0);

	}

	protected final ConcurrentMap<Integer, LookupEntity> createLookupEntityMap() throws ConnectorDataTransmissionException {
		final ConcurrentMap<Integer, Class<? extends IMonitoringRecord>> map = new ConcurrentHashMap<Integer, Class<? extends IMonitoringRecord>>();
		map.put(ConfigurationParameters.TEST_RECORD_ID, OperationExecutionRecord.class);
		map.put(-ConfigurationParameters.TEST_RECORD_ID, SignatureActivationCheckRecord.class);

		return ServiceConnectorFactory.createLookupEntityMap(map);
	}

	public Deque<Integer> initializeRecordReceiveOrder() {

		final Deque<Integer> recordReceiveOrderBeforeUpdate = new LinkedList<Integer>();
		recordReceiveOrderBeforeUpdate.add(0);
		recordReceiveOrderBeforeUpdate.add(1);
		recordReceiveOrderBeforeUpdate.add(2);
		recordReceiveOrderBeforeUpdate.add(3);
		recordReceiveOrderBeforeUpdate.add(4);
		recordReceiveOrderBeforeUpdate.add(5);
		recordReceiveOrderBeforeUpdate.add(6);
		recordReceiveOrderBeforeUpdate.add(7);
		recordReceiveOrderBeforeUpdate.add(8);
		recordReceiveOrderBeforeUpdate.add(9);
		recordReceiveOrderBeforeUpdate.add(10);
		recordReceiveOrderBeforeUpdate.add(11);

		return recordReceiveOrderBeforeUpdate;
	}

}
