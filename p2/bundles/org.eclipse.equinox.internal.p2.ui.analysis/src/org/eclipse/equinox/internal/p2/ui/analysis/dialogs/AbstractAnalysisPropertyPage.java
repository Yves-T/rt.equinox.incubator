package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.model.IIUElement;
import org.eclipse.equinox.internal.p2.ui.model.InstalledIUElement;
import org.eclipse.equinox.internal.p2.ui.model.ProfileElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PropertyPage;

public abstract class AbstractAnalysisPropertyPage extends PropertyPage {
	protected Display display;
	private GridData gdList, gdFull;

	protected Control createContents(Composite parent) {
		display = parent.getDisplay();
		noDefaultAndApplyButton();

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.widthHint = 350;
		composite.setLayoutData(data);

		getContents(composite);

		return composite;
	}

	protected IProfile getProfile() {
		IAdaptable element = getElement();
		if (element instanceof ProfileElement)
			return (IProfile) ((ProfileElement) element).getQueryable();
		else if (element instanceof InstalledIUElement) {
			String profileId = ((InstalledIUElement) element).getProfileId();
			IProfile profile = AnalysisActivator.getDefault().getKnownProfiles().getProfile(profileId);
			if (profile != null)
				return profile;

			IProfileRegistry profileRegistry = (IProfileRegistry) AnalysisActivator.getDefault().getAgent().getService(IProfileRegistry.SERVICE_NAME);
			if (profileRegistry != null)
				return profileRegistry.getProfile(profileId);
		} else if (element instanceof IUElement)
			return ((IUElement) element).getProfile();
		return null;
	}

	protected IInstallableUnit getIU() {
		if (getElement() instanceof IIUElement)
			return ((IIUElement) getElement()).getIU();
		return null;
	}

	protected GridData getGridData(int horizontalSpan) {
		GridData gdButton = new GridData(GridData.FILL_HORIZONTAL);
		gdButton.horizontalSpan = horizontalSpan;

		return gdButton;
	}

	protected GridData getFullGridData(Composite parent) {
		if (gdFull != null)
			return gdFull;
		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		gdFull = new GridData(GridData.FILL_BOTH);
		gdFull.horizontalSpan = 2;
		gdFull.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);

		return gdFull;
	}

	protected GridData getGridData(Composite parent) {
		if (gdList != null)
			return gdList;
		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		gdList = new GridData(GridData.FILL_HORIZONTAL);
		gdList.horizontalSpan = 2;
		gdList.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);

		return gdList;
	}

	protected abstract void getContents(Composite parent);

	protected void refreshTree(final AnalysisTreeViewer viewer) {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
	}
}
