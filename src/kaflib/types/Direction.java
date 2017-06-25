package kaflib.types;

/**
 * Provides compass directions.
 */
public class Direction {
	public enum Cardinal {
		NORTH,
		SOUTH,
		EAST,
		WEST,
		NORTHEAST,
		SOUTHEAST,
		SOUTHWEST,
		NORTHWEST
	}

	/**
	 * Returns an (x,y) coordinate offset for a grid that increases x to the
	 * right/east and y down/south.  E.g. north is (0, -1), southwest is 
	 * (-1, 1).
	 * @param cardinal
	 * @return
	 */
	public static Coordinate getOffset(Cardinal cardinal) throws Exception {
		switch (cardinal) {
		case NORTH:
			return new Coordinate(0, -1);
		case SOUTH:
			return new Coordinate(0, 1);
		case EAST:
			return new Coordinate(1, 0);
		case WEST:
			return new Coordinate(-1, 0);
		case NORTHEAST:
			return new Coordinate(1, -1);
		case SOUTHEAST:
			return new Coordinate(1, 1);
		case SOUTHWEST:
			return new Coordinate(-1, 1);
		case NORTHWEST:
			return new Coordinate(-1, -1);
		default:
			throw new Exception("Unsupported direction: " + cardinal + ".");
		}
	}
}
