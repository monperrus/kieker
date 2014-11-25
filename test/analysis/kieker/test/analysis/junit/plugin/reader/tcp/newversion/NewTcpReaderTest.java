package kieker.test.analysis.junit.plugin.reader.tcp.newversion;

import static kieker.test.analysis.util.AssertHelper.assertInstanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.tcp.newversion.NewTcpReader;
import kieker.analysis.plugin.reader.tcp.newversion.ServerSocketChannelFactory;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.common.util.RecordSerializer;
import kieker.common.util.registry.IMonitoringRecordReceiver;
import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;

import kieker.test.common.junit.AbstractKiekerTest;

public class NewTcpReaderTest extends AbstractKiekerTest implements IMonitoringRecordReceiver {

	final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	private RecordSerializer recordSerializer;

	@Before
	public void before() {
		final IRegistry<String> stringRegistry = new Registry<String>();
		stringRegistry.setRecordReceiver(this);

		this.recordSerializer = new RecordSerializer(stringRegistry);
	}

	@Override
	public boolean newMonitoringRecord(final IMonitoringRecord record) {
		this.recordSerializer.serialize(record, this.buffer);
		return true;
	}

	@Test
	public void testDeserialization() throws Exception {
		// build test data ----------------------------------------------------------------
		final long timestamp = 0;
		final long traceId = 1;
		final int orderIndex = 2;
		final String operationSignature = "test()";
		final String classSignature = "a.b.c";
		final BeforeOperationEvent beforeOperationEvent = new BeforeOperationEvent(timestamp, traceId, orderIndex, operationSignature, classSignature);

		this.recordSerializer.serialize(beforeOperationEvent, this.buffer);
		this.buffer.flip(); // prepare for read

		// build analysis -----------------------------------------------------------------
		final AnalysisController analysisController = new AnalysisController();

		// final ServerSocketChannel mockedServerSocketChannel = mock(ServerSocketChannel.class); // does not work due to final method 'close()'
		final ServerSocketChannel mockedServerSocketChannel = spy(new EmptyServerSocketChannelImpl());
		doReturn(mock(ServerSocket.class)).
				when(mockedServerSocketChannel).socket();
		doReturn(new MockedSocketChannel(NewTcpReaderTest.this.buffer)).
				when(mockedServerSocketChannel).accept();

		final ServerSocketChannelFactory mockedServerSocketChannelFactory = mock(ServerSocketChannelFactory.class);
		when(mockedServerSocketChannelFactory.openServerSocket()).
				thenReturn(mockedServerSocketChannel);

		final Configuration configuration0 = new Configuration();
		final NewTcpReader tcpReader = new NewTcpReader(configuration0, analysisController, mockedServerSocketChannelFactory);

		final Configuration configuration1 = new Configuration();
		final ListCollectionFilter<IMonitoringRecord> collectionFilter = new ListCollectionFilter<IMonitoringRecord>(configuration1, analysisController);

		analysisController.connect(tcpReader, kieker.analysis.plugin.reader.tcp.newversion.NewTcpReader.OUTPUT_PORT_NAME_RECORDS, collectionFilter,
				ListCollectionFilter.INPUT_PORT_NAME);

		// run test -----------------------------------------------------------------------
		analysisController.run();

		// assert -------------------------------------------------------------------------
		final List<IMonitoringRecord> records = collectionFilter.getList();

		final BeforeOperationEvent record = assertInstanceOf(BeforeOperationEvent.class, records.get(0));
		assertEquals(timestamp, record.getTimestamp());
		assertEquals(traceId, record.getTraceId());
		assertEquals(orderIndex, record.getOrderIndex());
		assertEquals(operationSignature, record.getOperationSignature());
		assertEquals(classSignature, record.getClassSignature());

		assertEquals(1, records.size());
	}
}
