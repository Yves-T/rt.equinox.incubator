/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.boot.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.equinox.internal.security.boot.MessageAccess;

public abstract class AbstractProxy implements InvocationHandler {

//	private static Logger s_logger = Logger.getLogger( AbstractProxy.class.getPackage( ).toString( ));
	
	private static Logger s_logger; 
	static {
	  Class cls = AbstractProxy.class;
	  Package abstractProxyPack = cls.getPackage();
	  String packageStr = abstractProxyPack.toString();
	  s_logger = Logger.getLogger( packageStr);
	}

	/* Preloaded Method objects for the methods in java.lang.Object */
	private static Method hashCodeMethod;
	private static Method equalsMethod;
	private static Method toStringMethod;

	static {
		try {
			hashCodeMethod 	= Object.class.getMethod( "hashCode", (Class[]) null); //$NON-NLS-1$
			equalsMethod 	= Object.class.getMethod( "equals", new Class[] { Object.class }); //$NON-NLS-1$
			toStringMethod 	= Object.class.getMethod( "toString", (Class[]) null); //$NON-NLS-1$
		} 
		catch( NoSuchMethodException e) {
			if ( s_logger.isLoggable( Level.WARNING)) {
				s_logger.log( Level.WARNING, MessageAccess.getString( "warn.method.not.found.1"), e); //$NON-NLS-1$
			}
			throw new NoSuchMethodError( e.getMessage( ));
		}
	}

	public AbstractProxy( ) { }

	public abstract Object invokeImpl( Object proxy, Method method, Object[] args)
		throws Throwable;

	public Object invoke( Object proxy, Method method, Object[] args) throws Throwable {

//		if ( s_logger.isLoggable( Level.FINER)) {
//			s_logger.entering( AbstractProxy.class.getName( ), "invoke"); //$NON-NLS-1$
//		}

		Class declaringClass = method.getDeclaringClass( );
		Object returnValue = null;
		
		if ( declaringClass == Object.class) {
			if ( method.equals( hashCodeMethod)) {
				returnValue = proxyHashCode( proxy);
			} 
			else if ( method.equals( equalsMethod)) {
				returnValue = proxyEquals( proxy, args[0]);
			} 
			else if ( method.equals(toStringMethod)) {
				returnValue = proxyToString( proxy);
			} 
			else {
				if ( s_logger.isLoggable( Level.WARNING)) {
					s_logger.log( Level.WARNING, MessageAccess.getString( "warn.method.not.expected.2"), method.getName( )); //$NON-NLS-1$
				}
				throw new InternalError( MessageAccess.getString( "warn.method.not.expected.1") + method.getName( )); //$NON-NLS-1$
			}
		} 
		else {
			/* START invoke the method */
            try {
    			returnValue = invokeImpl( proxy, method, args);
            } 
            catch( InvocationTargetException e) {
    			if ( s_logger.isLoggable( Level.WARNING)) {
    				s_logger.log( Level.WARNING, MessageAccess.getString( "warn.proxy.method.exception.1"), e); //$NON-NLS-1$
    			}
                throw e.getTargetException( );
            }
            /* END: invoke the method */
		}

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( AbstractProxy.class.getName( ), "invoke", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue;
	}

	protected Integer proxyHashCode( Object proxy) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( AbstractProxy.class.getName( ), "proxyHashCode"); //$NON-NLS-1$
		}

		Integer returnValue = new Integer( System.identityHashCode(proxy));

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( AbstractProxy.class.getName( ), "proxyHashCode", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue;
	}

	protected Boolean proxyEquals( Object proxy, Object other) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( AbstractProxy.class.getName( ), "proxyEquals"); //$NON-NLS-1$
		}

		Boolean returnValue = (proxy == other ? Boolean.TRUE : Boolean.FALSE); 

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( AbstractProxy.class.getName( ), "proxyEquals", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue; 
	}

	protected String proxyToString( Object proxy) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( AbstractProxy.class.getName( ), "proxyToString"); //$NON-NLS-1$
		}

		String returnValue = proxy.getClass( ).getName( ) + '@' +	Integer.toHexString( proxy.hashCode( ));

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( AbstractProxy.class.getName( ), "proxyToString", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue;
	}
}
