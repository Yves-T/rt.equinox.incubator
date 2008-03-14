package org.eclipse.equinox.security.sample;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class InstallMyTrustEngine implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		workbenchWindow = window;
	}

	public void run(IAction action) {
		System.err.println("engine run");
		if (AuthAppPlugin.InstallTrustEngine())
			MessageDialog.openInformation(workbenchWindow.getShell(), "Equinox Security Sample", "Installed custom trust engine");

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
