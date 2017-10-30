package kaflib.applications.axis;


public enum Unit {
	ARTILLERY(2, 2, 1, 4, Type.LAND),
	BATTLESHIP(4, 4, 2, 20, Type.SEA),
	BOMBER(4, 1, 1, 12, Type.AIR),
	CARRIER(1, 2, 1, 14, Type.SEA),
	CRUISER(3, 3, 1, 12, Type.SEA),
	DESTROYER(2, 2, 1, 8, Type.SEA),
	FIGHTER(3, 4, 1, 10, Type.AIR),
	INFANTRY(1, 2, 1, 3, Type.LAND),
	SUPPORTED_INFANTRY(2, 2, 1, 3, Type.LAND),
	SUB(2, 1, 1, 6, Type.SEA),
	TANK(3, 3, 1, 5, Type.LAND);
	
	public enum Type {
		LAND,
		SEA,
		AIR
	}
	
	private final int attack;
	private final int defense;
	private final int hp;
	private final int cost;

	private final Type type;
	
	private Unit(final int attack,
				 final int defense,
				 final int hp,
				 final int cost,
				 final Type type) {
		this.attack = attack;
		this.defense = defense;
		this.hp = hp;
		this.cost = cost;
		this.type = type;
	}
	
	public String toString() {
		return this.name().toLowerCase();
	}
	
	/**
	 * @return the attack
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * @return the defense
	 */
	public int getDefense() {
		return defense;
	}

	/**
	 * @return the hp
	 */
	public int getHP() {
		return hp;
	}

	/**
	 * @return the cost
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
	
}
