package kieker.monitoring.core.registry;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andre van Hoorn, Jan Waller
 */
public final class ControlFlowRegistry {
	private static final Log log = LogFactory.getLog(ControlFlowRegistry.class);
	
	private final AtomicLong lastThreadId;
	private final ThreadLocal<Long> threadLocalTraceId = new ThreadLocal<Long>();
	private final ThreadLocal<Integer> threadLocalEoi = new ThreadLocal<Integer>();
	private final ThreadLocal<Integer> threadLocalEss = new ThreadLocal<Integer>();
	
	/**
	 * @return the singleton instance of ControlFlowRegistry
	 */
	public final static ControlFlowRegistry getInstance() {
		return LazyHolder.INSTANCE;
	}
	
	private ControlFlowRegistry() {
		/*
		 * In order to (probabilistically!) avoid that other instances in our
		 * system (on another node, in another vm, ...) generate the same thread
		 * ids, we fill the left-most 16 bits of the thread id with a uniquely
		 * distributed random number (0,0000152587890625 = 0,00152587890625 %).
		 * As a consequence, this constitutes a uniquely distributed offset of
		 * size 2^(64-1-16) = 2^47 = 140737488355328L in the worst case.
		 * Note that we restrict ourselves to the positive long values so far.
		 * Of course, negative values may occur (as a result of an overflow) --
		 * this does not hurt!
		 */
		final Random r = new Random();
		final long base = ((long) r.nextInt(65536) << (Long.SIZE - 16 - 1));
		/*
		 * can be removed if considered stable
		 * log.info("base 0:" + ((long)0 << (Long.SIZE-16-1)));
		 * log.info("base 65535:" + ((long)65535 << (Long.SIZE-16-1)));
		 * log.info("base 65534:" + ((long)65534 << (Long.SIZE-16-1)));
		 * log.info("base 65534+1:" + ((long)65534+1 << (Long.SIZE-16-1)));
		 * log.info("base 65534+2:" + ((long)65534+2 << (Long.SIZE-16-1)));
		 * log.info("base r:" + ((long)r.nextInt(65536) << (Long.SIZE-16-1)));
		 * log.info("overflow?: " + (((long)65535 << (Long.SIZE-16-1))+ 140737488355328L));
		 * log.info("Long.SIZE: " + Long.SIZE);
		 * log.info("2^47="+Math.pow(2, 47));
		 * log.info("Long.MAX_VALUE: " + Long.MAX_VALUE);
		 * log.info("Long.MIN_VALUE: " + Long.MIN_VALUE);
		 * log.info("Long.MAX_VALUE+1: " + (Long.MAX_VALUE+1));
		 */
		lastThreadId = new AtomicLong(base);
		log.info("First threadId will be " + (base + 1));
	}
	
	/**
	 * This methods returns a globally unique trace id.
	 * 
	 * @return a globally unique trace id.
	 */
	public final long getUniqueTraceId() {
		long id = lastThreadId.incrementAndGet();
		// Since we use -1 as a marker for an invalid traceId,
		// it must not be returned!
		return (id == -1) ? lastThreadId.incrementAndGet() : id;
	}
	
	/**
	 * This method returns a thread-local traceid which is globally
	 * unique and stored it local for the thread.
	 * The thread is responsible for invalidating the stored curTraceId using
	 * the method unsetThreadLocalTraceId()!
	 */
	public final long getAndStoreUniqueThreadLocalTraceId() {
		long id = this.getUniqueTraceId();
		this.threadLocalTraceId.set(id);
		return id;
	}
	
	/**
	 * This method stores a thread-local curTraceId.
	 * The thread is responsible for invalidating the stored curTraceId using
	 * the method unsetThreadLocalTraceId()!
	 */
	public final void storeThreadLocalTraceId(long traceId) {
		this.threadLocalTraceId.set(traceId);
	}
	
	/**
	 * This method returns the thread-local traceid previously
	 * registered using the method registerTraceId(curTraceId).
	 * 
	 * @return the traceid. -1 if no curTraceId has been registered
	 *         for this thread.
	 */
	public final long recallThreadLocalTraceId() {
		Long traceIdObj = this.threadLocalTraceId.get();
		if (traceIdObj == null) {
			return -1;
		}
		return traceIdObj;
	}
	
	/**
	 * This method unsets a previously registered traceid.
	 */
	public final void unsetThreadLocalTraceId() {
		this.threadLocalTraceId.remove();
	}

	/**
	 * Used to explicitly register an curEoi.
	 * The thread is responsible for invalidating the stored curTraceId using
	 * the method unsetThreadLocalEOI()!
	 */

	public final void storeThreadLocalEOI(int eoi) {
		// log.info(Thread.currentThread().getId());
		this.threadLocalEoi.set(eoi);
	}

	/**
	 * Since this method accesses a ThreadLocal variable,
	 * it is not (necessary to be) thread-safe.
	 */

	public final int incrementAndRecallThreadLocalEOI() {
		// log.info(Thread.currentThread().getId());
		Integer curEoi = this.threadLocalEoi.get();
		if (curEoi == null) {
			log.fatal("eoi has not been registered before");
			return -1;
		}
		int newEoi = curEoi + 1;
		this.threadLocalEoi.set(newEoi);
		return newEoi;
	}

	/**
	 * This method returns the thread-local curEoi previously
	 * registered using the method registerTraceId(curTraceId).
	 * 
	 * @return the sessionid. -1 if no curEoi registered.
	 */

	public final int recallThreadLocalEOI() {
		Integer curEoi = this.threadLocalEoi.get();
		if (curEoi == null) {
			log.fatal("eoi has not been registered before");
			return -1;
		}
		return curEoi;
	}

	/**
	 * This method unsets a previously registered traceid.
	 */

	public final void unsetThreadLocalEOI() {
		this.threadLocalEoi.remove();
	}

	/**
	 * Used to explicitly register a sessionid that is to be collected within
	 * a servlet method (that knows the request object).
	 * The thread is responsible for invalidating the stored curTraceId using
	 * the method unsetThreadLocalSessionId()!
	 */

	public final void storeThreadLocalESS(int ess) {
		this.threadLocalEss.set(ess);
	}

	/**
	 * Since this method accesses a ThreadLocal variable,
	 * it is not (necessary to be) thread-safe.
	 */

	public final int recallAndIncrementThreadLocalESS() {
		Integer curEss = this.threadLocalEss.get();
		if (curEss == null) {
			log.fatal("ess has not been registered before");
			return -1;
		}
		this.threadLocalEss.set(curEss + 1);
		return curEss;
	}

	/**
	 * This method returns the thread-local curEss previously
	 * registered using the method registerTraceId(curTraceId).
	 * 
	 * @return the sessionid. -1 if no curEss registered.
	 */

	public final int recallThreadLocalESS() {
		Integer ess = this.threadLocalEss.get();
		if (ess == null) {
			log.fatal("ess has not been registered before");
			return -1;
		}
		return ess;
	}

	/**
	 * This method unsets a previously registered curEss.
	 */

	public final void unsetThreadLocalESS() {
		this.threadLocalEss.remove();
	}

	/**
	 * SINGLETON
	 */
	private final static class LazyHolder {
		private static final ControlFlowRegistry INSTANCE = new ControlFlowRegistry();
	}
}
