package kaflib.types;

public interface DistributedTask<T> {
	public void process(T data);
}
