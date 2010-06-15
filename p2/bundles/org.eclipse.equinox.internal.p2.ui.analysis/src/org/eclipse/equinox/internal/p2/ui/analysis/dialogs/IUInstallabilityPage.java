package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.InstallabilityViewer;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.swt.widgets.Composite;

public class IUInstallabilityPage extends AbstractAnalysisPropertyPage {

	protected void getContents(Composite parent) {
		Collection<IInstallableUnit> iu = new ArrayList<IInstallableUnit>(1);
		iu.add(getIU());
		new InstallabilityViewer(parent, getMetadataRepository(), iu);
	}

	private static IQueryable<IInstallableUnit> getMetadataRepository() {
		return AnalysisHelper.getMetadataRepository();
	}
}
