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

package kieker.analysis.plugin.reader.tcp;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.reader.AbstractReaderPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.exception.RecordInstantiationException;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.CachedRecordFactoryCatalog;
import kieker.common.record.factory.IRecordFactory;
import kieker.common.record.misc.RegistryRecord;
import kieker.common.util.registry.ILookup;
import kieker.common.util.registry.Lookup;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class NewTcpReader extends AbstractReaderPlugin {

	/** The name of the output port delivering the received records. */
	public static final String OUTPUT_PORT_NAME_RECORDS = "monitoringRecords";

	/** The name of the configuration determining the TCP port. */
	public static final String CONFIG_PROPERTY_NAME_PORT1 = "port1";
	/** The name of the configuration determining the TCP port. */
	public static final String CONFIG_PROPERTY_NAME_PORT2 = "port2";

	private static final int MESSAGE_BUFFER_SIZE = 65535;

	TcpServer stringRecordReader;
	private TcpServer monitoringRecordReader;

	final ILookup<String> stringRegistry = new Lookup<String>();
	private final CachedRecordFactoryCatalog cachedRecordFactoryCatalog = CachedRecordFactoryCatalog.getInstance();

	public NewTcpReader(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.createMonitoringRecordReader(new DefaultServerSocketChannelFactory());
		this.createStringRecordReader(new DefaultServerSocketChannelFactory());
	}

	protected void createMonitoringRecordReader(final ServerSocketChannelFactory serverSocketChannelFactory) {
		final int port = this.configuration.getIntProperty(CONFIG_PROPERTY_NAME_PORT1);
		final int messageBufferSize = MESSAGE_BUFFER_SIZE;
		final BufferListener listener = new BufferListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void read(final ByteBuffer buffer) {
				final int clazzId = buffer.getInt();
				final long loggingTimestamp = buffer.getLong();
				try { // NOCS (Nested try-catch)
					final String recordClassName = NewTcpReader.this.stringRegistry.get(clazzId);

					final IRecordFactory<? extends IMonitoringRecord> recordFactory = NewTcpReader.this.cachedRecordFactoryCatalog.get(recordClassName);
					final IMonitoringRecord record = recordFactory.create(buffer, NewTcpReader.this.stringRegistry);
					record.setLoggingTimestamp(loggingTimestamp);

					NewTcpReader.this.deliver(OUTPUT_PORT_NAME_RECORDS, record);
				} catch (final BufferUnderflowException ex) {
					NewTcpReader.this.log.error("Failed to create record.", ex);
				} catch (final RecordInstantiationException ex) {
					NewTcpReader.this.log.error("Failed to create record.", ex);
				}
			}
		};
		this.monitoringRecordReader = new TcpServer(serverSocketChannelFactory, port, messageBufferSize, listener, LOG);
	}

	protected void createStringRecordReader(final ServerSocketChannelFactory serverSocketChannelFactory) {
		final int port = this.configuration.getIntProperty(CONFIG_PROPERTY_NAME_PORT2);
		final int messageBufferSize = MESSAGE_BUFFER_SIZE;
		final BufferListener listener = new BufferListener() {
			@Override
			public void read(final ByteBuffer buffer) {
				RegistryRecord.registerRecordInRegistry(buffer, NewTcpReader.this.stringRegistry);
			}
		};
		this.stringRecordReader = new TcpServer(serverSocketChannelFactory, port, messageBufferSize, listener, LOG);
	}

	@Override
	public boolean init() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				NewTcpReader.this.stringRecordReader.start();
			}
		}).start();
		return super.init();
	}

	@Override
	public boolean read() {
		return this.monitoringRecordReader.start();
	}

	@Override
	public void terminate(final boolean error) {
		this.log.info("Shutdown of TCPReader requested.");
		this.monitoringRecordReader.terminate();
		this.stringRecordReader.terminate();
	}

	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_PORT1, Integer.toString(this.monitoringRecordReader.getPort()));
		configuration.setProperty(CONFIG_PROPERTY_NAME_PORT2, Integer.toString(this.stringRecordReader.getPort()));
		return configuration;
	}

}
