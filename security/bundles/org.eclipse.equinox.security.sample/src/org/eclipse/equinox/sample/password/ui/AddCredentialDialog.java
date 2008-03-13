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
package org.eclipse.equinox.sample.password.ui;

import org.eclipse.equinox.sample.password.model.UserCredential;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class AddCredentialDialog extends TitleAreaDialog {

	private Text serverText;
	private Text userNameText;
	private Text passwordText;
	private UserCredential userCredential;

	public AddCredentialDialog(Shell parentShell) {
		super(parentShell);
	}

	protected Control createContents(Composite parent) {
		return super.createContents(parent);
	}

	protected Control createDialogArea(Composite parent) {
		setTitle("Add New Credential");
		setMessage("Enter all fields ");

		Composite composite = new Composite(parent, SWT.None);
		composite.setLayout(new GridLayout(2, false));

		// create the ui components
		Label serverLabel = new Label(composite, SWT.None);
		serverLabel.setText("Server:");
		serverText = new Text(composite, SWT.None);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(serverText);

		Label userNameLabel = new Label(composite, SWT.None);
		userNameLabel.setText("UserName:");
		userNameText = new Text(composite, SWT.None);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(userNameText);

		Label passwordLabel = new Label(composite, SWT.None);
		passwordLabel.setText("Password:");
		passwordText = new Text(composite, SWT.None);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(passwordText);

		return composite;
	}

	protected void okPressed() {
		userCredential = new UserCredential(serverText.getText().trim(), userNameText.getText().trim(), passwordText.getText());
		super.okPressed();
	}

	public UserCredential getUserCredential() {
		return userCredential;
	}

}
