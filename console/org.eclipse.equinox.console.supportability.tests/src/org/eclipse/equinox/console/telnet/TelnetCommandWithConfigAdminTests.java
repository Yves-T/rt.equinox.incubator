package org.eclipse.equinox.console.telnet;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;


public class TelnetCommandWithConfigAdminTests {
	private static final int TEST_CONTENT = 100;
	private static final String STOP_COMMAND = "stop";
	private static final String HOST = "localhost";
	private static final String TELNET_PORT = "2223";
	private static final long WAIT_TIME = 5000;
	private static final String USE_CONFIG_ADMIN_PROP = "osgi.console.useConfigAdmin";
	private ManagedService configurator;
	
	
	@Test
	public void testTelnetCommandWithConfigAdmin() throws Exception {
		System.setProperty(USE_CONFIG_ADMIN_PROP, "true");
		
		CommandSession session = EasyMock.createMock(CommandSession.class);
    	session.put((String)EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().times(3);
        session.close();
		EasyMock.expectLastCall();
        EasyMock.replay(session);
        
        CommandProcessor processor = EasyMock.createMock(CommandProcessor.class);
        EasyMock.expect(processor.createSession((ConsoleInputStream)EasyMock.anyObject(), (PrintStream)EasyMock.anyObject(), (PrintStream)EasyMock.anyObject())).andReturn(session);
        EasyMock.replay(processor);
        
        ServiceRegistration<?> registration = EasyMock.createMock(ServiceRegistration.class);
        registration.setProperties((Dictionary)EasyMock.anyObject());

        EasyMock.expectLastCall();
        EasyMock.replay(registration);

        final BundleContext mockContext = new MockBundleContext(registration);
        BundleContext context = EasyMock.createMock(BundleContext.class);
        EasyMock.expect(
        		(ServiceRegistration) context.registerService(
        				(String)EasyMock.anyObject(), 
        				(ManagedService)EasyMock.anyObject(), 
        				(Dictionary<String, ?>)EasyMock.anyObject())
        	).andAnswer((IAnswer<ServiceRegistration<?>>) new IAnswer<ServiceRegistration<?>>() {
        		public ServiceRegistration<?> answer() {
        			return mockContext.registerService((String) EasyMock.getCurrentArguments()[0], (ManagedService) EasyMock.getCurrentArguments()[1], (Dictionary<String, ?>) EasyMock.getCurrentArguments()[2]);
        		}
			});
        EasyMock.expect(
        		context.registerService(
        				(String)EasyMock.anyObject(), 
        				(TelnetCommand)EasyMock.anyObject(), 
        				(Dictionary<String, ?>)EasyMock.anyObject())).andReturn(null);
        EasyMock.replay(context);
        
        TelnetCommand command = new TelnetCommand(processor, context);
        Dictionary props = new Hashtable();
		props.put("port", TELNET_PORT);
		props.put("host", HOST);
		configurator.updated(props);
        
        Socket socketClient = null;
        try {
            socketClient = new Socket(HOST, Integer.parseInt(TELNET_PORT));
            OutputStream outClient = socketClient.getOutputStream();
            outClient.write(TEST_CONTENT);
            outClient.write('\n');
            outClient.flush();

            // wait for the accept thread to finish execution
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException ie) {
                // do nothing
            }
        } finally {
            if (socketClient != null) {
                socketClient.close();
            }
            command.telnet(new String[] {STOP_COMMAND});
        }
	}
	
	class MockBundleContext implements BundleContext {

		private ServiceRegistration<?> registration;
		
		public MockBundleContext(ServiceRegistration<?> mockRegistration) {
			this.registration = mockRegistration;
		}
		
		@Override
		public String getProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getBundle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle installBundle(String location, InputStream input)
				throws BundleException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle installBundle(String location) throws BundleException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getBundle(long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle[] getBundles() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addServiceListener(ServiceListener listener, String filter)
				throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addServiceListener(ServiceListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeServiceListener(ServiceListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addBundleListener(BundleListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeBundleListener(BundleListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addFrameworkListener(FrameworkListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeFrameworkListener(FrameworkListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ServiceRegistration<?> registerService(String[] clazzes,
				Object service, Dictionary<String, ?> properties) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceRegistration<?> registerService(String clazz,
				Object service, Dictionary<String, ?> properties) {
			configurator = (ManagedService) service;
			return registration;
		}

		@Override
		public <S> ServiceRegistration<S> registerService(Class<S> clazz,
				S service, Dictionary<String, ?> properties) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceReference<?>[] getServiceReferences(String clazz,
				String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceReference<?>[] getAllServiceReferences(String clazz,
				String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServiceReference<?> getServiceReference(String clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S> Collection<ServiceReference<S>> getServiceReferences(
				Class<S> clazz, String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S> S getService(ServiceReference<S> reference) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean ungetService(ServiceReference<?> reference) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public File getDataFile(String filename) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Filter createFilter(String filter) throws InvalidSyntaxException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bundle getBundle(String location) {
			// TODO Auto-generated method stub
			return null;
		}

		
		
	}
}
