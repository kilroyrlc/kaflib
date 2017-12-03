package kaflib.applications.mtg;

import java.util.List;

public abstract class CardCollection implements Iterable<Card> {
	public abstract List<String> getNameList() throws Exception;
}
