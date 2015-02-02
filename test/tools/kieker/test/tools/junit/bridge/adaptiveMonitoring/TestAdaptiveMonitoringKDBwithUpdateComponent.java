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
 * JUnit test which tests the Update-Mechanism between KDB-side and Probe-side.
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */
public class TestAdaptiveMonitoringKDBwithUpdateComponent {

	private ServiceContainer kdb;
	private IServiceConnector connector;
	private TCPUpdateServerConnector updateConnector;

	public TestAdaptiveMonitoringKDBwithUpdateComponent() {
		// empty constructor
	}

	@Test
	public void testAdaptiveMonitoringKDB() throws ConnectorDataTransmissionException, IOException { // NOPMD
		// Test Lists
		// First List: 2 activated 1 deactivated
		final Deque<Integer> recordReceiveOrderBeforeUpdate = this.initializeRecordReceiveOrderBeforeUpdate();
		// Second List: 1 activated -> deactivated; 1 deactivated -> activated
		final Deque<Integer> recordReceiveOrderAfterUpdate = this.initializeRecordReceiveOrderAfterUpdate();

		final TCPClientForKDB client = new TCPClientForKDB(ConfigurationParameters.TCP_SINGLE_PORT);

		final TCPUpdateClientForKDB clientUpdate = new TCPUpdateClientForKDB(ConfigurationParameters.TCP_SINGLE_PORT2, client);
		final Thread clientThreadForUpdate = new Thread(clientUpdate, "ClientUpdate");

		final Thread clientThreadBeforeUpdate = new Thread(client, "T1");
		final Thread clientThreadAfterUpdate = new Thread(client, "T2");

		// CONFIGURATION of the used connector and monitoring controller
		final Configuration configuration = ConfigurationFactory.createSingletonConfiguration();
		configuration.setProperty(TCPSingleServerConnector.PORT, String.valueOf(ConfigurationParameters.TCP_SINGLE_PORT));

		final Configuration mconfig = ConfigurationFactory.createDefaultConfiguration();
		mconfig.setProperty(ConfigurationFactory.ADAPTIVE_MONITORING_ENABLED, "true");

		// INIT
		this.connector = new TCPSingleServerConnector(configuration, this.createLookupEntityMap());
		this.kdb = new ServiceContainer(mconfig, this.connector, false);
		this.updateConnector = new TCPUpdateServerConnector(this.kdb.getKiekerMonitoringController(), ConfigurationParameters.TCP_SINGLE_PORT2);

		final Thread updateThread = new Thread(this.updateConnector, "UpdateServer");

		// SetPatternList
		final List<String> patternList = new LinkedList<String>();
		patternList.add("+" + ConfigurationParameters.TEST_OPERATION_SIGNATURE); // activated
		patternList.add("+" + ConfigurationParameters.TEST_OPERATION_SIGNATURE_2); // activated
		patternList.add("-" + ConfigurationParameters.TEST_OPERATION_SIGNATURE_3); // deactivated
		this.kdb.getKiekerMonitoringController().setProbePatternList(patternList);

		// 1st RUN -----------------------------------------------------------------------
		// Update-Mechanism (UpdateClientForKDB - TCPUpdateServer)
		updateThread.start();
		clientThreadForUpdate.start();

		clientThreadBeforeUpdate.start();
		this.kdb.run();

		Assert.assertTrue("Unexpected order of records. ", recordReceiveOrderBeforeUpdate.toString().equals(this.kdb.getRecordOrder().toString()));
		Assert.assertTrue("Unexpected amount of cache misses. Should have been " + this.kdb.getCacheMisses(), this.kdb.getCacheMisses() == 3);

		this.kdb.setRecordOrder(); // delete RecordOrder received in KDB, hence it is possible to check correct received records after Updates
		this.kdb.setCacheMisses(); // reset CacheMisses, should be 0 cache misses, if update-mechanism is working correctly, cause the local cache on probe-side
									// should be updated

		// 2nd RUN ---------------------------------------------------------------------------------
		clientThreadAfterUpdate.start();

		// Both Methods should trigger the Update-Mechanism and update the local signature cache on probe-side which should result in no cache miss on probe-side
		// After this update, the received records should match the 2nd Test-List from the Beginning.
		this.kdb.deactivateProbe(ConfigurationParameters.TEST_OPERATION_SIGNATURE);
		this.kdb.activateProbe(ConfigurationParameters.TEST_OPERATION_SIGNATURE_3);

		this.kdb.run();

		Assert.assertTrue("Unexpected order of records. ", recordReceiveOrderAfterUpdate.toString().equals(this.kdb.getRecordOrder().toString()));
		Assert.assertTrue("Should have been no Cache-Miss but was " + this.kdb.getCacheMisses(), this.kdb.getCacheMisses() == 0);
	}

	protected final ConcurrentMap<Integer, LookupEntity> createLookupEntityMap() throws ConnectorDataTransmissionException {
		final ConcurrentMap<Integer, Class<? extends IMonitoringRecord>> map = new ConcurrentHashMap<Integer, Class<? extends IMonitoringRecord>>();
		map.put(ConfigurationParameters.TEST_RECORD_ID, OperationExecutionRecord.class);
		map.put(-ConfigurationParameters.TEST_RECORD_ID, SignatureActivationCheckRecord.class);

		return ServiceConnectorFactory.createLookupEntityMap(map);
	}

	public Deque<Integer> initializeRecordReceiveOrderBeforeUpdate() {

		final Deque<Integer> recordReceiveOrderBeforeUpdate = new LinkedList<Integer>();
		recordReceiveOrderBeforeUpdate.add(0);
		recordReceiveOrderBeforeUpdate.add(1);
		recordReceiveOrderBeforeUpdate.add(3);
		recordReceiveOrderBeforeUpdate.add(4);
		recordReceiveOrderBeforeUpdate.add(6);
		recordReceiveOrderBeforeUpdate.add(7);
		recordReceiveOrderBeforeUpdate.add(9);
		recordReceiveOrderBeforeUpdate.add(10);

		return recordReceiveOrderBeforeUpdate;

	}

	public Deque<Integer> initializeRecordReceiveOrderAfterUpdate() {

		final Deque<Integer> recordReceiveOrderAfterUpdate = new LinkedList<Integer>();
		recordReceiveOrderAfterUpdate.add(1);
		recordReceiveOrderAfterUpdate.add(2);
		recordReceiveOrderAfterUpdate.add(4);
		recordReceiveOrderAfterUpdate.add(5);
		recordReceiveOrderAfterUpdate.add(7);
		recordReceiveOrderAfterUpdate.add(8);
		recordReceiveOrderAfterUpdate.add(10);
		recordReceiveOrderAfterUpdate.add(11);

		return recordReceiveOrderAfterUpdate;

	}
}
