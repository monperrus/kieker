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

import com.carrotsearch.hppc.ObjectIntMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * @param <T>
 *            the type of the elements
 *
 * @author Christian Wulf
 *
 * @since 1.11
 */
public final class Registry<T> implements IRegistry<T> {

	private static final int EMPTY_INDICATOR = Integer.MIN_VALUE;

	private final ObjectIntMap<T> registeredEntries;
	private int nextIdentifier;

	// TODO: remove if migration has been completed
	private final ILookup<T> lookup = new Lookup<T>();

	public Registry() {
		this.registeredEntries = new ObjectIntOpenHashMap<T>();
	}

	@Override
	public int addIfAbsent(final T element) {
		int uniqueId = this.registeredEntries.getOrDefault(element, EMPTY_INDICATOR);
		if (EMPTY_INDICATOR == uniqueId) {
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
