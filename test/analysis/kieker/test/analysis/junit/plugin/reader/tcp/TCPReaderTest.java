package kieker.test.analysis.junit.plugin.reader.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import kieker.analysis.AnalysisController;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.tcp.TCPReader;
import kieker.analysis.plugin.reader.tcp.newversion.ServerSocketChannelFactory;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;

import kieker.test.common.junit.AbstractKiekerTest;

//@RunWith(MockitoJUnitRunner.class)
public class TCPReaderTest extends AbstractKiekerTest {

	private static class MockingServerSocketChannelFactory implements ServerSocketChannelFactory {
		final IRegistry<String> stringRegistry;

		public MockingServerSocketChannelFactory(final IRegistry<String> stringRegistry) {
			this.stringRegistry = stringRegistry;
		}

		private static class MockedServerSocketChannel extends ServerSocketChannel {

			private final ServerSocket serverSocket;
			private final SocketChannel socketChannel;

			protected MockedServerSocketChannel(final SelectorProvider provider, final SocketChannel socketChannel) {
				super(provider);
				this.serverSocket = Mockito.mock(ServerSocket.class);
				this.socketChannel = socketChannel;
			}

			@Override
			public ServerSocket socket() {
				return this.serverSocket;
			}

			@Override
			public SocketChannel accept() throws IOException {
				return this.socketChannel;
			}

			@Override
			public SocketAddress getLocalAddress() throws IOException {
				return null;
			}

			@Override
			public <T> T getOption(final SocketOption<T> name) throws IOException {
				return null;
			}

			@Override
			public Set<SocketOption<?>> supportedOptions() {
				return null;
			}

			@Override
			public ServerSocketChannel bind(final SocketAddress local, final int backlog) throws IOException {
				return null;
			}

			@Override
			public <T> ServerSocketChannel setOption(final SocketOption<T> name, final T value) throws IOException {
				return null;
			}

			@Override
			protected void implCloseSelectableChannel() throws IOException {}

			@Override
			protected void implConfigureBlocking(final boolean block) throws IOException {}
		}

		@Override
		public ServerSocketChannel openServerSocket() throws IOException {
			System.out.println("openServerSocket");
			final ServerSocket serverSocket = Mockito.mock(ServerSocket.class);

			final SocketChannel socketChannel = Mockito.mock(SocketChannel.class);
			Mockito.when(socketChannel.read(Matchers.isA(ByteBuffer.class))).thenAnswer(this.readBuffer());
			Mockito.when(socketChannel.read(Matchers.isA(ByteBuffer.class))).thenReturn(-1);

			// final ServerSocket serverSocket = Mockito.mock(ServerSocket.class);

			final ServerSocketChannel ssc = Mockito.spy(new MockedServerSocketChannel(null, socketChannel));
			// Mockito.doReturn(serverSocket).when(ssc).socket();
			// Mockito.doReturn(socketChannel).when(ssc).accept();

			// Mockito.doNothing().when(ssc).close();

			return ssc;
		}

		public Answer<Integer> readBuffer() {
			return new Answer<Integer>() {
				@Override
				public Integer answer(final InvocationOnMock invocation) throws Throwable {
					System.out.println("answer: ");
					final ByteBuffer byteBuffer = (ByteBuffer) invocation.getArguments()[0];

					final long timestamp = 0;
					final long traceId = 0;
					final int orderIndex = 0;
					final String operationSignature = "test()";
					final String classSignature = "a.b.c";
					final BeforeOperationEvent beforeOperationEvent = new BeforeOperationEvent(timestamp, traceId, orderIndex, operationSignature, classSignature);

					beforeOperationEvent.writeBytes(byteBuffer, MockingServerSocketChannelFactory.this.stringRegistry);

					// for (int i = 0; i < MockingServerSocketChannelFactory.this.stringRegistry.getSize(); i++) {
					// final String string = MockingServerSocketChannelFactory.this.stringRegistry.get(i);
					// final RegistryRecord registryRecord = new RegistryRecord(i, string);
					// registryRecord.writeBytes(byteBuffer, MockingServerSocketChannelFactory.this.stringRegistry);
					// }

					final int position = byteBuffer.position();
					byteBuffer.flip();

					return position;
				}
			};
		}
	}

	@Test
	public void testDeserialization() throws Exception {
		final AnalysisController analysisController = new AnalysisController();

		final IRegistry<String> stringRegistry = new Registry<String>();
		final ServerSocketChannelFactory serverSocketChannelFactory = new MockingServerSocketChannelFactory(stringRegistry);
		// Mockito.when(ServerSocketChannel.open()).thenReturn(ssc);

		final Configuration configuration0 = new Configuration();
		final TCPReader tcpReader = new TCPReader(configuration0, analysisController, serverSocketChannelFactory);

		// final TCPReader mockedTcpReader = Mockito.spy(tcpReader);
		// Mockito.when(mockedTcpReader.init()).thenReturn(true);

		final Configuration configuration1 = new Configuration();
		final ListCollectionFilter<IMonitoringRecord> collectionFilter = new ListCollectionFilter<IMonitoringRecord>(configuration1, analysisController);

		analysisController.connect(tcpReader, TCPReader.OUTPUT_PORT_NAME_RECORDS, collectionFilter, ListCollectionFilter.INPUT_PORT_NAME);

		analysisController.run();

	}
}
