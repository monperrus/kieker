/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.test.toolsteetime.junit.writeRead.filesystem;

import java.util.LinkedList;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import kieker.analysisteetime.plugin.reader.filesystem.fsReader.AsciiLogReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.controller.WriterController;
import kieker.monitoring.writer.filesystem.AsciiFileWriter;

import kieker.test.tools.junit.writeRead.TestDataRepository;
import kieker.test.tools.junit.writeRead.TestProbe;

import teetime.framework.test.StageTester;

/**
 * @author Christian Wulf
 *
 * @since 1.13
 */
public class AsciiWriterReaderTest {

	private static final TestDataRepository TEST_DATA_REPOSITORY = new TestDataRepository();
	private static final int TIMEOUT_IN_MS = 0;

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder(); // NOCS (@Rule must be public)

	public AsciiWriterReaderTest() {
		super();
	}

	@Test
	public void testUncompressedAsciiCommunication() throws Exception {
		// 1. define records to be triggered by the test probe
		final List<IMonitoringRecord> records = TEST_DATA_REPOSITORY.newTestRecords();

		final List<IMonitoringRecord> analyzedRecords = this.testAsciiCommunication(records, false);

		// 8. compare actual and expected records
		Assert.assertThat(analyzedRecords.subList(1, analyzedRecords.size()), CoreMatchers.is(CoreMatchers.equalTo(records)));
	}

	@Test
	public void testCompressedAsciiCommunication() throws Exception {
		// 1. define records to be triggered by the test probe
		final List<IMonitoringRecord> records = TEST_DATA_REPOSITORY.newTestRecords();

		final List<IMonitoringRecord> analyzedRecords = this.testAsciiCommunication(records, true);

		// 8. compare actual and expected records
		Assert.assertThat(analyzedRecords.subList(1, analyzedRecords.size()), CoreMatchers.is(CoreMatchers.equalTo(records)));
	}

	@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
	private List<IMonitoringRecord> testAsciiCommunication(final List<IMonitoringRecord> records, final boolean shouldDecompress) throws Exception {
		// 2. define monitoring config
		final Configuration config = ConfigurationFactory.createDefaultConfiguration();
		config.setProperty(ConfigurationFactory.WRITER_CLASSNAME, AsciiFileWriter.class.getName());
		config.setProperty(WriterController.RECORD_QUEUE_SIZE, "128");
		config.setProperty(WriterController.RECORD_QUEUE_INSERT_BEHAVIOR, "1");
		config.setProperty(AsciiFileWriter.CONFIG_PATH, this.tmpFolder.getRoot().getCanonicalPath());
		config.setProperty(AsciiFileWriter.CONFIG_SHOULD_COMPRESS, Boolean.toString(shouldDecompress));
		final MonitoringController monitoringController = MonitoringController.createInstance(config);

		// 3. define analysis config
		final String[] monitoringLogDirs = TEST_DATA_REPOSITORY.getAbsoluteMonitoringLogDirNames(this.tmpFolder.getRoot());

		final AsciiLogReader asciiLogReader = new AsciiLogReader(monitoringLogDirs, false, shouldDecompress);
		final List<IMonitoringRecord> outputList = new LinkedList<>();

		// 4. trigger records
		final TestProbe testProbe = new TestProbe(monitoringController);
		Assert.assertTrue(monitoringController.isMonitoringEnabled());
		testProbe.triggerRecords(records);
		Assert.assertTrue(monitoringController.isMonitoringEnabled());

		// 5. terminate monitoring
		monitoringController.terminateMonitoring();
		monitoringController.waitForTermination(TIMEOUT_IN_MS);

		// 6. execute the reader configuration
		StageTester.test(asciiLogReader).and().receive(outputList).from(asciiLogReader.getOutputPort()).start();

		// 7. return read records
		return outputList;
	}
}
