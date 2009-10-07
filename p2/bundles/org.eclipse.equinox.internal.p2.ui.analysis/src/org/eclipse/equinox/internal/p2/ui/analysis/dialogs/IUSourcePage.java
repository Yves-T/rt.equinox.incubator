package org.eclipse.equinox.internal.p2.ui.analysis.dialogs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class IUSourcePage extends AbstractAnalysisPropertyPage {
	private AnalysisTreeViewer sourceList;
	private TreeElement sourceRoot;

	protected void getContents(Composite parent) {
		sourceRoot = new TreeElement();

		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.IUAnalysisPage_AvailableIn);
		label.setLayoutData(getGridData(2));

		sourceList = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		sourceList.getControl().setLayoutData(getFullGridData(parent));
		sourceList.setInput(sourceRoot);

		populateSources();
	}

	// Populate a list of available sources for this IU
	private void populateSources() {
		final IInstallableUnit iu = getIU();
		sourceRoot.addChild(new TreeElement(Messages.IUAnalysisPage_Searching));
		sourceList.refresh();

		Job job = new Job(Messages.IUAnalysisPage_LocatingSources) {

			protected IStatus run(IProgressMonitor pMonitor) {
				try {
					IMetadataRepositoryManager manager = (IMetadataRepositoryManager) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IMetadataRepositoryManager.class.getName());
					URI[] addresses = manager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_NON_SYSTEM);
					SubMonitor monitor = SubMonitor.convert(pMonitor, addresses.length);
					final ArrayList sources = new ArrayList(addresses.length);
					for (int i = 0; i < addresses.length; i++) {
						IMetadataRepository repo;
						try {
							repo = manager.loadRepository(addresses[i], monitor);
							if (!repo.query(new InstallableUnitQuery(iu.getId(), iu.getVersion()), new Collector(), monitor).isEmpty())
								sources.add(repo);
						} catch (ProvisionException e) {
							// Should be logged?
						}
						monitor.worked(1);
					}
					sourceRoot.clear();
					if (sources.isEmpty())
						sourceRoot.addChild(new TreeElement(Messages.IUAnalysisPage_NoSources));
					Iterator iter = sources.iterator();
					while (iter.hasNext())
						sourceRoot.addChild(iter.next());

					refreshTree(sourceList);

					return Status.OK_STATUS;
				} finally {
					pMonitor.done();
				}
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}
}
