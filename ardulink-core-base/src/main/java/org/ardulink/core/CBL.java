package org.ardulink.core;

import static org.ardulink.util.anno.LapsedWith.NEXT_ARDULINK_VERSION_REFACTORING_DONE;

import java.util.concurrent.TimeUnit;

import org.ardulink.util.anno.LapsedWith;

@Deprecated
@LapsedWith(NEXT_ARDULINK_VERSION_REFACTORING_DONE)
// TODO purge this interface and fix compile errors
public interface CBL {
	Connection getConnection();

	boolean waitForArduinoToBoot(int waitsecs, TimeUnit timeUnit);
}
