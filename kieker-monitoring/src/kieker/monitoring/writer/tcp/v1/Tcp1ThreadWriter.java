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

package kieker.monitoring.writer.tcp.v1;

import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.writer.AbstractAsyncWriter;

/**
 * @author Christian Wulf
 *
 * @since 1.12
 */
public class Tcp1ThreadWriter extends AbstractAsyncWriter {

	private static final Log LOG = LogFactory.getLog(Tcp1ThreadWriter.class);
	private static final String PREFIX = Tcp1ThreadWriter.class.getName() + ".";

	public static final String CONFIG_HOSTNAME = PREFIX + "hostname"; // NOCS (afterPREFIX)
	public static final String CONFIG_PORT1 = PREFIX + "port1"; // NOCS (afterPREFIX)
	public static final String CONFIG_BUFFERSIZE = PREFIX + "bufferSize"; // NOCS (afterPREFIX)
	public static final String CONFIG_FLUSH = PREFIX + "flush"; // NOCS (afterPREFIX)

	// private Queue<IMonitoringRecord> queue;

	private final String hostname;
	private final int port1;
	private final int bufferSize;
	private final boolean flush;

	private TcpWriterThread worker;

	public Tcp1ThreadWriter(final Configuration configuration) {
		super(configuration);
		this.hostname = configuration.getStringProperty(CONFIG_HOSTNAME);
		this.port1 = configuration.getIntProperty(CONFIG_PORT1);
		this.bufferSize = configuration.getIntProperty(CONFIG_BUFFERSIZE);
		this.flush = configuration.getBooleanProperty(CONFIG_FLUSH);
	}

	@Override
	protected void init() throws Exception {
		this.worker = new TcpWriterThread(this.monitoringController, this.blockingQueue, this.hostname, this.port1, this.bufferSize, this.flush);
		this.addWorker(this.worker);
	}

	@Override
	public boolean newMonitoringRecordNonBlocking(final IMonitoringRecord monitoringRecord) {
		// delegates (string registry) records to the worker directly and thus ignores the prioritizedBlockingQueue
		try {
			this.blockingQueue.add(monitoringRecord);
			return true;
		} catch (final Exception e) {
			LOG.warn("An exception occurred", e);
		}
		return false;
	}

}
