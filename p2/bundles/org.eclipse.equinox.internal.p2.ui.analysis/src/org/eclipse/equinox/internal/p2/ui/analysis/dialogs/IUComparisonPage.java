package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.util.Iterator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
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
		IQueryResult<IInstallableUnit> ius = repo.query(QueryUtil.createIUQuery(getIU().getId(), getIU().getVersion()), new NullProgressMonitor());

		int iuCount = 0;
		Iterator<IInstallableUnit> iter = ius.iterator();
		while (iter.hasNext()) {
			IInstallableUnit iu = iter.next();
			TreeElement child = AnalysisHelper.diff(getIU(), iu);
			if (child != null)
				root.addChild(child);
			iuCount++;
		}
		if (root.getChildren().length == 0)
			root.addChild("No differences found between profile and source installable unit(s)");
		else if (iuCount == 0)
			root.addChild("No source for IU lcoated");

		tree.setInput(root);
	}
}
