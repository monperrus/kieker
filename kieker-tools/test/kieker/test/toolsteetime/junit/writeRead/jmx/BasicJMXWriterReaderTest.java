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

package kieker.test.toolsteetime.junit.writeRead.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.junit.Test;

import kieker.analysisteetime.plugin.reader.jmx.JMXReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.AbstractTraceEvent;
import kieker.common.record.misc.EmptyRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.jmx.JmxWriter;

import kieker.test.analysis.util.plugin.filter.flow.BookstoreEventRecordFactory;

import teetime.framework.test.StageTester;

/**
 * @author Jan Waller, Lars Blumke
 *
 * @since 1.8
 */
public class BasicJMXWriterReaderTest implements NotificationListener {

	private static final int TIMEOUT_IN_MS = 0;

	private static final String DOMAIN = "kieker.monitoring";
	private static final String CONTROLLER = "MonitoringController";
	private static final String PORT = "59999";
	private static final String LOGNAME = "MonitoringLog";

	/**
	 * Empty default constructor
	 */
	public BasicJMXWriterReaderTest() {
		// empty constructor
	}

	// @Ignore("this test has some major issues (produces non-determinssstic results due to Thread.sleep()) and does not pass so far (17.01.2018)")
	@Test
	public void testWriteRead() throws Exception {
		final String registryHostname = "localhost";
		final int registryPort = Integer.parseInt(BasicJMXWriterReaderTest.PORT);

		final String domain = BasicJMXWriterReaderTest.DOMAIN;
		final String remoteIdForMonitoringLog = BasicJMXWriterReaderTest.LOGNAME;

		final List<IMonitoringRecord> someEvents = this.provideEvents();

		// Create monitoring controller for JMXWriter
		final MonitoringController monCtrl = this.createAndExposeMonitoringController();

		final JMXReader jmxReader = new JMXReader(false, null, BasicJMXWriterReaderTest.DOMAIN,
				BasicJMXWriterReaderTest.LOGNAME, Integer.parseInt(BasicJMXWriterReaderTest.PORT), "localhost");
		final List<IMonitoringRecord> outputList = new ArrayList<>();

		final Thread client = new Thread(new Runnable() {
			@Override
			public void run() {
				StageTester.test(jmxReader).and().receive(outputList).from(jmxReader.getOutputPort()).start();
			}
		});
		client.start();

		// final JMXReaderLogic readerLogic = new JMXReaderLogic(false, null, domain, remoteIdForMonitoringLog, registryPort, registryHostname,
		// LogFactory.getLog(JMXReader.class), this);
		//
		// readerLogic.read();

		final Thread testProbe = new Thread(new Runnable() {
			@Override
			public void run() {
				// Send records
				for (final IMonitoringRecord record : someEvents) {
					monCtrl.newMonitoringRecord(record);
				}

				// try {
				// Thread.sleep(1000);
				// } catch (final InterruptedException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }

				monCtrl.terminateMonitoring();
				try {
					monCtrl.waitForTermination(TIMEOUT_IN_MS);
				} catch (final InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
		});
		// testProbe.start();
		testProbe.run();

		// this.checkControllerStateBeforeRecordsPassedToController(monCtrl);

		// Test the JMX Controller
		// final JMXServiceURL serviceURL = new JMXServiceURL(
		// "service:jmx:rmi:///jndi/rmi://localhost:" + BasicJMXWriterReaderTest.PORT + "/jmxrmi");
		// // service:jmx:rmi:///jndi/rmi://localhost:59999/jmxrmi
		// final JMXConnector jmx = JMXConnectorFactory.connect(serviceURL);
		// jmx.close();

		// Start the JMXReader
		// final JMXReaderThread jmxReaderThread = new JMXReaderThread(false, null, BasicJMXWriterReaderTest.DOMAIN,
		// BasicJMXWriterReaderTest.LOGNAME, Integer.parseInt(BasicJMXWriterReaderTest.PORT), "localhost");
		// jmxReaderThread.start();

		// this.checkControllerStateBeforeRecordsPassedToController(monCtrl);

		// Send records
		// for (final IMonitoringRecord record : someEvents) {
		// monCtrl.newMonitoringRecord(record);
		// }

		// Thread.sleep(3000); // wait a second to give the writer the chance to write the monitoring log.

		// this.checkControllerStateAfterRecordsPassedToController(monCtrl);

		// Thread.sleep(3000); // wait a second to give the writer the chance to write the monitoring log.

		// Need to terminate explicitly, because otherwise, the monitoring log directory cannot be removed
		// monCtrl.terminateMonitoring();
		// monCtrl.waitForTermination(TIMEOUT_IN_MS);

		client.join(TIMEOUT_IN_MS);
		// final List<IMonitoringRecord> monitoringRecords = jmxReaderThread.getOutputList();

		// testProbe.join(TIMEOUT_IN_MS);

		// readerLogic.getJmx().close();

		// Inspect records
		Assert.assertEquals("Unexpected set of records", someEvents, outputList);
	}

	private MonitoringController createAndExposeMonitoringController() {
		final Configuration config = ConfigurationFactory.createDefaultConfiguration();
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX, "true");
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_CONTROLLER, "true");
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_DOMAIN, BasicJMXWriterReaderTest.DOMAIN);
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_CONTROLLER_NAME, BasicJMXWriterReaderTest.CONTROLLER);
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_REMOTE, "true");
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_REMOTE_FALLBACK, "false");
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_REMOTE_NAME, "JMXServer");
		config.setProperty(ConfigurationFactory.ACTIVATE_JMX_REMOTE_PORT, BasicJMXWriterReaderTest.PORT);

		config.setProperty(ConfigurationFactory.WRITER_CLASSNAME, JmxWriter.class.getName());
		config.setProperty(JmxWriter.CONFIG_DOMAIN, "");
		config.setProperty(JmxWriter.CONFIG_LOGNAME, BasicJMXWriterReaderTest.LOGNAME);

		return MonitoringController.createInstance(config);
	}

	/**
	 * Returns a list of {@link IMonitoringRecord}s to be used in this test. Extending classes can override this method
	 * to use their own list of records.
	 *
	 * @return A list of records.
	 */
	protected List<IMonitoringRecord> provideEvents() {
		final List<IMonitoringRecord> someEvents = new ArrayList<IMonitoringRecord>();
		for (int i = 0; i < 5; i = someEvents.size()) {
			final List<AbstractTraceEvent> nextBatch = Arrays.asList(BookstoreEventRecordFactory
					.validSyncTraceAdditionalCallEventsGap(i, i, "Mn51D97t0", "srv-LURS0EMw").getTraceEvents());
			someEvents.addAll(nextBatch);
		}
		someEvents.add(new EmptyRecord()); // this record used to cause problems (#475)
		return someEvents;
	}

	/**
	 * Checks if the given {@link IMonitoringController} is in the expected state after having passed the records to the
	 * controller.
	 *
	 * @param monitoringController
	 *            The monitoring controller in question.
	 *
	 * @throws Exception
	 *             If something went wrong during the check.
	 */
	protected void checkControllerStateAfterRecordsPassedToController(final IMonitoringController monitoringController)
			throws Exception {
		// Test the JMX Controller
		final JMXServiceURL serviceURL = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://localhost:" + BasicJMXWriterReaderTest.PORT + "/jmxrmi");
		final ObjectName controllerObjectName = new ObjectName(BasicJMXWriterReaderTest.DOMAIN, "type",
				BasicJMXWriterReaderTest.CONTROLLER);

		final JMXConnector jmx = JMXConnectorFactory.connect(serviceURL);
		try {
			final MBeanServerConnection connection = jmx.getMBeanServerConnection();

			// final Object tmpObj = MBeanServerInvocationHandler.newProxyInstance(connection, controllerObjectName,
			// IMonitoringController.class, false);
			final IMonitoringController monCtrlViaJmx = JMX.newMBeanProxy(connection, controllerObjectName,
					IMonitoringController.class, false);

			// (required for the cast not being removed by Java 1.6 editors)
			// final IMonitoringController ctrlJMX = (IMonitoringController) tmpObj; // NOCS // NOPMD

			Assert.assertTrue(monitoringController.isMonitoringEnabled());
			Assert.assertTrue(monCtrlViaJmx.isMonitoringEnabled());

			Assert.assertTrue(monCtrlViaJmx.disableMonitoring());

			Assert.assertFalse(monitoringController.isMonitoringEnabled());
			Assert.assertFalse(monCtrlViaJmx.isMonitoringEnabled());
		} finally {
			jmx.close();
		}
	}

	/**
	 * Checks if the given {@link IMonitoringController} is in the expected state before having passed the records to
	 * the controller.
	 *
	 * @param monitoringController
	 *            The monitoring controller in question.
	 *
	 * @throws Exception
	 *             If something went wrong during the check.
	 */
	protected void checkControllerStateBeforeRecordsPassedToController(final IMonitoringController monitoringController)
			throws Exception {
		// Test the JMX Controller
		final JMXServiceURL serviceURL = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://localhost:" + BasicJMXWriterReaderTest.PORT + "/jmxrmi");
		// service:jmx:rmi:///jndi/rmi://localhost:59999/jmxrmi

		final JMXConnector jmx = JMXConnectorFactory.connect(serviceURL);
		final MBeanServerConnection connection = jmx.getMBeanServerConnection();

		final ObjectName controllerObjectName = new ObjectName(BasicJMXWriterReaderTest.DOMAIN, "type",
				BasicJMXWriterReaderTest.CONTROLLER);

		final Object tmpObj = JMX.newMBeanProxy(connection, controllerObjectName, IMonitoringController.class, false);
		final IMonitoringController ctrlJMX = (IMonitoringController) tmpObj; // NOCS // NOPMD (required for the cast
																				// not being removed by Java 1.6
																				// editors)

		Assert.assertTrue(monitoringController.isMonitoringEnabled());
		Assert.assertTrue(ctrlJMX.isMonitoringEnabled());

		final String domain = BasicJMXWriterReaderTest.DOMAIN;
		final String remoteIdForMonitoringLog = BasicJMXWriterReaderTest.LOGNAME;
		final ObjectName monitoringLog = new ObjectName(domain, "type", remoteIdForMonitoringLog);
		// kieker.monitoring:type=MonitoringLog

		connection.addNotificationListener(monitoringLog, this, null, null);
		// final KiekerJmxMonitoringLog log = JMX.newMBeanProxy(connection, monitoringLog, KiekerJmxMonitoringLog.class, false);

		// jmx.close();
	}

	/**
	 * Extra thread for JMXReader for testing
	 *
	 * @author Lars Bluemke
	 */
	private class JMXReaderThread extends Thread {
		private final JMXReader jmxReader;
		private final List<IMonitoringRecord> outputList;

		public JMXReaderThread(final boolean silentreconnect, final JMXServiceURL serviceURL, final String domain,
				final String logname, final int port, final String server) {
			this.jmxReader = new JMXReader(silentreconnect, serviceURL, domain, logname, port, server);
			this.outputList = new LinkedList<>();
		}

		@Override
		public void run() {
			StageTester.test(this.jmxReader).and().receive(this.outputList).from(this.jmxReader.getOutputPort())
					.start();
		}

		public List<IMonitoringRecord> getOutputList() {
			return this.outputList;
		}
	}

	@Override
	public synchronized void handleNotification(Notification notification, Object handback) {
		System.out.println("notification: " + notification + ", data=" + notification.getUserData());
	}
}
