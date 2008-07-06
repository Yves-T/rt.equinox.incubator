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

package org.aspectj.aunit;

import java.util.ArrayList;
import java.util.Iterator;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.WeaveMessage;
import org.aspectj.bridge.IMessage.Kind;

public class TestMessageHandler implements IMessageHandler {

	public final static boolean debug = false;
	private static ArrayList messages = new ArrayList();
	
	public TestMessageHandler() {
		super();
//		System.out.println("TestMessageHandler.TestMessageHandler() classLoader=" + getClass().getClassLoader());
	}

	public void dontIgnore(Kind kind) {
		// TODO Auto-generated method stub

	}

	public boolean handleMessage(IMessage message) throws AbortException {
		if (debug) System.err.println("? TestMessageHandler.handleMessage() message=" + message); 
		
		if (message instanceof WeaveMessage) {
			WeaveMessage weaveMessage = (WeaveMessage)message;
			messages.add(message);
			System.err.println(message);
		}
//		System.err.println(message);
		return true;
	}

	public boolean isIgnoring(Kind kind) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void clear () {
		messages.clear();
	}
	
	public static boolean containsMessage (String typeName, String aspectName) {
		return (getMessage(typeName,aspectName) != null);
//		for (Iterator i = messages.iterator(); i.hasNext();) {
//			WeaveMessage message = (WeaveMessage)i.next();
//			if (message.getAffectedtypename().equals(typeName)
//				&& message.getAspectname().equals(aspectName)) {
//					return true;
//				}
//		}
//		
//		return false;
	}

	public static String getMessage (String typeName, String aspectName) {
		for (Iterator i = messages.iterator(); i.hasNext();) {
			WeaveMessage message = (WeaveMessage)i.next();
			if (message.getAffectedtypename().equals(typeName)
				&& message.getAspectname().equals(aspectName)) {
					return message.toString();
				}
		}
		
		return null;
	}

	public static boolean precedes (String aspectName1, String aspectName2) {
		for (Iterator i = messages.iterator(); i.hasNext();) {
			WeaveMessage message = (WeaveMessage)i.next();
			IMessage.Kind kind = message.getKind();
		}
		
		return false;
	}

	public void ignore(Kind kind) {
		// TODO Auto-generated method stub
		
	}
}
