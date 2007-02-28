/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.examples.app.selector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.*;
import org.osgi.service.application.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ApplicationInfo {
	private final ServiceReference appDescRef;
	private ServiceTracker handle;
	private final ApplicationView applicationView;

	public ApplicationInfo(ServiceReference appDescRef, ApplicationView applicationView) {
		this.appDescRef = appDescRef; 
		try {
			Filter handleFilter = Activator.context.createFilter("(" + ApplicationHandle.APPLICATION_DESCRIPTOR + "=" + appDescRef.getProperty(ApplicationDescriptor.APPLICATION_PID) + ")");
			handle = new ServiceTracker(Activator.context, handleFilter, new HandleTracker());
			handle.open();
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.applicationView = applicationView;
	}
	public String getAppName() {
		ApplicationDescriptor appDesc = getApplication();
		if (appDesc == null)
			return null;
		Map props = appDesc.getProperties(null);
		String name = (String) props.get(ApplicationDescriptor.APPLICATION_NAME);
		if (name == null || name.length() == 0)
			name = appDesc.getApplicationId();
		return name;
	}

	private ApplicationDescriptor getApplication() {
		return (ApplicationDescriptor) Activator.context.getService(appDescRef);
	}
	public String toString() {
		return getAppName();
	}
	public boolean isEnabled() {
		Boolean launchable = (Boolean) appDescRef.getProperty(ApplicationDescriptor.APPLICATION_LAUNCHABLE);
		Boolean locked = (Boolean) appDescRef.getProperty(ApplicationDescriptor.APPLICATION_LOCKED);
		return launchable.booleanValue() && !locked.booleanValue();
	}
	public String getState() {
		ServiceReference handleRef = handle.getServiceReference();
		if (handleRef == null)
			return "inactive";
		return ((String) handleRef.getProperty(ApplicationHandle.APPLICATION_STATE)).toLowerCase();
	}

	public void launch() {
		ApplicationDescriptor app = getApplication();
		if (app != null)
			try {
				app.launch(null);
			} catch (ApplicationException e) {
				showMessage("Error while starting application:", e);
			}
	}
	public void destroy() {
		ApplicationHandle appHandle = (ApplicationHandle) handle.getService();
		if (appHandle != null)
			appHandle.destroy();
	}
	public void showMessage(String message, Throwable t) {
		if (applicationView.getViewer() == null)
			System.out.println(message);
		if (t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			String throwableText = new String(sw.getBuffer().toString());
			message = message + '\n' + throwableText;
		}
		MessageDialog.openInformation(
			applicationView.getViewer().getControl().getShell(),
			"Sample View",
			message);
	}
	class HandleTracker implements ServiceTrackerCustomizer {
		public Object addingService(ServiceReference reference) {
			return Activator.context.getService(reference);
		}

		public void modifiedService(ServiceReference reference, Object service) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					TableViewer viewer = applicationView.getViewer();
					if (viewer != null)
						applicationView.getViewer().update(ApplicationInfo.this, null);
				}
			});
			
		}

		public void removedService(ServiceReference reference, Object service) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					TableViewer viewer = applicationView.getViewer();
					if (viewer != null)
						viewer.update(ApplicationInfo.this, null);
				}
			});
		}	
	}
	public void close() {
		destroy();
		handle.close();
	}
}
