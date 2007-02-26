package org.eclipse.swt.examples.paint;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Application implements IApplication {
	PaintExample app;

	public Object start(IApplicationContext context) throws Exception {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				app = PaintExample.runApplication(display);
			}
		});
		while (!app.isDisposed())
			Thread.sleep(200);
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
