/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Webster           initial implementation      
 *******************************************************************************/

package org.aspectj.test.performance;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.loadtime.OverriddenDefaultMessageHandler;

public class PerformanceMessageHandler extends OverriddenDefaultMessageHandler implements Comparable, IMessageHandler {

	private final static boolean verbose = false;
	private static Set messageHandlers;
	private static long start = System.currentTimeMillis();
	private static long finish;
	private static long lastWeave;
	
	private String loaderName;
	private long wovenCount;
	private long joinPointCount = 0;
	
	public PerformanceMessageHandler () {
		if (verbose) System.err.println("? MessageHandler.MessageHandler()");

	}

	/*
	 * java.lang.Comparable
	 */
	public int compareTo(Object o) {
		String otherName;

		if (o instanceof PerformanceMessageHandler) {
			otherName = ((PerformanceMessageHandler)o).loaderName;
		}
		else {
			otherName = o.toString();
		}
		
		return loaderName.compareTo(otherName);
	}
	
//	public void dontIgnore(Kind kind) {
//	}

	public boolean handleMessage(IMessage message) throws AbortException {
//		if (message instanceof WeaveMessage) {
//			WeaveMessage weaveMessage = (WeaveMessage)message;
//			messages.add(message);
//			if (debug) System.out.println("TestMessageHandler.handleMessage() aspect=" + weaveMessage.getAspectname());
//			System.err.println(message);
//		}
		if (message.getKind() == IMessage.INFO) {
			if (message.getMessage().startsWith("register classloader")) {
				if (verbose) System.err.println("? MessageHandler.handleMessage() " + message);
				loaderName = message.getMessage().substring(21);
				synchronized (messageHandlers) {
					messageHandlers.add(this);
				}
			}

//			else if (message.getMessage().indexOf("bcel") != -1) {
//				if (verbose) System.err.println(message);
//			}
		}
		else if (message.getKind() == IMessage.DEBUG) {
			if (message.getMessage().startsWith("weaving")) {
				if (verbose) System.err.println("? MessageHandler.handleMessage() " + message);
				wovenCount++;
			}
		}
		else if (message.getKind() == IMessage.WEAVEINFO) {
			joinPointCount++;
		}
		
		lastWeave = System.currentTimeMillis();
		
		return super.handleMessage(message);
	}

//	public void ignore(Kind kind) {
//	}

//	public boolean isIgnoring(Kind kind) {
//		return false;
//	}
	
	public String toString () {
		return loaderName + " " + wovenCount + ((isIgnoring(IMessage.WEAVEINFO)? "" : "(" + joinPointCount + ")"));
	}
	
	private static void printReport() {
		System.err.println("*** org.eclipse.equinox.weaving.tests.performance ***");
		long wovenTotal = 0;
		long joinPointTotal = 0;
		for (Iterator i = messageHandlers.iterator(); i.hasNext();) {
			PerformanceMessageHandler handler = (PerformanceMessageHandler)i.next();
			System.err.println(handler);
			wovenTotal += handler.wovenCount;
			joinPointTotal += handler.joinPointCount;
		}
		System.err.println("Loaders: " + messageHandlers.size() + " Classes: " + wovenTotal + " Join Points: " + joinPointTotal + " Seconds=" + (finish-start)/1000);
	}

	static {
		if (verbose) System.err.println("? PerformanceMessageHandler.<clinit>()");
		
		messageHandlers = new TreeSet();
		
		Thread hook = new Thread () {
			public void run () {
				if (verbose) System.err.println("? MessageHandler$Thread.run()");
				finish = lastWeave;
				printReport();
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);
		
		TimerTask task = new TimerTask () {

			public void run() {
				if (verbose) System.err.println("? PerformanceMessageHandler$TimerTask.run()");
				long now = System.currentTimeMillis();
				if (now - lastWeave > 3000) {
					cancel();
					finish = lastWeave;
					printReport();
				}
			}
			
		};
		Timer timer = new Timer("org.eclipse.equinox.weaving.tests.performace",true);
		timer.scheduleAtFixedRate(task,3000,3000);
	}

}
