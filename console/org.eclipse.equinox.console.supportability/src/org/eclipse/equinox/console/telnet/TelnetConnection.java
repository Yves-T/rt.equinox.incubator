/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Lazar Kirchev, SAP AG - initial API and implementation  
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.console.telnet;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.eclipse.equinox.console.supportability.ConsoleInputHandler;

/**
 * This class manages a telnet connection. It is responsible for wrapping the original io streams
 * from the socket, and starting a CommandSession to execute commands from the telnet.
 *
 */
public class TelnetConnection extends Thread {
	private Socket socket;
	private CommandProcessor processor;
	private boolean isTelnetNegotiationFinished = false;
    private Callback callback;
    private static final long WAIT_INTERVAL = 1000;
    private static final long NEGOTIATION_TIMEOUT = 60000;
	
	public TelnetConnection (Socket socket, CommandProcessor processor) {
		this.socket = socket;
		this.processor = processor;
		callback = new NegotiationFinishedCallback(this);
	}
	
	public void run() {
		try {
			ConsoleInputStream in = new ConsoleInputStream();
			TelnetOutputStream out = new TelnetOutputStream(socket.getOutputStream());
			out.autoSend();
			TelnetInputHandler telnetInputHandler = new TelnetInputHandler(socket.getInputStream(), in, out, callback);
			telnetInputHandler.start();
			
			long start = System.currentTimeMillis();
			
			while(isTelnetNegotiationFinished == false && System.currentTimeMillis() - start < NEGOTIATION_TIMEOUT) {
				synchronized (this) {
					try {
						wait(WAIT_INTERVAL);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
			
			final CommandSession session;
			PrintStream output = new PrintStream(out);
			
			ConsoleInputStream inp = new ConsoleInputStream();
			
	        ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler(in, inp, out);
	        consoleInputHandler.getScanner().setBackspace(telnetInputHandler.getScanner().getBackspace());
	        consoleInputHandler.getScanner().setDel(telnetInputHandler.getScanner().getDel());
	        consoleInputHandler.getScanner().setCurrentEscapesToKey(telnetInputHandler.getScanner().getCurrentEscapesToKey());
	        consoleInputHandler.getScanner().setEscapes(telnetInputHandler.getScanner().getEscapes());
	        
	        consoleInputHandler.start();
	        
	        session = processor.createSession(inp, output, output);
			
			try {
	            session.execute("gosh --login --noshutdown");
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            session.close();
	            try {
	                socket.close();
	            }
	            catch (IOException e) {
	            	// do nothing
	            }
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void telnetNegotiationFinished() {
		isTelnetNegotiationFinished = true;
		notify();
	}
}
