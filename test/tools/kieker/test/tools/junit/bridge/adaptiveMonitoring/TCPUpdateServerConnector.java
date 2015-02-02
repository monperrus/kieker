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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import kieker.monitoring.core.controller.IMonitoringController;
import kieker.tools.bridge.connector.ConnectorDataTransmissionException;

/**
 * The Update-Server-Component of the KDB (currently as TCP) which is responsible for the Signature-Update-Mechanism in the adaptive monitoring context.
 * 
 * @author Micky Singh Multani
 * 
 * @since 1.11
 */
public class TCPUpdateServerConnector implements Runnable {

	private final IMonitoringController kiekerMonitoringController;
	private final int port;
	private ServerSocket serverSocket;
	private DataOutputStream out;

	TCPUpdateServerConnector(final IMonitoringController mc, final int port) {
		this.kiekerMonitoringController = mc;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			this.initialize();
		} catch (final ConnectorDataTransmissionException e1) {
			// exception
		}
		while (true) { // still needs a proper connection close mechanism
			synchronized (this.kiekerMonitoringController.getLockObject()) {
				try {
					// awakes, after update-signature-cache calculation in probe controller is done
					this.kiekerMonitoringController.getLockObject().wait();

					this.pushUpdate(this.kiekerMonitoringController.getSignatureToUpdate());

				} catch (final InterruptedException e) {
					// Exception
				}
			}
		}
	}

	public void initialize() throws ConnectorDataTransmissionException {
		try {
			this.serverSocket = new ServerSocket(this.port);
			final Socket socket = this.serverSocket.accept();
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (final IOException e) {
			throw new ConnectorDataTransmissionException(e.getMessage(), e);
		}

	}

	public void pushUpdate(final ConcurrentMap<String, Boolean> updateCache) {

		final Iterator<String> signOldCacheListIterator = updateCache.keySet().iterator();

		while (signOldCacheListIterator.hasNext()) {

			final String signature = signOldCacheListIterator.next();
			final int signActivated = (updateCache.get(signature) == true) ? 1 : 0;

			try {
				this.out.writeInt(signature.length());
				this.out.writeBytes(signature);
				this.out.writeInt(signActivated);
			} catch (final IOException e) {
				// Exception
			}

		}

	}

}
