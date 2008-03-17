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
package org.eclipse.equinox.security.sample;

import org.eclipse.equinox.security.ui.actions.SecurityContributionItemFactory;
import org.eclipse.jface.action.*;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class AuthActionBarAdvisor extends ActionBarAdvisor {

	private static final String FILE = "File"; //$NON-NLS-1$

	private static final String LOGOUT_ACTIONS = "logoutActions"; //$NON-NLS-1$
	private static final String LOGIN_ACTIONS = "loginActions"; //$NON-NLS-1$

	private IAction perspectiveAction, importAction, exportAction, prefAction, quitAction;
	private IWorkbenchWindow workbenchWindow;

	public AuthActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	public void makeActions(IWorkbenchWindow window) {
		this.workbenchWindow = window;
		perspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(workbenchWindow);
		importAction = ActionFactory.IMPORT.create(workbenchWindow);
		exportAction = ActionFactory.EXPORT.create(workbenchWindow);
		prefAction = ActionFactory.PREFERENCES.create(workbenchWindow);
		quitAction = ActionFactory.QUIT.create(workbenchWindow);
	}

	protected void fillMenuBar(IMenuManager menuBar) {

		menuBar.add(createFileMenu());
	}

	private MenuManager createFileMenu() {

		MenuManager menu = new MenuManager(FILE, IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		menu.add(new Separator());
		menu.add(perspectiveAction);
		menu.add(new GroupMarker(LOGIN_ACTIONS));
		menu.add(new GroupMarker(LOGOUT_ACTIONS));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(prefAction);
		menu.add(new Separator());
		menu.add(importAction);
		menu.add(exportAction);
		menu.add(new Separator());
		menu.add(quitAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	protected void fillStatusLine(IStatusLineManager manager) {
		manager.add(SecurityContributionItemFactory.SECURITY_STATUS.create(workbenchWindow));
	}
}
