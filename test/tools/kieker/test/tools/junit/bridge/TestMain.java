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

import kieker.tools.bridge.connector.ConnectorDataTransmissionException;

/**
 * @author Rexhep Hamiti
 *
 * @since 1.11
 */
public class TestMain {

	public static void main(final String[] args) throws ConnectorDataTransmissionException {
		final TestJNBridgeConnector test = new TestJNBridgeConnector();
		test.toString();
		test.testJNBridgeConnector();

	}

}
