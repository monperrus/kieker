/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.common.record.factory;

import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;

/**
 * Represents a factory that always returns null. Effectively, this resolver causes the tcp reader to fall back to the legacy, reflection-based record
 * reconstruction by {@link AbstractMonitoringRecord}.
 * For testing purposes only.
 *
 * @author Christian Wulf
 *
 * @since 1.12
 */
public class LegacyRecordFactoryResolver implements IRecordFactoryResolver {

	@Override
	public IRecordFactory<? extends IMonitoringRecord> get(final String recordClassName) {
		return null;
	}

}
