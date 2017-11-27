package kaflib.gui;

public interface AddRemoveListListener <T> {
	public T addPressed();
	public void itemSelected(final T item);
}
