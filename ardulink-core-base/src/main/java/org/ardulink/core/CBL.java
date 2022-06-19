package org.ardulink.core;

import static org.ardulink.util.anno.LapsedWith.NEXT_ARDULINK_VERSION_REFACTORING_DONE;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Connection.Listener;
import org.ardulink.util.anno.LapsedWith;

@Deprecated
@LapsedWith(NEXT_ARDULINK_VERSION_REFACTORING_DONE)
// TODO purge this interface and fix compile errors
public interface CBL {

	void addRawListener(Listener rawListener);

	void removeRawListener(Listener rawListener);

	void write(byte[] bytes) throws IOException;
	
	boolean waitForArduinoToBoot(int waitsecs, TimeUnit timeUnit);

}
