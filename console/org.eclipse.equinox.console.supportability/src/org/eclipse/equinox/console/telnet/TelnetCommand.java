/*******************************************************************************
 * Copyright (c) 2010 SAP AG
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Lazar Kirchev, SAP AG - initial API and implementation  
 *******************************************************************************/

package org.eclipse.equinox.console.telnet;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;

/**
 * This class implements a command for starting/stopping a simple telnet server.
 *
 */
public class TelnetCommand
{    
	private String defaultHost = null;
    private int defaultPort;
    private final CommandProcessor processor;
    private String host = null;
    private int port;
    private TelnetServer telnetServer;

    public TelnetCommand(CommandProcessor procesor, BundleContext context)
    {
        this.processor = procesor;
        String telnetPort = null;
        String consolePropValue = context.getProperty("osgi.console");
        if(consolePropValue != null) {
        	int index = consolePropValue.lastIndexOf(":");
        	if (index > -1) {
        		defaultHost = consolePropValue.substring(0, index);
        	}
        	telnetPort = consolePropValue.substring(index + 1);
        }
        if (telnetPort != null && !"".equals(telnetPort)) {
        	try {
        		defaultPort = Integer.parseInt(telnetPort);
			} catch (NumberFormatException e) {
				defaultPort = 2223;
			}
        } else {
        	defaultPort = 2223;
        }
    }

    @Descriptor("start/stop a telnet server")
    public synchronized void telnet(String[] arguments) throws Exception
    {
        String command = null;
        int newPort = 0;
        String newHost = null;
        
        for(int i = 0; i < arguments.length; i++) {
        	if("-?".equals(arguments[i]) || "-help".equals(arguments[i])) {
        		printHelp();
        		return;
        	} else if("start".equals(arguments[i])) {
        		command = "start";
        	} else if ("stop".equals(arguments[i])) {
        		command = "stop";
        	} else if ("-port".equals(arguments[i]) && (arguments.length > i + 1)) {
        		i++;
        		newPort = Integer.parseInt(arguments[i]);
        	} else if ("-host".equals(arguments[i]) && (arguments.length > i + 1)) {
        		i++;
        		newHost = arguments[i];
        	} else {
        		throw new Exception("Unrecognized telnet command/option " + arguments[i]);
        	}
        }
        
        if (command == null) {
        	throw new Exception("No telnet command specified");
        }
        
        if (newPort != 0) {
        	port = newPort;
        } else if (port == 0) {
        	port = defaultPort;
        }
        
        if (newHost != null) {
        	host = newHost;
        } else {
        	host = defaultHost;
        }

        if ("start".equals(command)) {
            if (telnetServer != null) {
                throw new IllegalStateException("telnet is already running on port " + port);
            }
            
            telnetServer = new TelnetServer(processor, host, port);
            telnetServer.setName("equinox telnet");
            telnetServer.start();    
        } else if ("stop".equals(command)) {
            if (telnetServer == null) {
                throw new IllegalStateException("telnet is not running.");
            }
            
            telnetServer.stopTelnetServer();
            telnetServer = null;
        } 
    }
    
    private void printHelp() {
    	StringBuffer help = new StringBuffer();
    	help.append("telnet - start simple telnet server");
    	help.append("\n");
    	help.append("Usage: telnet start | stop [-port port]");
    	help.append("\n");
    	help.append("\t");
    	help.append("-port");
    	help.append("\t");
    	help.append("listen port (default=");
    	help.append(defaultPort);
    	help.append(")");
    	help.append("\n");
    	help.append("\t");
    	help.append("-host");
    	help.append("\t");
    	help.append("local host address to listen on (default is none - listen on all network interfaces)");
    	help.append("\n");
    	help.append("\t");
    	help.append("-?, -help");
    	help.append("\t");
    	help.append("show help");
    	System.out.println(help.toString());          
    }
}
