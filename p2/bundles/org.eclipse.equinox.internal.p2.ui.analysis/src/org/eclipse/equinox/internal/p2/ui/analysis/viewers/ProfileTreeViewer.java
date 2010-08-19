package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.director.SimplePlanner;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

public class ProfileTreeViewer {

	protected AnalysisTreeViewer tree;
	protected Display display;
	protected Button[] radio;
	protected GridData gdFull;

	private IQueryable<IInstallableUnit> profile;
	private IInstallableUnit[] iusToRemove;
	private Collection<IInstallableUnit> brokenIUs;
	private TreeElement<IInstallableUnit> listView;
	private TreeElement<IUElement> profileView;
	private String defaultMessage;

	public ProfileTreeViewer(Composite parent, IQueryable<IInstallableUnit> profile, IInstallableUnit[] iusToRemove) {
		this(parent, profile, iusToRemove, null);
	}

	public ProfileTreeViewer(Composite parent, IQueryable<IInstallableUnit> profile, IInstallableUnit[] iusToRemove, String defaultMessage) {
		display = parent.getDisplay();
		this.defaultMessage = defaultMessage;
		this.profile = profile;
		this.iusToRemove = iusToRemove;

		tree = new AnalysisTreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.getControl().setLayoutData(getFullGridData(parent));

		createRadioButtons(parent);

		initialize();
	}

	private void createRadioButtons(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("View");
		group.setLayoutData(getGridData(2));
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		radio = new Button[2];
		radio[0] = new Button(group, SWT.RADIO);
		radio[0].setText("Direct");
		radio[0].setSelection(true);
		radio[0].setEnabled(false);

		radio[1] = new Button(group, SWT.RADIO);
		radio[1].setText("Profile");
		radio[1].setEnabled(false);

	}

	private void initialize() {
		Job job = new Job("Verifying Profile") {
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor sub = SubMonitor.convert(monitor);
				Collection<IInstallableUnit> collection = new ArrayList<IInstallableUnit>();
				IStatus status = AnalysisHelper.checkValidity(profile, iusToRemove, collection, sub.newChild(1));

				if (status.isOK() || status.matches(IStatus.CANCEL)) {
					setToDefault();
					return status;
				}
				brokenIUs = collection;
				populateList();
				setToList();
				populateTree(sub.newChild(1));

				if (monitor.isCanceled())
					return new Status(IStatus.CANCEL, AnalysisActivator.PLUGIN_ID, "Job cancelled");

				addListeners();
				return Status.OK_STATUS;
			}

			private void populateTree(IProgressMonitor monitor) {
				Collection<IInstallableUnit> roots = AnalysisHelper.getRoots(profile, monitor);
				profileView = new TreeElement<IUElement>();

				IQueryable<IInstallableUnit> queryable = new QueryableArray(AnalysisHelper.subtract(profile, iusToRemove));

				if (monitor.isCanceled())
					return;

				for (IInstallableUnit iu : roots)
					profileView.addChild(new IUElement(profileView, queryable, profile instanceof IProfile ? SimplePlanner.createSelectionContext(((IProfile) profile).getProperties()) : Collections.EMPTY_MAP, iu, false, true));
			}

			private void populateList() {
				listView = new TreeElement<IInstallableUnit>();
				listView.addAll(brokenIUs);
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}

	private void addListeners() {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					radio[0].addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							if (((Button) event.widget).getSelection())
								setToList();
						}
					});
					radio[0].setEnabled(true);
					radio[1].addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event event) {
							if (((Button) event.widget).getSelection())
								setToProfile();
						}
					});
					radio[1].setEnabled(true);
				}
			});
	}

	private void setToDefault() {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					TreeElement<TreeElement<?>> root = new TreeElement<TreeElement<?>>();
					root.addChild(new TreeElement<TreeElement<?>>(defaultMessage));
					tree.setInput(root);
					tree.refresh();
				}
			});
	}

	private void setToList() {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					tree.setInput(listView);
					tree.refresh();
				}
			});
	}

	private void setToProfile() {
		if (display != null)
			display.asyncExec(new Runnable() {
				public void run() {
					tree.setInput(profileView);
					tree.refresh();
				}
			});
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

	protected GridData getGridData(int horizontalSpan) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = horizontalSpan;

		return gd;
	}
}
