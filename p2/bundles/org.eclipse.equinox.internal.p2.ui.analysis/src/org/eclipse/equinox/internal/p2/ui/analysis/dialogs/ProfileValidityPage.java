package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.equinox.internal.p2.ui.analysis.viewers.ProfileTreeViewer;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ProfileValidityPage extends AbstractAnalysisPropertyPage {

	protected void getContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Results");
		label.setLayoutData(getGridData(2));

		new ProfileTreeViewer(parent, getProfile(), new IInstallableUnit[] {getIU()}, "Valid Profile");
	}
}
