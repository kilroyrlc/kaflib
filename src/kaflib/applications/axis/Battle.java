package kaflib.applications.axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaflib.types.Pair;
import kaflib.utils.RandomUtils;

public class Battle {
	private UnitStack attackers;
	private UnitStack defenders;
	private final List<String> log;
	private final RemovePolicy policy;
 	
	public enum Result {
		ATTACKER_WON,
		DEFENDER_WON,
		STALEMATE
	}
	
	public enum RemovePolicy {
		SUCCESS,
		COST
	}
	
	public Battle(final UnitStack attackers, 
				  final UnitStack defenders,
				  final RemovePolicy policy) throws Exception {
		this.attackers = new UnitStack(attackers);
		this.defenders = new UnitStack(defenders);
		this.policy = policy;
		log = new ArrayList<String>();
		log.add("Attackers: " + this.attackers.toString());
		log.add("Defenders: " + this.defenders.toString());

		if ((defenders.getLocation() == Unit.Type.LAND && attackers.getLocation() == Unit.Type.SEA) ||
			(defenders.getLocation() == Unit.Type.SEA && attackers.getLocation() == Unit.Type.LAND)) {
			throw new Exception("Attackers and defenders not agreed on what they are standing on.");
		}
	}
	
	
	public void attack(final Unit unit, final boolean attacker) throws Exception {
		int roll = RandomUtils.randomD6();

		if (attacker) {
			if (roll <= unit.getAttack()) {
				String casualty = defenders.markCasualty(unit, attackers, policy);
				log.add("Attacking " + unit.toString() + " rolled " + roll + ", hit " + casualty + ".");
			}
			else {
				log.add("Attacking " + unit.toString() + " rolled " + roll + ", missed.");
			}
		}
		else {
			if (roll <= unit.getDefense()) {
				String casualty = attackers.markCasualty(unit, defenders, policy);
				log.add("Defending " + unit.toString() + " rolled " + roll + ", hit " + casualty + ".");
			}
			else {
				log.add("Defending " + unit.toString() + " rolled " + roll + ", missed.");
			}			
		}
	}
	
	public Result resolve() throws Exception {
		if (defenders.getLocation() == Unit.Type.SEA) {
			return resolveSea();
		}
		else if (defenders.getLocation() == Unit.Type.LAND) {
			return resolveLand();
		}
		else {
			throw new Exception("Unable to determine defense location:\n" + defenders.toString());
		}
	}
	
	private Result resolveLand() throws Exception {
		if (defenders.hasAA() && attackers.getAirborne().size() > 0) {
			int roll;
			List<Unit> casualties = new ArrayList<Unit>();
			for (Unit unit : attackers.getAirborne()) {
				roll = RandomUtils.randomD6();
				if (roll == 1) {
					casualties.add(unit);
					log.add("AA: rolled " + roll + " killed " + unit.getClass() + ".");
				}
				else {
					log.add("AA: rolled " + roll + " did not kill " + unit.getClass() + ".");
				}
			}
			attackers.remove(casualties);
		}	
		
		while (!attackers.isEmpty() && !defenders.isEmpty()) {
			if (!attackers.canHit(defenders) && !defenders.canHit(attackers)) {
				System.out.println("Battle finished with units remaining on both sides.");
				break;
			}

			for (Unit attacker : attackers.getUnits()) {
				attack(attacker, true);
			}			
		
			for (Unit defender : defenders.getUnits()) {
				attack(defender, false);
			}
			
			log.add(attackers.removeCasualties());
			log.add(defenders.removeCasualties());
			
		}
		
		if (attackers.isEmpty() && defenders.isEmpty() ||
			!attackers.isEmpty() && !defenders.isEmpty()) {
			return Result.STALEMATE;
		}
		else if (attackers.isEmpty()) {
			return Result.DEFENDER_WON;
		}
		else if (defenders.isEmpty()) {
			return Result.ATTACKER_WON;
		}
		else {
			printLog();
			throw new Exception("Huh?");
		}
	}
	
	
	private Result resolveSea() throws Exception {
		while (!attackers.isEmpty() && !defenders.isEmpty()) {
			if (!attackers.canHit(defenders) && !defenders.canHit(attackers)) {
				System.out.println("Battle finished with units remaining on both sides.");
				break;
			}
			boolean attacking_subs_went = false;
			boolean defending_subs_went = false;

			// Attacking subs.
			if (attackers.hasSub() && !defenders.hasDestroyer()) {
				attacking_subs_went = true;
				for (Unit attacker : attackers.getSubs()) {
					attack(attacker, true);
				}			
			}
			// Defending subs.
			if (defenders.hasSub() && !attackers.hasDestroyer()) {
				defending_subs_went = true;
				for (Unit defender : defenders.getSubs()) {
					attack(defender, false);
				}
			}
			
			if (attacking_subs_went == true) {
				log.add(defenders.removeCasualties());			
				
				for (Unit attacker : attackers.getNonSubs()) {
					attack(attacker, true);
				}

			}
			else {
				for (Unit attacker : attackers.getUnits()) {
					attack(attacker, true);
				}			
			}
			
			if (defending_subs_went == true) {
				log.add(attackers.removeCasualties());
				for (Unit defender : defenders.getNonSubs()) {
					attack(defender, false);
				}

			}
			else {
				for (Unit defender : defenders.getUnits()) {
					attack(defender, false);
				}
			}
			
			log.add(attackers.removeCasualties());
			log.add(defenders.removeCasualties());
			
		}
		
		if (attackers.isEmpty() && defenders.isEmpty() ||
			!attackers.isEmpty() && !defenders.isEmpty()) {
			return Result.STALEMATE;
		}
		else if (attackers.isEmpty()) {
			return Result.DEFENDER_WON;
		}
		else if (defenders.isEmpty()) {
			return Result.ATTACKER_WON;
		}
		else {
			printLog();
			throw new Exception("Huh?");
		}
	}
	
	public int getRemainingUnits() {
		return attackers.size() + defenders.size();
	}
	
	public void printLog() {
		for (String line : log) {
			System.out.println(line);
		}
	}

	public static void main(String args[]) {
		try {
			List<Unit> units = new ArrayList<Unit>();

			units.add(Unit.INFANTRY);
			units.add(Unit.INFANTRY);
			//units.add(Unit.INFANTRY);
			//units.add(Unit.INFANTRY);
			units.add(Unit.INFANTRY);
			units.add(Unit.FIGHTER);

			UnitStack attackers = new UnitStack(units, false, UnitStack.Role.ATTACKER);
			units = new ArrayList<Unit>();
			units.add(Unit.ARTILLERY);
			UnitStack defenders = new UnitStack(units, true, UnitStack.Role.DEFENDER);

			Map<Result, Pair<Integer, Integer>> results = new HashMap<Result, Pair<Integer, Integer>>();
			results.put(Result.ATTACKER_WON, new Pair<Integer, Integer>(0, 0));
			results.put(Result.DEFENDER_WON, new Pair<Integer, Integer>(0, 0));
			results.put(Result.STALEMATE, new Pair<Integer, Integer>(0, 0));
			
			for (int i = 0; i < 10000; i++) {
				Battle battle = new Battle(attackers, defenders, RemovePolicy.SUCCESS);
				Result result = battle.resolve();
				
				results.put(result, new Pair<Integer, Integer>(results.get(result).getFirst() + 1,
															   results.get(result).getSecond() + battle.getRemainingUnits()));
				
				//battle.printLog();
			}
			System.out.println("Attacker:  " + results.get(Result.ATTACKER_WON).getFirst() + " average remaining: " + 
					           (results.get(Result.ATTACKER_WON).getSecond() / results.get(Result.ATTACKER_WON).getFirst()) + 
					           ".");
			System.out.println("Defender:  " + results.get(Result.DEFENDER_WON).getFirst() + " average remaining: " + 
			           (results.get(Result.DEFENDER_WON).getSecond() / results.get(Result.DEFENDER_WON).getFirst()) + 
			           ".");
			System.out.println("Stalemate: " + results.get(Result.STALEMATE).getFirst() + " average remaining: " + 
			           (results.get(Result.STALEMATE).getSecond() / results.get(Result.STALEMATE).getFirst()) + 
			           ".");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
