package org.eclipse.equinox.log;

import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

public interface ExtendedLogReaderService extends LogReaderService {

	public void addLogListener(LogListener listener, LogFilter filter);
}
