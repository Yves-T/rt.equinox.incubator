package org.eclipse.equinox.console.ssh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.DefaultConnectFuture;
import org.apache.sshd.server.Environment;
import org.easymock.EasyMock;
import org.eclipse.equinox.console.common.ConsoleInputStream;
import org.eclipse.equinox.console.storage.DigestUtil;
import org.eclipse.equinox.console.storage.SecureUserStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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


public class SshCommandWithConfigAdminTests {
	private static final int TEST_CONTENT = 100;
	private static final String USER_STORE_FILE_NAME = "org.eclipse.equinox.console.jaas.file";
	private static final String JAAS_CONFIG_FILE_NAME = "jaas.config";
	private static final String JAAS_CONFIG_PROPERTY_NAME = "java.security.auth.login.config";
	private static final String DEFAULT_USER_STORAGE = "osgi.console.ssh.useDefaultSecureStorage";
	private static final String STORE_FILE_NAME = SshCommandTests.class.getName() + "_store";
	private static final String GOGO_SHELL_COMMAND = "gosh --login --noshutdown";
	private static final String TRUE = "true";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String STOP_COMMAND = "stop";
	private static final String TERM_PROPERTY = "TERM";
	private static final String XTERM = "XTERM";
	private static final String HOST = "localhost";
	private static final String SSH_PORT = "2222";
	private static final long WAIT_TIME = 5000;
	private static final String USE_CONFIG_ADMIN_PROP = "osgi.console.useConfigAdmin";
	private ManagedService configurator;

	@Before
	public void init() throws Exception {
		clean();
		initStore();
        initJaasConfigFile();
	}
	
	@Test
	public void testSshCommandWithConfigAdmin() throws Exception {
		
		System.setProperty(USE_CONFIG_ADMIN_PROP, "true");
		
		CommandSession session = EasyMock.createMock(CommandSession.class);
		session.put((String)EasyMock.anyObject(), EasyMock.anyObject());
		EasyMock.expectLastCall().times(3);
		EasyMock.expect(session.execute(GOGO_SHELL_COMMAND)).andReturn(null);
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
        
		BundleContext context = EasyMock.createMock(BundleContext.class);
		EasyMock.expect(context.getProperty(DEFAULT_USER_STORAGE)).andReturn(TRUE).anyTimes();
		EasyMock.expect(
        		context.registerService(
        				(String)EasyMock.anyObject(), 
        				(ManagedService)EasyMock.anyObject(), 
        				(Dictionary<String, ?>)EasyMock.anyObject())
        	).andDelegateTo(new MockBundleContext(registration));
        EasyMock.expect(
        		context.registerService(
        				(String)EasyMock.anyObject(), 
        				(SshCommand)EasyMock.anyObject(), 
        				(Dictionary<String, ?>)EasyMock.anyObject())).andReturn(null);
		EasyMock.replay(context);

		Map<String, String> environment = new HashMap<String, String>();
		environment.put(TERM_PROPERTY, XTERM);
		Environment env = EasyMock.createMock(Environment.class);
		EasyMock.expect(env.getEnv()).andReturn(environment);
		EasyMock.replay(env);

		SshCommand command = new SshCommand(processor, context);
		Dictionary props = new Hashtable();
		props.put("port", SSH_PORT);
		props.put("host", HOST);
		configurator.updated(props);

		SshClient client = SshClient.setUpDefaultClient();
		client.start();
		try {
			ConnectFuture connectFuture = client.connect(HOST, Integer.valueOf(SSH_PORT));
			DefaultConnectFuture defaultConnectFuture = (DefaultConnectFuture) connectFuture;

			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException ie) {
				// do nothing
			}
			ClientSession sshSession = defaultConnectFuture.getSession();

			int ret = ClientSession.WAIT_AUTH;                
			sshSession.authPassword(USERNAME, PASSWORD);
			ret = sshSession.waitFor(ClientSession.WAIT_AUTH | ClientSession.CLOSED | ClientSession.AUTHED, 0);

			if ((ret & ClientSession.CLOSED) != 0) {
				System.err.println("error");
				System.exit(-1);
			}
			ClientChannel channel = sshSession.createChannel("shell");
			channel.setIn(new StringBufferInputStream(TEST_CONTENT + "\n"));
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			channel.setOut(byteOut);
			channel.setErr(byteOut);
			channel.open();
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException ie) {
				// do nothing
			}
			byte[] output = byteOut.toByteArray();
			Assert.assertEquals("Output not as expected",Integer.toString(TEST_CONTENT), new String(output).trim());
			sshSession.close(true);
		} finally {
			client.stop();
		}

		command.ssh(new String[] {STOP_COMMAND});
		return;
	}
	
	@After
	public void cleanUp() {
		clean();
	}
	
	private void clean() {
		System.setProperty(USER_STORE_FILE_NAME, "");
    	File file = new File(STORE_FILE_NAME);
    	if (file.exists()) {
    		file.delete();
    	}
    	
    	System.setProperty(JAAS_CONFIG_PROPERTY_NAME, "");
    	File jaasConfFile = new File(JAAS_CONFIG_FILE_NAME);
    	if (jaasConfFile.exists()) {
    		jaasConfFile.delete();
    	}
	}
	
	private void initStore() throws Exception {
		System.setProperty(USER_STORE_FILE_NAME, STORE_FILE_NAME);
        SecureUserStore.initStorage();
        SecureUserStore.putUser(USERNAME, DigestUtil.encrypt(PASSWORD), null);
	}
	
	private void initJaasConfigFile() throws Exception {
		System.setProperty(JAAS_CONFIG_PROPERTY_NAME, JAAS_CONFIG_FILE_NAME);
		File jaasConfFile = new File(JAAS_CONFIG_FILE_NAME);
    	if (!jaasConfFile.exists()) {
    		PrintWriter out = null;
    		try {
				out = new PrintWriter(jaasConfFile);
				out.println("equinox_console {");
				out.println("	org.eclipse.equinox.console.jaas.SecureStorageLoginModule REQUIRED;");
				out.println("};");
			} finally {
				if (out != null) {
					out.close();
				}
			}
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
