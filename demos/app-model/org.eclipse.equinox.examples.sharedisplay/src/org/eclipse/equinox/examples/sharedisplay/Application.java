package org.eclipse.equinox.examples.sharedisplay;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A demo application that creates a default SWT display that 
 * uses the main thread as its event dispatcher.
 *
 */
public class Application implements IApplication {
	Display display;

	public Object start(IApplicationContext context) throws Exception {
		// here we assume that nobody has created the default display yet
		// this will force the default display to be created using the main thread.
		display = Display.getDefault();
		// this brings down the splash screen
		context.applicationRunning();
		// do the standard SWT dispatching; we assume this is being called by the main thread
		while (!display.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return null;
	}

	public void stop() {
		if (display == null)
			return;
		// this is likely not going to be called by the main thread
		display.syncExec(new Runnable() {
			public void run() {
				// dispose all the shells still open for the display
				Shell[] shells = display.getShells();
				try {
					if (shells == null || shells.length == 0)
						return;
					for (int i = 0; i < shells.length; i++)
						if (!display.isDisposed() && !shells[i].isDisposed())
							shells[i].close();
				} finally {
					display.dispose();
				}
			}
		});
	}

}
