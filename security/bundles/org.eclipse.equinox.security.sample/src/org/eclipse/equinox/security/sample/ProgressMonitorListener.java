/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import org.eclipse.equinox.security.auth.events.ILoginListener;
import org.eclipse.swt.widgets.Display;

// Copied from AbstractLoginDialog
public class ProgressMonitorListener implements ILoginListener {

	public void onLoginStart(Subject subject) {
		final Display display = Display.getDefault();
		if (display == null)
			return;
		display.beep();
		display.syncExec(new Runnable() {
			public void run() {
				//				IProgressMonitor progress = getProgressMonitor();
				//				if (null != progress) {
				//					progress.beginTask("Login", null), IProgressMonitor.UNKNOWN);
				//					progress.worked(IProgressMonitor.UNKNOWN);
				//				}
			}
		});
	}

	public void onLoginFinish(Subject subject, final LoginException loginException) {
		final Display display = Display.getDefault();
		if (display == null)
			return;
		display.beep();
		display.syncExec(new Runnable() {
			public void run() {
				//				if (loginException != null) {
				//					IProgressMonitor progress = getProgressMonitor();
				//					if (null != progress) {
				//						progress.done();
				//					}
				//					handleLoginException(loginException);
				//				} else {
				//					IProgressMonitor progress = getProgressMonitor();
				//					if (null != progress) {
				//						progress.done();
				//					}
				//					close();
				//				}
			}
		});
	}
}
