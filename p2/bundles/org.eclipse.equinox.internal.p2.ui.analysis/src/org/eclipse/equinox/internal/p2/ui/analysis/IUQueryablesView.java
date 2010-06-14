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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

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
			new AddMetadataRepositoryDialog(getShell(), getProvisioningUI()).open();
		}
	}

	private class RemoveProfileAction extends Action {
		RemoveProfileAction() {
			setText(ProvAdminUIMessages.ProfilesView_RemoveProfileLabel);
			setToolTipText(ProvAdminUIMessages.ProfilesView_RemoveProfileTooltip);
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
			setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		}

		public void run() {
			Object[] selections = getSelection().toArray();
			List<String> profilesOnly = new ArrayList<String>();
			for (int i = 0; i < selections.length; i++) {
				if (selections[i] instanceof ProfileElement)
					profilesOnly.add(((ProfileElement) selections[i]).getProfileId());
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

	private AddRepositoryAction addRepositoryAction;

	protected void makeActions() {
		super.makeActions();
		addProfileAction = new AddProfileAction();
		addRepositoryAction = new AddRepositoryAction();
		removeProfileAction = new RemoveProfileAction();
	}

	protected Object getInput() {
		return new IUQueryables(ProvisioningUI.getDefaultUI());
	}

	protected void selectionChanged(IStructuredSelection ss) {
		super.selectionChanged(ss);
		Object[] selectionArray = ss.toArray();
		if (selectionArray.length > 0) {
			removeProfileAction.setEnabled(true);
		}
	}

	private Shell getShell() {
		return viewer.getControl().getShell();
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		super.fillLocalPullDown(manager);
		manager.add(addRepositoryAction);
	}

	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(addRepositoryAction);
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(addRepositoryAction);
	}

	private static class IUQueryables extends RootElement {

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
