package TaiGameCore.swing;

import java.awt.Container;

public interface ModalizableComponent {
	public Container getModalPanel();
	public void addModalScale(Runnable runnable);
	public void removeAllModalScale();
}
