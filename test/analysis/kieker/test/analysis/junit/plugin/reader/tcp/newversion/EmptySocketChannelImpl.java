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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class EmptySocketChannelImpl extends SocketChannel {

	protected EmptySocketChannelImpl() {
		super(null);
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
	public SocketChannel bind(final SocketAddress local) throws IOException {
		return null;
	}

	@Override
	public <T> SocketChannel setOption(final SocketOption<T> name, final T value) throws IOException {
		return null;
	}

	@Override
	public SocketChannel shutdownInput() throws IOException {
		return null;
	}

	@Override
	public SocketChannel shutdownOutput() throws IOException {
		return null;
	}

	@Override
	public Socket socket() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean isConnectionPending() {
		return false;
	}

	@Override
	public boolean connect(final SocketAddress remote) throws IOException {
		return false;
	}

	@Override
	public boolean finishConnect() throws IOException {
		return false;
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return null;
	}

	@Override
	public int read(final ByteBuffer dst) throws IOException {
		return 0;
	}

	@Override
	public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
		return 0;
	}

	@Override
	public int write(final ByteBuffer src) throws IOException {
		return 0;
	}

	@Override
	public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
		return 0;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {}

	@Override
	protected void implConfigureBlocking(final boolean block) throws IOException {}

}
