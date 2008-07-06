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

package org.aspectj.weaver.loadtime;

import org.aspectj.bridge.IMessage;

public class OverriddenDefaultMessageHandler extends DefaultMessageHandler {

	private boolean isVerbose = false;
    private boolean isDebug = false;

    public OverriddenDefaultMessageHandler() {
		ignore(IMessage.WEAVEINFO);
        ignore(IMessage.DEBUG);
        ignore(IMessage.INFO);
	}

    public void dontIgnore(IMessage.Kind kind) {
    	super.dontIgnore(kind);

		if (kind.equals(IMessage.INFO)) {
            isVerbose = true;
		} else if (kind.equals(IMessage.DEBUG)) {
            isDebug = true;
		}    
    }
    
	public void ignore(IMessage.Kind kind) {
		super.ignore(kind);
        
		if (kind.equals(IMessage.INFO)) {
            isVerbose = false;
        } else if (kind.equals(IMessage.DEBUG)) {
            isDebug = false;
        }
	}
	
    public boolean isIgnoring(IMessage.Kind kind) {
        if (kind.equals(IMessage.INFO)) {
            return !isVerbose;
        }
        if (kind.equals(IMessage.DEBUG)) {
            return !isDebug;
        }
        return super.isIgnoring(kind);
    }
	

}
