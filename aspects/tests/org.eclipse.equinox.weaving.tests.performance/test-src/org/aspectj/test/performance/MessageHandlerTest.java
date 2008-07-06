package org.aspectj.test.performance;

import junit.framework.TestCase;

public class MessageHandlerTest extends TestCase {

	public void testShutdownHook () {
		new PerformanceMessageHandler();
	}
}
