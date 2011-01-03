/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.console;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.service.resolver.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.*;

/**
 * This class provides methods to execute commands from the command line.  It registers
 * itself as a CommandProvider so it can be invoked by a CommandInterpreter.  The
 * FrameworkCommandProvider registers itself with the highest ranking (Integer.MAXVALUE) so it will always be
 * called first.  Other CommandProviders should register with lower rankings.
 *
 * The commands provided by this class are:
 ---Controlling the OSGi framework---
 close - shutdown and exit
 exit - exit immediately (System.exit)
 gc - perform a garbage collection
 init - uninstall all bundles
 launch - start the Service Management Framework
 ---Controlliing Bundles---
 install <url> {s[tart]} - install and optionally start bundle from the given URL
 refresh (<id>|<location>) - refresh the packages of the specified bundles
 start (<id>|<location>) - start the specified bundle(s)
 stop (<id>|<location>) - stop the specified bundle(s)
 uninstall (<id>|<location>) - uninstall the specified bundle(s)
 update (<id>|<location>|<*>) - update the specified bundle(s)
 ---Displaying Status---
 bundle (<id>|<location>) - display details for the specified bundle(s)
 bundles - display details for all installed bundles
 headers (<id>|<location>) - print bundle headers
 packages {<pkgname>|<id>|<location>} - display imported/exported package details
 props - display System properties
 services {filter} - display registered service details
 ss - display installed bundles (short status)
 status - display installed bundles and registered services
 threads - display threads and thread groups
 ---Log Commands---
 log {(<id>|<location>)} - display log entries
 ---Extras---
 exec <command> - execute a command in a separate process and wait
 fork <command> - execute a command in a separate process
 getprop <name> -  Displays the system properties with the given name, or all of them.
 ---Controlling StartLevel---
 sl {(<id>|<location>)} - display the start level for the specified bundle, or for the framework if no bundle specified
 setfwsl <start level> - set the framework start level
 setbsl <start level> (<id>|<location>) - set the start level for the bundle(s)
 setibsl <start level> - set the initial bundle start level
 
 *
 *  There is a method for each command which is named '_'+method.  The methods are
 *  invoked by a CommandInterpreter's execute method.
 */
public class FrameworkCommandProvider implements CommandProvider, SynchronousBundleListener {

	/** The system bundle context */
	private org.osgi.framework.BundleContext context;

	/** Strings used to format other strings */
	private String tab = "\t"; //$NON-NLS-1$
	private String newline = "\r\n"; //$NON-NLS-1$

	/** this list contains the bundles known to be lazily awaiting activation */
	private final List lazyActivation = new ArrayList();

	/**
	 *  Constructor.
	 *
	 *  initialize must be called after creating this object.
	 *
	 *  @param osgi The current instance of OSGi
	 */
	public FrameworkCommandProvider(BundleContext c) {
		context = c;
	}

	/**
	 *  Initialize this CommandProvider.
	 *
	 *  Registers this object as a CommandProvider with the highest ranking possible.
	 *  Adds this object as a SynchronousBundleListener.
	 *
	 *	@return this
	 */
	public FrameworkCommandProvider intialize() {
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_RANKING, new Integer(Integer.MAX_VALUE));
		context.registerService(CommandProvider.class.getName(), this, props);

		context.addBundleListener(this);
		return this;
	}

	/**
	 Answer a string (may be as many lines as you like) with help
	 texts that explain the command.  This getHelp() method uses the 
	 ConsoleMsg class to obtain the correct NLS data to display to the user.
	 
	 @return The help string
	 */
	public String getHelp() {
		StringBuffer help = new StringBuffer(1024);
		addHeader(Messages.CONSOLE_HELP_CONTROLLING_FRAMEWORK_HEADER, help);
		//		addCommand("launch", ConsoleMsg.CONSOLE_HELP_LAUNCH_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		//		addCommand("shutdown", ConsoleMsg.CONSOLE_HELP_SHUTDOWN_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("close", Messages.CONSOLE_HELP_CLOSE_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("exit", Messages.CONSOLE_HELP_EXIT_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		//		addCommand("init", ConsoleMsg.CONSOLE_HELP_INIT_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("setprop", Messages.CONSOLE_HELP_KEYVALUE_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_SETPROP_COMMAND_DESCRIPTION, help); //$NON-NLS-1$  
		addHeader(Messages.CONSOLE_HELP_CONTROLLING_BUNDLES_HEADER, help);
		addCommand("install", Messages.CONSOLE_HELP_INSTALL_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("uninstall", Messages.CONSOLE_HELP_UNINSTALL_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("start", Messages.CONSOLE_HELP_START_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("stop", Messages.CONSOLE_HELP_STOP_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("refresh", Messages.CONSOLE_HELP_REFRESH_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("update", Messages.CONSOLE_HELP_UPDATE_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addHeader(Messages.CONSOLE_HELP_DISPLAYING_STATUS_HEADER, help);
		addCommand("status", Messages.CONSOLE_HELP_STATE_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_STATUS_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("ss", Messages.CONSOLE_HELP_STATE_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_SS_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("services", Messages.CONSOLE_HELP_FILTER_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_SERVICES_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("packages", Messages.CONSOLE_HELP_PACKAGES_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_PACKAGES_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("bundles", Messages.CONSOLE_HELP_STATE_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_BUNDLES_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("bundle", Messages.CONSOLE_HELP_IDLOCATION_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_BUNDLE_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("headers", Messages.CONSOLE_HELP_IDLOCATION_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_HEADERS_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("log", Messages.CONSOLE_HELP_IDLOCATION_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_LOG_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addHeader(Messages.CONSOLE_HELP_EXTRAS_HEADER, help);
		addCommand("exec", Messages.CONSOLE_HELP_COMMAND_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_EXEC_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("fork", Messages.CONSOLE_HELP_COMMAND_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_FORK_COMMAND_DESCRIPTION, help); //$NON-NLS-1$
		addCommand("gc", Messages.CONSOLE_HELP_GC_COMMAND_DESCRIPTION, help); //$NON-NLS-1$ 
		addCommand("getprop ", Messages.CONSOLE_HELP_GETPROP_ARGUMENT_DESCRIPTION, Messages.CONSOLE_HELP_GETPROP_COMMAND_DESCRIPTION, help);//$NON-NLS-1$
		addHeader(Messages.STARTLEVEL_HELP_HEADING, help);
		addCommand("sl", Messages.CONSOLE_HELP_OPTIONAL_IDLOCATION_ARGUMENT_DESCRIPTION, Messages.STARTLEVEL_HELP_SL, help); //$NON-NLS-1$ 
		addCommand("setfwsl", Messages.STARTLEVEL_ARGUMENT_DESCRIPTION, Messages.STARTLEVEL_HELP_SETFWSL, help); //$NON-NLS-1$ 
		addCommand("setbsl", Messages.STARTLEVEL_IDLOCATION_ARGUMENT_DESCRIPTION, Messages.STARTLEVEL_HELP_SETBSL, help); //$NON-NLS-1$ 
		addCommand("setibsl", Messages.STARTLEVEL_ARGUMENT_DESCRIPTION, Messages.STARTLEVEL_HELP_SETIBSL, help); //$NON-NLS-1$ 
		addHeader(Messages.CONSOLE_HELP_PROFILE_HEADING, help);
		addCommand("profilelog", Messages.CONSOLE_HELP_PROFILELOG_DESCRIPTION, help); //$NON-NLS-1$ 	
		return help.toString();
	}

	/** Private helper method for getHelp.  Formats the help headers. */
	private void addHeader(String header, StringBuffer help) {
		help.append("---"); //$NON-NLS-1$
		help.append(header);
		help.append("---"); //$NON-NLS-1$
		help.append(newline);
	}

	/** Private helper method for getHelp.  Formats the command descriptions. */
	private void addCommand(String command, String description, StringBuffer help) {
		help.append(tab);
		help.append(command);
		help.append(" - "); //$NON-NLS-1$
		help.append(description);
		help.append(newline);
	}

	/** Private helper method for getHelp.  Formats the command descriptions with command arguements. */
	private void addCommand(String command, String parameters, String description, StringBuffer help) {
		help.append(tab);
		help.append(command);
		help.append(" "); //$NON-NLS-1$
		help.append(parameters);
		help.append(" - "); //$NON-NLS-1$
		help.append(description);
		help.append(newline);
	}

	/**
	 *  Handle the exit command.  Exit immediately (System.exit)
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _exit(CommandInterpreter intp) throws Exception {
		intp.println();
		System.exit(0);
	}

	//	/**
	//	 *  Handle the launch command.  Start the OSGi framework.
	//	 *
	//	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	//	 */
	//	public void _launch(CommandInterpreter intp) throws Exception {
	//		osgi.launch();
	//	}
	//
	//	/**
	//	 *  Handle the shutdown command.  Shutdown the OSGi framework.
	//	 *
	//	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	//	 */
	//	public void _shutdown(CommandInterpreter intp) throws Exception {
	//		osgi.shutdown();
	//	}

	/**
	 *  Handle the start command's abbreviation.  Invoke _start()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _sta(CommandInterpreter intp) throws Exception {
		_start(intp);
	}

	/**
	 *  Handle the start command.  Start the specified bundle(s).
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _start(CommandInterpreter intp) throws Exception {
		String nextArg = intp.nextArgument();
		if (nextArg == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (nextArg != null) {
			Bundle bundle = getBundleFromToken(intp, nextArg, true);
			if (bundle != null) {
				bundle.start();
			}
			nextArg = intp.nextArgument();
		}
	}

	/**
	 *  Handle the stop command's abbreviation.  Invoke _stop()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _sto(CommandInterpreter intp) throws Exception {
		_stop(intp);
	}

	/**
	 *  Handle the stop command.  Stop the specified bundle(s).
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _stop(CommandInterpreter intp) throws Exception {
		String nextArg = intp.nextArgument();
		if (nextArg == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (nextArg != null) {
			Bundle bundle = getBundleFromToken(intp, nextArg, true);
			if (bundle != null) {
				bundle.stop();
			}
			nextArg = intp.nextArgument();
		}
	}

	/**
	 *  Handle the install command's abbreviation.  Invoke _install()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _i(CommandInterpreter intp) throws Exception {
		_install(intp);
	}

	/**
	 *  Handle the install command.  Install and optionally start bundle from the given URL\r\n"
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _install(CommandInterpreter intp) throws Exception {
		String url = intp.nextArgument();
		if (url == null) {
			intp.println(Messages.CONSOLE_NOTHING_TO_INSTALL_ERROR);
		} else {
			Bundle bundle = context.installBundle(url);
			intp.print(Messages.CONSOLE_BUNDLE_ID_MESSAGE);
			intp.println(new Long(bundle.getBundleId()));

			String nextArg = intp.nextArgument();
			if (nextArg != null) {
				String start = nextArg.toLowerCase();
				if ("start".equals(start)) { //$NON-NLS-1$
					bundle.start();
				}
			}
		}

	}

	/**
	 *  Handle the update command's abbreviation.  Invoke _update()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _up(CommandInterpreter intp) throws Exception {
		_update(intp);
	}

	/**
	 *  Handle the update command.  Update the specified bundle(s).
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _update(CommandInterpreter intp) throws Exception {
		String token = intp.nextArgument();
		if (token == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (token != null) {

			if ("*".equals(token)) { //$NON-NLS-1$
				Bundle[] bundles = context.getBundles();

				int size = bundles.length;

				if (size > 0) {
					for (int i = 0; i < size; i++) {
						Bundle bundle = bundles[i];

						if (bundle.getBundleId() != 0) {
							try {
								bundle.update();
							} catch (BundleException e) {
								intp.printStackTrace(e);
							}
						}
					}
				} else {
					intp.println(Messages.CONSOLE_NO_INSTALLED_BUNDLES_ERROR);
				}
			} else {
				Bundle bundle = getBundleFromToken(intp, token, true);
				if (bundle != null) {
					String source = intp.nextArgument();
					try {
						if (source != null) {
							bundle.update(new URL(source).openStream());
						} else {
							bundle.update();
						}
					} catch (BundleException e) {
						intp.printStackTrace(e);
					}
				}
			}
			token = intp.nextArgument();
		}
	}

	/**
	 *  Handle the uninstall command's abbreviation.  Invoke _uninstall()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _un(CommandInterpreter intp) throws Exception {
		_uninstall(intp);
	}

	/**
	 *  Handle the uninstall command.  Uninstall the specified bundle(s).
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _uninstall(CommandInterpreter intp) throws Exception {
		String nextArg = intp.nextArgument();
		if (nextArg == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (nextArg != null) {
			Bundle bundle = getBundleFromToken(intp, nextArg, true);
			if (bundle != null) {
				bundle.uninstall();
			}
			nextArg = intp.nextArgument();
		}
	}

	/**
	 *  Handle the status command's abbreviation.  Invoke _status()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _s(CommandInterpreter intp) throws Exception {
		_status(intp);
	}

	private Object[] processOption(CommandInterpreter intp) {
		String option = intp.nextArgument();
		String filteredName = null;
		int stateFilter = -1;
		if (option != null && option.equals("-s")) { //$NON-NLS-1$
			String searchedState = intp.nextArgument();
			StringTokenizer tokens = new StringTokenizer(searchedState, ","); //$NON-NLS-1$
			while (tokens.hasMoreElements()) {
				String desiredState = (String) tokens.nextElement();
				Field match = null;
				try {
					match = Bundle.class.getField(desiredState.toUpperCase());
					if (stateFilter == -1)
						stateFilter = 0;
					stateFilter |= match.getInt(match);
				} catch (NoSuchFieldException e) {
					intp.println(Messages.CONSOLE_INVALID_INPUT + ": " + desiredState); //$NON-NLS-1$
					return null;
				} catch (IllegalAccessException e) {
					intp.println(Messages.CONSOLE_INVALID_INPUT + ": " + desiredState); //$NON-NLS-1$
					return null;
				}
			}
		} else {
			filteredName = option;
		}
		String tmp = intp.nextArgument();
		if (tmp != null)
			filteredName = tmp;
		return new Object[] {filteredName, new Integer(stateFilter)};
	}

	/**
	 *  Handle the status command.  Display installed bundles and registered services.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _status(CommandInterpreter intp) throws Exception {
		Object[] options = processOption(intp);
		if (options == null)
			return;

		Bundle[] bundles = context.getBundles();
		int size = bundles.length;

		if (size == 0) {
			intp.println(Messages.CONSOLE_NO_INSTALLED_BUNDLES_ERROR);
			return;
		}
		intp.print(Messages.CONSOLE_ID);
		intp.print(tab);
		intp.println(Messages.CONSOLE_BUNDLE_LOCATION_MESSAGE);
		intp.println(Messages.CONSOLE_STATE_BUNDLE_FILE_NAME_HEADER);
		for (int i = 0; i < size; i++) {
			Bundle bundle = bundles[i];
			if (!match(bundle, (String) options[0], ((Integer) options[1]).intValue()))
				continue;
			intp.print(new Long(bundle.getBundleId()));
			intp.print(tab);
			intp.println(bundle.getLocation());
			intp.print("  "); //$NON-NLS-1$
			intp.print(getStateName(bundle));
		}

		ServiceReference[] services = context.getServiceReferences(null, null);
		if (services != null) {
			intp.println(Messages.CONSOLE_REGISTERED_SERVICES_MESSAGE);
			size = services.length;
			for (int i = 0; i < size; i++) {
				intp.println(services[i]);
			}
		}
	}

	/**
	 *  Handle the services command's abbreviation.  Invoke _services()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _se(CommandInterpreter intp) throws Exception {
		_services(intp);
	}

	/**
	 *  Handle the services command.  Display registered service details.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _services(CommandInterpreter intp) throws Exception {
		String filter = null;

		String nextArg = intp.nextArgument();
		if (nextArg != null) {
			StringBuffer buf = new StringBuffer();
			while (nextArg != null) {
				buf.append(' ');
				buf.append(nextArg);
				nextArg = intp.nextArgument();
			}
			filter = buf.toString();
		}

		ServiceReference[] services = context.getServiceReferences(null, filter);
		if (services != null) {
			int size = services.length;
			if (size > 0) {
				for (int j = 0; j < size; j++) {
					ServiceReference service = services[j];
					intp.println(service);
					intp.print("  "); //$NON-NLS-1$
					intp.print(Messages.CONSOLE_REGISTERED_BY_BUNDLE_MESSAGE);
					intp.print(" "); //$NON-NLS-1$
					intp.println(service.getBundle());
					Bundle[] users = service.getUsingBundles();
					if (users != null) {
						intp.print("  "); //$NON-NLS-1$
						intp.println(Messages.CONSOLE_BUNDLES_USING_SERVICE_MESSAGE);
						for (int k = 0; k < users.length; k++) {
							intp.print("    "); //$NON-NLS-1$
							intp.println(users[k]);
						}
					} else {
						intp.print("  "); //$NON-NLS-1$
						intp.println(Messages.CONSOLE_NO_BUNDLES_USING_SERVICE_MESSAGE);
					}
				}
				return;
			}
		}
		intp.println(Messages.CONSOLE_NO_REGISTERED_SERVICES_MESSAGE);
	}

	/**
	 *  Handle the packages command's abbreviation.  Invoke _packages()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _p(CommandInterpreter intp) throws Exception {
		_packages(intp);
	}

	/**
	 *  Handle the packages command.  Display imported/exported package details.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _packages(CommandInterpreter intp) throws Exception {
		org.osgi.framework.Bundle bundle = null;

		String token = intp.nextArgument();
		if (token != null) {
			bundle = getBundleFromToken(intp, token, false);
		}

		PackageAdmin packageAdmin = Activator.getPackageAdmin();
		ExportedPackage[] packages = null;

		if (token != null)
			packages = packageAdmin.getExportedPackages(token);
		if (packages == null)
			packages = packageAdmin.getExportedPackages(bundle);

		if (packages == null) {
			intp.println(Messages.CONSOLE_NO_EXPORTED_PACKAGES_MESSAGE);
		} else {
			for (int i = 0; i < packages.length; i++) {
				ExportedPackage pkg = packages[i];
				intp.print(pkg);

				boolean removalPending = pkg.isRemovalPending();
				if (removalPending) {
					intp.print("("); //$NON-NLS-1$
					intp.print(Messages.CONSOLE_REMOVAL_PENDING_MESSAGE);
					intp.println(")"); //$NON-NLS-1$
				}

				org.osgi.framework.Bundle exporter = pkg.getExportingBundle();
				if (exporter != null) {
					intp.print("<"); //$NON-NLS-1$
					intp.print(exporter);
					intp.println(">"); //$NON-NLS-1$

					org.osgi.framework.Bundle[] importers = pkg.getImportingBundles();
					for (int j = 0; j < importers.length; j++) {
						intp.print("  "); //$NON-NLS-1$
						intp.print(importers[j]);
						intp.print(" "); //$NON-NLS-1$
						intp.println(Messages.CONSOLE_IMPORTS_MESSAGE);
					}
				} else {
					intp.print("<"); //$NON-NLS-1$
					intp.print(Messages.CONSOLE_STALE_MESSAGE);
					intp.println(">"); //$NON-NLS-1$
				}

			}
		}

	}

	/**
	 *  Handle the bundles command.  Display details for all installed bundles.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _bundles(CommandInterpreter intp) throws Exception {
		Object[] options = processOption(intp);
		if (options == null)
			return;

		Bundle[] bundles = context.getBundles();
		int size = bundles.length;

		if (size == 0) {
			intp.println(Messages.CONSOLE_NO_INSTALLED_BUNDLES_ERROR);
			return;
		}

		for (int i = 0; i < size; i++) {
			Bundle bundle = bundles[i];
			if (!match(bundle, (String) options[0], ((Integer) options[1]).intValue()))
				continue;
			long id = bundle.getBundleId();
			intp.println(bundle);
			intp.print("  "); //$NON-NLS-1$
			intp.print(NLS.bind(Messages.CONSOLE_ID_MESSAGE, String.valueOf(id)));
			intp.print(", "); //$NON-NLS-1$
			intp.print(NLS.bind(Messages.CONSOLE_STATUS_MESSAGE, getStateName(bundle)));
			intp.println();

			ServiceReference[] services = bundle.getRegisteredServices();
			if (services != null) {
				intp.print("  "); //$NON-NLS-1$
				intp.println(Messages.CONSOLE_REGISTERED_SERVICES_MESSAGE);
				for (int j = 0; j < services.length; j++) {
					intp.print("    "); //$NON-NLS-1$
					intp.println(services[j]);
				}
			} else {
				intp.print("  "); //$NON-NLS-1$
				intp.println(Messages.CONSOLE_NO_REGISTERED_SERVICES_MESSAGE);
			}

			services = bundle.getServicesInUse();
			if (services != null) {
				intp.print("  "); //$NON-NLS-1$
				intp.println(Messages.CONSOLE_SERVICES_IN_USE_MESSAGE);
				for (int j = 0; j < services.length; j++) {
					intp.print("    "); //$NON-NLS-1$
					intp.println(services[j]);
				}
			} else {
				intp.print("  "); //$NON-NLS-1$
				intp.println(Messages.CONSOLE_NO_SERVICES_IN_USE_MESSAGE);
			}
		}
	}

	/**
	 *  Handle the bundle command's abbreviation.  Invoke _bundle()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _b(CommandInterpreter intp) throws Exception {
		_bundle(intp);
	}

	/**
	 *  Handle the bundle command.  Display details for the specified bundle(s).
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _bundle(CommandInterpreter intp) throws Exception {
		String nextArg = intp.nextArgument();
		if (nextArg == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (nextArg != null) {
			Bundle bundle = getBundleFromToken(intp, nextArg, true);
			if (bundle != null) {
				long id = bundle.getBundleId();
				intp.println(bundle);
				intp.print("  "); //$NON-NLS-1$
				intp.print(NLS.bind(Messages.CONSOLE_ID_MESSAGE, String.valueOf(id)));
				intp.print(", "); //$NON-NLS-1$
				intp.print(NLS.bind(Messages.CONSOLE_STATUS_MESSAGE, getStateName(bundle)));
				intp.println();

				ServiceReference[] services = bundle.getRegisteredServices();
				if (services != null) {
					intp.print("  "); //$NON-NLS-1$
					intp.println(Messages.CONSOLE_REGISTERED_SERVICES_MESSAGE);
					for (int j = 0; j < services.length; j++) {
						intp.print("    "); //$NON-NLS-1$
						intp.println(services[j]);
					}
				} else {
					intp.print("  "); //$NON-NLS-1$
					intp.println(Messages.CONSOLE_NO_REGISTERED_SERVICES_MESSAGE);
				}

				services = bundle.getServicesInUse();
				if (services != null) {
					intp.print("  "); //$NON-NLS-1$
					intp.println(Messages.CONSOLE_SERVICES_IN_USE_MESSAGE);
					for (int j = 0; j < services.length; j++) {
						intp.print("    "); //$NON-NLS-1$
						intp.println(services[j]);
					}
				} else {
					intp.print("  "); //$NON-NLS-1$
					intp.println(Messages.CONSOLE_NO_SERVICES_IN_USE_MESSAGE);
				}

				PlatformAdmin platAdmin = Activator.getPlatformAdmin();
				BundleDescription desc = platAdmin.getState(false).getBundle(bundle.getBundleId());
				if (desc != null) {
					boolean title = true;
					ExportPackageDescription[] exports = desc.getExportPackages();
					if (exports == null || exports.length == 0) {
						intp.print("  "); //$NON-NLS-1$
						intp.println(Messages.CONSOLE_NO_EXPORTED_PACKAGES_MESSAGE);
					} else {
						title = true;

						for (int i = 0; i < exports.length; i++) {
							if (title) {
								intp.print("  "); //$NON-NLS-1$
								intp.println(Messages.CONSOLE_EXPORTED_PACKAGES_MESSAGE);
								title = false;
							}
							intp.print("    "); //$NON-NLS-1$
							intp.print(exports[i].getName());
							intp.print("; version=\""); //$NON-NLS-1$
							intp.print(exports[i].getVersion());
							intp.print("\""); //$NON-NLS-1$
							if (desc.isRemovalPending()) {
								intp.println(Messages.CONSOLE_EXPORTED_REMOVAL_PENDING_MESSAGE);
							} else {
								intp.println(Messages.CONSOLE_EXPORTED_MESSAGE);
							}
						}

						if (title) {
							intp.print("  "); //$NON-NLS-1$
							intp.println(Messages.CONSOLE_NO_EXPORTED_PACKAGES_MESSAGE);
						}
					}
					title = true;
					if (desc != null) {
						ExportPackageDescription[] imports = desc.getContainingState().getStateHelper().getVisiblePackages(desc, StateHelper.VISIBLE_INCLUDE_EE_PACKAGES);
						title = printImportedPackages(imports, intp, title);
					}

					if (title) {
						intp.print("  "); //$NON-NLS-1$
						intp.println(Messages.CONSOLE_NO_IMPORTED_PACKAGES_MESSAGE);
					}

					PackageAdmin packageAdmin = Activator.getPackageAdmin();
					if (packageAdmin != null) {
						intp.print("  "); //$NON-NLS-1$
						if ((packageAdmin.getBundleType(bundle) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) > 0) {
							org.osgi.framework.Bundle[] hosts = packageAdmin.getHosts(bundle);
							if (hosts != null) {
								intp.println(Messages.CONSOLE_HOST_MESSAGE);
								for (int i = 0; i < hosts.length; i++) {
									intp.print("    "); //$NON-NLS-1$
									intp.println(hosts[i]);
								}
							} else {
								intp.println(Messages.CONSOLE_NO_HOST_MESSAGE);
							}
						} else {
							org.osgi.framework.Bundle[] fragments = packageAdmin.getFragments(bundle);
							if (fragments != null) {
								intp.println(Messages.CONSOLE_FRAGMENT_MESSAGE);
								for (int i = 0; i < fragments.length; i++) {
									intp.print("    "); //$NON-NLS-1$
									intp.println(fragments[i]);
								}
							} else {
								intp.println(Messages.CONSOLE_NO_FRAGMENT_MESSAGE);
							}
						}

						RequiredBundle[] requiredBundles = packageAdmin.getRequiredBundles(null);
						RequiredBundle requiredBundle = null;
						if (requiredBundles != null) {
							for (int i = 0; i < requiredBundles.length; i++) {
								if (requiredBundles[i].getBundle() == bundle) {
									requiredBundle = requiredBundles[i];
									break;
								}
							}
						}

						if (requiredBundle == null) {
							intp.print("  "); //$NON-NLS-1$
							intp.println(Messages.CONSOLE_NO_NAMED_CLASS_SPACES_MESSAGE);
						} else {
							intp.print("  "); //$NON-NLS-1$
							intp.println(Messages.CONSOLE_NAMED_CLASS_SPACE_MESSAGE);
							intp.print("    "); //$NON-NLS-1$
							intp.print(requiredBundle);
							if (requiredBundle.isRemovalPending()) {
								intp.println(Messages.CONSOLE_REMOVAL_PENDING_MESSAGE);
							} else {
								intp.println(Messages.CONSOLE_PROVIDED_MESSAGE);
							}
						}
						title = true;
						for (int i = 0; i < requiredBundles.length; i++) {
							if (requiredBundles[i] == requiredBundle)
								continue;

							org.osgi.framework.Bundle[] depBundles = requiredBundles[i].getRequiringBundles();
							if (depBundles == null)
								continue;

							for (int j = 0; j < depBundles.length; j++) {
								if (depBundles[j] == bundle) {
									if (title) {
										intp.print("  "); //$NON-NLS-1$
										intp.println(Messages.CONSOLE_REQUIRED_BUNDLES_MESSAGE);
										title = false;
									}
									intp.print("    "); //$NON-NLS-1$
									intp.print(requiredBundles[i]);

									org.osgi.framework.Bundle provider = requiredBundles[i].getBundle();
									intp.print("<"); //$NON-NLS-1$
									intp.print(provider);
									intp.println(">"); //$NON-NLS-1$
								}
							}
						}
						if (title) {
							intp.print("  "); //$NON-NLS-1$
							intp.println(Messages.CONSOLE_NO_REQUIRED_BUNDLES_MESSAGE);
						}

					}
				}
			}
			nextArg = intp.nextArgument();
		}
	}

	private boolean printImportedPackages(ExportPackageDescription[] importedPkgs, CommandInterpreter intp, boolean title) {
		for (int i = 0; i < importedPkgs.length; i++) {
			if (title) {
				intp.print("  "); //$NON-NLS-1$
				intp.println(Messages.CONSOLE_IMPORTED_PACKAGES_MESSAGE);
				title = false;
			}
			intp.print("    "); //$NON-NLS-1$
			intp.print(importedPkgs[i].getName());
			intp.print("; version=\""); //$NON-NLS-1$
			intp.print(importedPkgs[i].getVersion());
			intp.print("\""); //$NON-NLS-1$
			Bundle exporter = context.getBundle(importedPkgs[i].getSupplier().getBundleId());
			if (exporter != null) {
				intp.print("<"); //$NON-NLS-1$
				intp.print(exporter);
				intp.println(">"); //$NON-NLS-1$
			} else {
				intp.print("<"); //$NON-NLS-1$
				intp.print(Messages.CONSOLE_STALE_MESSAGE);
				intp.println(">"); //$NON-NLS-1$
			}
		}
		return title;
	}

	/**
	 *  Handle the log command's abbreviation.  Invoke _log()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _l(CommandInterpreter intp) throws Exception {
		_log(intp);
	}

	/**
	 *  Handle the log command.  Display log entries.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _log(CommandInterpreter intp) throws Exception {
		long logid = -1;
		String token = intp.nextArgument();
		if (token != null) {
			Bundle bundle = getBundleFromToken(intp, token, false);

			if (bundle == null) {
				try {
					logid = Long.parseLong(token);
				} catch (NumberFormatException e) {
					return;
				}
			} else {
				logid = bundle.getBundleId();
			}
		}

		org.osgi.framework.ServiceReference logreaderRef = context.getServiceReference("org.osgi.service.log.LogReaderService"); //$NON-NLS-1$
		if (logreaderRef != null) {
			Object logreader = context.getService(logreaderRef);
			if (logreader != null) {
				try {
					Enumeration logs = (Enumeration) (logreader.getClass().getMethod("getLog", null).invoke(logreader, null)); //$NON-NLS-1$
					ArrayList entriesList = new ArrayList();
					while (logs.hasMoreElements())
						entriesList.add(0, logs.nextElement());
					Object[] entries = entriesList.toArray();
					if (entries.length == 0)
						return;
					Class clazz = entries[0].getClass();
					Method getBundle = clazz.getMethod("getBundle", null); //$NON-NLS-1$
					Method getLevel = clazz.getMethod("getLevel", null); //$NON-NLS-1$
					Method getMessage = clazz.getMethod("getMessage", null); //$NON-NLS-1$
					Method getServiceReference = clazz.getMethod("getServiceReference", null); //$NON-NLS-1$
					Method getException = clazz.getMethod("getException", null); //$NON-NLS-1$

					for (int i = 0; i < entries.length; i++) {
						Object logentry = entries[i];
						Bundle bundle = (Bundle) getBundle.invoke(logentry, null);

						if ((logid == -1) || ((bundle != null) && (logid == bundle.getBundleId()))) {
							Integer level = (Integer) getLevel.invoke(logentry, null);
							switch (level.intValue()) {
								case 4 :
									intp.print(">"); //$NON-NLS-1$
									intp.print(Messages.CONSOLE_DEBUG_MESSAGE);
									intp.print(" "); //$NON-NLS-1$
									break;
								case 3 :
									intp.print(">"); //$NON-NLS-1$
									intp.print(Messages.CONSOLE_INFO_MESSAGE);
									intp.print(" "); //$NON-NLS-1$
									break;
								case 2 :
									intp.print(">"); //$NON-NLS-1$
									intp.print(Messages.CONSOLE_WARNING_MESSAGE);
									intp.print(" "); //$NON-NLS-1$
									break;
								case 1 :
									intp.print(">"); //$NON-NLS-1$
									intp.print(Messages.CONSOLE_ERROR_MESSAGE);
									intp.print(" "); //$NON-NLS-1$
									break;
								default :
									intp.print(">"); //$NON-NLS-1$
									intp.print(level);
									intp.print(" "); //$NON-NLS-1$
									break;
							}

							if (bundle != null) {
								intp.print("["); //$NON-NLS-1$
								intp.print(new Long(bundle.getBundleId()));
								intp.print("] "); //$NON-NLS-1$
							}

							intp.print(getMessage.invoke(logentry, null));
							intp.print(" "); //$NON-NLS-1$

							ServiceReference svcref = (ServiceReference) getServiceReference.invoke(logentry, null);
							if (svcref != null) {
								intp.print("{"); //$NON-NLS-1$
								intp.print(Constants.SERVICE_ID);
								intp.print("="); //$NON-NLS-1$
								intp.print(svcref.getProperty(Constants.SERVICE_ID).toString());
								intp.println("}"); //$NON-NLS-1$
							} else {
								if (bundle != null) {
									intp.println(bundle.getLocation());
								} else {
									intp.println();
								}
							}

							Throwable t = (Throwable) getException.invoke(logentry, null);
							if (t != null) {
								intp.printStackTrace(t);
							}
						}
					}
				} finally {
					context.ungetService(logreaderRef);
				}
				return;
			}
		}

		intp.println(Messages.CONSOLE_LOGSERVICE_NOT_REGISTERED_MESSAGE);
	}

	/**
	 *  Handle the gc command.  Perform a garbage collection.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _gc(CommandInterpreter intp) throws Exception {
		long before = Runtime.getRuntime().freeMemory();

		/* Let the finilizer finish its work and remove objects from its queue */
		System.gc(); /* asyncronous garbage collector might already run */
		System.gc(); /* to make sure it does a full gc call it twice */
		System.runFinalization();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// do nothing
		}

		long after = Runtime.getRuntime().freeMemory();
		intp.print(Messages.CONSOLE_TOTAL_MEMORY_MESSAGE);
		intp.println(String.valueOf(Runtime.getRuntime().totalMemory()));
		intp.print(Messages.CONSOLE_FREE_MEMORY_BEFORE_GARBAGE_COLLECTION_MESSAGE);
		intp.println(String.valueOf(before));
		intp.print(Messages.CONSOLE_FREE_MEMORY_AFTER_GARBAGE_COLLECTION_MESSAGE);
		intp.println(String.valueOf(after));
		intp.print(Messages.CONSOLE_MEMORY_GAINED_WITH_GARBAGE_COLLECTION_MESSAGE);
		intp.println(String.valueOf(after - before));
	}

	//	/**
	//	 *  Handle the init command.  Uninstall all bundles.
	//	 *
	//	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	//	 */
	//	public void _init(CommandInterpreter intp) throws Exception {
	//		if (osgi.isActive()) {
	//			intp.print(newline);
	//			intp.println(ConsoleMsg.CONSOLE_FRAMEWORK_LAUNCHED_PLEASE_SHUTDOWN_MESSAGE);
	//			return;
	//		}
	//
	//		Bundle[] bundles = (Bundle[]) context.getBundles();
	//
	//		int size = bundles.length;
	//
	//		if (size > 0) {
	//			for (int i = 0; i < size; i++) {
	//				Bundle bundle = bundles[i];
	//
	//				if (bundle.getBundleId() != 0) {
	//					try {
	//						bundle.uninstall();
	//					} catch (BundleException e) {
	//						intp.printStackTrace(e);
	//					}
	//				}
	//			}
	//		} else {
	//			intp.println(ConsoleMsg.CONSOLE_NO_INSTALLED_BUNDLES_ERROR);
	//		}
	//		if (permAdmin != null) {
	//			// clear the permissions from permission admin
	//			permAdmin.setDefaultPermissions(null);
	//			String[] permLocations = permAdmin.getLocations();
	//			if (permLocations != null)
	//				for (int i = 0; i < permLocations.length; i++)
	//					permAdmin.setPermissions(permLocations[i], null);
	//		}
	//		// clear the permissions from conditional permission admin
	//		if (condPermAdmin != null)
	//			for (Enumeration infos = condPermAdmin.getConditionalPermissionInfos(); infos.hasMoreElements();)
	//				((ConditionalPermissionInfo) infos.nextElement()).delete();
	//	}

	/**
	 *  Handle the close command.  Shutdown and exit.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _close(CommandInterpreter intp) throws Exception {
		context.getBundle(0).stop();
	}

	/**
	 *  Handle the refresh command's abbreviation.  Invoke _refresh()
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _r(CommandInterpreter intp) throws Exception {
		_refresh(intp);
	}

	/**
	 *  Handle the refresh command.  Refresh the packages of the specified bundles.
	 *
	 *  @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _refresh(CommandInterpreter intp) throws Exception {
		PackageAdmin packageAdmin = Activator.getPackageAdmin();
		Bundle[] refresh = null;

		String token = intp.nextArgument();
		if (token != null) {
			Vector bundles = new Vector();

			while (token != null) {
				Bundle bundle = getBundleFromToken(intp, token, true);

				if (bundle != null) {
					bundles.addElement(bundle);
				}
				token = intp.nextArgument();
			}

			int size = bundles.size();

			if (size == 0) {
				intp.println(Messages.CONSOLE_INVALID_BUNDLE_SPECIFICATION_ERROR);
				return;
			}

			refresh = new Bundle[size];
			bundles.copyInto(refresh);
		}

		packageAdmin.refreshPackages(refresh);
	}

	/**
	 * Executes the given system command in a separate system process
	 * and waits for it to finish.
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _exec(CommandInterpreter intp) throws Exception {
		String command = intp.nextArgument();
		if (command == null) {
			intp.println(Messages.CONSOLE_NO_COMMAND_SPECIFIED_ERROR);
			return;
		}

		Process p = Runtime.getRuntime().exec(command);

		intp.println(NLS.bind(Messages.CONSOLE_STARTED_IN_MESSAGE, command, String.valueOf(p)));
		int result = p.waitFor();
		intp.println(NLS.bind(Messages.CONSOLE_EXECUTED_RESULT_CODE_MESSAGE, command, String.valueOf(result)));
	}

	/**
	 * Executes the given system command in a separate system process.  It does
	 * not wait for a result.
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _fork(CommandInterpreter intp) throws Exception {
		String command = intp.nextArgument();
		if (command == null) {
			intp.println(Messages.CONSOLE_NO_COMMAND_SPECIFIED_ERROR);
			return;
		}

		Process p = Runtime.getRuntime().exec(command);
		intp.println(NLS.bind(Messages.CONSOLE_STARTED_IN_MESSAGE, command, String.valueOf(p)));
	}

	/**
	 * Handle the headers command's abbreviation.  Invoke _headers()
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _h(CommandInterpreter intp) throws Exception {
		_headers(intp);
	}

	/**
	 * Handle the headers command.  Display headers for the specified bundle(s).
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _headers(CommandInterpreter intp) throws Exception {

		String nextArg = intp.nextArgument();
		if (nextArg == null) {
			intp.println(Messages.CONSOLE_NO_BUNDLE_SPECIFIED_ERROR);
		}
		while (nextArg != null) {
			Bundle bundle = getBundleFromToken(intp, nextArg, true);
			if (bundle != null) {
				intp.printDictionary(bundle.getHeaders(), Messages.CONSOLE_BUNDLE_HEADERS_TITLE);
			}
			nextArg = intp.nextArgument();
		}
	}

	/**
	 * Handles the props command's abbreviation.  Invokes _props()
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _pr(CommandInterpreter intp) throws Exception {
		_props(intp);
	}

	/**
	 * Handles the _props command.  Prints the system properties sorted.
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _props(CommandInterpreter intp) throws Exception {
		// TODO need to get the framework instance props somehow (a la FrameworkProperties)
		intp.printDictionary(System.getProperties(), Messages.CONSOLE_SYSTEM_PROPERTIES_TITLE);
	}

	//	/**
	//	 * Handles the setprop command's abbreviation.  Invokes _setprop()
	//	 *
	//	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	//	 */
	//	public void _setp(CommandInterpreter intp) throws Exception {
	//		_setprop(intp);
	//	}

	//	/**
	//	 * Handles the setprop command.  Sets the CDS property in the given argument.
	//	 *
	//	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	//	 */
	//	public void _setprop(CommandInterpreter intp) throws Exception {
	//		String argument = intp.nextArgument();
	//		if (argument == null) {
	//			intp.println(ConsoleMsg.CONSOLE_NO_PARAMETERS_SPECIFIED_TITLE);
	//			_props(intp);
	//		} else {
	//			InputStream in = new ByteArrayInputStream(argument.getBytes());
	//			try {
	//				Properties sysprops = FrameworkProperties.getProperties();
	//				Properties newprops = new Properties();
	//				newprops.load(in);
	//				intp.println(ConsoleMsg.CONSOLE_SETTING_PROPERTIES_TITLE);
	//				Enumeration keys = newprops.propertyNames();
	//				while (keys.hasMoreElements()) {
	//					String key = (String) keys.nextElement();
	//					String value = (String) newprops.get(key);
	//					sysprops.put(key, value);
	//					intp.println(tab + key + " = " + value); //$NON-NLS-1$
	//				}
	//			} catch (IOException e) {
	//				// ignore
	//			} finally {
	//				try {
	//					in.close();
	//				} catch (IOException e) {
	//					// ignore
	//				}
	//			}
	//		}
	//	}

	/**
	 * Prints the short version of the status.
	 * For the long version use "status".
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _ss(CommandInterpreter intp) throws Exception {
		Object[] options = processOption(intp);
		if (options == null)
			return;

		Bundle[] bundles = context.getBundles();
		if (bundles.length == 0) {
			intp.println(Messages.CONSOLE_NO_INSTALLED_BUNDLES_ERROR);
		} else {
			intp.print(newline);
			intp.print(Messages.CONSOLE_ID);
			intp.print(tab);
			intp.println(Messages.CONSOLE_STATE_BUNDLE_TITLE);
			for (int i = 0; i < bundles.length; i++) {
				Bundle b = bundles[i];
				if (!match(b, (String) options[0], ((Integer) options[1]).intValue()))
					continue;
				String label = b.getSymbolicName();
				if (label == null || label.length() == 0)
					label = b.toString();
				else
					label = label + "_" + b.getHeaders("").get(Constants.BUNDLE_VERSION); //$NON-NLS-1$ //$NON-NLS-2$
				intp.println(b.getBundleId() + "\t" + getStateName(b) + label); //$NON-NLS-1$ 
				PackageAdmin pa = Activator.getPackageAdmin();
				if ((pa.getBundleType(b) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) != 0) {
					Bundle[] hosts = pa.getHosts(b);
					if (hosts != null)
						for (int j = 0; j < hosts.length; j++)
							intp.println("\t            Master=" + hosts[j].getBundleId()); //$NON-NLS-1$
				} else {
					Bundle[] fragments = pa.getFragments(b);
					if (fragments != null) {
						intp.print("\t            Fragments="); //$NON-NLS-1$
						for (int f = 0; f < fragments.length; f++) {
							Bundle fragment = fragments[f];
							intp.print((f > 0 ? ", " : "") + fragment.getBundleId()); //$NON-NLS-1$ //$NON-NLS-2$
						}
						intp.println();
					}
				}
			}
		}
	}

	private boolean match(Bundle toFilter, String searchedName, int searchedState) {
		if ((toFilter.getState() & searchedState) == 0) {
			return false;
		}
		if (searchedName != null && toFilter.getSymbolicName() != null && toFilter.getSymbolicName().indexOf(searchedName) == -1) {
			return false;
		}
		return true;
	}

	/**
	 * Handles the threads command abbreviation.  Invokes _threads().
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _t(CommandInterpreter intp) throws Exception {
		_threads(intp);
	}

	/**
	 * Prints the information about the currently running threads
	 * in the embedded system.
	 *
	 * @param intp A CommandInterpreter object containing the command
	 * and it's arguments.
	 */
	public void _threads(CommandInterpreter intp) throws Exception {

		ThreadGroup[] threadGroups = getThreadGroups();
		Util.sort(threadGroups);

		ThreadGroup tg = getTopThreadGroup();
		Thread[] threads = new Thread[tg.activeCount()];
		int count = tg.enumerate(threads, true);
		Util.sort(threads);

		StringBuffer sb = new StringBuffer(120);
		intp.println();
		intp.println(Messages.CONSOLE_THREADGROUP_TITLE);
		for (int i = 0; i < threadGroups.length; i++) {
			tg = threadGroups[i];
			int all = tg.activeCount(); //tg.allThreadsCount();
			int local = tg.enumerate(new Thread[all], false); //tg.threadsCount();
			ThreadGroup p = tg.getParent();
			String parent = (p == null) ? "-none-" : p.getName(); //$NON-NLS-1$
			sb.setLength(0);
			sb.append(Util.toString(simpleClassName(tg), 18)).append(" ").append(Util.toString(tg.getName(), 21)).append(" ").append(Util.toString(parent, 16)).append(Util.toString(new Integer(tg.getMaxPriority()), 3)).append(Util.toString(new Integer(local), 4)).append("/").append(Util.toString(String.valueOf(all), 6)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			intp.println(sb.toString());
		}
		intp.print(newline);
		intp.println(Messages.CONSOLE_THREADTYPE_TITLE);
		for (int j = 0; j < count; j++) {
			Thread t = threads[j];
			if (t != null) {
				sb.setLength(0);
				sb.append(Util.toString(simpleClassName(t), 18)).append(" ").append(Util.toString(t.getName(), 21)).append(" ").append(Util.toString(t.getThreadGroup().getName(), 16)).append(Util.toString(new Integer(t.getPriority()), 3)); //$NON-NLS-1$ //$NON-NLS-2$
				if (t.isDaemon())
					sb.append(" [daemon]"); //$NON-NLS-1$
				intp.println(sb.toString());
			}
		}
	}

	/**
	 * Handles the sl (startlevel) command. 
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _sl(CommandInterpreter intp) throws Exception {
		if (isStartLevelSvcPresent(intp)) {
			org.osgi.framework.Bundle bundle = null;
			String token = intp.nextArgument();
			int value = 0;
			if (token != null) {
				bundle = getBundleFromToken(intp, token, true);
				if (bundle == null) {
					return;
				}
			}
			if (bundle == null) { // must want framework startlevel
				value = Activator.getStartLevel().getStartLevel();
				intp.println(NLS.bind(Messages.STARTLEVEL_FRAMEWORK_ACTIVE_STARTLEVEL, String.valueOf(value)));
			} else { // must want bundle startlevel
				value = Activator.getStartLevel().getBundleStartLevel(bundle);
				intp.println(NLS.bind(Messages.STARTLEVEL_BUNDLE_STARTLEVEL, new Long(bundle.getBundleId()), new Integer(value)));
			}
		}
	}

	/**
	 * Handles the setfwsl (set framework startlevel) command. 
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _setfwsl(CommandInterpreter intp) throws Exception {
		if (isStartLevelSvcPresent(intp)) {
			int value = 0;
			String token = intp.nextArgument();
			if (token == null) {
				intp.println(Messages.STARTLEVEL_NO_STARTLEVEL_GIVEN);
				value = Activator.getStartLevel().getStartLevel();
				intp.println(NLS.bind(Messages.STARTLEVEL_FRAMEWORK_ACTIVE_STARTLEVEL, String.valueOf(value)));
			} else {
				value = this.getStartLevelFromToken(intp, token);
				if (value > 0) {
					try {
						Activator.getStartLevel().setStartLevel(value);
						intp.println(NLS.bind(Messages.STARTLEVEL_FRAMEWORK_ACTIVE_STARTLEVEL, String.valueOf(value)));
					} catch (IllegalArgumentException e) {
						intp.println(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Handles the setbsl (set bundle startlevel) command. 
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _setbsl(CommandInterpreter intp) throws Exception {
		if (isStartLevelSvcPresent(intp)) {
			String token;
			Bundle bundle = null;
			token = intp.nextArgument();
			if (token == null) {
				intp.println(Messages.STARTLEVEL_NO_STARTLEVEL_OR_BUNDLE_GIVEN);
				return;
			}

			int newSL = this.getStartLevelFromToken(intp, token);

			token = intp.nextArgument();
			if (token == null) {
				intp.println(Messages.STARTLEVEL_NO_STARTLEVEL_OR_BUNDLE_GIVEN);
				return;
			}
			while (token != null) {
				bundle = getBundleFromToken(intp, token, true);
				if (bundle != null) {
					try {
						Activator.getStartLevel().setBundleStartLevel(bundle, newSL);
						intp.println(NLS.bind(Messages.STARTLEVEL_BUNDLE_STARTLEVEL, new Long(bundle.getBundleId()), new Integer(newSL)));
					} catch (IllegalArgumentException e) {
						intp.println(e.getMessage());
					}
				}
				token = intp.nextArgument();
			}
		}
	}

	/**
	 * Handles the setibsl (set initial bundle startlevel) command. 
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	public void _setibsl(CommandInterpreter intp) throws Exception {
		if (isStartLevelSvcPresent(intp)) {
			int value = 0;
			String token = intp.nextArgument();
			if (token == null) {
				intp.println(Messages.STARTLEVEL_NO_STARTLEVEL_GIVEN);
				value = Activator.getStartLevel().getInitialBundleStartLevel();
				intp.println(NLS.bind(Messages.STARTLEVEL_INITIAL_BUNDLE_STARTLEVEL, String.valueOf(value)));
			} else {
				value = this.getStartLevelFromToken(intp, token);
				if (value > 0) {
					try {
						Activator.getStartLevel().setInitialBundleStartLevel(value);
						intp.println(NLS.bind(Messages.STARTLEVEL_INITIAL_BUNDLE_STARTLEVEL, String.valueOf(value)));
					} catch (IllegalArgumentException e) {
						intp.println(e.getMessage());
					}
				}
			}
		}
	}

	public void _requiredBundles(CommandInterpreter intp) {
		_classSpaces(intp);
	}

	public void _classSpaces(CommandInterpreter intp) {
		String token = intp.nextArgument();
		PackageAdmin packageAdmin = Activator.getPackageAdmin();
		RequiredBundle[] symBundles = null;
		symBundles = packageAdmin.getRequiredBundles(token);
		if (symBundles == null) {
			intp.println(Messages.CONSOLE_NO_NAMED_CLASS_SPACES_MESSAGE);
		} else {
			for (int i = 0; i < symBundles.length; i++) {
				org.osgi.service.packageadmin.RequiredBundle symBundle = symBundles[i];
				intp.print(symBundle);

				boolean removalPending = symBundle.isRemovalPending();
				if (removalPending) {
					intp.print("("); //$NON-NLS-1$
					intp.print(Messages.CONSOLE_REMOVAL_PENDING_MESSAGE);
					intp.println(")"); //$NON-NLS-1$
				}

				org.osgi.framework.Bundle provider = symBundle.getBundle();
				if (provider != null) {
					intp.print("<"); //$NON-NLS-1$
					intp.print(provider);
					intp.println(">"); //$NON-NLS-1$

					org.osgi.framework.Bundle[] requiring = symBundle.getRequiringBundles();
					if (requiring != null)
						for (int j = 0; j < requiring.length; j++) {
							intp.print("  "); //$NON-NLS-1$
							intp.print(requiring[j]);
							intp.print(" "); //$NON-NLS-1$
							intp.println(Messages.CONSOLE_REQUIRES_MESSAGE);
						}
				} else {
					intp.print("<"); //$NON-NLS-1$
					intp.print(Messages.CONSOLE_STALE_MESSAGE);
					intp.println(">"); //$NON-NLS-1$
				}

			}
		}

	}

	/**
	 * Handles the profilelog command. 
	 *
	 * @param intp A CommandInterpreter object containing the command and it's arguments.
	 */
	//	public void _profilelog(CommandInterpreter intp) throws Exception {
	//		intp.println(Profile.getProfileLog());
	//	}
	public void _getPackages(CommandInterpreter intp) {
		String nextArg = intp.nextArgument();
		if (nextArg == null)
			return;
		Bundle bundle = getBundleFromToken(intp, nextArg, true);
		PlatformAdmin platformAdmin = Activator.getPlatformAdmin();
		BundleDescription desc = platformAdmin.getState(false).getBundle(bundle.getBundleId());
		ExportPackageDescription[] exports = platformAdmin.getStateHelper().getVisiblePackages(desc, StateHelper.VISIBLE_INCLUDE_EE_PACKAGES);
		for (int i = 0; i < exports.length; i++) {
			intp.println(exports[i] + ": " + platformAdmin.getStateHelper().getAccessCode(desc, exports[i])); //$NON-NLS-1$
		}

	}

	/**
	 * Checks for the presence of the StartLevel Service.  Outputs a message if it is not present.
	 * @param intp The CommandInterpreter object to be used to write to the console
	 * @return true or false if service is present or not
	 */
	protected boolean isStartLevelSvcPresent(CommandInterpreter intp) {
		boolean retval = false;
		org.osgi.framework.ServiceReference slSvcRef = context.getServiceReference("org.osgi.service.startlevel.StartLevel"); //$NON-NLS-1$
		if (slSvcRef != null) {
			org.osgi.service.startlevel.StartLevel slSvc = (org.osgi.service.startlevel.StartLevel) context.getService(slSvcRef);
			if (slSvc != null) {
				retval = true;
			}
		} else {
			intp.println(Messages.CONSOLE_CAN_NOT_USE_STARTLEVEL_NO_STARTLEVEL_SVC_ERROR);
		}
		return retval;
	}

	/**
	 *  Given a number or a token representing a bundle symbolic name or bundle location,
	 *  retrieve the Bundle object with that id.  The bundle symbolic name token is parsed as
	 *  symbolicname[@version]
	 *  
	 *	@param intp The CommandInterpreter
	 *  @param token A string containing a potential bundle it
	 *  @param error A boolean indicating whether or not to output a message
	 *  @return The requested Bundle object
	 */
	protected Bundle getBundleFromToken(CommandInterpreter intp, String token, boolean error) {
		Bundle bundle = null;
		try {
			long id = Long.parseLong(token);
			bundle = context.getBundle(id);
		} catch (NumberFormatException nfe) {

			// if not found, assume token is either symbolic name@version, or location
			String symbolicName = token;
			Version version = null;

			// check for @ -- this may separate either the version string, or be part of the
			// location
			int ix = token.indexOf("@"); //$NON-NLS-1$
			if (ix != -1) {
				if ((ix + 1) != token.length()) {
					try {
						// if the version parses, then use the token prior to @ as a symbolic name
						version = Version.parseVersion(token.substring(ix + 1, token.length()));
						symbolicName = token.substring(0, ix);
					} catch (IllegalArgumentException e) {
						// version doesn't parse, assume token is symbolic name without version, or location
					}
				}
			}

			Bundle[] bundles = context.getBundles();
			for (int i = 0, n = bundles.length; i < n; i++) {
				Bundle b = bundles[i];
				// if symbolicName matches, then matches if there is no version specific on command, or the version matches
				// if there is no version specified on command, pick first matching bundle
				if ((symbolicName.equals(b.getSymbolicName()) && (version == null || version.equals(Util.getVersionFromBundle(b)))) || token.equals(b.getLocation())) {
					bundle = b;
					break;
				}
			}
		}

		if ((bundle == null) && error) {
			intp.println(NLS.bind(Messages.CONSOLE_CANNOT_FIND_BUNDLE_ERROR, token));
		}

		return (bundle);
	}

	/**
	 *  Given a string containing a startlevel value, validate it and convert it to an int
	 * 
	 *  @param intp A CommandInterpreter object used for printing out error messages
	 *  @param value A string containing a potential startlevel
	 *  @return The start level or an int <0 if it was invalid
	 */
	protected int getStartLevelFromToken(CommandInterpreter intp, String value) {
		int retval = -1;
		try {
			retval = Integer.parseInt(value);
			if (Integer.parseInt(value) <= 0) {
				intp.println(Messages.STARTLEVEL_POSITIVE_INTEGER);
			}
		} catch (NumberFormatException nfe) {
			intp.println(Messages.STARTLEVEL_POSITIVE_INTEGER);
		}
		return retval;
	}

	/**
	 *  Given a bundle, return the string describing that bundle's state.
	 *
	 *  @param bundle A bundle to return the state of
	 *  @return A String describing the state
	 */
	protected String getStateName(Bundle bundle) {
		int state = bundle.getState();
		switch (state) {
			case Bundle.UNINSTALLED :
				return "UNINSTALLED "; //$NON-NLS-1$

			case Bundle.INSTALLED :
				return "INSTALLED   "; //$NON-NLS-1$

			case Bundle.RESOLVED :
				return "RESOLVED    "; //$NON-NLS-1$

			case Bundle.STARTING :
				synchronized (lazyActivation) {
					if (lazyActivation.contains(bundle)) {
						return "<<LAZY>>    "; //$NON-NLS-1$
					}
					return "STARTING    "; //$NON-NLS-1$
				}

			case Bundle.STOPPING :
				return "STOPPING    "; //$NON-NLS-1$

			case Bundle.ACTIVE :
				return "ACTIVE      "; //$NON-NLS-1$

			default :
				return Integer.toHexString(state);
		}
	}

	/**
	 * Answers all thread groups in the system.
	 *
	 * @return	An array of all thread groups.
	 */
	protected ThreadGroup[] getThreadGroups() {
		ThreadGroup tg = getTopThreadGroup();
		ThreadGroup[] groups = new ThreadGroup[tg.activeGroupCount()];
		int count = tg.enumerate(groups, true);
		if (count == groups.length) {
			return groups;
		}
		// get rid of null entries
		ThreadGroup[] ngroups = new ThreadGroup[count];
		System.arraycopy(groups, 0, ngroups, 0, count);
		return ngroups;
	}

	/**
	 * Answers the top level group of the current thread.
	 * <p>
	 * It is the 'system' or 'main' thread group under
	 * which all 'user' thread groups are allocated.
	 *
	 * @return	The parent of all user thread groups.
	 */
	protected ThreadGroup getTopThreadGroup() {
		ThreadGroup topGroup = Thread.currentThread().getThreadGroup();
		if (topGroup != null) {
			while (topGroup.getParent() != null) {
				topGroup = topGroup.getParent();
			}
		}
		return topGroup;
	}

	/**
	 * Returns the simple class name of an object.
	 *
	 * @param o The object for which a class name is requested
	 * @return	The simple class name.
	 */
	public String simpleClassName(Object o) {
		java.util.StringTokenizer t = new java.util.StringTokenizer(o.getClass().getName(), "."); //$NON-NLS-1$
		int ct = t.countTokens();
		for (int i = 1; i < ct; i++) {
			t.nextToken();
		}
		return t.nextToken();
	}

	// TODO need to get the framework instance props somehow (a la FrameworkProperties)
	public void _getprop(CommandInterpreter ci) throws Exception {
		Properties allProperties = System.getProperties();
		String filter = ci.nextArgument();
		Iterator propertyNames = new TreeSet(allProperties.keySet()).iterator();
		while (propertyNames.hasNext()) {
			String prop = (String) propertyNames.next();
			if (filter == null || prop.startsWith(filter)) {
				ci.println(prop + '=' + allProperties.getProperty(prop));
			}
		}
	}

	/**
	 * This is used to track lazily activated bundles.
	 */
	public void bundleChanged(BundleEvent event) {
		int type = event.getType();
		Bundle bundle = event.getBundle();
		synchronized (lazyActivation) {
			switch (type) {
				case BundleEvent.LAZY_ACTIVATION :
					if (!lazyActivation.contains(bundle)) {
						lazyActivation.add(bundle);
					}
					break;

				default :
					lazyActivation.remove(bundle);
					break;
			}
		}

	}
}
