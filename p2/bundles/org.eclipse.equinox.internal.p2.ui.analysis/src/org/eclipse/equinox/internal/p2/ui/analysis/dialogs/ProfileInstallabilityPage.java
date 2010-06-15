package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.InstallabilityViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ProfileInstallabilityPage extends AbstractAnalysisPropertyPage {
	protected AnalysisTreeViewer availableArtifactRepositories, availableMetadataRepositories, resultTree;
	protected TreeElement<?> artifactRoot, metadataRoot, resultRoot;
	protected Button query;

	protected void getContents(Composite parent) {
		IQueryable<IInstallableUnit> queryable = getQueryable();
		new InstallabilityViewer(parent, queryable, AnalysisHelper.getRoots(queryable, new NullProgressMonitor()));
	}
}
