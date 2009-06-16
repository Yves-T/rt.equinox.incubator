package org.eclipse.equinox.internal.p2.afterthefact.command;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;

public class RunExampleCommand implements CommandProvider {
	public static final String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	public RunExampleCommand(BundleContext context) {
	}

	public void _runInstallExample(CommandInterpreter interpreter) {
		new org.eclipse.equinox.internal.p2.afterthefact.Install().doInstall();
	}

	public String getHelp() {
		StringBuffer help = new StringBuffer();
		help.append("---"); //$NON-NLS-1$
		help.append(NEW_LINE);
		help.append("\trunInstallExample <iu id>"); //$NON-NLS-1$
		help.append(NEW_LINE);
		return help.toString();
	}
}