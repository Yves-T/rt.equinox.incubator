/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import org.eclipse.jface.action.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.*;

public class AuthWorkbenchAdvisor extends WorkbenchAdvisor {

	public String getInitialWindowPerspectiveId() {
		return "org.eclipse.equinox.security.sample.subjectPerspective"; //$NON-NLS-1$
	}

	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		//super.preWindowOpen( configurer);
		configurer.setShowMenuBar(true);
		configurer.setInitialSize(new Point(640, 480));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		configurer.setTitle("Authenticated Application");
	}

	public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
		//super.fillActionBars(window, configurer, flags);
		if (0 != (flags & ActionBarAdvisor.FILL_MENU_BAR)) {
			fillMenuBar(window, configurer);
		}
	}

	private void fillMenuBar(IWorkbenchWindow window, IActionBarConfigurer configurer) {
		IMenuManager menuBar = configurer.getMenuManager();
		menuBar.add(createFileMenu(window));
	}

	private MenuManager createFileMenu(IWorkbenchWindow window) {

		MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		menu.add(new Separator());
		menu.add(new GroupMarker("loginActions"));
		menu.add(new GroupMarker("logoutActions"));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(ActionFactory.PREFERENCES.create(window));
		menu.add(ActionFactory.QUIT.create(window));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}
}
