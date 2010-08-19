package org.eclipse.equinox.internal.p2.ui.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.admin.ProfilesView;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.analysis.model.RequirementElement;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisLabelProvider;
import org.eclipse.equinox.internal.p2.ui.model.ProvElement;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.expression.IExpression;
import org.eclipse.equinox.p2.query.CollectionResult;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class IUPropertiesView extends ProfilesView {
	private IUProperties input;

	private class FocusAction extends Action {
		FocusAction() {
			setText("Focus on");
			setToolTipText("Focus on");
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		}

		public void run() {
			IUElement element = input.getElement();
			if (element != null) {
				QueryableFilterSelectionDialog dialog = new QueryableFilterSelectionDialog(viewer.getControl().getShell(), (IQueryable<IInstallableUnit>) element.getQueryable());
				dialog.setBlockOnOpen(true);
				dialog.open();
				if (dialog.getFirstResult() != null) {
					input.setIUElement(new IUElement(null, (IQueryable<IInstallableUnit>) element.getQueryable(), (IInstallableUnit) dialog.getFirstResult()));
				}
			}
		}
	}

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
		manager.add(focusAction);
		focusAction.setEnabled(false);
	}

	protected void makeActions() {
		super.makeActions();
		focusAction = new FocusAction();
	}

	private Action focusAction;

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
					focusAction.setEnabled(element != null);
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
			RequirementsFullfilledQuery query = new RequirementsFullfilledQuery(element.getIU());
			queryable.query(query, new NullProgressMonitor());

			Collection<RequirementElement> children = new ArrayList<RequirementElement>(query.getMatchingRequirements().size());
			for (IRequirement requirement : query.getMatchingRequirements())
				children.add(new RequirementElement(this, requirement));

			return children.toArray();
		}

		public String getLabel(Object o) {
			return element.getLabel(o);
		}
	}

	private static class RequirementsFullfilledQuery implements IQuery<IInstallableUnit> {
		private Collection<IRequirement> matchingRequirements = new HashSet<IRequirement>();
		private IInstallableUnit iu;

		public RequirementsFullfilledQuery(IInstallableUnit iu) {
			this.iu = iu;
		}

		public IQueryResult<IInstallableUnit> perform(Iterator<IInstallableUnit> iterator) {
			Collection<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
			while (iterator.hasNext()) {
				IInstallableUnit candidateIU = iterator.next();
				for (IRequirement requirement : candidateIU.getRequirements()) {
					if (requirement.isMatch(iu)) {
						ius.add(candidateIU);
						matchingRequirements.add(requirement);
					}
				}
			}
			return new CollectionResult<IInstallableUnit>(ius);
		}

		public IExpression getExpression() {
			return null;
		}

		Collection<IRequirement> getMatchingRequirements() {
			return matchingRequirements;
		}
	}
}