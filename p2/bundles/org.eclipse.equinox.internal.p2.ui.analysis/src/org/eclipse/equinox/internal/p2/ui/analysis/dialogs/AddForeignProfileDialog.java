package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.io.File;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/*
 * Based on org.eclipse.equinox.internal.p2.ui.admin.dialogs.AddProfileDialog
 */
public class AddForeignProfileDialog extends StatusDialog {
	ForeignProfileDialog profileDialogArea;
	private Button okButton;

	public AddForeignProfileDialog(Shell parent) {
		super(parent);
		setTitle(Messages.AddProfileDialog_Title);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		setOkEnablement(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected Control createDialogArea(Composite parent) {
		profileDialogArea = new ForeignProfileDialog(parent, new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				verifyComplete();
			}
		});
		Dialog.applyDialogFont(profileDialogArea.getComposite());
		return profileDialogArea.getComposite();
	}

	public void verifyComplete() {
		if (okButton == null)
			return;
		if (profileDialogArea.getProfilePath().equals("")) //$NON-NLS-1$
			setOkEnablement(false);
		else
			setOkEnablement(true);
	}

	protected void setOkEnablement(boolean enable) {
		if (okButton != null && !okButton.isDisposed())
			okButton.setEnabled(enable);
	}

	protected void okPressed() {
		verifyComplete();
		if (okButton.isEnabled()) {
			addProfile();
			super.okPressed();
		}
	}

	private void addProfile() {
		String profilePath = profileDialogArea.getProfilePath();
		ForeignProfile profile = new ForeignProfile(new File(profilePath));
		AnalysisActivator.getDefault().getKnownProfiles().addProfile(profile);
	}

}