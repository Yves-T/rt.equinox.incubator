/*******************************************************************************
 * Copyright (c) 2011 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Lazar Kirchev, SAP AG - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.console.ssh;

import java.util.HashSet;
import java.util.Set;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.osgi.framework.BundleContext;

/**
 *  Shell factory used by the SSH server to create a SSH shell
 *
 */
public class SshShellFactory implements Factory<Command> {
	
	private CommandProcessor processor;
	private BundleContext context;
	private Set<SshShell> shells = new HashSet<SshShell>();
	
	public SshShellFactory(CommandProcessor processor, BundleContext context) {
		this.processor = processor;
		this.context = context;
	}
	
	public Command create() {
		SshShell shell = new SshShell(processor, context);
		shells.add(shell);
		return shell;
	}
	
	public void exit() {
		for(SshShell shell : shells) {
			shell.onExit();
		}
	}
}
