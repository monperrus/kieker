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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class EmptyServerSocketChannelImpl extends ServerSocketChannel {

	protected EmptyServerSocketChannelImpl() {
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
	public ServerSocketChannel bind(final SocketAddress local, final int backlog) throws IOException {
		return null;
	}

	@Override
	public <T> ServerSocketChannel setOption(final SocketOption<T> name, final T value) throws IOException {
		return null;
	}

	@Override
	public ServerSocket socket() {
		return null;
	}

	@Override
	public SocketChannel accept() throws IOException {
		return null;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {}

	@Override
	protected void implConfigureBlocking(final boolean block) throws IOException {}

}
