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
public final class IdRegistry<T> implements IIdRegistry<T> {

	private final Map<T, Long> identifiers = new HashMap<T, Long>();
	private long nextFreeIdentifier = 0;

	@Override
	public long get(final T element) {
		return this.identifiers.get(element);
	}

	@Override
	public void putIfAbsent(final T element) {
		if (!this.identifiers.containsKey(element)) {
			final long newIdentifier = this.nextFreeIdentifier++;
			this.identifiers.put(element, newIdentifier);
		}
	}

}
