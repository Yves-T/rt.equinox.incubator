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
 * @author eric
 *
 */
public class DisabledView extends ViewPart {

	public static String VIEW_ID = "org.eclipse.equinox.security.sample.diabledView";

	private TreeViewer viewer;

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText("Bundle");
		column1.setWidth(200);
		TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
		column2.setText("Policy");
		column2.setWidth(200);
		TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
		column3.setText("Message");
		column3.setWidth(200);

		PlatformAdmin plaformAdmin = AuthAppPlugin.getPlatformAdmin();
		State state = plaformAdmin.getState(false);

		// iterate through each bundle in the state and check
		BundleDescription[] bds = plaformAdmin.getState(false).getDisabledBundles();
		List disableBundles = new LinkedList();
		for (int i = 0; i < bds.length; i++) {
			disableBundles.add(bds[i]);
		}

		viewer.setContentProvider(new DisabledTableContentProvider(state));
		viewer.setLabelProvider(new DisabledTableLabelProvider());
		viewer.setInput(disableBundles);
	}

	//	public List getDisabledBundles() {
	//		PlatformAdmin plaformAdmin = AuthAppPlugin.getPlatformAdmin();
	//		State state = plaformAdmin.getState(false);
	//
	//		// iterate through each bundle in the state and check
	//		BundleDescription[] bds = plaformAdmin.getState().getDisabledBundles();
	//		List disableBundles = new LinkedList();
	//		for (int i = 0; i < bds.length; i++) {
	//			disableBundles.add(bds[i]);
	//		}
	//
	//		return disableBundles;
	//	}

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
