package org.eclipse.equinox.internal.p2.ui.analysis;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.ProvUIProvisioningListener;
import org.eclipse.equinox.internal.p2.ui.admin.ProfilesView;
import org.eclipse.equinox.internal.p2.ui.admin.ProvAdminUIMessages;
import org.eclipse.equinox.internal.p2.ui.admin.dialogs.AddMetadataRepositoryDialog;
import org.eclipse.equinox.internal.p2.ui.analysis.dialogs.AddForeignProfileDialog;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfileElement;
import org.eclipse.equinox.internal.p2.ui.model.MetadataRepositoryElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.internal.p2.ui.model.RootElement;
import org.eclipse.equinox.internal.p2.ui.viewers.StructuredViewerProvisioningListener;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;

public class IUQueryablesView extends ProfilesView {
	private class AddProfileAction extends Action {
		AddProfileAction() {
			setText(Messages.ProfilesView2_AddProfileText);
			setToolTipText(Messages.ProfilesView2_AddProfileToolTip);
			setImageDescriptor(ProvUIImages.getImageDescriptor(ProvUIImages.IMG_PROFILE));
		}

		public void run() {
			new AddForeignProfileDialog(viewer.getControl().getShell()).open();
		}
	}

	private class AddRepositoryAction extends Action {
		AddRepositoryAction() {
			setText(Messages.ProfilesView2_AddRepositoryText);
			setToolTipText(Messages.ProfilesView2_AddRepositoryToolTip);
			setImageDescriptor(ProvUIImages.getImageDescriptor(ProvUIImages.IMG_METADATA_REPOSITORY));
		}

		public void run() {
			new AddMetadataRepositoryDialog(viewer.getControl().getShell(), getProvisioningUI()).open();
		}
	}

	private class RemoveAction extends Action {
		RemoveAction() {
			setText(ProvAdminUIMessages.ProfilesView_RemoveProfileLabel);
			setToolTipText(ProvAdminUIMessages.ProfilesView_RemoveProfileTooltip);
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
			setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		}

		public void run() {
			List<String> profilesOnly = new ArrayList<String>();
			for (Object selection : getSelection().toArray()) {
				if (selection instanceof ProfileElement)
					profilesOnly.add(((ProfileElement) selection).getProfileId());
				else if (selection instanceof MetadataRepositoryElement) {
					AnalysisHelper.getMetadataRepositoryManager().removeRepository(((MetadataRepositoryElement) selection).getLocation());
				}
			}
			AnalysisActivator.getDefault().getKnownProfiles().removeProfile(profilesOnly.toArray(new String[profilesOnly.size()]));
		}
	}

	private StructuredViewerProvisioningListener listener;

	protected void addListeners() {
		super.addListeners();
		listener = new StructuredViewerProvisioningListener(getClass().getName(), viewer, ProvUIProvisioningListener.PROV_EVENT_METADATA_REPOSITORY);
		ProvUI.addProvisioningListener(listener);
	}

	private Action addRepositoryAction, addProfileAction, propertiesAction, removeProfileAction;

	protected void makeActions() {
		propertiesAction = new PropertyDialogAction(this.getSite(), viewer);
		addProfileAction = new AddProfileAction();
		addRepositoryAction = new AddRepositoryAction();
		removeProfileAction = new RemoveAction();
		super.makeActions();
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
	}

	protected IAction getDoubleClickAction() {
		return propertiesAction;
	}

	protected Object getInput() {
		return new IUQueryables(ProvisioningUI.getDefaultUI());
	}

	protected void configureViewer(TreeViewer treeViewer) {
		super.configureViewer(treeViewer);
		getSite().setSelectionProvider(treeViewer);
	}

	protected void selectionChanged(IStructuredSelection ss) {
		propertiesAction.setEnabled(false);
		removeProfileAction.setEnabled(false);
		if (ss.size() == 1 && (ss.getFirstElement() instanceof ForeignProfileElement) || ss.getFirstElement() instanceof MetadataRepositoryElement) {
			removeProfileAction.setEnabled(true);
		}
		Object[] selectionArray = ss.toArray();
		if (selectionArray.length > 0) {
			propertiesAction.setEnabled(true);
		}
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(addProfileAction);
		manager.add(addRepositoryAction);
		manager.add(removeProfileAction);
		manager.add(propertiesAction);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(addProfileAction);
		manager.add(addRepositoryAction);
		if (removeProfileAction.isEnabled()) {
			manager.add(removeProfileAction);
		}
		if (propertiesAction.isEnabled()) {
			manager.add(propertiesAction);
		}
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(addProfileAction);
		manager.add(addRepositoryAction);
		manager.add(removeProfileAction);
	}

	private static class IUQueryables extends RootElement {

		@SuppressWarnings({"rawtypes", "unchecked"})
		public IUQueryables(ProvisioningUI ui) {
			super(ui);
			Collection queries = new ArrayList();
			queries.add(AnalysisActivator.getDefault().getKnownProfiles());
			queries.add(new MetadataRepositoryLocations());
			setQueryable(QueryUtil.compoundQueryable(queries));
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		public String getLabel(Object o) {
			return "IUs";
		}

		@Override
		protected int getDefaultQueryType() {
			return AnalysisQueryProvider.IU_QUERYABLES;
		}

	}

	private static class MetadataRepositoryLocations implements IQueryable<URI> {
		public IQueryResult<URI> query(IQuery<URI> query, IProgressMonitor monitor) {
			return query.perform(Arrays.asList(AnalysisHelper.getMetadataRepositoryManager().getKnownRepositories(IRepositoryManager.REPOSITORIES_ALL)).iterator());
		}
	}
}