package org.eclipse.equinox.internal.p2.ui.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.ui.admin.ProfilesView;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.analysis.model.RequirementElement;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisLabelProvider;
import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

public class IUPropertiesView extends ProfilesView {
	private IUProperties input;

	protected Object getInput() {
		if (input == null)
			input = new IUProperties(getProvisioningUI());
		return input;
	}

	protected void configureViewer(TreeViewer treeViewer) {
		super.configureViewer(treeViewer);
		attachSelectionListener();
	}

	protected ILabelProvider getLabelProvider() {
		return new AnalysisLabelProvider();
	}

	protected void selectionChanged(IStructuredSelection ss) {
	}

	protected void fillLocalPullDown(IMenuManager manager) {
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
	}

	private void attachSelectionListener() {
		// get the selection service from our local service locator
		ISelectionService selectionService = (ISelectionService) getSite().getService(ISelectionService.class);
		// attach the selection listener
		selectionService.addPostSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();
				if ((element == null || element instanceof IUElement) && input != null) {
					input.setIUElement((IUElement) element);
				}
			}
		});
	}

	public class IUProperties extends ProvElement {
		private IUElement element;

		public IUProperties(ProvisioningUI ui) {
			super(ui);
		}

		public IUElement getElement() {
			return element;
		}

		void setIUElement(IUElement element) {
			this.element = element;
			viewer.refresh();
		}

		@SuppressWarnings("unchecked")
		public Object[] getChildren(Object o) {
			if (element == null)
				return new Object[0];
			IQueryable<IInstallableUnit> queryable = (IQueryable<IInstallableUnit>) element.getQueryable();
			Collection<IInstallableUnit> roots = AnalysisHelper.getRoots(queryable, new NullProgressMonitor());
			roots.remove(element.getIU());
			IQueryable<IInstallableUnit> queryable2 = new QueryableArray(AnalysisHelper.subtract(queryable, new IInstallableUnit[] {element.getIU()}));

			Collection<IRequirement> missingRequirements = new HashSet<IRequirement>();
			missingRequirements.addAll(AnalysisHelper.getMissingRequirements(roots.toArray(new IInstallableUnit[roots.size()]), queryable2, (queryable instanceof IProfile) ? ((IProfile) queryable).getProperties() : Collections.EMPTY_MAP, new NullProgressMonitor()));
			Collection<RequirementElement> children = new ArrayList<RequirementElement>(missingRequirements.size());
			for (IRequirement requirement : missingRequirements)
				children.add(new RequirementElement(this, requirement));

			return children.toArray();
		}

		public String getLabel(Object o) {
			return element.getLabel(o);
		}
	}
}
