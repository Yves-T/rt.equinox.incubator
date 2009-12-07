package org.eclipse.equinox.internal.p2.ui.analysis.wizard;

import org.eclipse.equinox.p2.engine.IProfile;

import java.util.Hashtable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class IUWizardPage extends WizardPage {

	private IProfile profile;
	private IInstallableUnit[] ius;

	protected IUWizardPage(String pageName, IProfile profile) {
		super(pageName);
		this.profile = profile;
	}

	public IUWizardPage(String pageName, String title, ImageDescriptor titleImage, IProfile profile) {
		super(pageName, title, titleImage);
		this.profile = profile;
	}

	public void createControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		FillLayout layout = new FillLayout();
		sashForm.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		sashForm.setLayoutData(data);
		initializeDialogUnits(sashForm);

		GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		GridData gdFull = new GridData(GridData.FILL_BOTH);
		gdFull.horizontalSpan = 2;
		gdFull.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 5);

		AnalysisTreeViewer viewer = new AnalysisTreeViewer(sashForm, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getControl().setLayoutData(gdFull);
		viewer.setInput(getInput());
		setControl(sashForm);
	}

	public boolean canFlipToNextPage() {
		return ius.length > 0;
	}

	public boolean isPageComplete() {
		return ius.length > 0;
	}

	public boolean canComplete() {
		return ius.length > 0;
	}

	private TreeElement getInput() {
		TreeElement root = new TreeElement();

		ius = AnalysisHelper.satisfyRequirements(((MissingRequirementWizardPage) getPreviousPage()).getRequirements(), new Hashtable(profile.getProperties()), new NullProgressMonitor());
		for (int i = 0; i < ius.length; i++)
			root.addChild(ius[i]);

		if (ius.length == 0) {
			root.addChild(new TreeElement("No suitable installable units located"));
		}

		return root;
	}

	public IInstallableUnit[] getRootIUs() {
		return ius;
	}

	public String getDescription() {
		return "Installable units chosen to fullfill the profile's unsatisified requirements";
	}

	public String getTitle() {
		return "InstallableUnits Selected";
	}

	public Image getImage() {
		return ProvUIImages.getImage(ProvUIImages.WIZARD_BANNER_REVERT);
	}
}
