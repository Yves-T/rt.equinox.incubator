package org.eclipse.equinox.log.test;

import org.osgi.framework.Bundle;

import org.eclipse.equinox.log.LogFilter;

import org.eclipse.equinox.log.ExtendedLogReaderService;

import org.eclipse.equinox.log.ExtendedLogService;

import junit.framework.TestCase;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class ExtendedLogServiceTest extends TestCase {

	private ExtendedLogService log;
	private ServiceReference logReference;
	private ExtendedLogReaderService reader;
	private ServiceReference readerReference;
	private TestListener listener;

	public ExtendedLogServiceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		Activator.getBundle("org.eclipse.equinox.log").start();
		logReference = Activator.getBundleContext().getServiceReference(ExtendedLogService.class.getName());
		readerReference = Activator.getBundleContext().getServiceReference(ExtendedLogReaderService.class.getName());

		log = (ExtendedLogService) Activator.getBundleContext().getService(logReference);
		reader = (ExtendedLogReaderService) Activator.getBundleContext().getService(readerReference);

		listener = new TestListener();
		reader.addLogListener(listener);
	}

	protected void tearDown() throws Exception {
		reader.removeLogListener(listener);
		Activator.getBundleContext().ungetService(logReference);
		Activator.getBundleContext().ungetService(readerReference);
		Activator.getBundle("org.eclipse.equinox.log").stop();
	}

	public void testLogContext() throws Exception {
		synchronized (listener) {
			log.log(this, LogService.LOG_INFO, null);
			listener.wait();
		}
		assertTrue(listener.getEntryX().getContext() == this);
	}

	public void testNullLogContext() throws Exception {
		synchronized (listener) {
			log.log(null, LogService.LOG_INFO, null);
			listener.wait();
		}
		assertTrue(listener.getEntryX().getContext() == null);
	}

	public void testLogContextWithNullThrowable() throws Exception {
		synchronized (listener) {
			log.log(this, LogService.LOG_INFO, null, null);
			listener.wait();
		}
		assertTrue(listener.getEntryX().getContext() == this);
	}

	public void testIsLoggableTrue() throws Exception {
		if (!log.isLoggable(LogService.LOG_INFO))
			fail();
	}

	public void testNotIsLoggableWithNoListener() throws Exception {
		reader.removeLogListener(listener);
		if (log.isLoggable(LogService.LOG_INFO))
			fail();
	}

	public void testNotIsLoggableWithListener() throws Exception {
		reader.addLogListener(listener, new LogFilter() {

			public boolean isLoggable(Bundle b, String loggerName, int logLevel) {
				return false;
			}

		});
		if (log.isLoggable(LogService.LOG_INFO))
			fail();
	}
}
