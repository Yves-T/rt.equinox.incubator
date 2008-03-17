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
package org.eclipse.equinox.security.sample.callback;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.sample.keystore.nls.SampleMessages;
import org.eclipse.equinox.security.sample.keystore.ui.AbstractLoginDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

/**
 * Collects a name and password using an SWT dialog
 */
public class BasicAuthHandler extends AbstractLoginDialog {

	private String username;
	private char[] password;

	public BasicAuthHandler() {
		super(null);
	}

	protected Point getInitialSize() {
		return new Point(350, 250);
	}

	protected Control createContents(Composite parent) {
		Control composite = super.createContents(parent);
		Button cancel = getButton(IDialogConstants.CANCEL_ID);
		cancel.setText(SampleMessages.exitButton);
		return composite;
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogarea = (Composite) super.createDialogArea(parent);
		dialogarea.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite c = new Composite(dialogarea, SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(c);

		// username UIs
		new Label(c, SWT.NONE).setText("Username:");

		Text usernameText = new Text(c, SWT.SINGLE | SWT.BORDER);
		usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		usernameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				username = text.getText();
			}
		});
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(usernameText);

		// password UIs
		new Label(c, SWT.NONE).setText("Password:");

		Text passwordText = new Text(c, SWT.SINGLE | SWT.BORDER);
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passwordText.setEchoChar('*');
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				String data = text.getText();
				if (null != data) {
					password = data.toCharArray();
				}
			}
		});
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(passwordText);
		return c;
	}

	public IProgressMonitor getProgressMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void handleLoginException(LoginException loginException) {
		// TODO Auto-generated method stub

	}

	public void internalHandle() {
		Callback[] callbacks = getCallbacks();
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				((NameCallback) callbacks[i]).setName(username);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(password);
			}
		}

	}

}
