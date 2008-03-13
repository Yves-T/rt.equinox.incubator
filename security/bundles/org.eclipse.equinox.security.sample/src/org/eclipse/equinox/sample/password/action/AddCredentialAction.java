/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.sample.password.action;

import org.eclipse.equinox.sample.password.model.UserCredential;
import org.eclipse.equinox.sample.password.service.IUserCredentialService;
import org.eclipse.equinox.sample.password.ui.AddCredentialDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;

public class AddCredentialAction extends Action {

	public AddCredentialAction(String action) {
		super(action);
	}

	public void run() {
		AddCredentialDialog addDialog = new AddCredentialDialog(null);
		if (addDialog.open() == Window.OK) {
			UserCredential userCredential = addDialog.getUserCredential();

			IUserCredentialService.userCredentialService.addCredential(userCredential);

			// refresh the table with new credential
			firePropertyChange(new PropertyChangeEvent(this, "refresh", "", ""));

		}
	}
}
