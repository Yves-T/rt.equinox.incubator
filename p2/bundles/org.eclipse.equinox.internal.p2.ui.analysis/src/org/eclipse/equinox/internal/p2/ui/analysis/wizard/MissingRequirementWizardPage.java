package org.eclipse.equinox.internal.p2.ui.analysis.wizard;

import org.eclipse.equinox.p2.engine.IProfile;

import java.util.Hashtable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.ui.ProvUIImages;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.model.RequirementElement;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.AnalysisTreeViewer;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
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

public class MissingRequirementWizardPage extends WizardPage {
	protected IProfile profile;
	private IRequiredCapability[] req;

	public MissingRequirementWizardPage(String pageName, IProfile profile) {
		super(pageName);
		this.profile = profile;
	}

	public MissingRequirementWizardPage(String pageName, String title, ImageDescriptor titleImage, IProfile profile) {
		super(pageName, title, titleImage);
		this.profile = profile;
	}

	public boolean canFlipToNextPage() {
		return req.length > 0;
	}

	public boolean isPageComplete() {
		return req.length > 0;
	}

	public boolean canComplete() {
		return req.length > 0;
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

	private TreeElement getInput() {
		TreeElement root = new TreeElement();
		req = AnalysisHelper.getMissingRequirements(AnalysisHelper.getProfileRoots(profile, new NullProgressMonitor()), profile, new Hashtable(profile.getProperties()), new NullProgressMonitor());
		for (int i = 0; i < req.length; i++)
			root.addChild(new RequirementElement(root, req[i]));

		if (req.length == 0) {
			root.addChild(new TreeElement("All requirements satisfied"));
		}
		return root;
	}

	public IRequiredCapability[] getRequirements() {
		return req;
	}

	public String getDescription() {
		return "Unfullfilled requirements derived from the profile's root installable units";
	}

	public String getTitle() {
		return "Unsatisifed Requirements";
	}

	public Image getImage() {
		return ProvUIImages.getImage(ProvUIImages.WIZARD_BANNER_REVERT);
	}
}
