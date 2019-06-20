package kaflib.graphics;

/**
 * Defines a generic filter type.
 */
public abstract class Filter {
	
	public abstract void applyInPlace(Canvas canvas) throws Exception;
	
	public abstract Canvas apply(final Canvas canvas) throws Exception;
}
