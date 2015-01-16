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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Assert;

import kieker.test.tools.junit.bridge.ConfigurationParameters;

/**
 * Client for the KDB adaptive monitoring tests
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */

public class TCPClientForKDB implements Runnable {

	private final int port;
	private final ConcurrentMap<String, Boolean> signatureCache = new ConcurrentHashMap<String, Boolean>();
	DataOutputStream outToClient;
	DataInputStream inFromServer;
	List<String> opList;

	public TCPClientForKDB(final int port) {
		this.port = port;
	}

	@Override
	public void run() {

		boolean connected = false;

		while (!connected) {
			try {
				final Socket connectionSocket = new Socket(ConfigurationParameters.HOSTNAME, this.port);
				connected = true;
				try {
					this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());
					this.inFromServer = new DataInputStream(connectionSocket.getInputStream());

					this.opList = new ArrayList<String>(3);
					this.opList.add(ConfigurationParameters.TEST_OPERATION_SIGNATURE);
					this.opList.add(ConfigurationParameters.TEST_OPERATION_SIGNATURE_2);
					this.opList.add(ConfigurationParameters.TEST_OPERATION_SIGNATURE_3);

					for (int i = 0; i < 3; i++) {
						final String opSignature = this.opList.get(i);
						if (this.isProbeActivated(this.opList.get(i))) {
							this.outToClient.writeInt(ConfigurationParameters.TEST_RECORD_ID);
							this.outToClient.writeInt(opSignature.length());
							this.outToClient.writeBytes(opSignature);
							this.outToClient.writeInt(ConfigurationParameters.TEST_SESSION_ID.length());
							this.outToClient.writeBytes(ConfigurationParameters.TEST_SESSION_ID);
							this.outToClient.writeLong(ConfigurationParameters.TEST_TRACE_ID);
							this.outToClient.writeLong(ConfigurationParameters.TEST_TIN);
							this.outToClient.writeLong(ConfigurationParameters.TEST_TOUT);
							this.outToClient.writeInt(ConfigurationParameters.TEST_HOSTNAME.length());
							this.outToClient.writeBytes(ConfigurationParameters.TEST_HOSTNAME);
							this.outToClient.writeInt(i); // send the record ID
							this.outToClient.writeInt(ConfigurationParameters.TEST_ESS);
						}
					}

					connectionSocket.close();

				} catch (final IOException e) {
					// exception catch required, as run cannot have any additional throws
					Assert.fail("Sending data to server failed: " + e.getMessage());
				} catch (final InterruptedException e) {
					// ignore
				}
			} catch (final UnknownHostException e) {
				Assert.fail("Unknown host " + e.getMessage());
			} catch (final IOException e) {
				// polling for the server
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e1) {
					// can be ignored
				}
			}

		}
	}

	public boolean isProbeActivated(final String signature) throws IOException, InterruptedException {

		final Boolean active = this.signatureCache.get(signature);

		if (null == active) {

			this.outToClient.writeInt(-ConfigurationParameters.TEST_RECORD_ID);
			this.outToClient.writeInt(signature.length());
			this.outToClient.writeBytes(signature);

			final int x = this.inFromServer.readInt();

			if (x == 1) {
				this.signatureCache.put(signature, true);
				return true;
			} else {
				this.signatureCache.put(signature, false);
				return false;
			}
		} else {
			return active;
		}
	}

}
