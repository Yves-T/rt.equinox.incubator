package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
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
	protected TreeElement<Object> artifactRoot, metadataRoot, resultRoot;
	protected Button query;
	private GridData gdList, gdFull;
	private Display display;
	private Collection<IInstallableUnit> ius;
	private IQueryable<IInstallableUnit> queryable;

	public InstallabilityViewer(Composite parent, IQueryable<IInstallableUnit> queryable, Collection<IInstallableUnit> ius) {
		this.ius = ius;
		this.display = parent.getDisplay();
		this.queryable = queryable;
		getContents(parent);
	}

	protected void getContents(Composite parent) {
		artifactRoot = new TreeElement<Object>();
		metadataRoot = new TreeElement<Object>();

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

		resultRoot = new TreeElement<Object>();

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
				SubMonitor subMon = SubMonitor.convert(monitor, "Determining Profile Installability", 15);
				try {
					Slicer slicer = new Slicer(queryable, (Map<String, String>) Collections.EMPTY_MAP, true);
					Collection<IInstallableUnit> iuCollection = slicer.slice(ius.toArray(new IInstallableUnit[ius.size()]), subMon.newChild(10)).query(QueryUtil.ALL_UNITS, subMon.newChild(1)).toSet();
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

	private TreeElement<?> getMissingArtifacts(Collection<IInstallableUnit> iuCollection, Object[] repositories, IProgressMonitor monitor) {
		CompositeArtifactRepository repo = CompositeArtifactRepository.createMemoryComposite(AnalysisActivator.getDefault().getAgent());

		for (int i = 0; i < repositories.length; i++)
			repo.addChild(((IArtifactRepository) repositories[i]).getLocation());

		TreeElement<IArtifactKey> content = TreeElement.getIArtifactKeyTreeElement();

		Iterator<IInstallableUnit> iuIterator = iuCollection.iterator();
		while (iuIterator.hasNext())
			for (IArtifactKey key : iuIterator.next().getArtifacts())
				if (!repo.contains(key))
					content.addChild(key);

		if (!content.hasChildren())
			content.setText(Messages.ProfileInstallabilityPage_AllArtifactsAvailable);
		else
			content.setText(Messages.ProfileInstallabilityPage_MissingArtifacts);
		return content;
	}

	private TreeElement<?> getMissingIUs(Collection<IInstallableUnit> iuCollection, Object[] repositories, IProgressMonitor monitor) {
		TreeElement<IInstallableUnit> content = new TreeElement<IInstallableUnit>();
		CompositeMetadataRepository repo = CompositeMetadataRepository.createMemoryComposite(AnalysisActivator.getDefault().getAgent());

		for (int i = 0; i < repositories.length; i++)
			repo.addChild(((IMetadataRepository) repositories[i]).getLocation());

		content.addAll(containsIUs(repo, iuCollection, monitor));

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

	private Collection<IInstallableUnit> containsIUs(IMetadataRepository repo, Collection<IInstallableUnit> ius, IProgressMonitor monitor) {
		Set<IInstallableUnit> missingIUs = new HashSet<IInstallableUnit>();
		missingIUs.addAll(ius);

		missingIUs.removeAll(repo.query(QueryUtil.createIUAnyQuery(), monitor).toSet());

		return missingIUs;
	}

	private void populateRepositories(IProgressMonitor monitor) {
		Job job = new Job("Loading Metadata Repositories") {
			protected IStatus run(IProgressMonitor monitor) {
				metadataRoot.addChild(new TreeElement<String>("Loading Repositories"));
				refreshTree(availableMetadataRepositories);

				IMetadataRepositoryManager manager = AnalysisHelper.getMetadataRepositoryManager();
				URI[] addresses = manager.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);

				Collection<Object> repoList = new ArrayList<Object>();
				for (int i = 0; i < addresses.length; i++) {
					try {
						repoList.add(manager.loadRepository(addresses[i], monitor));
					} catch (ProvisionException e) {
					}
				}

				metadataRoot.clear();
				metadataRoot.addAll(repoList);
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
				artifactRoot.addChild(new TreeElement<IArtifactRepository>("Loading Repositories"));
				refreshTree(availableArtifactRepositories);

				IArtifactRepositoryManager aManager = AnalysisHelper.getArtifactRepositoryManager();
				URI[] addresses = aManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);

				Collection<Object> repoList = new ArrayList<Object>();
				for (int i = 0; i < addresses.length; i++) {
					try {
						repoList.add(aManager.loadRepository(addresses[i], monitor));
					} catch (ProvisionException e) {
					}
				}

				artifactRoot.clear();
				artifactRoot.addAll(repoList);
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
