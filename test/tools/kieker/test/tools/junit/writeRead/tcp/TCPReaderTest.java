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

package kieker.test.tools.junit.writeRead.tcp;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.AnalysisControllerThread;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.tcp.TCPReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.tcp.TCPWriter;
import kieker.tpmon.monitoringRecord.executions.KiekerExecutionRecord;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class TCPReaderTest {

	private static final String PORT1 = "10133";
	private static final String PORT2 = "10134";

	private IMonitoringController monitoringController;
	private AnalysisController analysisController;

	private ListCollectionFilter<IMonitoringRecord> sinkFilter;
	private AnalysisControllerThread analysisThread;

	@Before
	public void before() throws IllegalStateException, AnalysisConfigurationException {
		this.configureAnalysis();
		this.analysisThread = new AnalysisControllerThread(this.analysisController);
		this.analysisThread.start();

		// monitoring needs to be configured after the analysis side has been started because:
		// configuration inits TcpWriter which in turn connects to its host within its init phase (unbelievable, but true)
		this.configureMonitoring();
	}

	@Test
	public void testExistingLegacyRecord() throws Exception {
		final String operationSignature = "a.b.c.test(int)";
		final String sessionId = "1";
		final long traceId = 1;
		final long tin = 11111111;
		final long tout = 22222222;
		final String hostname = "www.test.de";
		final int eoi = 3;
		final int ess = 4;
		final OperationExecutionRecord record = new OperationExecutionRecord(operationSignature, sessionId, traceId, tin, tout, hostname, eoi, ess);

		this.monitoringController.newMonitoringRecord(record);
		this.monitoringController.terminateMonitoring();

		this.analysisThread.awaitTermination(1, TimeUnit.SECONDS);

		Assert.assertEquals(1, this.sinkFilter.getList().size());

		final IMonitoringRecord actualRecord = this.sinkFilter.getList().get(0);
		Assert.assertTrue(actualRecord instanceof OperationExecutionRecord);
		Assert.assertEquals(record, actualRecord);
	}

	// @Test
	// public void testExistingRecord() throws Exception {
	// // final BeforeOperationEvent record = new BeforeOperationEvent(timestamp, traceId, orderIndex, operationSignature, classSignature);
	// // monitoringController.newMonitoringRecord(record);
	//
	// // this.analysisController.run();
	//
	// // Assert.assertEquals();
	// }

	@Test
	public void testVirtualExistingRecord() throws Exception {
		final String operationSignature = "a.b.c.test(int)";
		final String sessionId = "1";
		final long traceId = 1;
		final long tin = 11111111;
		final long tout = 22222222;
		final String hostname = "www.test.de";
		final int eoi = 3;
		final int ess = 4;
		final KiekerExecutionRecord record = new KiekerExecutionRecord(operationSignature, sessionId, traceId, tin, tout, hostname, eoi, ess);

		this.monitoringController.newMonitoringRecord(record);
		this.monitoringController.terminateMonitoring();

		this.analysisThread.awaitTermination(1, TimeUnit.SECONDS);

		Assert.assertEquals(1, this.sinkFilter.getList().size());

		final IMonitoringRecord actualRecord = this.sinkFilter.getList().get(0);
		Assert.assertFalse(actualRecord instanceof KiekerExecutionRecord);
		Assert.assertTrue(actualRecord instanceof OperationExecutionRecord);
		final OperationExecutionRecord castedActualRecord = (OperationExecutionRecord) actualRecord;

		Assert.assertNotEquals(record, castedActualRecord);
		Assert.assertEquals(record.getTin(), castedActualRecord.getTin());
		Assert.assertEquals(record.getTout(), castedActualRecord.getTout());
	}

	//
	// @Test
	// public void testNonExistingRecord() throws Exception {
	//
	// }

	private void configureMonitoring() {
		final Configuration monitoringConfig = ConfigurationFactory.createDefaultConfiguration();
		monitoringConfig.setProperty(ConfigurationFactory.WRITER_CLASSNAME, TCPWriter.class.getName());
		monitoringConfig.setProperty(TCPWriter.CONFIG_PORT1, TCPReaderTest.PORT1);
		monitoringConfig.setProperty(TCPWriter.CONFIG_PORT2, TCPReaderTest.PORT2);
		this.monitoringController = MonitoringController.createInstance(monitoringConfig);
	}

	private void configureAnalysis() throws AnalysisConfigurationException {
		this.analysisController = new AnalysisController();

		final Configuration readerConfig = new Configuration();
		readerConfig.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT1, TCPReaderTest.PORT1);
		readerConfig.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT2, TCPReaderTest.PORT2);
		final TCPReader tcpReader = new TCPReader(readerConfig, this.analysisController);

		this.sinkFilter = new ListCollectionFilter<IMonitoringRecord>(new Configuration(), this.analysisController);

		this.analysisController.connect(tcpReader, TCPReader.OUTPUT_PORT_NAME_RECORDS, this.sinkFilter, ListCollectionFilter.INPUT_PORT_NAME);
	}

}
