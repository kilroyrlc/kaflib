package kaflib.types;

import kaflib.utils.RandomUtils;

/**
 * Provides compass directions.
 */
public enum Direction {
		NORTH,
		SOUTH,
		EAST,
		WEST,
		NORTHEAST,
		SOUTHEAST,
		SOUTHWEST,
		NORTHWEST;
	

	/**
	 * Returns an (x,y) coordinate offset for a grid that increases x to the
	 * right/east and y down/south.  E.g. north is (0, -1), southwest is 
	 * (-1, 1).
	 * @param cardinal
	 * @return
	 */
	public static Coordinate getOffset(Direction direction) throws Exception {
		switch (direction) {
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
			throw new Exception("Unsupported direction: " + direction + ".");
		}
	}
	
	public static Direction getOpposite(Direction direction) throws Exception {
		switch (direction) {
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case EAST:
			return WEST;
		case WEST:
			return EAST;
		case NORTHEAST:
			return SOUTHWEST;
		case SOUTHEAST:
			return NORTHWEST;
		case SOUTHWEST:
			return NORTHEAST;
		case NORTHWEST:
			return SOUTHWEST;
		default:
			throw new Exception("Unsupported direction: " + direction + ".");
		}
	}
	
	/**
	 * Gets a random direction 45 off the current.
	 * @param current
	 * @return
	 */
	public static Direction getRandomFortyFive(final Direction current) throws Exception {
		boolean random = RandomUtils.randomBoolean();
		switch (current) {
		case NORTH:
			if (random) {
				return NORTHEAST;
			}
			else {
				return NORTHWEST;
			}
		case SOUTH:
			if (random) {
				return SOUTHEAST;
			}
			else {
				return SOUTHWEST;
			}
		case EAST:
			if (random) {
				return NORTHEAST;
			}
			else {
				return SOUTHEAST;
			}
		case WEST:
			if (random) {
				return NORTHWEST;
			}
			else {
				return SOUTHWEST;
			}
		case NORTHEAST:
			if (random) {
				return NORTH;
			}
			else {
				return EAST;
			}
		case SOUTHEAST:			
			if (random) {
				return SOUTH;
			}
			else {
				return EAST;
			}
		case SOUTHWEST:
			if (random) {
				return SOUTH;
			}
			else {
				return WEST;
			}
		case NORTHWEST:
			if (random) {
				return NORTH;
			}
			else {
				return WEST;
			}
		default:
			throw new Exception("Unsupported direction: " + current + ".");
		}
	}
	
	public static Direction getRandomOrthogonal(final Direction current) throws Exception {
		boolean random = RandomUtils.randomBoolean();
		switch (current) {
		case NORTH:
			if (random) {
				return EAST;
			}
			else {
				return WEST;
			}
		case SOUTH:
			if (random) {
				return EAST;
			}
			else {
				return WEST;
			}
		case EAST:
			if (random) {
				return NORTH;
			}
			else {
				return SOUTH;
			}
		case WEST:
			if (random) {
				return NORTH;
			}
			else {
				return SOUTH;
			}
		case NORTHEAST:
			if (random) {
				return NORTHWEST;
			}
			else {
				return SOUTHEAST;
			}
		case SOUTHEAST:			
			if (random) {
				return NORTHEAST;
			}
			else {
				return SOUTHWEST;
			}
		case SOUTHWEST:
			if (random) {
				return SOUTHEAST;
			}
			else {
				return NORTHWEST;
			}
		case NORTHWEST:
			if (random) {
				return NORTHEAST;
			}
			else {
				return SOUTHWEST;
			}
		default:
			throw new Exception("Unsupported direction: " + current + ".");
		}
	}
	
	/**
	 * Generates a new direction tending toward preferred but no more than
	 * 45 degrees off current.
	 * @param current
	 * @param preferred
	 * @return
	 * @throws Exception
	 */
	public static Direction steer(final Direction current, final Direction preferred) throws Exception {
		if (current == preferred) {
			return current;
		}
		
		switch (current) {
		case NORTH:
			if (preferred == SOUTH) {
				if (RandomUtils.randomBoolean()) {
					return NORTHWEST;
				}
				else {
					return NORTHEAST;
				}
			}
			else if (preferred == NORTHWEST || preferred == WEST || preferred == SOUTHWEST) {
				return NORTHWEST;
			}
			else {
				return NORTHEAST;
			}
		case SOUTH:
			if (preferred == NORTH) {
				if (RandomUtils.randomBoolean()) {
					return SOUTHWEST;
				}
				else {
					return SOUTHEAST;
				}
			}
			else if (preferred == NORTHWEST || preferred == WEST || preferred == SOUTHWEST) {
				return SOUTHWEST;
			}
			else {
				return SOUTHEAST;
			}
		case EAST:
			if (preferred == WEST) {
				if (RandomUtils.randomBoolean()) {
					return SOUTHEAST;
				}
				else {
					return NORTHEAST;
				}
			}
			else if (preferred == NORTHWEST || preferred == NORTH || preferred == NORTHEAST) {
				return NORTHEAST;
			}
			else {
				return SOUTHEAST;
			}			
		case WEST:
			if (preferred == EAST) {
				if (RandomUtils.randomBoolean()) {
					return SOUTHWEST;
				}
				else {
					return NORTHWEST;
				}
			}
			else if (preferred == NORTHWEST || preferred == NORTH || preferred == NORTHEAST) {
				return NORTHWEST;
			}
			else {
				return SOUTHWEST;
			}	
		case NORTHEAST:
			if (preferred == SOUTHWEST) {
				if (RandomUtils.randomBoolean()) {
					return NORTH;
				}
				else {
					return EAST;
				}
			}
			else if (preferred == NORTH || preferred == WEST || preferred == NORTHWEST) {
				return NORTH;
			}
			else {
				return EAST;
			}	
		case SOUTHEAST:
			if (preferred == NORTHWEST) {
				if (RandomUtils.randomBoolean()) {
					return SOUTH;
				}
				else {
					return EAST;
				}
			}
			else if (preferred == NORTH || preferred == EAST || preferred == NORTHEAST) {
				return EAST;
			}
			else {
				return SOUTH;
			}
		case SOUTHWEST:
			if (preferred == NORTHEAST) {
				if (RandomUtils.randomBoolean()) {
					return SOUTH;
				}
				else {
					return WEST;
				}
			}
			else if (preferred == NORTH || preferred == WEST || preferred == NORTHWEST) {
				return WEST;
			}
			else {
				return SOUTH;
			}
		case NORTHWEST:
			if (preferred == SOUTHEAST) {
				if (RandomUtils.randomBoolean()) {
					return NORTH;
				}
				else {
					return WEST;
				}
			}
			else if (preferred == NORTH || preferred == EAST || preferred == NORTHEAST) {
				return NORTH;
			}
			else {
				return WEST;
			}
		default:
			throw new Exception("Unsupported direction: " + current + ".");
		}
	}
	
}
