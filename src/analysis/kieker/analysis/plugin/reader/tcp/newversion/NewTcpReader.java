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

package kieker.analysis.plugin.reader.tcp.newversion;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.reader.AbstractReaderPlugin;
import kieker.analysis.plugin.reader.tcp.TcpServer;
import kieker.common.configuration.Configuration;
import kieker.common.exception.RecordInstantiationException;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.CachedRecordFactoryCatalog;
import kieker.common.record.factory.IRecordFactory;
import kieker.common.record.misc.RegistryRecord;
import kieker.common.util.registry.ILookup;
import kieker.common.util.registry.Lookup;
import kieker.monitoring.writer.tcp.NewTcpWriter;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
@Plugin(description = "A reader which reads records from a TCP port",
		outputPorts = {
			@OutputPort(name = NewTcpReader.OUTPUT_PORT_NAME_RECORDS, eventTypes = { IMonitoringRecord.class }, description = "Output Port of the TCPReader")
		},
		configuration = {
			@Property(name = NewTcpReader.CONFIG_PROPERTY_NAME_PORT1, defaultValue = "10133",
					description = "The first port of the server used for the TCP connection."),
			@Property(name = NewTcpReader.CONFIG_PROPERTY_NAME_PORT2, defaultValue = "10134",
					description = "The second port of the server used for the TCP connection.")
		})
public final class NewTcpReader extends AbstractReaderPlugin implements ReadListener {

	/** The name of the output port delivering the received records. */
	public static final String OUTPUT_PORT_NAME_RECORDS = "monitoringRecords";

	/** The name of the configuration determining the TCP port. */
	public static final String CONFIG_PROPERTY_NAME_PORT1 = "port1";
	/** The name of the configuration determining the TCP port. */
	public static final String CONFIG_PROPERTY_NAME_PORT2 = "port2";

	private static final int MESSAGE_BUFFER_SIZE = 65535;

	private TcpServer monitoringRecordReader;

	final ILookup<String> stringRegistry = new Lookup<String>();
	private final CachedRecordFactoryCatalog cachedRecordFactoryCatalog = CachedRecordFactoryCatalog.getInstance();

	public NewTcpReader(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.createMonitoringRecordReader(new DefaultServerSocketChannelFactory());
	}

	/**
	 * Used in tests; declared 'public' because kieker does not use the same package for tests
	 *
	 * @param byteBufferFactory
	 */
	public NewTcpReader(final Configuration configuration, final IProjectContext projectContext, final ServerSocketChannelFactory serverSocketChannelFactory) {
		super(configuration, projectContext);
		this.createMonitoringRecordReader(serverSocketChannelFactory);
	}

	@Override
	public void read(final ByteBuffer buffer) {
		final int clazzId = buffer.getInt();
		final long loggingTimestamp = buffer.getLong();

		if (clazzId == NewTcpWriter.REGISTRY_RECORD_CLASS_ID) {
			this.deserializeStringRecord(clazzId, loggingTimestamp, buffer);
		} else {
			this.deserializeMonitoringRecord(clazzId, loggingTimestamp, buffer);
		}
	}

	protected void createMonitoringRecordReader(final ServerSocketChannelFactory serverSocketChannelFactory) {
		final int port = this.configuration.getIntProperty(CONFIG_PROPERTY_NAME_PORT1);
		final int messageBufferSize = MESSAGE_BUFFER_SIZE;
		this.monitoringRecordReader = new TcpServer(serverSocketChannelFactory, port, messageBufferSize, this, LOG);
	}

	protected void deserializeStringRecord(final int clazzId, final long loggingTimestamp, final ByteBuffer buffer) {
		RegistryRecord.registerRecordInRegistry(buffer, this.stringRegistry);
	}

	protected void deserializeMonitoringRecord(final int clazzId, final long loggingTimestamp, final ByteBuffer buffer) {
		try { // NOCS (Nested try-catch)
			final String recordClassName = NewTcpReader.this.stringRegistry.get(clazzId);

			final IRecordFactory<? extends IMonitoringRecord> recordFactory = NewTcpReader.this.cachedRecordFactoryCatalog.get(recordClassName);
			final IMonitoringRecord record = recordFactory.create(buffer, NewTcpReader.this.stringRegistry);
			record.setLoggingTimestamp(loggingTimestamp);
			// System.out.println("Deserialized: " + record);
			NewTcpReader.this.deliver(OUTPUT_PORT_NAME_RECORDS, record);
		} catch (final BufferUnderflowException ex) {
			NewTcpReader.this.log.error("Failed to create record.", ex);
		} catch (final RecordInstantiationException ex) {
			NewTcpReader.this.log.error("Failed to create record.", ex);
		}
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
