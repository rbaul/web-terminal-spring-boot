package com.github.terminal.web.services.ssh;

public interface SshSessionHolderListener {
	default void onCloseSession(String sessionId) {}
	
	default void onCloseChannel(String sessionId, int channelId) {}
}
