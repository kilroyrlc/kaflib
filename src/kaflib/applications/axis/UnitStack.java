package kaflib.applications.axis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kaflib.utils.StringUtils;

/**
 * Defines a stack of units with the attack or defense role.  Manages a list
 * of casualties that do damage but then can be cleared at the client's behest.
 */
public class UnitStack {
	public enum Role {
		ATTACKER,
		DEFENDER
	}
	
	private final List<Unit> units;
	private final List<Unit> casualties;
	private final boolean has_aa;
	private final Unit.Type location;
	private final Role role;
	private int battleship_tips;

	
	public UnitStack(final List<Unit> units, final boolean hasAA, final Role role) throws Exception {
		this.units = new ArrayList<Unit>();
		this.units.addAll(units);
		this.has_aa = hasAA;
		this.role = role;
		casualties = new ArrayList<Unit>();
		
		Unit.Type location = null;
		for (Unit unit : units) {
			if (unit == Unit.BATTLESHIP) {
				battleship_tips ++;
			}
			
			if (unit.getType() == Unit.Type.LAND) {
				if (location == null || location == Unit.Type.LAND || location == Unit.Type.AIR) {
					location = Unit.Type.LAND;
				}
				else {
					throw new Exception("Stack has mix of land and sea: " + unit + " / " + location + ".");
				}
			}
			else if (unit.getType() == Unit.Type.SEA) {
				if (location == null || location == Unit.Type.SEA || location == Unit.Type.AIR) {
					location = Unit.Type.SEA;
				}
				else {
					throw new Exception("Stack has mix of land and sea " + unit + " / " + location + ".");
				}
			}
			else {
				if (location == null) {
					location = Unit.Type.AIR;
				}
			}
		}
		this.location = location;
		
	}
	
	public UnitStack(final UnitStack stack) {
		this.units = new ArrayList<Unit>();
		this.units.addAll(stack.getUnits());
		this.has_aa = stack.hasAA();
		this.location = stack.getLocation();
		this.role = stack.getRole();
		this.battleship_tips = stack.getBattleshipTips();
		casualties = new ArrayList<Unit>();
	}
	
	public int getBattleshipTips() {
		return battleship_tips;
	}
	
	public Role getRole() {
		return role;
	}
	
	public boolean isEmpty() {
		return units.size() == 0;
	}

	public Unit.Type getLocation() {
		return location;
	}
	
	public boolean hasAA() {
		return has_aa;
	}
	
	public int size() {
		return units.size();
	}
	
	public List<Unit> getUnits() {
		List<Unit> list = new ArrayList<Unit>();
		list.addAll(units);
		list.addAll(casualties);
		return list;
	}
	
	public List<Unit> getNonSubs() {
		List<Unit> list = new ArrayList<Unit>();
		for (Unit unit : units) {
			if (unit != Unit.SUB) {
				list.add(unit);
			}
		}
		for (Unit unit : casualties) {
			if (unit != Unit.SUB) {
				list.add(unit);
			}
		}
		return list;
	}
	
	public String toString() {
		try {
			return StringUtils.concatenate(units, " ");
		}
		catch (Exception e) {
			return "[Unable to create string]";
		}
	}
	
	public void remove(final Collection<Unit> casualties) throws Exception {
		for (Unit unit : casualties) {
			units.remove(unit);
		}
	}
	
	/**
	 * Clears the casualty list and returns a descriptive message.
	 * @return
	 * @throws Exception
	 */
	public String removeCasualties() throws Exception {
		String message;
		if (role == Role.ATTACKER) {
			message = "Attacker lost: [" + StringUtils.concatenate(casualties, " ") + 
									   "], has: [" + toString() + "] " + battleship_tips + " tips.";
		}
		else {
			message = "Defender lost: [" + StringUtils.concatenate(casualties, " ") + 
									   "], has: [" + toString() + "] " + battleship_tips + " tips.";
		}
		casualties.clear();
		return message;
	}
	
	public void markCasualties(final int casualties, 
					   		 final Unit attacker,
					   		 final UnitStack attackerStack,
					   		 final Battle.RemovePolicy policy) throws Exception {
		for (int i = 0; i < casualties; i++) {
			markCasualty(attacker, attackerStack, policy);
		}
	}
	
	/**
	 * Deals with a single casualty.
	 * @param attackingUnit
	 * @param attackingStack
	 * @param policy
	 * @throws Exception
	 */
	public String markCasualty(final Unit attackingUnit,
			   		   		 final UnitStack attackingStack,
			   		   		 final Battle.RemovePolicy policy) throws Exception {
		if (units.size() == 0) {
			return "[no units]";
		}
		
		if (battleship_tips > 0 && canHit(attackingUnit, Unit.BATTLESHIP, attackingStack)) {
			battleship_tips--;
			return "tip";
		}
		
		Unit casualty = getCandidate(attackingUnit, attackingStack, policy);
		if (casualty != null) {
			casualties.add(casualty);
			units.remove(casualty);
			return casualty.toString();
		}
		else {
			return "[nothing to hit]";
		}
	}
	
	public boolean canHit(final UnitStack theirStack) {
		for (Unit unit : getUnits()) {
			for (Unit other : theirStack.getUnits()) {
				if (canHit(other, unit, theirStack)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canHit(final Unit them,
						  final Unit unit,
						  final UnitStack theirStack) {
		if (unit.getType() == Unit.Type.AIR) {
			// Subs can't hit air.
			if (them == Unit.SUB) {
				return false;
			}
			else {
				return true;
			}
		}
		else if (unit == Unit.SUB) {
			if (them.getType() == Unit.Type.AIR &&
				!theirStack.hasDestroyer()) {
				return false;
			}
			else {
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	private Unit getCandidate(final Unit them,
							 final UnitStack theirStack,
							 final Battle.RemovePolicy policy) throws Exception {
		Unit candidate = null;
		
		for (Unit unit : units) {
			if (!canHit(them, unit, theirStack)) {
				continue;
			}
			if (candidate == null) {
				candidate = unit;
			}
			else {
				if (policy == Battle.RemovePolicy.COST) {
					if (unit.getCost() < candidate.getCost()) {
						candidate = unit;
					}
				}
				else if (policy == Battle.RemovePolicy.SUCCESS) {
					if (role == Role.ATTACKER) {
						if (unit.getAttack() < candidate.getAttack()) {
							candidate = unit;
						}
					}
					else if (role == Role.DEFENDER) {
						if (unit.getDefense() < candidate.getDefense()) {
							candidate = unit;
						}
					}
					else {
						throw new Exception("Okay Switzerland.");
					}
				}
				else {
					throw new Exception("Unknown policy: " + policy + ".");
				}
			}
		}
		return candidate;
	}
	
	public List<Unit> getAirborne() {
		List<Unit> planes = new ArrayList<Unit>();
		for (Unit unit : units) {
			planes.add(unit);
		}
		for (Unit unit : casualties) {
			planes.add(unit);
		}

		return planes;
	}
	
	public boolean hasDestroyer() {
		for (Unit unit : units) {
			if (unit == Unit.DESTROYER) {
				return true;
			}
		}
		for (Unit unit : casualties) {
			if (unit == Unit.DESTROYER) {
				return true;
			}
		}
		return false;
	}
	
	public List<Unit> getSubs() {
		List<Unit> subs = new ArrayList<Unit>();
		for (Unit unit : units) {
			if (unit == Unit.SUB) {
				subs.add(unit);
			}
		}
		for (Unit unit : casualties) {
			if (unit == Unit.SUB) {
				subs.add(unit);
			}
		}
		return subs;
	}
	
	public boolean hasSub() {
		for (Unit unit : units) {
			if (unit == Unit.SUB) {
				return true;
			}
		}
		for (Unit unit : casualties) {
			if (unit == Unit.SUB) {
				return true;
			}
		}
		return false;
	}
	
}
