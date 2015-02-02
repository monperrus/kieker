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
package kieker.test.tools.junit.bridge.adaptiveMonitoring;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import kieker.tools.bridge.connector.ConnectorDataTransmissionException;

import kieker.test.tools.junit.bridge.ConfigurationParameters;

/**
 * Client for the KDB adaptive monitoring Update tests
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */

public class TCPUpdateClientForKDB implements Runnable {

	private final int port;
	DataOutputStream outToClient;
	DataInputStream inFromServer;
	List<String> opList;
	TCPClientForKDB client;

	private static final int BUF_LEN = 65536;
	private final byte[] buffer = new byte[BUF_LEN];

	public TCPUpdateClientForKDB(final int port, final TCPClientForKDB client) {
		this.port = port;
		this.client = client;
	}

	@Override
	public void run() {

		try {
			this.initialize();
		} catch (final ConnectorDataTransmissionException e1) {
			// exception
		}

		while (true) { // still needs a proper connection close mechanism
			try {
				final int bufLen = this.inFromServer.readInt();
				this.inFromServer.readFully(this.buffer, 0, bufLen);
				final String signature = new String(this.buffer, 0, bufLen, "UTF-8");
				final int activated = this.inFromServer.readInt();

				this.client.updateSignatureCache(signature, activated);
			} catch (final UnknownHostException e) {
				// Exception
			} catch (final IOException e) {
				// Exception
			}
		}
	}

	public void initialize() throws ConnectorDataTransmissionException {
		try {
			Socket connectionSocket;
			connectionSocket = new Socket(ConfigurationParameters.HOSTNAME, this.port);
			this.inFromServer = new DataInputStream(connectionSocket.getInputStream());
		} catch (final IOException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}

	}
}
