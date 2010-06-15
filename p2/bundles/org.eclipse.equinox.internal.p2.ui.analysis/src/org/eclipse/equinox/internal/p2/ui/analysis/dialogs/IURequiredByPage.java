package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.ProfileTreeViewer;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IURequiredByPage extends AbstractAnalysisPropertyPage {

	protected void getContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.IUAnalysisPage_RequiredBy);
		label.setLayoutData(getGridData(2));

		new ProfileTreeViewer(parent, getQueryable(), new IInstallableUnit[] {getIU()}, "Not Required");
	}
}
