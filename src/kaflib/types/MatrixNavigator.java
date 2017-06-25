package kaflib.types;

import java.util.List;

/**
 * Provides read-only access to a matrix.  E.g. pass this to matrix cell types
 * to let them access neighbors but not change the matrix.
 * @param <T>
 */
public interface MatrixNavigator<T> {
	/**
	 * Returns the specified matrix element.
	 * @param row
	 * @param column
	 * @return
	 * @throws Exception
	 */
	public T get(final int row, final int column) throws Exception;
	
	/**
	 * Returns the specified neighbor, or null if at a boundary.
	 * @param me
	 * @param direction
	 * @return
	 * @throws Exception
	 */
	public T getNeighbor(final Coordinate me, 
						 final Direction.Cardinal direction) throws Exception;
	
	/**
	 * Returns a list of nonnull neighbors.
	 * @param me
	 * @return
	 * @throws Exception
	 */
	public List<T> getNeighbors(final Coordinate me, final boolean nsewOnly) throws Exception;
}
