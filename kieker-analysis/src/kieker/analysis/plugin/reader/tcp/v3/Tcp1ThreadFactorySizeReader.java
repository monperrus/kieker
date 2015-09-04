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

package kieker.analysis.plugin.reader.tcp.v3;

import java.nio.ByteBuffer;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.reader.AbstractReaderPlugin;
import kieker.analysis.plugin.reader.tcp.server.DefaultServerSocketChannelFactory;
import kieker.analysis.plugin.reader.tcp.server.ReadListener;
import kieker.analysis.plugin.reader.tcp.server.ServerSocketChannelFactory;
import kieker.analysis.plugin.reader.tcp.server.TcpServer;
import kieker.common.configuration.Configuration;
import kieker.common.exception.RecordInstantiationException;
import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.CachedRecordFactoryCatalog;
import kieker.common.record.factory.IRecordFactory;
import kieker.common.record.factory.RecordFactoryResolver;
import kieker.common.record.misc.RegistryRecord;
import kieker.common.util.registry.ILookup;
import kieker.common.util.registry.Lookup;

/**
 * @author Christian Wulf
 *
 * @since 1.12
 */
@Plugin(description = "A reader which reads records from a TCP port", outputPorts = {
	@OutputPort(name = Tcp1ThreadFactorySizeReader.OUTPUT_PORT_NAME_RECORDS, eventTypes = { IMonitoringRecord.class }, description = "Output Port of the TCPReader")
}, configuration = {
	@Property(name = Tcp1ThreadFactorySizeReader.CONFIG_PROPERTY_NAME_PORT1, defaultValue = "10133", description = "The port of the server used for the TCP connection.")
})
public final class Tcp1ThreadFactorySizeReader extends AbstractReaderPlugin implements ReadListener {

	/** The name of the output port delivering the received records. */
	public static final String OUTPUT_PORT_NAME_RECORDS = "monitoringRecords";

	/** The name of the configuration determining the TCP port. */
	public static final String CONFIG_PROPERTY_NAME_PORT1 = "port1";

	private static final int MESSAGE_BUFFER_SIZE = 65535;

	private TcpServer monitoringRecordReader;

	private final ILookup<String> stringRegistry = new Lookup<String>();
	private final CachedRecordFactoryCatalog cachedRecordFactoryCatalog;

	public Tcp1ThreadFactorySizeReader(final Configuration configuration, final IProjectContext projectContext) {
		this(configuration, projectContext, new DefaultServerSocketChannelFactory());
	}

	/**
	 * Used in tests; declared 'public' because kieker does not use the same package for tests
	 *
	 * @param byteBufferFactory
	 */
	public Tcp1ThreadFactorySizeReader(final Configuration configuration, final IProjectContext projectContext,
			final ServerSocketChannelFactory serverSocketChannelFactory) {
		super(configuration, projectContext);
		this.cachedRecordFactoryCatalog = new CachedRecordFactoryCatalog(new RecordFactoryResolver());
		this.createMonitoringRecordReader(serverSocketChannelFactory);
	}

	protected void createMonitoringRecordReader(final ServerSocketChannelFactory serverSocketChannelFactory) {
		final int port = this.configuration.getIntProperty(CONFIG_PROPERTY_NAME_PORT1);
		final int messageBufferSize = MESSAGE_BUFFER_SIZE;
		this.monitoringRecordReader = new TcpServer(serverSocketChannelFactory, port, messageBufferSize, this, LOG);
	}

	@Override
	public boolean read(final ByteBuffer buffer) {
		final int remainingInBuffer = buffer.remaining();
		// identify record class
		if (remainingInBuffer < AbstractMonitoringRecord.TYPE_SIZE_INT) {
			return false;
		}
		final int clazzId = buffer.getInt();

		if (clazzId == RegistryRecord.CLASS_ID) {
			return this.deserializeStringRecord(buffer, remainingInBuffer); // TODO check size before reading buffer as below
		}

		return this.deserializeMonitoringRecord(clazzId, buffer, remainingInBuffer);
	}

	private boolean deserializeStringRecord(final ByteBuffer buffer, final int remainingInBuffer) {
		RegistryRecord.registerRecordInRegistry(buffer, this.stringRegistry);
		return true;
	}

	private boolean deserializeMonitoringRecord(final int clazzId, final ByteBuffer buffer, final int remainingInBuffer) {
		// identify logging timestamp
		if (remainingInBuffer < AbstractMonitoringRecord.TYPE_SIZE_LONG) {
			return false;
		}
		final long loggingTimestamp = buffer.getLong();

		final String recordClassName = this.stringRegistry.get(clazzId);
		// identify record data
		final IRecordFactory<? extends IMonitoringRecord> recordFactory = this.cachedRecordFactoryCatalog.get(recordClassName);
		if (remainingInBuffer < recordFactory.getRecordSizeInBytes()) { // includes the case where size is -1
			return false;
		}

		try {
			final IMonitoringRecord record = recordFactory.create(buffer, this.stringRegistry);
			record.setLoggingTimestamp(loggingTimestamp);

			this.deliver(OUTPUT_PORT_NAME_RECORDS, record);
		} catch (final RecordInstantiationException ex) {
			// for other reasons than due to a BufferUnderflowException
			this.log.error("Failed to create record of type " + recordClassName, ex);
		}

		return true;
	}

	@Override
	public boolean read() {
		return this.monitoringRecordReader.start();
	}

	@Override
	public void terminate(final boolean error) {
		this.log.info("Shutdown of TCPReader requested.");
		this.monitoringRecordReader.terminate();
	}

	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_PORT1, Integer.toString(this.monitoringRecordReader.getPort()));
		return configuration;
	}

}
