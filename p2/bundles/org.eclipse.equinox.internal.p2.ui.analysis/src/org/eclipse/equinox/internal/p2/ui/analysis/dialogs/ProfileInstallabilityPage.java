package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.Hashtable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.InstallabilityViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ProfileInstallabilityPage extends AbstractAnalysisPropertyPage {
	protected AnalysisTreeViewer availableArtifactRepositories, availableMetadataRepositories, resultTree;
	protected TreeElement<?> artifactRoot, metadataRoot, resultRoot;
	protected Button query;

	protected void getContents(Composite parent) {
		new InstallabilityViewer(parent, getProfile(), AnalysisHelper.getProfileRoots(getProfile(), new NullProgressMonitor()), new Hashtable<String, String>(getProfile().getProperties()));
	}
}
