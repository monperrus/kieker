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

package kieker.test.tools.junit.bridge;

import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.controller.JNBridgeMonitoringController;

/**
 * @author Rexhep Hamiti
 *
 * @since 1.11
 */
public class JNBClientforServer implements Runnable {

	private final JNBridgeMonitoringController controller;

	/**
	 * Constructor
	 */
	public JNBClientforServer() {
		this.controller = JNBridgeMonitoringController.getInstance();
	}

	/**
	 * The run method from Runnable
	 */
	@Override
	public void run() {

		for (int i = 0; i < ConfigurationParameters.SEND_NUMBER_OF_RECORDS; i++) {
			final OperationExecutionRecord record = new OperationExecutionRecord(ConfigurationParameters.TEST_OPERATION_SIGNATURE,
					ConfigurationParameters.TEST_SESSION_ID, ConfigurationParameters.TEST_TRACE_ID, ConfigurationParameters.TEST_TIN,
					ConfigurationParameters.TEST_TOUT, ConfigurationParameters.TEST_HOSTNAME, i, ConfigurationParameters.TEST_ESS);
			this.controller.newMonitoringRecord(record);
		}
	}
}
