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
package org.eclipse.equinox.security.view;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SecurityIcon extends ContributionItem {

	public SecurityIcon(String id) {
		super(id);
	}

	public void fill(Composite parent) {
		Button securityBt = new Button(parent, SWT.NONE);
		securityBt.setText("TODO");
	}

	//if there are disabled bundles that need attention, show a warning overlay
	//register a listener for bundles being disabled due to security policy
	//animate a change in color when a bundle does not meet signature requirements

	//if double-clicked and there are disabled bundles, open a dialog for managing them
	//if right-clicked, show a menu for security related stuff (e.g.: Preferences...)
}