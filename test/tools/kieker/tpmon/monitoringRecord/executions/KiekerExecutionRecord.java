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

package kieker.tpmon.monitoringRecord.executions;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.common.util.registry.IRegistry;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class KiekerExecutionRecord extends OperationExecutionRecord {

	private static final long serialVersionUID = 3403328112784070360L;

	public KiekerExecutionRecord(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
	}

	public KiekerExecutionRecord(final Object[] values, final Class<?>[] valueTypes) {
		super(values, valueTypes);
	}

	public KiekerExecutionRecord(final Object[] values) {
		super(values);
	}

	public KiekerExecutionRecord(final String operationSignature, final String sessionId, final long traceId, final long tin, final long tout,
			final String hostname, final int eoi, final int ess) {
		super(operationSignature, sessionId, traceId, tin, tout, hostname, eoi, ess);
	}

}
