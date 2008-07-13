/*******************************************************************************
 * Copyright (c) 2008 Heiko Seeberger and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Heiko Seeberger - initial implementation
 ******************************************************************************/

package org.eclipse.equinox.weaving.hello.aspects;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.PrintStream;

/**
 * Just for demo: Messes around with ...demo.hello activator's start and stop
 * methods.
 * 
 * @author Heiko Seeberger
 */
public aspect HelloAspect {
    
    /**
     * Replaces the "Hello world!" output with "Hi from HelloAspect ;-)".
     */
    void around() : cflowbelow(execution(void BundleActivator.start(BundleContext))) 
                    && call(void PrintStream.println(String))
                    && !within(HelloAspect) {
        System.out.println("Hi from HelloAspect ;-)");
    }
    
    /**
     * Replaces the "Good bye world!" output with "Bye from HelloAspect ;-)".
     */
    void around() : cflowbelow(execution(void BundleActivator.stop(BundleContext))) 
                    && call(void PrintStream.println(String))
                    && !within(HelloAspect) {
        System.out.println("Bye from HelloAspect ;-)");
    }
}
