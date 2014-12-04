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

package kieker.analysis.configuration;

import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;
import kieker.common.util.registry.newversion.ILookup;
import kieker.common.util.registry.newversion.Lookup;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
class LookupRegistry<T> implements IRegistry<T>, ILookup<T> {

	private final Registry<T> registry = new Registry<T>();
	private final Lookup<T> lookup = new Lookup<T>();

	@Override
	public void add(final int uniqueId, final T element) {
		throw new IllegalStateException("Operation not supported by this implementation");
	}

	@Override
	public T get(final int uniqueId) {
		return this.lookup.get(uniqueId);
	}

	@Override
	public int addIfAbsent(final T element) {
		final int uniqueId = this.registry.addIfAbsent(element);
		this.lookup.add(uniqueId, element);
		return uniqueId;
	}

	@Override
	public int get(final T element) {
		return this.registry.get(element);
	}

	@Override
	public int getSize() {
		return this.registry.getSize();
	}

	@Override
	public boolean set(final T value, final int id) {
		return this.registry.set(value, id);
	}

}
