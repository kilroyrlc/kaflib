package kaflib.graphics;

/**
 * Defines and abstract superclass for image hashing functions.  Since there 
 * are a number of approaches for equivalence (e.g. mirror/rotate) and
 * heuristics, this is fairly minimal.
 */
public abstract class ImageHash {

	protected abstract String getSerial();
	
}
