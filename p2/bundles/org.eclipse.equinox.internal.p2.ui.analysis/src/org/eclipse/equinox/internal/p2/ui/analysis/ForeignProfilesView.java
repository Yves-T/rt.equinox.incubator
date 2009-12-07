package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.equinox.p2.engine.IProfile;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.admin.ProfilesView;
import org.eclipse.equinox.internal.p2.ui.admin.ProvAdminUIMessages;
import org.eclipse.equinox.internal.p2.ui.analysis.dialogs.AddForeignProfileDialog;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfileElement;
import org.eclipse.equinox.internal.p2.ui.analysis.wizard.RepairProfileWizard;
import org.eclipse.equinox.internal.p2.ui.model.Profiles;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ForeignProfilesView extends ProfilesView {
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

	private class RemoveProfileAction extends Action {
		RemoveProfileAction() {
			setText(ProvAdminUIMessages.ProfilesView_RemoveProfileLabel);
			setToolTipText(ProvAdminUIMessages.ProfilesView_RemoveProfileTooltip);
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
			setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		}

		public void run() {
			Object[] selections = getSelection().toArray();
			List profilesOnly = new ArrayList();
			for (int i = 0; i < selections.length; i++) {
				if (selections[i] instanceof ForeignProfileElement)
					profilesOnly.add(((ForeignProfileElement) selections[i]).getProfileId());
			}
			AnalysisActivator.getDefault().getKnownProfiles().removeProfile((String[]) profilesOnly.toArray(new String[profilesOnly.size()]));
		}
	}

	private class FixProfileAction extends Action {
		FixProfileAction() {
			setText("Repair Profile");
			setToolTipText("Repair Profile");
			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));
			setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV_DISABLED));
		}

		public void run() {
			Object[] selections = getSelection().toArray();
			List profilesOnly = new ArrayList();
			for (int i = 0; i < selections.length; i++) {
				if (selections[i] instanceof ForeignProfileElement)
					profilesOnly.add(((ForeignProfileElement) selections[i]).getProfileId());
			}
			if (profilesOnly.size() > 0)
				RepairProfileWizard.launch(AnalysisActivator.getDefault().getKnownProfiles().getProfile((String) profilesOnly.get(0)), viewer.getControl().getShell());
		}
	}

	protected FixProfileAction repairProfileAction;

	protected void makeActions() {
		super.makeActions();
		addProfileAction = new AddProfileAction();
		removeProfileAction = new RemoveProfileAction();
		repairProfileAction = new FixProfileAction();
	}

	protected Object getInput() {
		return new Profiles(AnalysisActivator.getDefault().getPolicy());
	}

	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(repairProfileAction);
	}

	protected void selectionChanged(IStructuredSelection ss) {
		super.selectionChanged(ss);
		Object[] selectionArray = ss.toArray();
		if (selectionArray.length > 0) {
			removeProfileAction.setEnabled(true);
			for (int i = 0; i < selectionArray.length; i++) {
				IProfile profile = (IProfile) ProvUI.getAdapter(selectionArray[i], IProfile.class);
				if (profile == null) {
					repairProfileAction.setEnabled(false);
					break;
				}
			}
		}
	}
}
