package org.eclipse.equinox.log.test;

import junit.framework.TestCase;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class LogServiceTest extends TestCase {

	private LogService log;
	private ServiceReference logReference;
	private LogReaderService reader;
	private ServiceReference readerReference;
	private TestListener listener;

	public LogServiceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		Activator.getBundle("org.eclipse.equinox.log").start();
		logReference = Activator.getBundleContext().getServiceReference(LogService.class.getName());
		readerReference = Activator.getBundleContext().getServiceReference(LogReaderService.class.getName());

		log = (LogService) Activator.getBundleContext().getService(logReference);
		reader = (LogReaderService) Activator.getBundleContext().getService(readerReference);

		listener = new TestListener();
		reader.addLogListener(listener);
	}

	protected void tearDown() throws Exception {
		reader.removeLogListener(listener);
		Activator.getBundleContext().ungetService(logReference);
		Activator.getBundleContext().ungetService(readerReference);
		Activator.getBundle("org.eclipse.equinox.log").stop();
	}

	public void testLogDebug() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_DEBUG, "debug");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_DEBUG);
	}

	public void testLogError() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_ERROR, "error");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_ERROR);
	}

	public void testLogInfo() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_INFO, "info");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_INFO);
	}

	public void testLogWarning() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_WARNING, "warning");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_WARNING);
	}

	public void testLogZeroLevel() throws Exception {
		synchronized (listener) {
			log.log(0, "zero");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == 0);
	}

	public void testLogNegativeLevel() throws Exception {
		synchronized (listener) {
			log.log(-1, "negative");
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == -1);
	}

	public void testLogMessage() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_INFO, "message");
			listener.wait();
		}
		assertTrue(listener.getEntry().getMessage().equals("message"));
	}

	public void testLogNullMessage() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_INFO, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getMessage() == null);
	}

	public void testLogThrowable() throws Exception {
		Throwable t = new Throwable("throwable");
		synchronized (listener) {
			log.log(LogService.LOG_INFO, null, t);
			listener.wait();
		}
		assertTrue(listener.getEntry().getException().getMessage().equals(t.getMessage()));
	}

	public void testLogNullThrowable() throws Exception {
		synchronized (listener) {
			log.log(LogService.LOG_INFO, null, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getException() == null);
	}

	public void testLogServiceReference() throws Exception {
		synchronized (listener) {
			log.log(logReference, LogService.LOG_INFO, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getServiceReference().equals(logReference));
	}

	public void testNullLogServiceReference() throws Exception {
		synchronized (listener) {
			log.log(null, LogService.LOG_INFO, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogServiceReferenceWithNullThrowable() throws Exception {
		synchronized (listener) {
			log.log(logReference, LogService.LOG_INFO, null, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getServiceReference().equals(logReference));
	}

	public void testLogNull1() throws Exception {
		synchronized (listener) {
			log.log(0, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == 0);
		assertTrue(listener.getEntry().getMessage() == null);
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogNull2() throws Exception {
		synchronized (listener) {
			log.log(0, null, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == 0);
		assertTrue(listener.getEntry().getMessage() == null);
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogNull3() throws Exception {
		synchronized (listener) {
			log.log(null, 0, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == 0);
		assertTrue(listener.getEntry().getMessage() == null);
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogNull4() throws Exception {
		synchronized (listener) {
			log.log(null, 0, null, null);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == 0);
		assertTrue(listener.getEntry().getMessage() == null);
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogFull1() throws Exception {
		String message = "test";
		synchronized (listener) {
			log.log(LogService.LOG_INFO, message);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_INFO);
		assertTrue(listener.getEntry().getMessage().equals(message));
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogFull2() throws Exception {
		String message = "test";
		Throwable t = new Throwable("test");
		synchronized (listener) {
			log.log(LogService.LOG_INFO, message, t);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_INFO);
		assertTrue(listener.getEntry().getMessage().equals(message));
		assertTrue(listener.getEntry().getException().getMessage().equals(t.getMessage()));
		assertTrue(listener.getEntry().getServiceReference() == null);
	}

	public void testLogFull3() throws Exception {
		String message = "test";
		synchronized (listener) {
			log.log(logReference, LogService.LOG_INFO, message);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_INFO);
		assertTrue(listener.getEntry().getMessage().equals(message));
		assertTrue(listener.getEntry().getException() == null);
		assertTrue(listener.getEntry().getServiceReference() == logReference);
	}

	public void testLogFull4() throws Exception {
		String message = "test";
		Throwable t = new Throwable("test");
		synchronized (listener) {
			log.log(logReference, LogService.LOG_INFO, message, t);
			listener.wait();
		}
		assertTrue(listener.getEntry().getLevel() == LogService.LOG_INFO);
		assertTrue(listener.getEntry().getMessage().equals(message));
		assertTrue(listener.getEntry().getException().getMessage().equals(t.getMessage()));
		assertTrue(listener.getEntry().getServiceReference() == logReference);
	}

}
