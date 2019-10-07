package kaflib.applications.fantasyfootball;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;

public class Player {
	public enum Position {
		QB,
		RB,
		WR,
		TE,
		K,
		D_ST
	}
	
	private final String name;
	private final String team;
	private final Position position;
	private final Set<String> teams;
	private final Set<Position> positions;
	private final int hash_code;
	

	public Player(final String name, final String team, final String position) throws Exception {
		this.name = name;
		this.team = team;
		this.position = getPosition(position);
		teams = new HashSet<String>();
		positions = new HashSet<Position>();
		teams.add(this.team);
		positions.add(this.position);
		hash_code = (name + position).hashCode();

	}
	
	public Player(final String string) throws Exception {
		String input = new String(string.getBytes("UTF-8"));
		input = StringUtils.replace(input, "[\\w\\d\\s\\-\\/\\,\\.]", ' ');

		teams = new HashSet<String>();
		positions = new HashSet<Position>();
		
		String namereg = "([^\\,]+)[\\,]?";
		String teamreg = "([\\w]{2,3})";
		String posreg = "([\\w]{1,3})";
		
		Pattern pattern = Pattern.compile("^\\s*(\\w+)\\s*D\\/ST.*$");
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String temp = matcher.group(1);
			StringUtils.truncateAt(temp, "*");
			CheckUtils.checkNonEmpty(temp, "name: " + input);
			name = temp;
			team = temp;
			teams.add(team);
			position = Position.D_ST;
			positions.add(position);
		}
		else {
			pattern = Pattern.compile("^\\s*" + namereg + 
					  "\\s+" + teamreg + 
					  "[\\s\\-]+" + posreg + 
					  ".*$");	

			matcher = pattern.matcher(input);

			if (!matcher.matches()) {
				pattern = Pattern.compile("^\\s*" + namereg + 
						"\\s*" + teamreg + 
						"(.*)$");
				matcher = pattern.matcher(input);
				if (matcher.matches()) {
					System.out.println(matcher.group(1) + "/" + 
							matcher.group(2) + "/" + 
							matcher.group(3));
				}
				else {
					System.out.println("Doesn't match name and .");
				}

				pattern = Pattern.compile("^\\s*" + namereg + 
						"(.*)$");
				matcher = pattern.matcher(input);
				if (matcher.matches()) {
					System.out.println(matcher.group(1) + "/" + matcher.group(2));
				}
				else {
					System.out.println("Doesn't match just name.");
				}

				throw new Exception("Unable to match: " + input + ".");
			}	
			
			String temp = matcher.group(1);
			StringUtils.truncateAt(temp, "*");
			CheckUtils.checkNonEmpty(temp, "name: " + input);
			name = temp;
			
			temp = matcher.group(2);
			CheckUtils.checkNonEmpty(temp, "team: " + input);
			teams.add(temp);
			team = temp;
			
			Position p = getPosition(matcher.group(3));
			positions.add(p);
			position = p;	
		}	
		hash_code = (name + position).hashCode();
	}
	
	public String toString() {
		return name + " " + position + " [" + team + "]";
	}
	
	public int hashCode() {
		return hash_code;
	}

	public boolean equals(final Object other) {
		if (other instanceof Player) {
			return equals((Player) other);
		}
		else {
			return false;
		}
	}
	
	public boolean equals(final Player other) {
		return name.equals(other.getName()) && position == other.getPosition();
	}
	
	public static Position getPosition(final String string) throws Exception {
		if (string.matches("^\\s*[Qq][Bb]\\s*$")) {
			return Position.QB;
		}
		else if (string.matches("^\\s*[Rr][Bb]\\s*$")) {
			return Position.RB;
		}
		else if (string.matches("^\\s*[Ww][Rr]\\s*$")) {
			return Position.WR;
		}
		else if (string.matches("^\\s*[Tt][Ee]\\s*$")) {
			return Position.TE;
		}
		else if (string.matches("^\\s*[Dd][/][Ss][Tt]\\s*$")) {
			return Position.D_ST;
		}
		else if (string.matches("^\\s*[Dd][Ee][Ff]\\s*$")) {
			return Position.D_ST;
		}
		else if (string.matches("^\\s*[Dd][.][Ss][Tt]\\s*$")) {
			return Position.D_ST;
		}
		else if (string.matches("^\\s*[Kk]\\s*$")) {
			return Position.K;
		}
		else {
			throw new Exception("Unknown position: " + string + ".");
		}
	}
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the current_team
	 */
	public String getTeam() {
		return team;
	}

	/**
	 * @return the current_position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @return the teams
	 */
	public Set<String> getTeams() {
		return teams;
	}

	/**
	 * @return the positions
	 */
	public Set<Position> getPositions() {
		return positions;
	}
	
}
