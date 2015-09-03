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

import kieker.common.record.IMonitoringRecord;

/**
 * @author Christian Wulf
 *
 * @since 1.12
 */
public interface IRecordFactoryResolver {

	/**
	 * @param recordClassName
	 * @return a new instance of the record factory belonging to the given <code>recordClassName</code> or <code>null</code> if such a record factory could not be
	 *         found or instantiated
	 */
	public abstract IRecordFactory<? extends IMonitoringRecord> get(String recordClassName);

}
