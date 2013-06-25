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

package kieker.tools.bridge;

import java.lang.reflect.Constructor;

import kieker.common.record.IMonitoringRecord;

/**
 * 
 * 
 * @author Reiner Jung
 * @since 1.8
 */
public final class LookupEntity {
	// TODO: there should be no real speedup compared to getter/setter (or is here anything special at work?)

	/**
	 * List of parameter types for a given IMonitoringRecord.
	 */
	public final Class<?>[] parameterTypes; // NOCS (for speedup reasons these properties are public)

	/**
	 * Constructor for an IMonitoringRecord class.
	 */
	public final Constructor<? extends IMonitoringRecord> constructor; // NOCS (for speedup reasons these properties are public)

	/**
	 * Construct one new LookupEntry.
	 * 
	 * @param constructor
	 *            constructor for a IMonitoringRecord class
	 * @param parameterTypes
	 *            monitoring record property type list
	 */
	public LookupEntity(final Constructor<? extends IMonitoringRecord> constructor, final Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		this.constructor = constructor;
	}
}