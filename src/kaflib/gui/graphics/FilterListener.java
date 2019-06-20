package kaflib.gui.graphics;

import kaflib.graphics.Filter;

public interface FilterListener {
	
	/**
	 * Supplies a filter to be applied.  This is invoked from a non-gui thread,
	 * disabling the input of the panel and re-enabling when this function 
	 * returns.
	 * @param filter
	 */
	public void apply(final Filter filter, final boolean test);
}
