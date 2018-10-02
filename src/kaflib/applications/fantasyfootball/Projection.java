package kaflib.applications.fantasyfootball;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Projection {
	private Float last;				// Last season.
	private Float projected;		// FF number.
	private Float expected;			// Our number.

	public Projection() throws Exception {
		last = null;
		projected = null;
		expected = null;
	}
	
	/**
	 * @return the last
	 */
	public Float getLast() {
		return last;
	}

	/**
	 * @param last the last to set
	 */
	public void setLast(final Float last) throws Exception {
		if (this.last != null && this.last != last) {
			throw new Exception("Trying to reset last: " + this.last + 
								"/" + last + ".");
		}
		
		this.last = last;
	}
	
	public Float getProjected() {
		return projected;
	}

	public String toString() {
		try {
			return getString();
		}
		catch (Exception e) {
			return "last/" + getLast() + ", " +
				   "proj/" + getProjected();
		}

	}

	
	public String getString() throws Exception {
		return "last/" + getLast() + ", " +
			   "proj/" + getProjected() + ", " +
			   "exp/" + getExpected();
	}

	/**
	 * @param expected the expected to set
	 */
	public void setProjected(final Float projected) throws Exception {
		if (this.projected != null && this.projected != projected) {
			throw new Exception("Trying to reset projected: " + this.projected + 
								"/" + expected + ".");
		}
		this.projected = projected;
	}

	public Float getExpected() {
		if (expected != null) {
			return expected;
		}
		
		if (last == null && projected == null) {
			return new Float(0);
		}
		else if (last != null && projected != null) {
			expected = (last + projected) / 2;
			expected += (projected - expected) / 2;
		}
		else if (last == null) {
			expected = projected;
		}
		else {
			expected = last / 2;
		}
		return expected;
	}
	
	public static List<PlayerProjection> createRanking(final Player.Position position,
													   final Map<Player, Projection> stats) throws Exception {
		
		List<PlayerProjection> list = new ArrayList<PlayerProjection>();
		for (Player player : stats.keySet()) {
			if (player.getPosition() == position) {
				int index;
				for (index = 0; index < list.size(); index++) {
					if (stats.get(player).getExpected() > 
						list.get(index).getProjection().getExpected()) {
						break;
					}
				}
				list.add(index, new PlayerProjection(player, stats.get(player)));
			}
		}
		return list;
		
	}
	
}
