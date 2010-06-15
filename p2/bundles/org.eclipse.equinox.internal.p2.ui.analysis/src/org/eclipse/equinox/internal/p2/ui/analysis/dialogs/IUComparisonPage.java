package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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
		Job job = new Job("Comparing IUs") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final TreeElement<Object> root = new TreeElement<Object>();
				IMetadataRepository repo = AnalysisHelper.getMetadataRepository();
				IQueryResult<IInstallableUnit> ius = repo.query(QueryUtil.createIUQuery(getIU().getId(), getIU().getVersion()), monitor);

				int iuCount = 0;
				for (IInstallableUnit iu : ius.toSet()) {
					TreeElement<TreeElement<String>> child = AnalysisHelper.diff(getIU(), iu);
					if (child != null)
						root.addChild(child);
					iuCount++;
				}
				if (root.getChildren().length == 0)
					root.addChild("No differences found between profile and source installable unit(s)");
				else if (iuCount == 0)
					root.addChild("No source for IU located");

				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						tree.setInput(root);
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
