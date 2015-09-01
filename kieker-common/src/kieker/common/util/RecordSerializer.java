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

package kieker.common.util;

import java.nio.ByteBuffer;

import kieker.common.record.IMonitoringRecord;
import kieker.common.record.misc.RegistryRecord;
import kieker.common.util.registry.ILookup;

/**
 * @author Christian Wulf
 *
 * @since 1.12
 */
public final class RecordSerializer {

	private final ILookup<String> stringRegistry;

	public RecordSerializer(final ILookup<String> stringRegistry) {
		super();
		this.stringRegistry = stringRegistry;
		// TODO not possible so far, since id must be non-negative
		// this.stringRegistry.set(RegistryRecord.class.getName(), RegistryRecord.CLASS_ID);
	}

	public final void serialize(final IMonitoringRecord record, final ByteBuffer buffer) {
		record.registerStrings(this.stringRegistry);

		final int recordClassId;
		if (record instanceof RegistryRecord) {
			recordClassId = RegistryRecord.CLASS_ID;
		} else {
			recordClassId = this.stringRegistry.get(record.getClass().getName());
		}

		buffer.putInt(recordClassId);
		buffer.putLong(record.getLoggingTimestamp());
		record.writeBytes(buffer, this.stringRegistry);
	}
}
