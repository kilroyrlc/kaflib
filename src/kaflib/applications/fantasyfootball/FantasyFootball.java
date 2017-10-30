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
import kaflib.utils.StringUtils;
import kaflib.web.ParseUtils;

public class FantasyFootball {

	public static void main(String args[]) {
		try {
			JFrame frame = new JFrame();
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
			    	if (!stats.containsKey(player)) {
			    		stats.put(player, new Projection());
			    	}
			    	float value = table.get(player);
			    	if (is_projected) {
			    		stats.get(player).setProjected(value);
			    	}
			    	else {
			    		stats.get(player).setLast(value);
			    	}
			    }
		    }

		    postprocess(stats, directory);
		    System.out.println("Done with application.");
		    System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
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
	    Map<Player, Float> stats = new HashMap<Player, Float>();
	    
	    Elements rows = ParseUtils.select(document, ".pncPlayerRow");
	    if (rows == null || rows.size() == 0) {
	    	throw new Exception("Unable to find playertable.");
	    }
	    
	    for (Element row : rows) {
	    	Elements selection = row.select(".playertablePlayerName");
	    	if (selection == null || selection.size() != 1) {
	    		throw new Exception("Parse fail on:\n" + StringUtils.truncateIf(row.html(), 256));
	    	}

	    	Player player = new Player(selection.text());
	    	selection = row.select(".playertableStat.appliedPoints.sortedCell");
	    	if (selection == null || selection.size() != 1) {
	    		throw new Exception("Parse fail on:\n" + row.html());
	    	}
	    	Float stat = Float.valueOf(selection.text());
	    	
	    	if (stats.containsKey(player) && stats.get(player) != stat) {
	    		throw new Exception(player + ": " + stat + " / " + stats.get(player));
	    	}
	    	stats.put(player, stat);
	    }
	    return stats;
	}

	
}
