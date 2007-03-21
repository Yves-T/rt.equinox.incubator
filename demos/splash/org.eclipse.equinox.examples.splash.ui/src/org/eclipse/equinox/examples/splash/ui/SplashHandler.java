/**
 * 
 */
package org.eclipse.equinox.examples.splash.ui;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.equinox.examples.splash.Splash;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * @author aniefer
 * 
 */
public class SplashHandler extends AbstractSplashHandler {
	private boolean initialized = false;

	public Shell getSplash() {
		if (!initialized) {
			init();
		}
		return super.getSplash();
	}

	public void init() {
		Shell shell = Splash.getSplash().getShell();
		super.init(shell);
	}

	private boolean movieLoaded = false, done = false;

	public void init(final Shell splash) {
		splash.setSize( 440, 430);
		splash.setLayout(new GridLayout(1, true));
		final Browser browser = new Browser(splash, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final Button button = new Button(splash, SWT.PUSH);

		button.setText("Press to close"); //$NON-NLS-1$
		button.setBounds( 570 / 2 - button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x / 2,
						  splash.getSize().y - 20, splash.getSize().x, 20);

		button.setVisible(false);
		button.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent event) {
			}
			public void completed(ProgressEvent event) {
				movieLoaded = true;
				button.setVisible(true);
				button.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						done = true;
					}
				});
			}
		});

		URL url = Activator.getDefault().getBundle().getEntry("/content/debugging.html");
		
		try {
			browser.setUrl(FileLocator.toFileURL(url).toExternalForm());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //$NON-NLS-1$
		
		// make the shell bigger to see the flash better
		splash.layout(true);
		while (!movieLoaded) {
			while (splash.getDisplay().readAndDispatch());
		}
	}

	public void dispose() {
		getSplash().setActive();

		if (movieLoaded) {
			while (!done)
				getSplash().getDisplay().readAndDispatch();
		}
	}
}
