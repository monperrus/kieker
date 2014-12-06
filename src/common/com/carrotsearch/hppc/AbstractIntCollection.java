package com.carrotsearch.hppc;

import java.util.Arrays;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.predicates.IntPredicate;

/**
 * Common superclass for collections.
 */
@javax.annotation.Generated(date = "2014-12-06T10:00:22+0100", value = "HPPC generated from: AbstractIntCollection.java")
abstract class AbstractIntCollection implements IntCollection
{
	/**
	 * Default implementation uses a predicate for removal.
	 */
	/*  */
	@Override
	public int removeAll(final IntLookupContainer c)
	{
		// We know c holds sub-types of int and we're not modifying c, so go unchecked.
		final IntContainer c2 = c;
		return this.removeAll(new IntPredicate()
		{
			@Override
			public boolean apply(final int k)
			{
				return c2.contains(k);
			}
		});
	}

	/**
	 * Default implementation uses a predicate for retaining.
	 */
	/*  */
	@Override
	public int retainAll(final IntLookupContainer c)
	{
		// We know c holds sub-types of int and we're not modifying c, so go unchecked.
		final IntContainer c2 = c;
		return this.removeAll(new IntPredicate()
		{
			@Override
			public boolean apply(final int k)
			{
				return !c2.contains(k);
			}
		});
	}

	/**
	 * Default implementation redirects to {@link #removeAll(IntPredicate)} and negates the predicate.
	 */
	@Override
	public int retainAll(final IntPredicate predicate)
	{
		return this.removeAll(new IntPredicate()
		{
			@Override
			public boolean apply(final int value)
			{
				return !predicate.apply(value);
			}
		});
	}

	/**
	 * Default implementation of copying to an array.
	 */
	@Override
	public int[] toArray()

	{
		final int size = this.size();
		final int[] array =
				new int[size];

		int i = 0;
		for (final IntCursor c : this)
		{
			array[i++] = c.value;
		}
		return array;
	}

	/*  */

	/**
	 * Convert the contents of this container to a human-friendly string.
	 */
	@Override
	public String toString()
	{
		return Arrays.toString(this.toArray());
	}
}
