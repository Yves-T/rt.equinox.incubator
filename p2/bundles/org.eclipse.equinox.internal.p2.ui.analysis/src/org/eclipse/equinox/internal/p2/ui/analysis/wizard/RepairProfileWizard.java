package org.eclipse.equinox.internal.p2.ui.analysis.wizard;

import org.eclipse.equinox.p2.engine.IProvisioningPlan;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.director.DirectorActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisHelper;
import org.eclipse.equinox.internal.p2.ui.analysis.model.ForeignProfile;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitPropertyOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.Operand;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepositoryManager;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class RepairProfileWizard extends Wizard {
	IProfile profile;
	MissingRequirementWizardPage p1;
	IUWizardPage p2;

	public RepairProfileWizard(IProfile profile, IRequiredCapability[] req, IInstallableUnit[] newIUs) {
		super();
		this.profile = profile;
	}

	public void addPages() {
		// requirement page
		p1 = new MissingRequirementWizardPage("PageName", profile);
		addPage(p1);
		p2 = new IUWizardPage("PageName", profile);
		p2.setPreviousPage(p1);
		addPage(p2);
	}

	public String getWindowTitle() {
		return "Repair profile: " + profile;
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof MissingRequirementWizardPage) {
			return p2;
		}
		return null;
	}

	public boolean performFinish() {
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(p2.getRootIUs());

		IPlanner planner = (IPlanner) ServiceHelper.getService(DirectorActivator.context, IPlanner.class.getName());

		IProvisioningPlan plan = planner.getProvisioningPlan(request, getProvisioningContext(), new NullProgressMonitor());
		if (plan.getStatus().isOK() && profile instanceof ForeignProfile) {
			ForeignProfile profile = (ForeignProfile) this.profile;
			Operand[] o = plan.getOperands();
			Map side = plan.getSideEffectChanges();
			List iusToAdd = new ArrayList(o.length + side.size());
			List iusToRemove = new ArrayList();
			for (int i = 0; i < o.length; i++) {
				if (o[i] instanceof InstallableUnitOperand) {
					InstallableUnitOperand operand = (InstallableUnitOperand) o[i];
					if (operand.first() != null) {
						profile.removeInstallableUnit(operand.first());
						iusToRemove.add(operand.first());
					}
					if (operand.second() != null) {
						profile.addInstallableUnit(operand.second());
						iusToAdd.add(operand.second());
					}
				} else if (o[i] instanceof InstallableUnitPropertyOperand) {
					InstallableUnitPropertyOperand operand = (InstallableUnitPropertyOperand) o[i];
					if (operand.second() == null)
						profile.removeInstallableUnitProperty(operand.getInstallableUnit(), operand.getKey());
					else
						profile.setInstallableUnitProperty(operand.getInstallableUnit(), operand.getKey(), (String) operand.second());
				}
			}
			Iterator keys = side.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				side.get(key);
			}
			return profile.saveProfile();
		}
		return false;
	}

	private ProvisioningContext getProvisioningContext() {
		IMetadataRepositoryManager mgr = (IMetadataRepositoryManager) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IMetadataRepositoryManager.class.getName());
		ProvisioningContext context = new ProvisioningContext(mgr.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM));
		return context;
	}

	public static void launch(final IProfile profile, final Shell shell) {
		Job job = new Job("Launching Repair profile dialog") {
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor sub = SubMonitor.convert(monitor, 3);
				try {
					Dictionary properties = new Hashtable(profile.getProperties());

					sub.beginTask("Determining profile roots", 1);
					final IInstallableUnit[] profileRoots = AnalysisHelper.getProfileRoots(profile, sub.newChild(1));

					sub.beginTask("Calculating missing requirements", 1);
					final IRequiredCapability[] req = AnalysisHelper.getMissingRequirements(profileRoots, profile, properties, sub.newChild(1));

					sub.beginTask("Satisfying requirements", 1);
					final IInstallableUnit[] newIUs = AnalysisHelper.satisfyRequirements(req, properties, sub.newChild(1));

					if (shell.getDisplay() != null)
						shell.getDisplay().asyncExec(new Runnable() {
							public void run() {
								new WizardDialog(shell, new RepairProfileWizard(profile, req, newIUs)).open();
							}
						});

					return Status.OK_STATUS;
				} finally {
					sub.done();
				}
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}
}
