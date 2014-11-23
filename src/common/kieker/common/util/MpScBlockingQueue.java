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

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jctools.queues.QueueFactory;
import org.jctools.queues.spec.ConcurrentQueueSpec;
import org.jctools.queues.spec.Ordering;
import org.jctools.queues.spec.Preference;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public final class MpScBlockingQueue<E> implements BlockingQueue<E> {

	private final Queue<E> queue;
	private Object queueIsFullLock;
	private Object queueIsEmptyLock;
	private final int capacity;

	public MpScBlockingQueue() {
		this.capacity = 100;
		this.queue = QueueFactory.newQueue(new ConcurrentQueueSpec(0, 1, this.capacity, Ordering.FIFO, Preference.NONE));
	}

	@Override
	public boolean add(final E e) {
		return this.queue.add(e);
	}

	@Override
	public boolean offer(final E e) {
		return this.queue.offer(e);
	}

	@Override
	public int size() {
		return this.queue.size();
	}

	@Override
	public boolean isEmpty() {
		return this.queue.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		return this.queue.contains(o);
	}

	@Override
	public E remove() {
		return this.queue.remove();
	}

	@Override
	public E poll() {
		return this.queue.poll();
	}

	@Override
	public E element() {
		return this.queue.element();
	}

	@Override
	public Iterator<E> iterator() {
		return this.queue.iterator();
	}

	@Override
	public E peek() {
		return this.queue.peek();
	}

	@Override
	public Object[] toArray() {
		return this.queue.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return this.queue.toArray(a);
	}

	@Override
	public boolean remove(final Object o) {
		return this.queue.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.queue.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return this.queue.addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return this.queue.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return this.queue.retainAll(c);
	}

	@Override
	public void clear() {
		this.queue.clear();
	}

	@Override
	public boolean equals(final Object o) {
		return this.queue.equals(o);
	}

	@Override
	public int hashCode() {
		return this.queue.hashCode();
	}

	@Override
	public void put(final E e) throws InterruptedException {
		synchronized (this.queueIsFullLock) {
			while (!this.queue.offer(e)) {
				this.queueIsFullLock.wait();
			}
		}
	}

	@Override
	public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
		// synchronized (this.queueIsFullLock) {
		// while (!this.queue.offer(e)) {
		// this.queueIsFullLock.wait(unit.toMillis(timeout));
		// }
		// }
		throw new IllegalStateException("not yet implemented");
	}

	@Override
	public E take() throws InterruptedException {
		synchronized (this.queueIsEmptyLock) {
			final E element = this.queue.poll();
			while (null == element) {
				this.queueIsEmptyLock.wait();
			}
			return element;
		}
	}

	@Override
	public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
		// TODO implement poll with t/o
		throw new IllegalStateException("not yet implemented");
	}

	@Override
	public int remainingCapacity() {
		return this.capacity - this.queue.size();
	}

	@Override
	public int drainTo(final Collection<? super E> c) {
		throw new IllegalStateException("Operation not supported");
	}

	@Override
	public int drainTo(final Collection<? super E> c, final int maxElements) {
		throw new IllegalStateException("Operation not supported");
	}

}
