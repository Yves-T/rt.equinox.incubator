/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Niefer - IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.examples.splash;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

public class Splash implements StartupMonitor, SynchronousBundleListener {
	static private Splash splash = null;
	private Display display = null;
	private Shell shell = null;
	private Image background = null;
	private boolean initialized = false;
	private Text text = null;
	private Color color = null;
	
	public Splash() {
		splash = this;
	}
	
	public static Splash getSplash() {
		return splash;
	}
	
	public Shell getShell() {
		return shell;
	}
	private void initialize() {
		initialized = true;
		
		Integer handle = Integer.getInteger("org.eclipse.equinox.launcher.splash.handle");
		if (handle == null)
			return;
		
		display = new Display();
		shell = Shell.internal_new(display, handle.intValue());
		String splashLoc = System.getProperty("org.eclipse.equinox.launcher.splash.location"); //$NON-NLS-1$
		background = loadImage(splashLoc);
		
		shell.setLayout(new FillLayout());
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setBackgroundImage(background);
		
		Composite panel = new Composite(shell, SWT.BORDER);
		panel.setLayout(new GridLayout(2, false));
		{
			Label label = new Label(panel, SWT.NONE);
			label.setText("Bundle:"); //$NON-NLS-1$
			text = new Text(panel, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		shell.layout(true);
		while(display.readAndDispatch());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.runnable.StartupMonitor#applicationRunning()
	 */
	public void applicationRunning() {
		shell.dispose();
		if(color != null)
			color.dispose();
		if(background != null)
			background.dispose();
		Activator.getDefault().getContext().removeBundleListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.runnable.StartupMonitor#update()
	 */
	public void update() {
		if( !initialized)
			initialize();
		if (display != null) {
			while (display.readAndDispatch());

		}
	}

	public void bundleChanged(BundleEvent event) {
		if (display == null || text == null || text.isDisposed())
			return;
			
		String str = event.getBundle().getSymbolicName() + " ";
		switch(event.getType()) {
			case BundleEvent.INSTALLED : str += " installed.";	break;
			case BundleEvent.STARTED   : str += " started.";	break;
			case BundleEvent.STARTING  : str += " starting.";	break;
			case BundleEvent.LAZY_ACTIVATION : str += " being lazy."; break;
		}
		final String label = str;
		display.asyncExec( new Runnable() {
			public void run() {
				text.setText(label);
			}
		});
	}
	private Image loadImage(String splashLoc) {
		Image background = null;
		if (splashLoc != null) {
			try {
				InputStream input = new BufferedInputStream(
						new FileInputStream(splashLoc));
				background = new Image(display, input);
			} catch (IOException e) {
			}
		} 
		return background;
	}

}



