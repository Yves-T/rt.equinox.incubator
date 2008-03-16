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
package org.eclipse.equinox.sample.password.view;

import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.equinox.sample.password.action.AddCredentialAction;
import org.eclipse.equinox.sample.password.model.UserCredential;
import org.eclipse.equinox.sample.password.service.IUserCredentialService;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class UserCredentailView extends ViewPart {
	public static final String ID = "org.eclipse.equinox.security.sample.credentialView";
	private Action addCredAction;
	private Action removeCredAction;
	private Action showAction;
	private TableViewer viewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new UserCredentialContentProvider());
		viewer.setLabelProvider(new UserCredentialLabelProvider());

		Table credentialTable = viewer.getTable();
		credentialTable.setLinesVisible(true);
		credentialTable.setHeaderVisible(true);
		TableColumn tc1 = new TableColumn(credentialTable, SWT.NULL);
		tc1.setWidth(200);
		tc1.setText("URL");
		tc1.addListener(SWT.Selection, new CredentialSorter(CredentialSorter.URL));

		TableColumn tc2 = new TableColumn(credentialTable, SWT.NULL);
		tc2.setWidth(80);
		tc2.setText("UserName");
		tc2.addListener(SWT.Selection, new CredentialSorter(CredentialSorter.USERNAME));

		TableColumn tc3 = new TableColumn(credentialTable, SWT.NULL);
		tc3.setWidth(80);
		tc3.setText("Password");

		viewer.setInput(getUserCredentials());;

		// Create menu and toolbars.
		createActions();
		createToolbar();
		createContextMenu();
		hookGlobalActions();
	}

	public void createActions() {
		addCredAction = new AddCredentialAction("Add");
		addCredAction.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				refreshTable();
			}
		});

		removeCredAction = new Action("Remove") {
			public void run() {
				UserCredential userCredential = (UserCredential) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (userCredential != null) {
					IUserCredentialService.userCredentialService.removeCredential(userCredential);
					refreshTable();
				}
			}
		};
		//		deleteItemAction.setImageDescriptor(getImageDescriptor("delete.gif"));

		showAction = new Action("Show") {
			public void run() {
				UserCredential userCredential = (UserCredential) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (userCredential != null) {
					MessageDialog md = new MessageDialog(viewer.getTable().getShell(), "Password Show", null, userCredential.getPassword(), 2, new String[] {"OK"}, 1);
					md.open();
				}
			}
		};
	}

	protected void refreshTable() {
		viewer.setInput(getUserCredentials());
	}

	private void createToolbar() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		mgr.add(addCredAction);
		mgr.add(removeCredAction);
		mgr.add(showAction);
	}

	/**
	 * Create context menu.
	 */
	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu.
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Hook global actions
	 */
	private void hookGlobalActions() {
		IActionBars bars = getViewSite().getActionBars();
		bars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, removeCredAction);
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0 && removeCredAction.isEnabled()) {
					removeCredAction.run();
				}
			}
		});
	}

	private void fillContextMenu(IMenuManager mgr) {
		mgr.add(addCredAction);
		mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		mgr.add(removeCredAction);
		mgr.add(new Separator());
	}

	private UserCredential[] getUserCredentials() {
		UserCredential ucs[] = null;
		try {
			//			ISecurePreferences seurePref = Activator.getSecurePreference();
			ISecurePreferences seurePref = AuthAppPlugin.getSecurePreferences();
			String[] keys = seurePref.keys();

			// create objects
			ucs = new UserCredential[keys.length];
			for (int i = 0; i < keys.length; i++) {
				String[] fields = keys[i].split("::");
				UserCredential toAdd = new UserCredential(fields[1], fields[0], seurePref.get(keys[i], ""));
				ucs[i] = toAdd;
			}
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ucs;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

	public class CredentialSorter implements Listener {

		public static final int URL = 0;
		public static final int USERNAME = 1;

		private int sortBy;

		public CredentialSorter(int sortBy) {
			this.sortBy = sortBy;
		}

		public void handleEvent(Event e) {
			Table tableCredentials = viewer.getTable();
			TableColumn sortColumn = tableCredentials.getSortColumn();
			TableColumn currentColumn = (TableColumn) e.widget;
			int dir = tableCredentials.getSortDirection();

			if (sortColumn == currentColumn) {
				dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				tableCredentials.setSortColumn(currentColumn);
				dir = SWT.UP;
			}

			final int direction = dir;
			UserCredential[] ucs = getUserCredentials();
			Arrays.sort(ucs, new Comparator() {
				public int compare(Object arg0, Object arg1) {
					UserCredential uc1 = (UserCredential) arg0;
					UserCredential uc2 = (UserCredential) arg1;

					if (sortBy == URL) {
						String url1 = uc1.getServerURL();
						String url2 = uc2.getServerURL();
						if (direction == SWT.UP)
							return url1.compareTo(url2);
						return url2.compareTo(url1);

					} else if (sortBy == USERNAME) {
						String username1 = uc1.getUsername();
						String username2 = uc2.getUsername();
						if (direction == SWT.UP)
							return username1.compareTo(username2);
						return username2.compareTo(username1);
					}
					return 0; // should never get here ...
				}
			});

			tableCredentials.setSortDirection(dir);
			tableCredentials.clearAll();
			viewer.setInput(ucs);
		}
	}

	public class UserCredentialContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public class UserCredentialLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			UserCredential uc = (UserCredential) element;
			switch (columnIndex) {
				case 0 :
					return uc.getServerURL();
				case 1 :
					return uc.getUsername();
				case 2 :
					return "**********";
			}
			return null;
		}

	}
}