package kaflib.applications.fantasyfootball;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import kaflib.applications.fantasyfootball.Player.Position;
import kaflib.types.Matrix;
import kaflib.utils.CheckUtils;
import kaflib.utils.StringUtils;
import kaflib.web.ParseUtils;

public class FantasyFootball {

	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    JFileChooser chooser = new JFileChooser();
		    chooser.setMultiSelectionEnabled(true);
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		    if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION ||
		    	chooser.getSelectedFile() == null) {
		    	return;
		    }
		    File[] files = chooser.getSelectedFiles();
		    File directory = new File(".");
		    
		    if (files.length > 0) {
		    	directory = files[0].getParentFile();
		    }
	    	if (!directory.isDirectory()) {
	    		throw new Exception("Not a dir: " + directory + ".");
	    	}


		    Map<Player, Projection> stats = new HashMap<Player, Projection>();

		    for (File file : files) {
		    	boolean is_projected = false;
		    	if (file.getName().contains("proj")) {
		    		is_projected = true;
		    	}
		    	
			    Map<Player, Float> table = parseTable(file);
			    
			    for (Player player : table.keySet()) {
			    	CheckUtils.check(player, "player");
			    	
			    	if (!stats.containsKey(player)) {
			    		stats.put(player, new Projection());
			    	}
			    	Float value = table.get(player);
			    	
			    	try {
			    		if (value != null) {
					    	if (is_projected) {
					    		stats.get(player).setProjected(value);
					    	}
					    	else {
					    		stats.get(player).setLast(value);
					    	}
			    		}
			    	}
			    	catch (Exception e) {
			    		System.out.println("" + player + ": " + stats.get(player));
			    		System.out.println("-> " + value + " / " + is_projected);
			    		throw e;
			    	}
			    }
		    }
		    postprocess(stats, directory);
		    System.out.println("Done with application.");
		    System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void postprocess(final Map<Player, Projection> stats, final File directory) throws Exception {
		
		List<PlayerProjection> qb = Projection.createRanking(Position.QB, stats);
		List<PlayerProjection> rb = Projection.createRanking(Position.RB, stats);
		List<PlayerProjection> wr = Projection.createRanking(Position.WR, stats);
		List<PlayerProjection> te = Projection.createRanking(Position.TE, stats);
		List<PlayerProjection> d_st = Projection.createRanking(Position.D_ST, stats);
		List<PlayerProjection> k = Projection.createRanking(Position.K, stats);
		Float qb_base = qb.get(13).getProjection().getExpected();
		Float rb_base = rb.get(31).getProjection().getExpected();
		Float wr_base = wr.get(31).getProjection().getExpected();
		Float te_base = te.get(19).getProjection().getExpected();
		Float d_st_base = d_st.get(13).getProjection().getExpected();
		Float k_base = k.get(13).getProjection().getExpected();
		
		for (PlayerProjection p : qb) {
			p.setBaseline(qb_base);
		}
		for (PlayerProjection p : rb) {
			p.setBaseline(rb_base);
		}
		for (PlayerProjection p : wr) {
			p.setBaseline(wr_base);
		}
		for (PlayerProjection p : te) {
			p.setBaseline(te_base);
		}
		for (PlayerProjection p : d_st) {
			p.setBaseline(d_st_base);
		}
		for (PlayerProjection p : k) {
			p.setBaseline(k_base);
		}
		
		List<PlayerProjection> aggregate = new ArrayList<PlayerProjection>();
		aggregate.addAll(qb);
		aggregate.addAll(rb);
		aggregate.addAll(wr);
		aggregate.addAll(te);
		aggregate.addAll(d_st);
		aggregate.addAll(k);
		
		Collections.sort(aggregate, new Comparator<PlayerProjection>(){
			@Override
			public int compare(PlayerProjection o1, PlayerProjection o2) {
				if (o1.getDifference() > o2.getDifference()) {
					return -1;
				}
				else if (o1.getDifference() < o2.getDifference()) {
					return 1;
				}
				else {
					return 0;
				}
			}
		});
		
		Matrix<String> matrix = new Matrix<String>();
		matrix.setColumnLabels("Player", "Position", "Team", "Last", 
							   "Projected", "Expected", "Baseline", 
							   "Difference");
		for (PlayerProjection p : aggregate) {
			System.out.println("Adding " + p);
			List<String> values = new ArrayList<String>();
			values.add(p.getPlayer().getName());
			values.add(p.getPlayer().getPosition().toString());
			values.add(p.getPlayer().getTeam());
			if (p.getProjection().getLast() != null) {
				values.add("" + p.getProjection().getLast().intValue());
			}
			else {
				values.add("");
			}
			if (p.getProjection().getProjected() != null) {
				values.add("" + p.getProjection().getProjected().intValue());
			}	
			else {
				values.add("");
			}
			values.add("" + p.getProjection().getExpected().intValue());
			values.add("" + p.getBaseline().intValue());
			values.add("" + p.getDifference().intValue());
			matrix.addRow(values);
		}
		matrix.toCSV(new File(directory, "sheet.csv"));
		matrix.toXLSX(new File(directory, "sheet.xlsx"));
	}
	
	public static Map<Player, Float> parseTable(final File file) throws Exception {
		Document document = Jsoup.parse(file, "UTF-8");
	    Elements rows = ParseUtils.select(document, ".ysf-player-name");
	    if (rows == null || rows.size() == 0) {
	    	return parseTableESPN(file);
	    }
	    else {
	    	return parseTableYahoo(file);
	    }
	}
	
	private static void checkOne(final String field, 
								 final Elements elements,
								 final Element value) throws Exception {
		if (elements == null) {
			throw new Exception("No matches for " + field + ".\n" + value);
		}
		else if (elements.size() != 1) {
			throw new Exception("Results for " + field + ": " + elements.size() + ".\n" + value);
		}
		else {
			
		}
	}
	
	public static Map<Player, Float> parseTableYahoo(final File file) throws Exception {
	    Document document = Jsoup.parse(file, "UTF-8");
	    Map<Player, Float> stats = new HashMap<Player, Float>();

	    Elements rows = ParseUtils.select(document, "#players-table tbody tr");
	    if (rows == null || rows.size() == 0) {
	    	throw new Exception("Unable to find playertable.");
	    }
	    for (Element row : rows) {
	    	Elements status = row.select(".ysf-player-name");
	    	checkOne("status", status, row);

	    	Elements name = status.get(0).select(".Nowrap.name.F-link");
	    	if (name == null || name.size() != 1) {
		    	name = status.get(0).select("a");
		    	checkOne("name", name, status.get(0));
	    	}
	    	
	    	Elements team = status.get(0).select(".Fz-xxs");
	    	checkOne("team", team, status.get(0));

	    	Player player;
	    	if (team.text().trim().toLowerCase().endsWith("def")) {
	    		player = new Player(team.text().substring(0, 4) + " " + team.text());
	    	}
	    	else {
	    		player = new Player(name.text() + " " + team.text());
	    	}
	    	if (player.getName().equals("T. Taylor") || 
	    		player.getName().equals("T. Williams") || 
	    		player.getName().equals("D. Thomas") || 
	    		player.getName().equals("J. Nelson") || 
	    		player.getName().equals("T. Smith") || 
	    		player.getName().equals("J. Brown") || 
	    		player.getName().equals("D. Moore") || 
	    		player.getName().equals("D. Washington") || 
	    		player.getName().equals("D. Williams") || 
	    		player.getName().equals("D. Johnson")) {
	    		continue;
	    	}

	    	Elements gp = row.select(".F-faded.Bdrend");
	    	checkOne("gp", gp, row);

	    	int games = 0;
	    	if (!gp.text().trim().equals("-")) {
	    		games = StringUtils.toInt(gp.text());
	    	}
	    	
	    	Elements score = row.select(".Fw-b");
	    	checkOne("score", score, row);

	    	Float stat = Float.valueOf(score.text());
	    	if (games > 6) {
	    		stat = (stat / games) * 16;
	    	}
	    	else {
	    		stat = null;
	    	}
	    	
	    	if (stats.containsKey(player) && stats.get(player) != stat) {
	    		throw new Exception(player + ": " + stat + " / " + stats.get(player));
	    	}
	    	
	    	stats.put(player, stat);
	    }
	    return stats;
	}
	
	public static Map<Player, Float> parseTableESPN(final File file) throws Exception {
	    Document document = Jsoup.parse(file, "UTF-8");
	    Map<Player, Float> stats = new HashMap<Player, Float>();

	    Elements rows = ParseUtils.select(document, ".Table2__tbody tr");
	    if (rows == null || rows.size() == 0) {
	    	throw new Exception("Unable to find playertable.");
	    }
	    
	    for (Element row : rows) {
	    	String name;
	    	String team;
	    	String position;
	    	Elements columns = row.select("td");

	    	Elements selection = columns.get(0).select("div .player__column");
	    	if (selection == null || selection.size() < 1) {
	    		throw new Exception("Parse fail (" + selection.size() + ") on:\n" + StringUtils.truncateIf(row.html(), 512));
	    	}
	    	name = selection.get(0).attr("title");
	    	name = selection.text();
	    	
	    	selection = row.select(".playerinfo__playerteam");
	    	if (selection == null || selection.size() != 1) {
	    		throw new Exception("Parse fail on:\n" + StringUtils.truncateIf(row.html(), 512));
	    	}
	    	team = selection.text();

	    	selection = row.select(".playerinfo__playerpos");
	    	if (selection == null || selection.size() != 1) {
	    		throw new Exception("Parse fail on:\n" + StringUtils.truncateIf(row.html(), 512));
	    	}
	    	position = selection.text();
	    	
	    	Player player = new Player(name, team, position);

	    	Float stat = null;
	    	for (Element column : columns) {
		    	selection = column.select("div .total");

		    	for (Element element : selection) {
		    		if (element.attr("title").equals("Fantasy Points")) {
		    			if (selection.text().trim().equals("--")) {
		    				stat = new Float(0.0);
		    			}
		    			else {
			    			try {
			    				stat = Float.valueOf(selection.text());
			    			}
			    			catch (Exception e) {
			    				System.out.println("Value: " + selection.text() + ".");
			    				throw e;
			    			}
		    			}
		    		}
		    	}
		    	if (stat != null) {
		    		break;
		    	}
	    	}
	    	
	    	if (stat == null) { 
	    		throw new Exception("Parse fail on:\n" + row.html());
	    	}
	    	
	    	if (stats.containsKey(player) && stats.get(player) != stat) {
	    		throw new Exception(player + ": " + stat + " / " + stats.get(player));
	    	}
	    	stats.put(player, stat);
	    }
	    return stats;
	}

	
}
