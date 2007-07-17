package org.eclipse.equinox.examples.app.helloworld;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Application implements IApplication {
	private boolean active;

	public synchronized Object start(IApplicationContext context) throws Exception {
		System.out.println("Hello World!!");
		active = true;
		while (active) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		System.out.println("Goodbye World!!");
		return null;
	}

	public synchronized void stop() {
		active = false;
		notifyAll();
	}

}
