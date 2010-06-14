package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.helpers.FileUtils;
import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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
		BundleContext context = AnalysisActivator.getDefault().getContext();
		ServiceReference agentProviderRef = context.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
		IProvisioningAgentProvider provider = null;
		if (agentProviderRef != null)
			provider = (IProvisioningAgentProvider) context.getService(agentProviderRef);
		if (provider == null)
			LogHelper.log(new Status(IStatus.ERROR, AnalysisActivator.PLUGIN_ID, ""));

		try {
			IProvisioningAgent agent = provider.createAgent(doLayout());
			agent.registerService(PROP_P2_PROFILE, "Profile");
			IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);

			for (IProfile profile : profileRegistry.getProfiles())
				AnalysisActivator.getDefault().getKnownProfiles().addProfile(profile);
		} catch (Exception e) {
			final String msg = "Unable to instantiate p2 agent at location "; //$NON-NLS-1$
			LogHelper.log(new Status(IStatus.ERROR, AnalysisActivator.PLUGIN_ID, msg, e));
		}
		//		String profilePath = profileDialogArea.getProfilePath();
		//		ForeignProfile profile = new ForeignProfile(new File(profilePath));
	}

	private URI doLayout() throws IOException {
		File location = File.createTempFile(String.valueOf("temp"), String.valueOf(System.nanoTime()));
		location.delete();
		location.mkdir();
		location.deleteOnExit();
		File profiles = new File(new File(new File(location, "org.eclipse.equinox.p2.engine"), "profileRegistry"), "Profile.profile");
		profiles.mkdirs();

		File profile = new File(profileDialogArea.getProfilePath());
		FileUtils.copy(profile, new File(profiles, profile.getName()), new File(""), true);

		return location.toURI();
	}

	static private final String PROP_P2_PROFILE = "eclipse.p2.profile"; //$NON-NLS-1$
}