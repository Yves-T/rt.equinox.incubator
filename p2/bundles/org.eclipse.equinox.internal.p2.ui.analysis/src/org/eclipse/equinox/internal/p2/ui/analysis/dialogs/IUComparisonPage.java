package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.Iterator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IUComparisonPage extends AbstractAnalysisPropertyPage {
	private AnalysisTreeViewer tree;

	protected void getContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Comparison:");
		label.setLayoutData(getGridData(2));

		tree = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.getControl().setLayoutData(getFullGridData(parent));
		initialize();
	}

	private void initialize() {
		TreeElement root = new TreeElement();
		IMetadataRepository repo = AnalysisHelper.getMetadataRepository();
		Collector ius = repo.query(new InstallableUnitQuery(getIU().getId(), getIU().getVersion()), new Collector(), new NullProgressMonitor());

		Iterator iter = ius.iterator();
		while (iter.hasNext()) {
			IInstallableUnit iu = (IInstallableUnit) iter.next();
			TreeElement child = AnalysisHelper.diff(getIU(), iu);
			if (child != null)
				root.addChild(child);
		}
		if (root.getChildren().length == 0)
			root.addChild("No differences found between profile and source installable unit(s)");
		else if (ius.size() == 0)
			root.addChild("No source for IU lcoated");

		tree.setInput(root);
	}
}
