package org.eclipse.equinox.internal.p2.ui.analysis;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

public class QueryableFilterSelectionDialog extends FilteredItemsSelectionDialog {
	private Collection<IInstallableUnit> elements;
	private IQueryable<IInstallableUnit> queryable;

	public QueryableFilterSelectionDialog(Shell shell, IQueryable<IInstallableUnit> queryable) {
		super(shell, false);
		this.queryable = queryable;
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = AnalysisActivator.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = AnalysisActivator.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
			@Override
			public boolean matchItem(Object item) {
				if (item instanceof IInstallableUnit)
					return matches(((IInstallableUnit) item).getId());
				return matches(item.toString());
			}

			@Override
			public boolean isConsistentItem(Object item) {
				return true;
			}

		};
	}

	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException {
		SubMonitor mob = SubMonitor.convert(progressMonitor, "Searching", 100);
		synchronized (lock) {
			if (elements == null)
				elements = queryable.query(QueryUtil.ALL_UNITS, mob.newChild(30)).toSet();
		}
		mob.setWorkRemaining(elements.size());
		for (Iterator<IInstallableUnit> iter = elements.iterator(); iter.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			mob.worked(1);
		}
		progressMonitor.done();
	}

	@Override
	public String getElementName(Object item) {
		return item.toString();
	}

	final Object lock = new Object();
}
