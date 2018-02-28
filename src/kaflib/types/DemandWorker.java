package kaflib.types;

/**
 * Defines a thread that calls subtype's process() function in a loop until
 * a threshold is met then finishes, but can be restarted on demand.
 * E.g. you have a gui thread that kicks into gear based on user input, but
 * then stops looping after a bit.
 */
public abstract class DemandWorker {

	private Worker thread;
	private final long interval_ms;
	private final long timeout;
	private int loops_left;

	public DemandWorker(final long intervalMS, final long timeout) throws Exception {
		this(intervalMS, timeout, false);
	}

	public DemandWorker(final long intervalMS, final long timeout, final boolean kick) throws Exception {
		thread = null;
		this.interval_ms = intervalMS;
		this.timeout = timeout;
		if (kick) {
			kick();
		}
	}

	/**
	 * Called every interval after a kick until timeout reached.
	 */
	public abstract void processLoop();
	
	private int getLoopsLeft() throws Exception {
		long left = timeout / interval_ms;
		if (left > Integer.MAX_VALUE) {
			throw new Exception("Way too many loops left: " + left + ".");
		}
		return (int) left;
	}
	
	/**
	 * Called by public kick if it looks like the thread finished.
	 * Synchronizing on this one so the public one can be called without
	 * so as much performance penalty.
	 * @throws Exception
	 */
	private synchronized void restartThread() throws Exception {
		if (thread == null || thread.isDone()) {
			thread = new Worker() {
				@Override
				protected void process() throws Exception {
					loops_left = getLoopsLeft();
					while (loops_left > 0) {
						loops_left--;
						processLoop();
						Thread.sleep(interval_ms);
					}
				}
			};
			thread.start();					
		}
	}
	
	/**
	 * Kicks off the thread or, if it's already going, resets the time left to
	 * loop.
	 * @throws Exception
	 */
	public void kick() throws Exception {
		// Thread already going, reset loops left.
		if (thread == null || thread.isDone()) {
			restartThread();
		}
		else {
			loops_left = getLoopsLeft();
			return;
		}
	}
	
}
