package kieker.test.analysis.junit.plugin.reader.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import kieker.analysis.AnalysisController;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.tcp.ServerSocketChannelFactory;
import kieker.analysis.plugin.reader.tcp.TCPReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;

public class TCPReaderTest {

	private static class MockingServerSocketChannelFactory implements ServerSocketChannelFactory {
		final IRegistry<String> stringRegistry;

		public MockingServerSocketChannelFactory(final IRegistry<String> stringRegistry) {
			this.stringRegistry = stringRegistry;
		}

		@Override
		public ServerSocketChannel openServerSocket() throws IOException {
			final ServerSocketChannel ssc = Mockito.mock(ServerSocketChannel.class);

			final SocketChannel socketChannel = Mockito.mock(SocketChannel.class);
			Mockito.when(ssc.accept()).thenReturn(socketChannel);

			Mockito.when(socketChannel.read(Matchers.isA(ByteBuffer.class))).thenAnswer(this.readBuffer());
			Mockito.when(socketChannel.read(Matchers.isA(ByteBuffer.class))).thenReturn(-1);

			return ssc;
		}

		public Answer<Integer> readBuffer() {
			return new Answer<Integer>() {
				@Override
				public Integer answer(final InvocationOnMock invocation) throws Throwable {
					final ByteBuffer byteBuffer = (ByteBuffer) invocation.getArguments()[0];

					final long timestamp = 0;
					final long traceId = 0;
					final int orderIndex = 0;
					final String operationSignature = "test()";
					final String classSignature = "a.b.c";
					final BeforeOperationEvent beforeOperationEvent = new BeforeOperationEvent(timestamp, traceId, orderIndex, operationSignature, classSignature);

					beforeOperationEvent.writeBytes(byteBuffer, MockingServerSocketChannelFactory.this.stringRegistry);

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

		final TCPReader mockedTcpReader = Mockito.spy(tcpReader);
		Mockito.when(mockedTcpReader.init()).thenReturn(true);

		final Configuration configuration1 = new Configuration();
		final ListCollectionFilter<IMonitoringRecord> collectionFilter = new ListCollectionFilter<IMonitoringRecord>(configuration1, analysisController);

		analysisController.connect(tcpReader, TCPReader.OUTPUT_PORT_NAME_RECORDS, collectionFilter, ListCollectionFilter.INPUT_PORT_NAME);

		analysisController.run();

	}
}
