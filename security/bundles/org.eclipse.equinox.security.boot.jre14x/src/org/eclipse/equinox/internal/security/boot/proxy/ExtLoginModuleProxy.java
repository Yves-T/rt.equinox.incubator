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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.eclipse.equinox.internal.security.boot.MessageAccess;

public class ExtLoginModuleProxy extends AbstractProxy implements LoginModule {

//	private static Logger s_logger = Logger.getLogger( ExtLoginModuleProxy.class.getPackage( ).toString( ));
	
	private static Logger s_logger; 
	static {
	  Class cls = ExtLoginModuleProxy.class;
	  Package extLoginPack = cls.getPackage();
	  String packageStr = extLoginPack.toString();
	  s_logger = Logger.getLogger( packageStr);
	}
	
	public interface ILoginModuleFactory {
		LoginModule getTargetLoginModule( Map options);
	}
	
	private static ILoginModuleFactory s_loginModuleFactory = null;
	
	private static ILoginModuleFactory getFactory( ) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "getFactory"); //$NON-NLS-1$
		}

		if ( null == s_loginModuleFactory) {
			
			if ( s_logger.isLoggable( Level.SEVERE)) {
				s_logger.log( Level.SEVERE, MessageAccess.getString( "err.loginmodule.factory.not.set.1")); //$NON-NLS-1$
			}
			throw new RuntimeException( MessageAccess.getString( "err.loginmodule.factory.not.set.1")); //$NON-NLS-1$
		}
		ILoginModuleFactory returnValue = s_loginModuleFactory;
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "getFactory", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue;
	}
	
	public static void setFactory( ILoginModuleFactory loginModuleFactory) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "setFactory", new Object[] {loginModuleFactory}); //$NON-NLS-1$
		}

		s_loginModuleFactory = loginModuleFactory;
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "setFactory"); //$NON-NLS-1$
		}
	}
	
	private static Object newProxyInstance( Map options) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "newProxyInstance", new Object[] {options}); //$NON-NLS-1$
		}

		LoginModule target = getFactory( ).getTargetLoginModule( options);

		Object returnValue = Proxy.newProxyInstance(	target.getClass( ).getClassLoader( ),
														target.getClass( ).getInterfaces( ),
														new ExtLoginModuleProxy( target)); 

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "newProxyInstance", new Object[] {returnValue}); //$NON-NLS-1$
		}
		return returnValue;
	}

	public ExtLoginModuleProxy( ) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "<<constructor>>"); //$NON-NLS-1$
		}

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "<<constructor>>"); //$NON-NLS-1$
		}
	}
	
	private ExtLoginModuleProxy( LoginModule target) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "<<constructor>>", new Object[] {target}); //$NON-NLS-1$
		}
		
		m_target = target;
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "<<constructor>>"); //$NON-NLS-1$
		}
	}
	
	private LoginModule m_target = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.security.jaas.BaseProxy#invokeImpl(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invokeImpl( Object proxy, Method method, Object[] args) throws Throwable {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "invokeImpl", new Object[] {proxy,method,args}); //$NON-NLS-1$
		}

		if ( null == m_target) {
			throw new LoginException( MessageAccess.getString( "err.loginmodule.not.set.1")); //$NON-NLS-1$
		}
		
		Object returnValue = method.invoke( m_target, args);
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "invokeImpl", new Object[] {returnValue}); //$NON-NLS-1$
		}

		return returnValue;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	public void initialize( Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "initialize", new Object[] {subject,callbackHandler,sharedState,options}); //$NON-NLS-1$
		}

		m_target = (LoginModule)ExtLoginModuleProxy.newProxyInstance( options);
		m_target.initialize( subject, callbackHandler, sharedState, options);

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "initialize"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#login()
	 */
	public boolean login( )
		throws LoginException {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "login"); //$NON-NLS-1$
		}
		
		boolean returnValue = m_target.login( );

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "login", new Object[] {Boolean.valueOf( returnValue)}); //$NON-NLS-1$
		}
		
		return returnValue;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#commit()
	 */
	public boolean commit( )
		throws LoginException {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "commit"); //$NON-NLS-1$
		}
		
		boolean returnValue = m_target.commit( );

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "commit", new Object[] {Boolean.valueOf( returnValue)}); //$NON-NLS-1$
		}

		return returnValue; 
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#abort()
	 */
	public boolean abort( )
		throws LoginException {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "abort"); //$NON-NLS-1$
		}
		
		boolean returnValue = m_target.abort( );

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "abort", new Object[] {Boolean.valueOf( returnValue)}); //$NON-NLS-1$
		}

		return returnValue; 
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#logout()
	 */
	public boolean logout( )
		throws LoginException {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ExtLoginModuleProxy.class.getName( ), "logout"); //$NON-NLS-1$
		}
		
		boolean returnValue = m_target.logout( );

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ExtLoginModuleProxy.class.getName( ), "logout", new Object[] {Boolean.valueOf( returnValue)}); //$NON-NLS-1$
		}

		return returnValue;
	}
}
