package kaflib.applications.fantasyfootball;


import kaflib.utils.CheckUtils;

public class PlayerProjection {
	private final Player player;
	private final Projection projection;
	private Float baseline;
	
	public PlayerProjection(final Player player, 
							final Projection projection) throws Exception {
		CheckUtils.check(player, "player");
		CheckUtils.check(projection, "projection");
		
		this.player = player;
		this.projection = projection;
		this.baseline = null;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Projection getProjection() {
		return projection;
	}

	public void setBaseline(final Float baseline) throws Exception{
		if (this.baseline != null && this.baseline != baseline) {
			throw new Exception("Baseline already set to: " + this.baseline + ".");
		}
		this.baseline = baseline;
	}
	
	public Float getBaseline() {
		return baseline;
	}
	
	public Float getDifference() {
		if (baseline == null) {
			return null;
		}
		return projection.getExpected() - baseline;
	}
	
	public String toString() {
		return player + ": " + projection + ", diff/" + getDifference() + "[" + baseline + "]";
	}
	
}
