package kieker.test.analysis.junit.plugin.reader.tcp.newversion;

import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import kieker.analysis.AnalysisController;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.tcp.newversion.NewTcpReader;
import kieker.analysis.plugin.reader.tcp.newversion.ServerSocketChannelFactory;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.common.util.RecordSerializer;
import kieker.common.util.registry.ILookup;
import kieker.common.util.registry.IMonitoringRecordReceiver;
import kieker.common.util.registry.Lookup;

import kieker.test.analysis.util.AssertHelper;
import kieker.test.common.junit.AbstractKiekerTest;

public class NewTcpReaderTest extends AbstractKiekerTest implements IMonitoringRecordReceiver {

	final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	private RecordSerializer recordSerializer;

	@Before
	public void before() {
		final ILookup<String> stringRegistry = new Lookup<String>();
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
		final ServerSocketChannel mockedServerSocketChannel = Mockito.spy(new EmptyServerSocketChannelImpl());
		Mockito.doReturn(Mockito.mock(ServerSocket.class)).
		when(mockedServerSocketChannel).socket();
		Mockito.doReturn(new MockedSocketChannel(NewTcpReaderTest.this.buffer)).
		when(mockedServerSocketChannel).accept();

		final ServerSocketChannelFactory mockedServerSocketChannelFactory = Mockito.mock(ServerSocketChannelFactory.class);
		Mockito.when(mockedServerSocketChannelFactory.openServerSocket()).
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

		final BeforeOperationEvent record = AssertHelper.assertInstanceOf(BeforeOperationEvent.class, records.get(0));
		Assert.assertEquals(timestamp, record.getTimestamp());
		Assert.assertEquals(traceId, record.getTraceId());
		Assert.assertEquals(orderIndex, record.getOrderIndex());
		Assert.assertEquals(operationSignature, record.getOperationSignature());
		Assert.assertEquals(classSignature, record.getClassSignature());

		Assert.assertEquals(1, records.size());
	}
}
