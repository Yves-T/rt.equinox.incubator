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
package org.eclipse.equinox.security.sample.keystore.ui;

import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.security.sample.keystore.nls.SampleMessages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class KeyStoreLoginDialog extends AbstractLoginDialog {

	Display display = Display.getCurrent();

	String storePassword;
	String confirmPassword;
	Text passwordText;
	Text confirmText;

	public KeyStoreLoginDialog() {
		super(null);
	}

	protected Point getInitialSize() {
		return new Point(380, 280);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Platform.getProduct().getName());
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
		Composite composite = new Composite(dialogarea, SWT.NONE);

		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 0;
		formLayout.marginWidth = 0;
		composite.setLayout(formLayout);

		Label promptLabel = new Label(composite, SWT.NONE);
		promptLabel.setText(((TextOutputCallback) getCallbacks()[0]).getMessage());

		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(0, 10);
		formData.right = new FormAttachment(100, 0);
		formData.width = 360;
		promptLabel.setLayoutData(formData);

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText(((PasswordCallback) getCallbacks()[1]).getPrompt());

		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(promptLabel, 10);
		//formData.right = new FormAttachment(100, -10);
		passwordLabel.setLayoutData(formData);

		passwordText = new Text(composite, SWT.BORDER);
		passwordText.setEchoChar('*');
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text theText = (Text) e.widget;
				storePassword = theText.getText();
			}
		});

		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(passwordLabel, 2);
		formData.right = new FormAttachment(100, -10);
		//formData.width = 200;
		passwordText.setLayoutData(formData);

		if (getCallbacks().length > 2) {
			Label confirmLabel = new Label(composite, SWT.NONE);
			confirmLabel.setText(((PasswordCallback) getCallbacks()[2]).getPrompt());

			formData = new FormData();
			formData.left = new FormAttachment(0, 10);
			formData.top = new FormAttachment(passwordText, 10);
			//formData.right = new FormAttachment( 100, 10);
			confirmLabel.setLayoutData(formData);

			confirmText = new Text(composite, SWT.BORDER);
			confirmText.setEchoChar('*');
			confirmText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					Text theText = (Text) e.widget;
					confirmPassword = theText.getText();
				}
			});

			formData = new FormData();
			formData.left = new FormAttachment(0, 10);
			formData.top = new FormAttachment(confirmLabel, 2);
			formData.right = new FormAttachment(100, -10);
			confirmText.setLayoutData(formData);
		}

		//createButtonBar( composite);

		//formData = new FormData();
		//formData.left = new FormAttachment(0, 0);
		//formData.top = new FormAttachment(confirmLabel);
		//formData.bottom = new FormAttachment(100, 10);

		//getButtonBar().setLayoutData(formData);

		return composite;
	}

	public IProgressMonitor getProgressMonitor() {
		// TODO Auto-generated method stub
		return null;
	}

	public void handleLoginException(LoginException le) {
		// TODO Auto-generated method stub
	}

	public void internalHandle() {
		Callback[] cb = getCallbacks();
		((PasswordCallback) cb[1]).setPassword(storePassword.toCharArray());
		if (getCallbacks().length > 2) {
			((PasswordCallback) cb[2]).setPassword(confirmPassword.toCharArray());
		}
	}
}
