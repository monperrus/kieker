package kieker.test.toolsteetime.junit.writeRead.amqp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import kieker.analysisteetime.plugin.reader.amqp.AMQPReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.AbstractTraceEvent;
import kieker.common.record.misc.EmptyRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.amqp.AmqpWriter;

import kieker.test.analysis.util.plugin.filter.flow.BookstoreEventRecordFactory;

import teetime.framework.test.StageTester;

public class BasicAMQPWriterReaderTest {
	private static final int TIMEOUT_IN_MS = 0;

	// AMQPReader constructor arguments
	// See the amqp uri scheme documentation for detailed information about uri
	private final EmbeddedAMQPBroker broker = new EmbeddedAMQPBroker();
	private final String uri = "amqp://guest:guest@localhost" + ":" + this.broker.getPort();
	private final String exchangeName = "";
	private final String queueName = "testQueue";
	private final int heartbeat = 60;

	@Test
	public void testWriteRead() throws Exception {
		final List<IMonitoringRecord> inputRecords = this.provideEvents();

		// new AmqpBrokerThread().start();
		// Thread.sleep(10000);
		System.out.println("Im starting the broker");
		this.broker.start();
		System.out.println("Im back");

		// Create monitoring controller for AMQPWriter
		final Configuration config = ConfigurationFactory.createDefaultConfiguration();
		config.setProperty(ConfigurationFactory.WRITER_CLASSNAME, AmqpWriter.class.getName());
		config.setProperty(AmqpWriter.CONFIG_URI, this.uri);
		config.setProperty(AmqpWriter.CONFIG_EXCHANGENAME, this.exchangeName);
		config.setProperty(AmqpWriter.CONFIG_QUEUENAME, this.queueName);
		config.setProperty(AmqpWriter.CONFIG_HEARTBEAT, this.heartbeat);
		final MonitoringController monCtrl = MonitoringController.createInstance(config);

		System.out.println("2");

		// Start the reader
		final AmqpReaderThread amqpReaderThread = new AmqpReaderThread(this.uri, this.queueName, this.heartbeat);
		amqpReaderThread.start();
		Thread.sleep(1000); // wait a second to make sure the reader is ready to read

		this.checkControllerStateBeforeRecordsPassedToController(monCtrl);

		// Send records
		for (final IMonitoringRecord record : inputRecords) {
			monCtrl.newMonitoringRecord(record);
		}

		Thread.sleep(1000); // wait a second to give the FS writer the chance to write the monitoring log.

		this.checkControllerStateAfterRecordsPassedToController(monCtrl);

		final List<IMonitoringRecord> outputRecords = amqpReaderThread.getOutputList();

		// Inspect records (sublist is used to exclude the KiekerMetadataRecord sent by the monitoring controller)
		Assert.assertEquals("Unexpected set of records", inputRecords, outputRecords.subList(1, outputRecords.size()));

		// Need to terminate explicitly, because otherwise, the monitoring log directory cannot be removed
		monCtrl.terminateMonitoring();
		monCtrl.waitForTermination(TIMEOUT_IN_MS);
	}

	protected void checkControllerStateBeforeRecordsPassedToController(final IMonitoringController monitoringController) {
		Assert.assertTrue(monitoringController.isMonitoringEnabled());
	}

	protected void checkControllerStateAfterRecordsPassedToController(final IMonitoringController monitoringController) {
		Assert.assertTrue(monitoringController.isMonitoringEnabled());
		monitoringController.disableMonitoring();
		Assert.assertFalse(monitoringController.isMonitoringEnabled());
	}

	/**
	 * Returns a list of {@link IMonitoringRecord}s to be used in this test. Extending classes can override this method to use their own list of records.
	 *
	 * @return A list of records.
	 */
	protected List<IMonitoringRecord> provideEvents() {
		final List<IMonitoringRecord> someEvents = new ArrayList<IMonitoringRecord>();
		for (int i = 0; i < 5; i = someEvents.size()) {
			final List<AbstractTraceEvent> nextBatch = Arrays.asList(
					BookstoreEventRecordFactory.validSyncTraceAdditionalCallEventsGap(i, i, "Mn51D97t0",
							"srv-LURS0EMw").getTraceEvents());
			someEvents.addAll(nextBatch);
		}
		someEvents.add(new EmptyRecord()); // this record used to cause problems (#475)
		return someEvents;
	}

	/**
	 * Extra thread for AmqpReader for testing
	 *
	 * @author Lars Bluemke
	 */
	private class AmqpReaderThread extends Thread {
		private final AMQPReader amqpReader;
		private final List<IMonitoringRecord> outputList;

		public AmqpReaderThread(final String uri, final String queueName, final int heartbeat) {
			this.amqpReader = new AMQPReader(uri, queueName, heartbeat);
			this.outputList = new LinkedList<>();
		}

		@Override
		public void run() {
			StageTester.test(this.amqpReader).and().receive(this.outputList).from(this.amqpReader.getOutputPort()).start();
		}

		public List<IMonitoringRecord> getOutputList() {
			return this.outputList;
		}
	}

}
