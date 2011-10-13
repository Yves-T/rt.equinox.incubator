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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import java.io.Closeable;
import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.eclipse.equinox.console.common.ConsoleOutputStream;
import org.eclipse.equinox.console.common.KEYS;
import org.eclipse.equinox.console.common.terminal.ANSITerminalTypeMappings;
import org.eclipse.equinox.console.common.terminal.SCOTerminalTypeMappings;
import org.eclipse.equinox.console.common.terminal.TerminalTypeMappings;
import org.eclipse.equinox.console.common.terminal.VT100TerminalTypeMappings;
import org.eclipse.equinox.console.common.terminal.VT220TerminalTypeMappings;
import org.eclipse.equinox.console.common.terminal.VT320TerminalTypeMappings;
import org.eclipse.equinox.console.storage.SecureUserStore;
import org.eclipse.equinox.console.supportability.ConsoleInputHandler;
import org.eclipse.equinox.console.supportability.ConsoleInputScanner;
import org.osgi.framework.BundleContext;

/**
 * This class manages a ssh connection. It is responsible for wrapping the original io streams
 * from the ssh server, and starting a CommandSession to execute commands from the ssh.
 *
 */
public class SshShell implements Command, Closeable {
	
	private CommandProcessor processor;
	private BundleContext context;
	private InputStream in;
	private OutputStream out;
	private ExitCallback callback;
	private Thread thread;
	
	private final Map<String, TerminalTypeMappings> supportedEscapeSequences;
	private static final String DEFAULT_TTYPE = File.separatorChar == '/' ? "XTERM" : "ANSI";
	private TerminalTypeMappings currentMappings;
	private Map<String, KEYS> currentEscapesToKey;
	private static final String PROMPT = "prompt";
    private static final String OSGI_PROMPT = "osgi> ";
    private static final String SCOPE = "SCOPE";
    private static final String EQUINOX_SCOPE = "equinox:*";
    private static final String INPUT_SCANNER = "INPUT_SCANNER";
    private static final String SSH_INPUT_SCANNER = "SSH_INPUT_SCANNER";
    private static final String USER_STORAGE_PROPERTY_NAME = "osgi.console.ssh.useDefaultSecureStorage";
    private static final String DEFAULT_USER = "equinox";
    private static final String TERMINAL_PROPERTY = "TERM";
    private static final String CLOSEABLE = "CLOSEABLE";
	private static final int ADD_USER_COUNTER_LIMIT = 2;
	
	public SshShell(CommandProcessor processor, BundleContext context) {
		this.processor = processor;
		this.context = context;
		supportedEscapeSequences = new HashMap<String, TerminalTypeMappings> ();
        supportedEscapeSequences.put("ANSI", new ANSITerminalTypeMappings());
        supportedEscapeSequences.put("WINDOWS", new ANSITerminalTypeMappings());
        supportedEscapeSequences.put("VT100", new VT100TerminalTypeMappings());
        VT220TerminalTypeMappings vtMappings = new VT220TerminalTypeMappings();
        supportedEscapeSequences.put("VT220", vtMappings);
        supportedEscapeSequences.put("XTERM", vtMappings);
        supportedEscapeSequences.put("VT320", new VT320TerminalTypeMappings());
        supportedEscapeSequences.put("SCO", new SCOTerminalTypeMappings());
        
        currentMappings = supportedEscapeSequences.get(DEFAULT_TTYPE);
        currentEscapesToKey = currentMappings.getEscapesToKey();
	}

	public void setInputStream(InputStream in) {
		this.in = in;
	}

	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	public void setErrorStream(OutputStream err) {
		// do nothing
	}

	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}

	public void start(Environment env) throws IOException {
		String term = env.getEnv().get(TERMINAL_PROPERTY);
		TerminalTypeMappings mapping = supportedEscapeSequences.get(term.toUpperCase());
		if(mapping != null) {
			currentMappings = mapping;
			currentEscapesToKey = mapping.getEscapesToKey();
		}
		
		ConsoleInputStream input = new ConsoleInputStream();
		ConsoleOutputStream outp = new ConsoleOutputStream(out);
		SshInputHandler inputHandler = new SshInputHandler(in, input, outp);
		inputHandler.getScanner().setBackspace(currentMappings.getBackspace());
		inputHandler.getScanner().setDel(currentMappings.getDel());
		inputHandler.getScanner().setCurrentEscapesToKey(currentEscapesToKey);
		inputHandler.getScanner().setEscapes(currentMappings.getEscapes());
		inputHandler.start();
		
		ConsoleInputStream inp = new ConsoleInputStream();
        ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler(input, inp, outp);
        consoleInputHandler.getScanner().setBackspace(currentMappings.getBackspace());
        consoleInputHandler.getScanner().setDel(currentMappings.getDel());
        consoleInputHandler.getScanner().setCurrentEscapesToKey(currentEscapesToKey);
        consoleInputHandler.getScanner().setEscapes(currentMappings.getEscapes());
        ((ConsoleInputScanner)consoleInputHandler.getScanner()).setContext(context);
        consoleInputHandler.start();
        
        final CommandSession session;
		final PrintStream output = new PrintStream(outp);
		
        session = processor.createSession(inp, output, output);
        session.put(SCOPE, EQUINOX_SCOPE);
        session.put(PROMPT, OSGI_PROMPT);
        session.put(INPUT_SCANNER, consoleInputHandler.getScanner());
        session.put(SSH_INPUT_SCANNER, inputHandler.getScanner());
        // Store this closeable object in the session, so that the disconnect command can close it
        session.put(CLOSEABLE, this);
        ((ConsoleInputScanner)consoleInputHandler.getScanner()).setSession(session);
        
        thread = new Thread() {
        	public void run() {
        		try {
        			if ("true".equals(context.getProperty(USER_STORAGE_PROPERTY_NAME))) {
        				String[] names = SecureUserStore.getUserNames();
        				for (String name : names) {
        					// if the default user is the only user, request creation of a new user and delete the default
        					if (DEFAULT_USER.equals(name)) {
        						if (names.length == 1) {
        							session.getConsole().println("Currently the default user is the only one; since it will be deleted after first login, create a new user:");
        							boolean isUserAdded =false;
        							int count = 0;
        							while (!isUserAdded && count < ADD_USER_COUNTER_LIMIT ){
        								isUserAdded = ((Boolean) session.execute("addUser")).booleanValue();
        								count++;
        							}
        							if (!isUserAdded) {
        								break;
        							}
        						}
        						if (SecureUserStore.existsUser(name)) {
        							SecureUserStore.deleteUser(name);
        						}
        						break;
        					}
        				}
        			}
					session.execute("gosh --login --noshutdown");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    session.close();
                }
        	}
        };
        
        thread.start();
	}

	public void destroy() {
		return;
	}
	
	public void onExit() {
		thread.interrupt();
		callback.onExit(0);
	}

	public void close() {
		onExit();
	}

}
