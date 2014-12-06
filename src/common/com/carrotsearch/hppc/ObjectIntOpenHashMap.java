package com.carrotsearch.hppc;

import static com.carrotsearch.hppc.HashContainerUtils.PERTURBATIONS;

import java.util.Arrays;
import java.util.Iterator;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.carrotsearch.hppc.hash.MurmurHash3;
import com.carrotsearch.hppc.predicates.IntPredicate;
import com.carrotsearch.hppc.predicates.ObjectPredicate;
import com.carrotsearch.hppc.procedures.IntProcedure;
import com.carrotsearch.hppc.procedures.ObjectIntProcedure;
import com.carrotsearch.hppc.procedures.ObjectProcedure;

/**
 * A hash map of <code>KType</code> to <code>int</code>, implemented using open
 * addressing with linear probing for collision resolution.
 *
 * <p>
 * The internal buffers of this implementation ({@link #keys}, {@link #values}, {@link #allocated}) are always allocated to the nearest size that is a power of two.
 * When the capacity exceeds the given load factor, the buffer size is doubled.
 * </p>
 *
 * <p>
 * See {@link ObjectObjectOpenHashMap} class for API similarities and differences against Java Collections.
 *
 * <p>
 * This implementation supports <code>null</code> keys.
 * </p>
 *
 * <p>
 * <b>Important node.</b> The implementation uses power-of-two tables and linear probing, which may cause poor performance (many collisions) if hash values are not
 * properly distributed. This implementation uses rehashing using {@link MurmurHash3}.
 * </p>
 *
 * @author This code is inspired by the collaboration and implementation in the <a
 *         href="http://fastutil.dsi.unimi.it/">fastutil</a> project.
 */
@javax.annotation.Generated(date = "2014-12-06T10:00:22+0100", value = "HPPC generated from: ObjectIntOpenHashMap.java")
public class ObjectIntOpenHashMap<KType>
		implements ObjectIntMap<KType>, Cloneable
{
	/**
	 * Minimum capacity for the map.
	 */
	public final static int MIN_CAPACITY = HashContainerUtils.MIN_CAPACITY;

	/**
	 * Default capacity.
	 */
	public final static int DEFAULT_CAPACITY = HashContainerUtils.DEFAULT_CAPACITY;

	/**
	 * Default load factor.
	 */
	public final static float DEFAULT_LOAD_FACTOR = HashContainerUtils.DEFAULT_LOAD_FACTOR;

	/**
	 * Hash-indexed array holding all keys.
	 *
	 * <p>
	 * <strong>Important!</strong> The actual value in this field is always an instance of <code>Object[]</code>. Be warned that <code>javac</code> emits additional
	 * casts when <code>keys</code> are directly accessed; <strong>these casts may result in exceptions at runtime</strong>. A workaround is to cast directly to
	 * <code>Object[]</code> before accessing the buffer's elements (although it is highly recommended to use a {@link #iterator()} instead. </pre>
	 *
	 * @see #values
	 */
	public KType[] keys;

	/**
	 * Hash-indexed array holding all values associated to the keys
	 * stored in {@link #keys}.
	 *
	 * <p>
	 * <strong>Important!</strong> The actual value in this field is always an instance of <code>Object[]</code>. Be warned that <code>javac</code> emits additional
	 * casts when <code>values</code> are directly accessed; <strong>these casts may result in exceptions at runtime</strong>. A workaround is to cast directly to
	 * <code>Object[]</code> before accessing the buffer's elements (although it is highly recommended to use a {@link #iterator()} instead. </pre>
	 *
	 * @see #keys
	 */
	public int[] values;

	/**
	 * Information if an entry (slot) in the {@link #values} table is allocated
	 * or empty.
	 *
	 * @see #assigned
	 */
	public boolean[] allocated;

	/**
	 * Cached number of assigned slots in {@link #allocated}.
	 */
	public int assigned;

	/**
	 * The load factor for this map (fraction of allocated slots
	 * before the buffers must be rehashed or reallocated).
	 */
	public final float loadFactor;

	/**
	 * Resize buffers when {@link #allocated} hits this value.
	 */
	protected int resizeAt;

	/**
	 * The most recent slot accessed in {@link #containsKey} (required for {@link #lget}).
	 *
	 * @see #containsKey
	 * @see #lget
	 */
	protected int lastSlot;

	/**
	 * We perturb hashed values with the array size to avoid problems with
	 * nearly-sorted-by-hash values on iterations.
	 *
	 * @see "http://issues.carrot2.org/browse/HPPC-80"
	 */
	protected int perturbation;

	/**
	 * Creates a hash map with the default capacity of {@value #DEFAULT_CAPACITY},
	 * load factor of {@value #DEFAULT_LOAD_FACTOR}.
	 *
	 * <p>
	 * See class notes about hash distribution importance.
	 * </p>
	 */
	public ObjectIntOpenHashMap()
	{
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Creates a hash map with the given initial capacity, default load factor of {@value #DEFAULT_LOAD_FACTOR}.
	 *
	 * <p>
	 * See class notes about hash distribution importance.
	 * </p>
	 *
	 * @param initialCapacity
	 *            Initial capacity (greater than zero and automatically
	 *            rounded to the next power of two).
	 */
	public ObjectIntOpenHashMap(final int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a hash map with the given initial capacity,
	 * load factor.
	 *
	 * <p>
	 * See class notes about hash distribution importance.
	 * </p>
	 *
	 * @param initialCapacity
	 *            Initial capacity (greater than zero and automatically
	 *            rounded to the next power of two).
	 *
	 * @param loadFactor
	 *            The load factor (greater than zero and smaller than 1).
	 */
	public ObjectIntOpenHashMap(int initialCapacity, final float loadFactor)
	{
		initialCapacity = Math.max(initialCapacity, MIN_CAPACITY);

		assert initialCapacity > 0 : "Initial capacity must be between (0, " + Integer.MAX_VALUE + "].";
		assert (loadFactor > 0) && (loadFactor <= 1) : "Load factor must be between (0, 1].";

		this.loadFactor = loadFactor;
		this.allocateBuffers(HashContainerUtils.roundCapacity(initialCapacity));
	}

	/**
	 * Create a hash map from all key-value pairs of another container.
	 */
	public ObjectIntOpenHashMap(final ObjectIntAssociativeContainer<KType> container)
	{
		this((int) (container.size() * (1 + DEFAULT_LOAD_FACTOR)));
		this.putAll(container);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int put(final KType key, final int value)
	{
		assert this.assigned < this.allocated.length;

		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				final int oldValue = this.values[slot];
				this.values[slot] = value;
				return oldValue;
			}

			slot = (slot + 1) & mask;
		}

		// Check if we need to grow. If so, reallocate new data, fill in the last element
		// and rehash.
		if (this.assigned == this.resizeAt) {
			this.expandAndPut(key, value, slot);
		} else {
			this.assigned++;
			this.allocated[slot] = true;
			this.keys[slot] = key;
			this.values[slot] = value;
		}
		return (0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int putAll(
			final ObjectIntAssociativeContainer<? extends KType> container)
	{
		final int count = this.assigned;
		for (final ObjectIntCursor<? extends KType> c : container)
		{
			this.put(c.key, c.value);
		}
		return this.assigned - count;
	}

	/**
	 * Puts all key/value pairs from a given iterable into this map.
	 */
	@Override
	public int putAll(
			final Iterable<? extends ObjectIntCursor<? extends KType>> iterable)
	{
		final int count = this.assigned;
		for (final ObjectIntCursor<? extends KType> c : iterable)
		{
			this.put(c.key, c.value);
		}
		return this.assigned - count;
	}

	/**
	 * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. An equivalent
	 * of the following code:
	 *
	 * <pre>
	 * if (!map.containsKey(key))
	 * 	map.put(value);
	 * </pre>
	 *
	 * <p>
	 * This method saves to {@link #lastSlot} as a side effect of each call.
	 * </p>
	 *
	 * @param key
	 *            The key of the value to check.
	 * @param value
	 *            The value to put if <code>key</code> does not exist.
	 * @return <code>true</code> if <code>key</code> did not exist and <code>value</code> was placed in the map.
	 */
	public boolean putIfAbsent(final KType key, final int value)
	{
		if (!this.containsKey(key))
		{
			this.put(key, value);
			return true;
		}
		return false;
	}

	/**
	 * <a href="http://trove4j.sourceforge.net">Trove</a>-inspired API method. A logical
	 * equivalent of the following code (but does not update {@link #lastSlot}):
	 *
	 * <pre>
	 * if (containsKey(key))
	 * {
	 * 	int v = (int) (lget() + additionValue);
	 * 	lset(v);
	 * 	return v;
	 * }
	 * else
	 * {
	 * 	put(key, putValue);
	 * 	return putValue;
	 * }
	 * </pre>
	 *
	 * @param key
	 *            The key of the value to adjust.
	 * @param putValue
	 *            The value to put if <code>key</code> does not exist.
	 * @param additionValue
	 *            The value to add to the existing value if <code>key</code> exists.
	 * @return Returns the current value associated with <code>key</code> (after changes).
	 */

	@Override
	public int putOrAdd(final KType key, final int putValue, final int additionValue)
	{
		assert this.assigned < this.allocated.length;

		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				return this.values[slot] = this.values[slot] + additionValue;
			}

			slot = (slot + 1) & mask;
		}

		if (this.assigned == this.resizeAt) {
			this.expandAndPut(key, putValue, slot);
		} else {
			this.assigned++;
			this.allocated[slot] = true;
			this.keys[slot] = key;
			this.values[slot] = putValue;
		}
		return putValue;
	}

	/**
	 * An equivalent of calling
	 *
	 * <pre>
	 * if (containsKey(key))
	 * {
	 * 	int v = (int) (lget() + additionValue);
	 * 	lset(v);
	 * 	return v;
	 * }
	 * else
	 * {
	 * 	put(key, additionValue);
	 * 	return additionValue;
	 * }
	 * </pre>
	 *
	 * @param key
	 *            The key of the value to adjust.
	 * @param additionValue
	 *            The value to put or add to the existing value if <code>key</code> exists.
	 * @return Returns the current value associated with <code>key</code> (after changes).
	 */

	@Override
	public int addTo(final KType key, final int additionValue)
	{
		return this.putOrAdd(key, additionValue, additionValue);
	}

	/**
	 * Expand the internal storage buffers (capacity) and rehash.
	 */
	private void expandAndPut(final KType pendingKey, final int pendingValue, final int freeSlot)
	{
		assert this.assigned == this.resizeAt;
		assert !this.allocated[freeSlot];

		// Try to allocate new buffers first. If we OOM, it'll be now without
		// leaving the data structure in an inconsistent state.
		final KType[] oldKeys = this.keys;
		final int[] oldValues = this.values;
		final boolean[] oldAllocated = this.allocated;

		this.allocateBuffers(HashContainerUtils.nextCapacity(this.keys.length));

		// We have succeeded at allocating new data so insert the pending key/value at
		// the free slot in the old arrays before rehashing.
		this.lastSlot = -1;
		this.assigned++;
		oldAllocated[freeSlot] = true;
		oldKeys[freeSlot] = pendingKey;
		oldValues[freeSlot] = pendingValue;

		// Rehash all stored keys into the new buffers.
		final KType[] keys = this.keys;
		final int[] values = this.values;
		final boolean[] allocated = this.allocated;
		final int mask = allocated.length - 1;
		for (int i = oldAllocated.length; --i >= 0;)
		{
			if (oldAllocated[i])
			{
				final KType k = oldKeys[i];
				final int v = oldValues[i];

				int slot = Internals.rehash(k, this.perturbation) & mask;
				while (allocated[slot])
				{
					slot = (slot + 1) & mask;
				}

				allocated[slot] = true;
				keys[slot] = k;
				values[slot] = v;
			}
		}

		/*  */Arrays.fill(oldKeys, null); /*  */
		/*  */
	}

	/**
	 * Allocate internal buffers for a given capacity.
	 *
	 * @param capacity
	 *            New capacity (must be a power of two).
	 */
	private void allocateBuffers(final int capacity)
	{
		final KType[] keys = Internals.<KType[]>newArray(capacity);
		final int[] values = new int[capacity];
		final boolean[] allocated = new boolean[capacity];

		this.keys = keys;
		this.values = values;
		this.allocated = allocated;

		this.resizeAt = Math.max(2, (int) Math.ceil(capacity * this.loadFactor)) - 1;
		this.perturbation = this.computePerturbationValue(capacity);
	}

	/**
	 * <p>
	 * Compute the key perturbation value applied before hashing. The returned value should be non-zero and ideally different for each capacity. This matters because
	 * keys are nearly-ordered by their hashed values so when adding one container's values to the other, the number of collisions can skyrocket into the worst case
	 * possible.
	 *
	 * <p>
	 * If it is known that hash containers will not be added to each other (will be used for counting only, for example) then some speed can be gained by not
	 * perturbing keys before hashing and returning a value of zero for all possible capacities. The speed gain is a result of faster rehash operation (keys are
	 * mostly in order).
	 */
	protected int computePerturbationValue(final int capacity)
	{
		return PERTURBATIONS[Integer.numberOfLeadingZeros(capacity)];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(final KType key)
	{
		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				this.assigned--;
				final int v = this.values[slot];
				this.shiftConflictingKeys(slot);
				return v;
			}
			slot = (slot + 1) & mask;
		}

		return (0);
	}

	/**
	 * Shift all the slot-conflicting keys allocated to (and including) <code>slot</code>.
	 */
	protected void shiftConflictingKeys(int slotCurr)
	{
		// Copied nearly verbatim from fastutil's impl.
		final int mask = this.allocated.length - 1;
		int slotPrev, slotOther;
		while (true)
		{
			slotCurr = ((slotPrev = slotCurr) + 1) & mask;

			while (this.allocated[slotCurr])
			{
				slotOther = Internals.rehash(this.keys[slotCurr], this.perturbation) & mask;
				if (slotPrev <= slotCurr)
				{
					// We are on the right of the original slot.
					if ((slotPrev >= slotOther) || (slotOther > slotCurr)) {
						break;
					}
				}
				else
				{
					// We have wrapped around.
					if ((slotPrev >= slotOther) && (slotOther > slotCurr)) {
						break;
					}
				}
				slotCurr = (slotCurr + 1) & mask;
			}

			if (!this.allocated[slotCurr]) {
				break;
			}

			// Shift key/value pair.
			this.keys[slotPrev] = this.keys[slotCurr];
			this.values[slotPrev] = this.values[slotCurr];
		}

		this.allocated[slotPrev] = false;

		/*  */
		this.keys[slotPrev] = null;
		/*  */
		/*  */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int removeAll(final ObjectContainer<? extends KType> container)
	{
		final int before = this.assigned;

		for (final ObjectCursor<? extends KType> cursor : container)
		{
			this.remove(cursor.value);
		}

		return before - this.assigned;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int removeAll(final ObjectPredicate<? super KType> predicate)
	{
		final int before = this.assigned;

		final KType[] keys = this.keys;
		final boolean[] states = this.allocated;

		for (int i = 0; i < states.length;)
		{
			if (states[i])
			{
				if (predicate.apply(keys[i]))
				{
					this.assigned--;
					this.shiftConflictingKeys(i);
					// Repeat the check for the same i.
					continue;
				}
			}
			i++;
		}
		return before - this.assigned;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Use the following snippet of code to check for key existence first and then retrieve the value if it exists.
	 * </p>
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	value = map.lget();
	 * </pre>
	 * <p>
	 * The above code <strong>cannot</strong> be used by multiple concurrent threads because a call to {@link #containsKey} stores the temporary slot number in
	 * {@link #lastSlot}. An alternative to the above conditional statement is to use {@link #getOrDefault} and provide a custom default value sentinel (not present
	 * in the value set).
	 * </p>
	 */
	@Override
	public int get(final KType key)
	{
		// Same as:
		// getOrDefault(key, ((int) 0))
		// but let's keep it duplicated for VMs that don't have advanced inlining.
		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				return this.values[slot];
			}

			slot = (slot + 1) & mask;
		}
		return (0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOrDefault(final KType key, final int defaultValue)
	{
		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				return this.values[slot];
			}

			slot = (slot + 1) & mask;
		}
		return defaultValue;
	}

	/*  */
	/**
	 * Returns the last key stored in this has map for the corresponding
	 * most recent call to {@link #containsKey}.
	 *
	 * <p>
	 * Use the following snippet of code to check for key existence first and then retrieve the key value if it exists.
	 * </p>
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	value = map.lkey();
	 * </pre>
	 *
	 * <p>
	 * This is equivalent to calling:
	 * </p>
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	key = map.keys[map.lslot()];
	 * </pre>
	 */
	public KType lkey()
	{
		return this.keys[this.lslot()];
	}

	/*  */

	/**
	 * Returns the last value saved in a call to {@link #containsKey}.
	 *
	 * @see #containsKey
	 */
	public int lget()
	{
		assert this.lastSlot >= 0 : "Call containsKey() first.";
		assert this.allocated[this.lastSlot] : "Last call to exists did not have any associated value.";

		return this.values[this.lastSlot];
	}

	/**
	 * Sets the value corresponding to the key saved in the last
	 * call to {@link #containsKey}, if and only if the key exists
	 * in the map already.
	 *
	 * @see #containsKey
	 * @return Returns the previous value stored under the given key.
	 */
	public int lset(final int key)
	{
		assert this.lastSlot >= 0 : "Call containsKey() first.";
		assert this.allocated[this.lastSlot] : "Last call to exists did not have any associated value.";

		final int previous = this.values[this.lastSlot];
		this.values[this.lastSlot] = key;
		return previous;
	}

	/**
	 * @return Returns the slot of the last key looked up in a call to {@link #containsKey} if
	 *         it returned <code>true</code>.
	 *
	 * @see #containsKey
	 */
	public int lslot()
	{
		assert this.lastSlot >= 0 : "Call containsKey() first.";
		return this.lastSlot;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Saves the associated value for fast access using {@link #lget} or {@link #lset}.
	 * </p>
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	value = map.lget();
	 * </pre>
	 *
	 * or, to modify the value at the given key without looking up
	 * its slot twice:
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	map.lset(map.lget() + 1);
	 * </pre>
	 *
	 * or, to retrieve the key-equivalent object from the map:
	 *
	 * <pre>
	 * if (map.containsKey(key))
	 * 	map.lkey();
	 * </pre>
	 *
	 * *
	 * *
	 * <p>
	 * <strong>Important:</strong> {@link #containsKey} and consecutive {@link #lget}, {@link #lset} or {@link #lkey} must not be used by concurrent threads because
	 * {@link #lastSlot} is used to store state.
	 * </p>
	 * */
	@Override
	public boolean containsKey(final KType key)
	{
		final int mask = this.allocated.length - 1;
		int slot = Internals.rehash(key, this.perturbation) & mask;
		while (this.allocated[slot])
		{
			if (((key) == null ? (this.keys[slot]) == null : (key).equals((this.keys[slot]))))
			{
				this.lastSlot = slot;
				return true;
			}
			slot = (slot + 1) & mask;
		}
		this.lastSlot = -1;
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Does not release internal buffers.
	 * </p>
	 */
	@Override
	public void clear()
	{
		this.assigned = 0;

		// States are always cleared.
		Arrays.fill(this.allocated, false);

		/*  */
		Arrays.fill(this.keys, null); // Help the GC.
		/*  */

		/*  */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size()
	{
		return this.assigned;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Note that an empty container may still contain many deleted keys (that occupy buffer space). Adding even a single element to such a container may cause
	 * rehashing.
	 * </p>
	 */
	@Override
	public boolean isEmpty()
	{
		return this.size() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		int h = 0;
		for (final ObjectIntCursor<KType> c : this)
		{
			h += Internals.rehash(c.key) + Internals.rehash(c.value);
		}
		return h;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj != null)
		{
			if (obj == this) {
				return true;
			}

			if (obj instanceof ObjectIntMap)
			{
				/*  */
				@SuppressWarnings("unchecked")
				final/*  */
				ObjectIntMap<KType> other = (ObjectIntMap<KType>) obj;
				if (other.size() == this.size())
				{
					for (final ObjectIntCursor<KType> c : this)
					{
						if (other.containsKey(c.key))
						{
							final int v = other.get(c.key);
							if (((c.value) == (v)))
							{
								continue;
							}
						}
						return false;
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * An iterator implementation for {@link #iterator}.
	 */
	private final class EntryIterator extends AbstractIterator<ObjectIntCursor<KType>>
	{
		private final ObjectIntCursor<KType> cursor;

		public EntryIterator()
		{
			this.cursor = new ObjectIntCursor<KType>();
			this.cursor.index = -1;
		}

		@Override
		protected ObjectIntCursor<KType> fetch()
		{
			int i = this.cursor.index + 1;
			final int max = ObjectIntOpenHashMap.this.keys.length;
			while ((i < max) && !ObjectIntOpenHashMap.this.allocated[i])
			{
				i++;
			}

			if (i == max) {
				return this.done();
			}

			this.cursor.index = i;
			this.cursor.key = ObjectIntOpenHashMap.this.keys[i];
			this.cursor.value = ObjectIntOpenHashMap.this.values[i];

			return this.cursor;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<ObjectIntCursor<KType>> iterator()
	{
		return new EntryIterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends ObjectIntProcedure<? super KType>> T forEach(final T procedure)
	{
		final KType[] keys = this.keys;
		final int[] values = this.values;
		final boolean[] states = this.allocated;

		for (int i = 0; i < states.length; i++)
		{
			if (states[i]) {
				procedure.apply(keys[i], values[i]);
			}
		}

		return procedure;
	}

	/**
	 * Returns a specialized view of the keys of this associated container.
	 * The view additionally implements {@link ObjectLookupContainer}.
	 */
	@Override
	public KeysContainer keys()
	{
		return new KeysContainer();
	}

	/**
	 * A view of the keys inside this hash map.
	 */
	public final class KeysContainer
			extends AbstractObjectCollection<KType> implements ObjectLookupContainer<KType>
	{
		private final ObjectIntOpenHashMap<KType> owner =
				ObjectIntOpenHashMap.this;

		@Override
		public boolean contains(final KType e)
		{
			return ObjectIntOpenHashMap.this.containsKey(e);
		}

		@Override
		public <T extends ObjectProcedure<? super KType>> T forEach(final T procedure)
		{
			final KType[] localKeys = this.owner.keys;
			final boolean[] localStates = this.owner.allocated;

			for (int i = 0; i < localStates.length; i++)
			{
				if (localStates[i]) {
					procedure.apply(localKeys[i]);
				}
			}

			return procedure;
		}

		@Override
		public <T extends ObjectPredicate<? super KType>> T forEach(final T predicate)
		{
			final KType[] localKeys = this.owner.keys;
			final boolean[] localStates = this.owner.allocated;

			for (int i = 0; i < localStates.length; i++)
			{
				if (localStates[i])
				{
					if (!predicate.apply(localKeys[i])) {
						break;
					}
				}
			}

			return predicate;
		}

		@Override
		public boolean isEmpty()
		{
			return this.owner.isEmpty();
		}

		@Override
		public Iterator<ObjectCursor<KType>> iterator()
		{
			return new KeysIterator();
		}

		@Override
		public int size()
		{
			return this.owner.size();
		}

		@Override
		public void clear()
		{
			this.owner.clear();
		}

		@Override
		public int removeAll(final ObjectPredicate<? super KType> predicate)
		{
			return this.owner.removeAll(predicate);
		}

		@Override
		public int removeAllOccurrences(final KType e)
		{
			final boolean hasKey = this.owner.containsKey(e);
			int result = 0;
			if (hasKey)
			{
				this.owner.remove(e);
				result = 1;
			}
			return result;
		}
	}

	/**
	 * An iterator over the set of assigned keys.
	 */
	private final class KeysIterator extends AbstractIterator<ObjectCursor<KType>>
	{
		private final ObjectCursor<KType> cursor;

		public KeysIterator()
		{
			this.cursor = new ObjectCursor<KType>();
			this.cursor.index = -1;
		}

		@Override
		protected ObjectCursor<KType> fetch()
		{
			int i = this.cursor.index + 1;
			final int max = ObjectIntOpenHashMap.this.keys.length;
			while ((i < max) && !ObjectIntOpenHashMap.this.allocated[i])
			{
				i++;
			}

			if (i == max) {
				return this.done();
			}

			this.cursor.index = i;
			this.cursor.value = ObjectIntOpenHashMap.this.keys[i];

			return this.cursor;
		}
	}

	/**
	 * @return Returns a container with all values stored in this map.
	 */
	@Override
	public IntContainer values()
	{
		return new ValuesContainer();
	}

	/**
	 * A view over the set of values of this map.
	 */
	private final class ValuesContainer extends AbstractIntCollection
	{
		public ValuesContainer() {}

		@Override
		public int size()
		{
			return ObjectIntOpenHashMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return ObjectIntOpenHashMap.this.isEmpty();
		}

		@Override
		public boolean contains(final int value)
		{
			// This is a linear scan over the values, but it's in the contract, so be it.
			final boolean[] allocated = ObjectIntOpenHashMap.this.allocated;
			final int[] values = ObjectIntOpenHashMap.this.values;

			for (int slot = 0; slot < allocated.length; slot++)
			{
				if (allocated[slot] && ((value) == (values[slot])))
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public <T extends IntProcedure> T forEach(final T procedure)
		{
			final boolean[] allocated = ObjectIntOpenHashMap.this.allocated;
			final int[] values = ObjectIntOpenHashMap.this.values;

			for (int i = 0; i < allocated.length; i++)
			{
				if (allocated[i]) {
					procedure.apply(values[i]);
				}
			}

			return procedure;
		}

		@Override
		public <T extends IntPredicate> T forEach(final T predicate)
		{
			final boolean[] allocated = ObjectIntOpenHashMap.this.allocated;
			final int[] values = ObjectIntOpenHashMap.this.values;

			for (int i = 0; i < allocated.length; i++)
			{
				if (allocated[i])
				{
					if (!predicate.apply(values[i])) {
						break;
					}
				}
			}

			return predicate;
		}

		@Override
		public Iterator<IntCursor> iterator()
		{
			return new ValuesIterator();
		}

		@Override
		public int removeAllOccurrences(final int e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int removeAll(final IntPredicate predicate)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * An iterator over the set of assigned values.
	 */
	private final class ValuesIterator extends AbstractIterator<IntCursor>
	{
		private final IntCursor cursor;

		public ValuesIterator()
		{
			this.cursor = new IntCursor();
			this.cursor.index = -1;
		}

		@Override
		protected IntCursor fetch()
		{
			int i = this.cursor.index + 1;
			final int max = ObjectIntOpenHashMap.this.keys.length;
			while ((i < max) && !ObjectIntOpenHashMap.this.allocated[i])
			{
				i++;
			}

			if (i == max) {
				return this.done();
			}

			this.cursor.index = i;
			this.cursor.value = ObjectIntOpenHashMap.this.values[i];

			return this.cursor;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectIntOpenHashMap<KType> clone()
	{
		try
		{
			/*  */
			@SuppressWarnings("unchecked")
			final/*  */
			ObjectIntOpenHashMap<KType> cloned =
					(ObjectIntOpenHashMap<KType>) super.clone();

			cloned.keys = this.keys.clone();
			cloned.values = this.values.clone();
			cloned.allocated = this.allocated.clone();

			return cloned;
		} catch (final CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert the contents of this map to a human-friendly string.
	 */
	@Override
	public String toString()
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append("[");

		boolean first = true;
		for (final ObjectIntCursor<KType> cursor : this)
		{
			if (!first) {
				buffer.append(", ");
			}
			buffer.append(cursor.key);
			buffer.append("=>");
			buffer.append(cursor.value);
			first = false;
		}
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * Creates a hash map from two index-aligned arrays of key-value pairs.
	 */
	public static <KType> ObjectIntOpenHashMap<KType> from(final KType[] keys, final int[] values)
	{
		if (keys.length != values.length) {
			throw new IllegalArgumentException("Arrays of keys and values must have an identical length.");
		}

		final ObjectIntOpenHashMap<KType> map = new ObjectIntOpenHashMap<KType>();
		for (int i = 0; i < keys.length; i++)
		{
			map.put(keys[i], values[i]);
		}
		return map;
	}

	/**
	 * Create a hash map from another associative container.
	 */
	public static <KType> ObjectIntOpenHashMap<KType> from(final ObjectIntAssociativeContainer<KType> container)
	{
		return new ObjectIntOpenHashMap<KType>(container);
	}

	/**
	 * Create a new hash map without providing the full generic signature (constructor
	 * shortcut).
	 */
	public static <KType> ObjectIntOpenHashMap<KType> newInstance()
	{
		return new ObjectIntOpenHashMap<KType>();
	}

	/**
	 * Returns a new object with no key perturbations (see {@link #computePerturbationValue(int)}). Only use when sure the container will not
	 * be used for direct copying of keys to another hash container.
	 */
	public static <KType> ObjectIntOpenHashMap<KType> newInstanceWithoutPerturbations()
	{
		return new ObjectIntOpenHashMap<KType>() {
			@Override
			protected int computePerturbationValue(final int capacity) {
				return 0;
			}
		};
	}

	/**
	 * Create a new hash map without providing the full generic signature (constructor
	 * shortcut).
	 */
	public static <KType> ObjectIntOpenHashMap<KType> newInstance(final int initialCapacity, final float loadFactor)
	{
		return new ObjectIntOpenHashMap<KType>(initialCapacity, loadFactor);
	}

	/**
	 * Create a new hash map without providing the full generic signature (constructor
	 * shortcut). The returned instance will have enough initial capacity to hold <code>expectedSize</code> elements without having to resize.
	 */
	public static <KType> ObjectIntOpenHashMap<KType> newInstanceWithExpectedSize(final int expectedSize)
	{
		return ObjectIntOpenHashMap.newInstanceWithExpectedSize(expectedSize, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Create a new hash map without providing the full generic signature (constructor
	 * shortcut). The returned instance will have enough initial capacity to hold <code>expectedSize</code> elements without having to resize.
	 */
	public static <KType> ObjectIntOpenHashMap<KType> newInstanceWithExpectedSize(final int expectedSize, final float loadFactor)
	{
		return ObjectIntOpenHashMap.newInstance((int) (expectedSize / loadFactor) + 1, loadFactor);
	}
}
