package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.Hashtable;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.InstallabilityViewer;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.swt.widgets.Composite;

public class IUInstallabilityPage extends AbstractAnalysisPropertyPage {

	protected void getContents(Composite parent) {
		new InstallabilityViewer(parent, AnalysisHelper.getMetadataRepository(), new IInstallableUnit[] {getIU()}, new Hashtable(getProfile().getProperties()));
	}
}
