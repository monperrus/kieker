/***************************************************************************
 * Copyright 2013 Kieker Project (http://kieker-monitoring.net)
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

package kieker.test.monitoring.manualInstrumentation.helloWorld;

import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Andre van Hoorn
 * 
 * @since 0.91
 */
public final class HelloWorld {

	private HelloWorld() {}

	/**
	 * This main method starts the example.
	 * 
	 * @param args
	 *            The command line arguments. They have no effect.
	 */
	public static void main(final String[] args) {
		System.out.println("Hello"); // NOPMD (System.out)

		final IMonitoringController monitoringController = MonitoringController.getInstance();
		final ITimeSource timeSource = monitoringController.getTimeSource();

		// recording of the start time of doSomething
		final long startTime = timeSource.getTime();
		HelloWorld.doSomething();
		final long endTime = timeSource.getTime();
		monitoringController.newMonitoringRecord(HelloWorld.createOperationExecutionRecord("kieker.component.method()", 1, startTime, endTime));
	}

	private static void doSomething() {
		System.out.println("doing something"); // NOPMD (System.out)
		// .. some application logic does something meaningful ..
	}

	private static OperationExecutionRecord createOperationExecutionRecord(final String opString, final long traceId, final long tin, final long tout) {
		return new OperationExecutionRecord(opString, OperationExecutionRecord.NO_SESSION_ID, traceId, tin, tout, OperationExecutionRecord.NO_HOSTNAME,
				OperationExecutionRecord.NO_EOI_ESS, OperationExecutionRecord.NO_EOI_ESS);
	}
}