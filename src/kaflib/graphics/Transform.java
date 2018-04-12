package kaflib.graphics;

import java.util.HashSet;
import java.util.Set;

import kaflib.utils.StringUtils;


public abstract class Transform {
	public enum Status {
		READY,
		PROCESSING,
		SUCCESS,
		FAILURE
	}
	
	protected Status status;
	protected final Set<String> messages;
	
	private final Canvas output;

	protected Transform(final Canvas output) throws Exception {
		this.output = output;
		messages = new HashSet<String>();
		status = Status.READY;

	}
	
	public synchronized void start() throws Exception {
		if (status != Status.READY) {
			throw new Exception("Cannot call start on status: " + status.name() + ".");
		}
		status = Status.PROCESSING;
	}

	public Status getStatus() {
		return status;
	}
	
	public String getMessages() throws Exception {
		return StringUtils.concatenate(messages, "\n", true);
	}
	
	public Canvas getResult() throws Exception {
		if (status != Status.SUCCESS) {
			throw new Exception("Exceptions occurred: " + getMessages());
		}
		return output;
	}
	
	protected Canvas getOutput() {
		return output;
	}
	
	public abstract Status waitUntilFinished(final Long timeoutMS) throws Exception;
	
}
