package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.model.IUElement;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
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

	private IProfile profile;
	private IInstallableUnit[] iusToRemove;
	private Collection brokenIUs;
	private TreeElement listView, profileView;
	private String defaultMessage;

	public ProfileTreeViewer(Composite parent, IProfile profile, IInstallableUnit[] iusToRemove) {
		this(parent, profile, iusToRemove, null);
	}

	public ProfileTreeViewer(Composite parent, IProfile profile, IInstallableUnit[] iusToRemove, String defaultMessage) {
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
		radio[0].setText("Flat");
		radio[0].setSelection(true);
		radio[0].setEnabled(false);

		radio[1] = new Button(group, SWT.RADIO);
		radio[1].setText("Tree");
		radio[1].setEnabled(false);

	}

	private void initialize() {
		Job job = new Job("Verifying Profile") {
			protected IStatus run(IProgressMonitor monitor) {
				Collector collector = new Collector();
				IStatus status = AnalysisHelper.checkValidity(profile, iusToRemove, collector, monitor);

				if (status.isOK() || status.matches(IStatus.CANCEL)) {
					setToDefault();
					return status;
				}
				brokenIUs = collector.toCollection();
				populateList();
				setToList();
				populateTree(monitor);

				if (monitor.isCanceled())
					return new Status(IStatus.CANCEL, AnalysisActivator.PLUGIN_ID, "Job cancelled");

				addListeners();
				return Status.OK_STATUS;
			}

			private void populateTree(IProgressMonitor monitor) {
				IInstallableUnit[] roots = AnalysisHelper.getProfileRoots(profile, monitor);
				profileView = new TreeElement();
				Dictionary properties = new Hashtable(profile.getProperties());
				IQueryable queryable = new QueryableArray(AnalysisHelper.subtract(profile, iusToRemove));

				for (int i = 0; i < roots.length; i++) {
					IUElement iue = new IUElement(profileView, queryable, profile, properties, roots[i], false, true);

					profileView.addChild(iue);
					if (monitor.isCanceled())
						break;
				}
			}

			private void populateList() {
				listView = new TreeElement();
				listView.addChildren(brokenIUs);
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
					TreeElement root = new TreeElement();
					root.addChild(new TreeElement(defaultMessage));
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
