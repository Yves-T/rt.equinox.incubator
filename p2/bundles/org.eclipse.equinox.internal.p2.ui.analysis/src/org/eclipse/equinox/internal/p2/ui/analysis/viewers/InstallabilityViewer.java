package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.p2.ui.analysis.query.MissingQuery;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.metadata.IArtifactKey;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepositoryManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class InstallabilityViewer {
	protected AnalysisTreeViewer availableArtifactRepositories, availableMetadataRepositories, resultTree;
	protected TreeElement artifactRoot, metadataRoot, resultRoot;
	protected Button query;
	private GridData gdList, gdFull;
	private Display display;
	private IInstallableUnit[] ius;
	private Dictionary properties;
	private IQueryable queryable;

	public InstallabilityViewer(Composite parent, IQueryable queryable, IInstallableUnit[] ius, Dictionary properties) {
		this.ius = ius;
		this.display = parent.getDisplay();
		this.properties = properties;
		this.queryable = queryable;
		getContents(parent);
	}

	protected void getContents(Composite parent) {
		artifactRoot = new TreeElement();
		metadataRoot = new TreeElement();

		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.ProfileInstallabilityPage_MetadataRepositories);
		label.setLayoutData(getGridData(2));

		availableMetadataRepositories = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		availableMetadataRepositories.getControl().setLayoutData(getGridData(parent));
		availableMetadataRepositories.setInput(metadataRoot);

		label = new Label(parent, SWT.NONE);
		label.setText(Messages.ProfileInstallabilityPage_ArtifactRepositories);
		label.setLayoutData(getGridData(2));

		availableArtifactRepositories = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		availableArtifactRepositories.getControl().setLayoutData(getGridData(parent));
		availableArtifactRepositories.setInput(artifactRoot);

		label = new Label(parent, SWT.NONE);
		label.setText(Messages.ProfileInstallabilityPage_Results);
		label.setLayoutData(getGridData(2));

		resultTree = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		resultTree.getControl().setLayoutData(getFullGridData(parent));

		resultRoot = new TreeElement();

		resultTree.setInput(resultRoot);

		query = new Button(parent, SWT.NONE);
		query.setText(Messages.ProfileInstallabilityPage_QueryButton);
		query.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				calculate();
			}
		});

		populateRepositories(new NullProgressMonitor());
	}

	private void calculate() {
		final Object[] artifactAddresses = (Object[]) availableArtifactRepositories.getSelected();
		final Object[] metadataAddresses = (Object[]) availableMetadataRepositories.getSelected();

		// Clear existing results
		resultRoot.clear();
		resultTree.refresh();

		Job job = new Job("Determining Profile Installability") {
			protected IStatus run(IProgressMonitor monitor) {
				SubProgressMonitor subMon = new SubProgressMonitor(monitor, 15);
				try {
					subMon.beginTask("Determining Profile Installability", 15);

					Slicer slicer = new Slicer(queryable, properties, true);
					Collection iuCollection = slicer.slice(ius, new SubProgressMonitor(subMon, 10)).query(InstallableUnitQuery.ANY, new Collector(), new SubProgressMonitor(subMon, 1)).toCollection();
					subMon.worked(1);
					if (subMon.isCanceled())
						return Status.CANCEL_STATUS;

					resultRoot.addChild(getMissingArtifacts(iuCollection, artifactAddresses, new SubProgressMonitor(subMon, 2)));
					subMon.worked(2);
					if (subMon.isCanceled())
						return Status.CANCEL_STATUS;

					resultRoot.addChild(getMissingIUs(iuCollection, metadataAddresses, new SubProgressMonitor(subMon, 2)));
					subMon.worked(2);
					if (subMon.isCanceled())
						return Status.CANCEL_STATUS;

					refreshTree(resultTree);
					return Status.OK_STATUS;
				} finally {
					subMon.done();
				}
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}

	private TreeElement getMissingArtifacts(Collection iuCollection, Object[] repositories, IProgressMonitor monitor) {
		CompositeArtifactRepository repo = CompositeArtifactRepository.createMemoryComposite();

		for (int i = 0; i < repositories.length; i++)
			repo.addChild(((IArtifactRepository) repositories[i]).getLocation());

		TreeElement content = TreeElement.getIArtifactKeyTreeElement();

		Iterator iuIterator = iuCollection.iterator();
		while (iuIterator.hasNext()) {
			IInstallableUnit iu = (IInstallableUnit) iuIterator.next();
			IArtifactKey[] artifactKeys = iu.getArtifacts();
			for (int i = 0; i < artifactKeys.length; i++) {
				if (!repo.contains(artifactKeys[i]))
					content.addChild(artifactKeys[i]);
			}
		}

		if (!content.hasChildren())
			content.setText(Messages.ProfileInstallabilityPage_AllArtifactsAvailable);
		else
			content.setText(Messages.ProfileInstallabilityPage_MissingArtifacts);
		return content;
	}

	private TreeElement getMissingIUs(Collection iuCollection, Object[] repositories, IProgressMonitor monitor) {
		TreeElement content = new TreeElement();
		CompositeMetadataRepository repo = CompositeMetadataRepository.createMemoryComposite();

		for (int i = 0; i < repositories.length; i++)
			repo.addChild(((IMetadataRepository) repositories[i]).getLocation());

		content.addChildren(containsIUs(repo, iuCollection, monitor));

		if (!content.hasChildren())
			content.setText(Messages.ProfileInstallabilityPage_AllIUsAvailable);
		else
			content.setText(Messages.ProfileInstallabilityPage_MissingIUs);
		return content;
	}

	protected GridData getGridData(int horizontalSpan) {
		GridData gdButton = new GridData(GridData.FILL_HORIZONTAL);
		gdButton.horizontalSpan = horizontalSpan;

		return gdButton;
	}

	protected GridData getFullGridData(Composite parent) {
		if (gdFull != null)
			return gdFull;
		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		gdFull = new GridData(GridData.FILL_BOTH);
		gdFull.horizontalSpan = 2;
		gdFull.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);

		return gdFull;
	}

	protected GridData getGridData(Composite parent) {
		if (gdList != null)
			return gdList;
		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		gdList = new GridData(GridData.FILL_HORIZONTAL);
		gdList.horizontalSpan = 2;
		gdList.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);

		return gdList;
	}

	protected void refreshTree(final AnalysisTreeViewer viewer) {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
	}

	private Collection containsIUs(IMetadataRepository repo, Collection ius, IProgressMonitor monitor) {
		Map missingIUs = new HashMap(ius.size());
		Iterator iter = ius.iterator();
		while (iter.hasNext())
			missingIUs.put(iter.next(), Boolean.TRUE);
		MissingQuery query = new MissingQuery(missingIUs);
		repo.query(query, new Collector(), monitor);

		return query.getMissingIUs();
	}

	private void populateRepositories(IProgressMonitor monitor) {
		Job job = new Job("Loading Metadata Repositories") {
			protected IStatus run(IProgressMonitor monitor) {
				metadataRoot.addChild(new TreeElement("Loading Repositories"));
				refreshTree(availableMetadataRepositories);

				IMetadataRepositoryManager manager = (IMetadataRepositoryManager) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IMetadataRepositoryManager.class.getName());
				URI[] addresses = manager.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);

				ArrayList repoList = new ArrayList();
				for (int i = 0; i < addresses.length; i++) {
					try {
						repoList.add(manager.loadRepository(addresses[i], monitor));
					} catch (ProvisionException e) {
					}
				}

				metadataRoot.clear();
				metadataRoot.addChildren(repoList);
				refreshTree(availableMetadataRepositories);

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();

		job = new Job("Loading Artifact Repositories") {
			protected IStatus run(IProgressMonitor monitor) {
				artifactRoot.addChild(new TreeElement("Loading Repositories"));
				refreshTree(availableArtifactRepositories);

				IArtifactRepositoryManager aManager = (IArtifactRepositoryManager) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IArtifactRepositoryManager.class.getName());
				URI[] addresses = aManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);

				ArrayList repoList = new ArrayList();
				for (int i = 0; i < addresses.length; i++) {
					try {
						repoList.add(aManager.loadRepository(addresses[i], monitor));
					} catch (ProvisionException e) {
					}
				}

				artifactRoot.clear();
				artifactRoot.addChildren(repoList);
				refreshTree(availableArtifactRepositories);

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}
}
