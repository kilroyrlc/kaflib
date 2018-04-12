package kaflib.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Farms the specified work set out to a number of threads matching the 
 * work set size.
 * @param <T>
 */
public class DistributedProcessor<T> {
	private final Set<Worker> workers;
	
	public DistributedProcessor(final DistributedTask<T> task,
								final Collection<T> work) throws Exception {
		workers = new HashSet<Worker>();
		for (final T t : work) {
			Worker worker = new Worker() {
				@Override
				protected void process() throws Exception {
					task.process(t);
				}
			};
			workers.add(worker);
		}
	}
	
	public void start() {
		for (Worker worker : workers) {
			worker.start();
		}
	}
	
	public boolean done() {
		return Worker.areDone(workers);
	}
	
	public void waitUntilFinished(final Long timeoutMS) throws Exception {
		Worker.waitUntilDone(workers, timeoutMS);
	}
}
