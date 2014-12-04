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

package kieker.common.util.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public final class Registry<T> implements IRegistry<T> {

	// TODO use a HPC implementation with primitive int values
	// I recommend: http://labs.carrotsearch.com/hppc.html
	private final Map<T, Integer> registeredEntries;
	private int nextIdentifier;

	// TODO remove if migration has been completed
	private final ILookup<T> lookup = new Lookup<T>();

	public Registry() {
		this.registeredEntries = new HashMap<T, Integer>();
	}

	@Override
	public int addIfAbsent(final T element) {
		Integer uniqueId = this.registeredEntries.get(element);
		if (null == uniqueId) {
			uniqueId = this.nextIdentifier++;
			this.registeredEntries.put(element, uniqueId);
		}
		return uniqueId;
	}

	@Override
	public final int get(final T element) {
		return this.registeredEntries.get(element);
	}

	@Override
	public int getSize() {
		return this.registeredEntries.size();
	}

	@Override
	public boolean set(final T value, final int id) {
		return this.lookup.set(value, id);
	}

	@Override
	public T get(final int i) {
		return this.lookup.get(i);
	}

}
