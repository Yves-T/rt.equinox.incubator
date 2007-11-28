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
package org.eclipse.equinox.security.sample;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SubjectPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		//		layout.addStandaloneView("org.eclipse.equinox.security.sample.subjectView", false, IPageLayout.TOP, IPageLayout.RATIO_MAX, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.eclipse.equinox.security.sample.subjectView", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.eclipse.pde.runtime.RegistryBrowser", IPageLayout.RIGHT, 0.5f, IPageLayout.ID_OUTLINE);
		//layout.addView("org.eclipse.ui.console.ConsoleView", IPageLayout.BOTTOM, 0.25f, IPageLayout.ID_PROBLEM_VIEW);
		layout.addPerspectiveShortcut("org.eclipse.equinox.security.sample.subjectPerspective");
		layout.addShowViewShortcut("org.eclipse.equinox.security.sample.subjectView");
	}
}
