package org.eclipse.swt.examples.graphics;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;

public class Application implements IApplication {
	AdvancedGraphics app;

	public Object start(IApplicationContext context) throws Exception {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				app = new AdvancedGraphics();
				app.open(display);
			}
		});
		while (!app.isDisposed())
			Thread.sleep(200);
		app.dispose();
		return null;
	}

	public void stop() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (app != null && !app.isDisposed())
					app.dispose();
			}
		});
	}
}
