package com.github.terminal.web.services.ssh;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class ShellConsoleSessionManager {
	private final Map<String, SshSessionHolder> shellSessions = new HashMap<>();
	private final Map<String, ShellSession> shellSessionMap = new HashMap<>();
	
	public ShellConsoleSessionManager() {
		initShellSessionOutputChecker();
	}
	
	private void addNewSession(SshSessionHolder session) {
		session.setSshSessionHolderListener(new SshSessionHolderListener() {
			@Override
			public void onCloseSession(String sessionId) {
				removeSession(sessionId);
			}
		});
		shellSessions.put(session.getSessionId(), session);
	}
	
	public void initShellSessionOutputChecker() {
		Thread thread = new Thread(() -> {
			while (true) {
				shellSessionMap.values().forEach(shellSession -> {
					try {
						shellSession.checkOutput();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public ShellSession openNewSession(SshSessionHolder.ConnectionInfo connectionInfo, ShellSessionListener shellSessionListener) {
		SshSessionHolder session = new SshSessionHolder(connectionInfo);
		addNewSession(session);
		ShellSession shellSession = new ShellSession(session, shellSessionListener);
		shellSessionMap.put(session.getSessionId(), shellSession);
		return shellSession;
		
	}
	
	
	public void close(String sessionId) {
		SshSessionHolder shellSshSessionHolder = shellSessions.get(sessionId);
		if (shellSshSessionHolder != null) {
			ShellSession shellSession = shellSessionMap.get(sessionId);
			shellSession.stop();
			log.info("Session closed '{}'", sessionId);
			shellSshSessionHolder.close();
			removeSession(sessionId);
		}
	}
	
	private void removeSession(String sessionId) {
		shellSessions.remove(sessionId);
		shellSessionMap.remove(sessionId);
	}
	
	public ShellSession getShellSession(String id) {
		return shellSessionMap.get(id);
	}
	
}
