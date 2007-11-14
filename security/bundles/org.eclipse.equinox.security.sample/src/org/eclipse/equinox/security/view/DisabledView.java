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

import java.util.LinkedList;
import java.util.List;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

/**
 * This view would list all the disabled bundle in the system.
 *
 */
public class DisabledView extends ViewPart {

	public static String VIEW_ID = "org.eclipse.equinox.security.sample.diabledView";

	private TreeViewer viewer;

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);

		TreeColumn bundleCol = new TreeColumn(tree, SWT.LEFT);
		bundleCol.setText("Bundle");
		bundleCol.setWidth(200);

		TreeColumn policyCol = new TreeColumn(tree, SWT.CENTER);
		policyCol.setText("Policy");
		policyCol.setWidth(200);

		TreeColumn msgCol = new TreeColumn(tree, SWT.RIGHT);
		msgCol.setText("Message");
		msgCol.setWidth(200);

		PlatformAdmin plaformAdmin = AuthAppPlugin.getPlatformAdmin();
		State state = plaformAdmin.getState(false);

		// iterate through each bundle in the state and check
		BundleDescription[] bds = state.getDisabledBundles();
		List disableBundles = new LinkedList();
		for (int i = 0; i < bds.length; i++) {
			disableBundles.add(bds[i]);
		}

		viewer.setContentProvider(new DisabledTableContentProvider(state));
		viewer.setLabelProvider(new DisabledTableLabelProvider());
		viewer.setInput(disableBundles);
	}

	public void setFocus() {

	}

	class DisabledTableContentProvider implements ITreeContentProvider {

		private State state;

		public DisabledTableContentProvider(State state) {
			this.state = state;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof BundleDescription) {
				DisabledInfo disabledInfo[] = state.getDisabledInfos((BundleDescription) parentElement);
				return disabledInfo;
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return (element instanceof BundleDescription);
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List) {
				List disabledBundles = (List) inputElement;
				return disabledBundles.toArray(new BundleDescription[disabledBundles.size()]);
			} else if (inputElement instanceof DisabledInfo[]) {
				return (DisabledInfo[]) inputElement;
			}
			return null;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class DisabledTableLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof BundleDescription) {
				BundleDescription bd = (BundleDescription) element;
				switch (columnIndex) {
					case 0 :
						return bd.getSymbolicName();
				}
			} else {
				if (element instanceof DisabledInfo) {
					DisabledInfo disabledInf = (DisabledInfo) element;
					switch (columnIndex) {
						case 1 :
							return disabledInf.getPolicyName();
						case 2 :
							return disabledInf.getMessage();
					}
				}
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}
}
