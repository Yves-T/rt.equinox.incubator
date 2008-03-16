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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Collects a name and password using an SWT dialog
 */
public class BasicAuthHandler implements CallbackHandler {

	private class BasicAuthDialog extends TitleAreaDialog {

		String username;
		char[] password;

		public BasicAuthDialog(Shell parentShell) {
			super(parentShell);
		}

		protected Point getInitialSize() {
			return new Point(250, 150);
		}

		protected Control createContents(Composite parent) {

			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 2;
			parent.setLayout(gridLayout);

			new Label(parent, SWT.NONE).setText("Username:");

			Text usernameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
			usernameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			usernameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent event) {
					Text text = (Text) event.widget;
					username = text.getText();
				}
			});

			new Label(parent, SWT.NONE).setText("Password:");

			Text passwordText = new Text(parent, SWT.SINGLE | SWT.BORDER);
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
			return parent;
		}
	}

	private BasicAuthDialog dialog;

	public BasicAuthHandler() {
		dialog = new BasicAuthDialog(Display.getDefault().getActiveShell());
	}

	public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
		dialog.setBlockOnOpen(true);
		dialog.open();

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				((NameCallback) callbacks[i]).setName(dialog.username);
			} else if (callbacks[i] instanceof PasswordCallback) {
				((PasswordCallback) callbacks[i]).setPassword(dialog.password);
			} else {
				throw new UnsupportedCallbackException(callbacks[i]);
			}
		}
	}

}
