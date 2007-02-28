/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.examples.app.selector;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceReference;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ApplicationView extends ViewPart implements ServiceTrackerCustomizer {
	public static final String ID = "org.eclipse.equinox.app.selector.appview";

	private TableViewer viewer;
	private Action startAction;
	private Action stopAction;
	private ServiceTracker applications;

	class ApplicationContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent){
			return (Object[]) parent;
		}
	}
	class ApplicationLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			switch (index) {
			case 0:
				return ((ApplicationInfo) obj).getAppName();
			case 1:
				return ((ApplicationInfo) obj).isEnabled() ? "yes" : "no";
			case 2:
				return ((ApplicationInfo) obj).getState();
			default:
				return null;
			}
		}
		public Image getColumnImage(Object obj, int index) {
			if (index == 0)
				return getImage(obj);
			return null;
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ApplicationView() {
		applications = new ServiceTracker(Activator.context, ApplicationDescriptor.class.getName(), this);
		applications.open();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		TableViewer newViewer = new TableViewer(parent, SWT.BORDER|SWT.FULL_SELECTION);
		newViewer.setContentProvider(new ApplicationContentProvider());
		newViewer.setLabelProvider(new ApplicationLabelProvider());
		newViewer.setSorter(new NameSorter());
		newViewer.getTable().setLinesVisible(true);
		newViewer.getTable().setHeaderVisible(true);
		TableColumn column = new TableColumn(newViewer.getTable(),SWT.NONE);
		column.setWidth(150);
		column.setText("Application");
		
		column = new TableColumn(newViewer.getTable(),SWT.NONE);
		column.setWidth(75);
		column.setText("Enabled");

		column = new TableColumn(newViewer.getTable(),SWT.NONE);
		column.setWidth(75);
		column.setText("State");
		newViewer.setInput(applications.getServices());
		viewer = newViewer;
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = viewer.getSelection();
				ApplicationInfo appInfo = (ApplicationInfo) ((IStructuredSelection) selection)
						.getFirstElement();
				startAction.setEnabled(appInfo.isEnabled());
				stopAction.setEnabled(!appInfo.isEnabled());
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ApplicationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(startAction);
		manager.add(new Separator());
		manager.add(stopAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(startAction);
		manager.add(stopAction);
	}

	private void makeActions() {
		startAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				ApplicationInfo appInfo = (ApplicationInfo) ((IStructuredSelection)selection).getFirstElement();
				appInfo.launch();
			}
		};
		startAction.setText("Start");
		startAction.setToolTipText("Start Application");
		startAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		stopAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				ApplicationInfo appInfo = (ApplicationInfo) ((IStructuredSelection)selection).getFirstElement();
				appInfo.destroy();
			}
		};
		stopAction.setText("Stop");
		stopAction.setToolTipText("Stop Application");
		stopAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public Object[] createModel() {
		return applications.getServices();
	}

	@Override
	public void dispose() {
		viewer = null;
		Object[] applicationInfos = applications.getServices();
		if (applicationInfos != null) {
			for (int i = 0; i < applicationInfos.length; i++)
				((ApplicationInfo) applicationInfos[i]).close();
		}
		super.dispose();
	}

	public Object addingService(ServiceReference reference) {
		if (!"any.thread".equals(reference.getProperty("eclipse.application.type")))
			return null;
			
		final ApplicationInfo info = new ApplicationInfo(reference, this);
		final TableViewer updateViewer = getViewer();
		if (updateViewer != null) {
			if (PlatformUI.getWorkbench().getDisplay().isDisposed())
				return info;
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					updateViewer.add(info);
				}
			});
		}
		return info;
	}

	public void modifiedService(ServiceReference reference, final Object service) {
		final TableViewer updateViewer = getViewer();
		if (updateViewer == null)
			return;
		if (PlatformUI.getWorkbench().getDisplay().isDisposed())
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateViewer.update(service, null);
			}
		});
	}

	public void removedService(ServiceReference reference, final Object service) {
		final TableViewer updateViewer = getViewer();
		if (updateViewer == null)
			return;
		if (PlatformUI.getWorkbench().getDisplay().isDisposed())
			return;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateViewer.remove(service);
			}
		});
	}
	
	TableViewer getViewer() {
		return viewer;
	}
}