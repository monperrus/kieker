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

package kieker.monitoring.writer.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.record.IMonitoringRecord;
import kieker.common.util.RecordSerializer;
import kieker.common.util.registry.IRegistry;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.writer.AbstractAsyncThread;
import kieker.monitoring.writer.AbstractAsyncWriter;

/**
 *
 * @author Jan Waller
 *
 * @since 1.8
 */
public final class NewTcpWriter extends AbstractAsyncWriter {

	private static final Log LOG = LogFactory.getLog(NewTcpWriter.class);
	private static final String PREFIX = NewTcpWriter.class.getName() + ".";

	public static final String CONFIG_HOSTNAME = PREFIX + "hostname"; // NOCS (afterPREFIX)
	public static final String CONFIG_PORT1 = PREFIX + "port1"; // NOCS (afterPREFIX)
	// public static final String CONFIG_PORT2 = PREFIX + "port2"; // NOCS (afterPREFIX)
	public static final String CONFIG_BUFFERSIZE = PREFIX + "bufferSize"; // NOCS (afterPREFIX)
	public static final String CONFIG_FLUSH = PREFIX + "flush"; // NOCS (afterPREFIX)

	// TODO final, JCTools
	private Queue<IMonitoringRecord> queue;

	private final String hostname;
	private final int port1;
	private final int bufferSize;
	private final boolean flush;

	private NewTcpWriterThread worker;

	public NewTcpWriter(final Configuration configuration) {
		super(configuration);
		this.hostname = configuration.getStringProperty(CONFIG_HOSTNAME);
		this.port1 = configuration.getIntProperty(CONFIG_PORT1);
		// this.port2 = configuration.getIntProperty(CONFIG_PORT2);
		// should be check for buffers too small for a single record?
		this.bufferSize = configuration.getIntProperty(CONFIG_BUFFERSIZE);
		this.flush = configuration.getBooleanProperty(CONFIG_FLUSH);
	}

	@Override
	protected void init() throws Exception {
		this.worker = new NewTcpWriterThread(this.monitoringController, this.blockingQueue, this.hostname, this.port1, this.bufferSize, this.flush);
		this.addWorker(this.worker);
		// this.addWorker(new NewTcpWriterThread(this.monitoringController, this.prioritizedBlockingQueue, this.hostname, this.port2, this.bufferSize, this.flush));
	}

	@Override
	public boolean newMonitoringRecordNonBlocking(final IMonitoringRecord monitoringRecord) {
		// return this.newMonitoringRecord(monitoringRecord); // ignore prioritizedBlockingQueue
		try {
			this.worker.consume(monitoringRecord);
			return true;
		} catch (final Exception e) {
			LOG.warn("An exception occurred", e);
		}
		return false;
	}
}

/**
 *
 * @author Jan Waller
 *
 * @since 1.8
 */
class NewTcpWriterThread extends AbstractAsyncThread {
	private static final Log LOG = LogFactory.getLog(NewTcpWriterThread.class);

	private final SocketChannel socketChannel;
	private final ByteBuffer byteBuffer;
	private final IRegistry<String> stringRegistry;
	private final boolean flush;
	private final RecordSerializer recordSerializer;

	public NewTcpWriterThread(final IMonitoringController monitoringController, final BlockingQueue<IMonitoringRecord> writeQueue, final String hostname,
			final int port, final int bufferSize, final boolean flush) throws IOException {
		super(monitoringController, writeQueue);
		this.byteBuffer = ByteBuffer.allocateDirect(bufferSize);
		this.socketChannel = SocketChannel.open(new InetSocketAddress(hostname, port));
		this.stringRegistry = this.monitoringController.getStringRegistry();
		this.flush = flush;
		this.recordSerializer = new RecordSerializer(this.stringRegistry);
	}

	@Override
	protected void consume(final IMonitoringRecord monitoringRecord) throws Exception {
		final ByteBuffer buffer = this.byteBuffer;

		if ((monitoringRecord.getSize() + 4 + 8) > buffer.remaining()) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				this.socketChannel.write(buffer);
			}
			buffer.clear();
		}

		this.recordSerializer.serialize(monitoringRecord, buffer);

		if (this.flush) {
			buffer.flip();
			while (buffer.hasRemaining()) {
				this.socketChannel.write(buffer);
			}
			buffer.clear();
		}
	}

	@Override
	protected void cleanup() {
		try {
			final ByteBuffer buffer = this.byteBuffer;
			buffer.flip();
			while (buffer.hasRemaining()) {
				this.socketChannel.write(buffer);
			}
			this.socketChannel.close();
		} catch (final IOException ex) {
			LOG.error("Error closing connection", ex);
		}
	}
}
