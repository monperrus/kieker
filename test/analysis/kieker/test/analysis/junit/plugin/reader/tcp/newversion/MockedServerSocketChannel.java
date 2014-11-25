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

package kieker.test.analysis.junit.plugin.reader.tcp.newversion;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class MockedServerSocketChannel extends EmptyServerSocketChannelImpl {

	final ByteBuffer buffer;

	public MockedServerSocketChannel(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public ServerSocket socket() {
		return mock(ServerSocket.class);
	}

	@Override
	public SocketChannel accept() throws IOException {
		// final SocketChannel socketChannel = mock(SocketChannel.class); // does not work due to final method 'close()'
		return new MockedSocketChannel(this.buffer);
	}

	// inherit final close()

}
