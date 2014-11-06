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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import kieker.common.logging.Log;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class TcpServer {

	private final ServerSocketChannelFactory serverSocketChannelFactory;
	private final int port;
	private final int messageBufferSize;
	private final BufferListener listener;
	private final Log log;

	private volatile boolean terminated;
	private Thread readerThread;

	public TcpServer(final ServerSocketChannelFactory serverSocketChannelFactory, final int port, final int messageBufferSize,
			final BufferListener listener, final Log log) {
		super();
		this.serverSocketChannelFactory = serverSocketChannelFactory;
		this.port = port;
		this.messageBufferSize = messageBufferSize;
		this.listener = listener;
		this.log = log;
	}

	public boolean start() {
		this.readerThread = Thread.currentThread();

		ServerSocketChannel serversocket = null;
		try {
			serversocket = this.serverSocketChannelFactory.openServerSocket();
			serversocket.socket().bind(new InetSocketAddress(this.port));
			if (this.log.isDebugEnabled()) {
				this.log.debug("Listening on port " + this.port);
			}
			// BEGIN also loop this one?
			final SocketChannel socketChannel = serversocket.accept();
			final ByteBuffer buffer = ByteBuffer.allocateDirect(this.messageBufferSize);
			while ((socketChannel.read(buffer) != -1) && (!this.terminated)) {
				buffer.flip();
				// System.out.println("Reading, remaining:" + buffer.remaining());
				try {
					while (buffer.hasRemaining()) {
						buffer.mark();
						this.listener.read(buffer);
					}
					buffer.clear();
				} catch (final BufferUnderflowException ex) {
					buffer.reset();
					// System.out.println("Underflow, remaining:" + buffer.remaining());
					buffer.compact();
				}
			}
			// System.out.println("Channel closing...");
			socketChannel.close();
			// END also loop this one?
		} catch (final ClosedByInterruptException ex) {
			this.log.warn("Reader interrupted", ex);
			return this.terminated;
		} catch (final IOException ex) {
			this.log.error("Error while reading", ex);
			return false;
		} finally {
			if (null != serversocket) {
				try {
					serversocket.close();
				} catch (final IOException e) {
					if (this.log.isDebugEnabled()) {
						this.log.debug("Failed to close TCP connection!", e);
					}
				}
			}
		}
		return true;
	}

	public void terminate() {
		this.terminated = true;
		if (null != this.readerThread) {
			this.readerThread.interrupt();
		}
	}

	public int getPort() {
		return this.port;
	}
}
