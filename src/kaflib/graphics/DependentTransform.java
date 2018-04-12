package kaflib.graphics;

import kaflib.types.Worker;

public abstract class DependentTransform extends Transform {

	private final Worker worker;
	
	protected DependentTransform(Canvas input) throws Exception {
		super(new Canvas(input));
		worker = new Worker() {

			@Override
			protected void process() throws Exception {
				try {
					processImage();
				}
				catch (Exception e) {
					messages.add(e.getMessage());
					e.printStackTrace();
					status = Status.FAILURE;
				}
			}
			
		};
	}

	public synchronized void start() throws Exception {
		super.start();
		worker.start();
	}
	
	protected abstract void processImage() throws Exception;
	
	@Override
	public Status waitUntilFinished(Long timeoutMS) throws Exception {
		worker.blockUntilDone(timeoutMS);
		if (status == Status.PROCESSING) {
			status = Status.SUCCESS;
		}
		return status;
	}

}
