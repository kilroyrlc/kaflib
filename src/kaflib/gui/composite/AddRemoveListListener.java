package kaflib.gui.composite;

public interface AddRemoveListListener <T> {
	public T addPressed();
	public T removePressed(final T item);
	public void itemSelected(final T item);
}
