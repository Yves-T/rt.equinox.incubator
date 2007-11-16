package org.eclipse.equinox.security.sample;

import org.eclipse.equinox.security.view.SecurityIcon;
import org.eclipse.jface.action.*;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class AuthActionBarAdvisor extends ActionBarAdvisor {

	private IAction reloadAction, importAction, exportAction, prefAction, quitAction;

	public AuthActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	public void makeActions(IWorkbenchWindow window) {
		reloadAction = new ReloadAction("Reload");
		importAction = ActionFactory.IMPORT.create(window);
		exportAction = ActionFactory.EXPORT.create(window);
		prefAction = ActionFactory.PREFERENCES.create(window);
		quitAction = ActionFactory.QUIT.create(window);
	}

	protected void fillMenuBar(IMenuManager menuBar) {

		menuBar.add(createFileMenu());
	}

	private MenuManager createFileMenu() {

		MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		menu.add(new Separator());
		menu.add(new GroupMarker("loginActions"));
		menu.add(new GroupMarker("logoutActions"));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(reloadAction);
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
		manager.add(new SecurityIcon("1"));
	}
}
