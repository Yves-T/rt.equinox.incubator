/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
			try {
			if (!display.readAndDispatch())
				display.sleep();
			} catch (ThreadDeath th) {
				throw th;
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Error err) {
				err.printStackTrace();
			}
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
