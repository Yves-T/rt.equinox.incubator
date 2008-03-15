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
package org.eclipse.equinox.security.sample.ssl;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class ServerURLDialog extends TitleAreaDialog {

	private String serverURL;
	private Text textURL;

	public ServerURLDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		setMessage("Input your server url");

		new Label(composite, SWT.LEFT).setText("Server URL:");
		textURL = new Text(composite, SWT.LEFT | SWT.BORDER);

		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayoutFactory.swtDefaults().generateLayout(composite);

		return composite;
	}

	protected void okPressed() {
		serverURL = textURL.getText();
		super.okPressed();
	}

	public String getSeverURL() {
		return serverURL;
	}
}