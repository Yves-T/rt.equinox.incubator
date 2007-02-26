package org.eclipse.swt.examples.addressbook;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Application implements IApplication {
	Shell shell;

	public Object start(IApplicationContext context) throws Exception {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				shell = AddressBook.runApplication(display);
			}
		});
		while (!shell.isDisposed())
			Thread.sleep(200);
		return null;
	}

	public void stop() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (shell != null && !shell.isDisposed())
					shell.close();
			}
		});
	}
}
