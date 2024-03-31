package com.github.terminal.web.services.ssh;

import com.github.terminal.web.exceptions.SshWebTerminalException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Getter
public class SshSessionHolder implements Closeable {
	
	private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	private static final int DEFAULT_PORT = 22;
	private static final int TERMINAL_HEIGHT = 1000;
	private static final int TERMINAL_WIDTH = 1000;
	private static final int TERMINAL_WIDTH_IN_PIXELS = 1000;
	private static final int TERMINAL_HEIGHT_IN_PIXELS = 1000;
	private static final int DEFAULT_WAIT_TIMEOUT = 100;
	
	private final String sessionId;
	private final ConnectionInfo connectionInfo;
	private final Session session;
	private final Map<Integer, ChannelHolder<?>> channels = new HashMap<>();
	
	private final static Map<String, String> noHostKeyChecking = getProperties();
	
	private static Map<String, String> getProperties() {
		Map<String, String> props = new HashMap<>();
		props.put("StrictHostKeyChecking", "no");
		props.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
		
		return Collections.unmodifiableMap(props);
	}
	
	@Setter
	private SshSessionHolderListener sshSessionHolderListener;
	
	public SshSessionHolder(ConnectionInfo connectionInfo) {
		this(connectionInfo, noHostKeyChecking);
	}
	
	public SshSessionHolder(ConnectionInfo connectionInfo, Map<String, String> props) {
		this.sessionId = generateSessionHolderId();
		this.connectionInfo = connectionInfo;
		this.session = newSession(props);
	}
	
	public <C extends Channel> ChannelHolder<C> openChannel(Class<C> channelTypeClass) {
		ChannelType channelType = ChannelType.getByClass(channelTypeClass).orElseThrow(() -> new SshWebTerminalException("Not channel support"));
		ChannelHolder<C> channelHolder = new ChannelHolder<>(session, channelType, channelId -> {
			closeChannel(channelId);
			return null;
		});
		channels.put(channelHolder.getId(), channelHolder);
		return channelHolder;
	}
	
	public void closeChannel(int channelId) {
		ChannelHolder<?> channelHolder = channels.get(channelId);
		if (channelHolder != null) {
			channelHolder.close();
			channels.remove(channelId);
			if (sshSessionHolderListener != null && channelHolder.getType() == ChannelType.SHELL) {
				sshSessionHolderListener.onCloseChannel(sessionId, channelId);
			}
		}
	}
	
	private String generateSessionHolderId() {
		return UUID.randomUUID().toString();
	}
	
	private Session newSession(Map<String, String> props) {
		try {
			Properties config = new Properties();
			config.putAll(props);
			
			JSch jsch = new JSch();
			Session newSession = jsch.getSession(connectionInfo.getUsername(), connectionInfo.getHost(), connectionInfo.getPort());
			newSession.setPassword(connectionInfo.getPassword());
			newSession.setUserInfo(new User(connectionInfo.getUsername(), connectionInfo.getPassword()));
			newSession.setDaemonThread(true);
			newSession.setConfig(config);
			newSession.connect(DEFAULT_CONNECT_TIMEOUT);
			return newSession;
		} catch (JSchException e) {
			throw new SshSessionHolderException(String.format("Cannot create session for '%s'", connectionInfo), e);
		}
	}
	
	@Override
	public void close() {
		if (channels != null) {
			channels.values().forEach(ChannelHolder::close);
		}
		if (session != null) {
			session.disconnect();
		}
		if (sshSessionHolderListener != null) {
			sshSessionHolderListener.onCloseSession(sessionId);
		}
	}
	
	@Getter
	@RequiredArgsConstructor
	public enum ChannelType {
		SHELL(Constants.SHELL_CHANNEL, ChannelShell.class),
		EXEC(Constants.EXEC_CHANNEL, ChannelExec.class),
		SFTP(Constants.SFTP_CHANNEL, ChannelSftp.class);
		
		private final String type;
		private final Class<? extends Channel> channelClassType;
		
		public static Optional<ChannelType> getByClass(Class<? extends Channel> channelClassType) {
			return Arrays.stream(values())
					.filter(channelType -> channelClassType == channelType.getChannelClassType())
					.findFirst();
		}
		
		public static class Constants {
			public static final String EXEC_CHANNEL = "exec";
			public static final String SHELL_CHANNEL = "shell";
			public static final String SFTP_CHANNEL = "sftp";
		}
	}
	
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class User implements UserInfo, UIKeyboardInteractive {
		
		private String username;
		private String password;
		
		@Override
		public String getPassword() {
			return password;
		}
		
		@Override
		public boolean promptYesNo(String str) {
			return false;
		}
		
		@Override
		public String getPassphrase() {
			return username;
		}
		
		@Override
		public boolean promptPassphrase(String message) {
			return true;
		}
		
		@Override
		public boolean promptPassword(String message) {
			return true;
		}
		
		@Override
		public void showMessage(String message) {
			// do nothing
		}
		
		@Override
		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
			return null;
		}
	}
	
	public static class SshSessionHolderException extends RuntimeException {
		public SshSessionHolderException(String message) {
			super(message);
		}
		
		public SshSessionHolderException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * Connection uri
	 * ssh://user:pass@host/work/dir/path
	 */
//	public static URI createUri(String ip, String username, String password, String directory) {
//		return URI.create(String.format("ssh://%s:%s@%s%s",
//				URLEncoder.encode(username, StandardCharsets.UTF_8),
//				URLEncoder.encode(password, StandardCharsets.UTF_8),
//				ip,
//				directory == null ? "" : directory));
//	}
	
	@Getter
	@Setter
	@Builder
	@ToString
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ChannelHolder<C extends Channel> implements Closeable {
		private ChannelType type;
		
		private C channel;
		
		private Function<Integer, Void> closeCallback;
		
		public ChannelHolder(Session session, ChannelType type, Function<Integer, Void> closeCallback) {
			this.type = type;
			this.closeCallback = closeCallback;
			this.channel = newChannel(session, type);
		}
		
		public int getId() {
			return getChannel().getId();
		}
		
		@SuppressWarnings("unchecked")
		private C newChannel(Session session, ChannelType channelType) {
			try {
				Channel newChannel = session.openChannel(channelType.getType());
				if (newChannel instanceof ChannelShell) {
					ChannelShell channelShell = (ChannelShell) newChannel;
					channelShell.setPtyType("ANSI", TERMINAL_WIDTH, TERMINAL_HEIGHT, TERMINAL_WIDTH_IN_PIXELS, TERMINAL_HEIGHT_IN_PIXELS);
				}
				return (C) newChannel;
			} catch (JSchException e) {
				throw new SshSessionHolderException(String.format("Cannot create '%s' channel", channelType), e);
			}
		}
		
		public void assertExitStatus(String failMessage) {
			if (channel.getExitStatus() != 0 || StringUtils.hasText(failMessage)) {
				throw new SshSessionHolderException(String.format("Exit status %s\n%s", channel.getExitStatus(), failMessage));
			}
		}
		
		public void execute() throws JSchException, InterruptedException {
			channel.connect();
			channel.start();
			while (!channel.isEOF()) {
				Thread.sleep(DEFAULT_WAIT_TIMEOUT);
			}
		}
		
		@Override
		public void close() {
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
				if (closeCallback != null) {
					closeCallback.apply(channel.getId());
				}
			}
		}
	}
	
	@Getter
	@Setter
	@Builder
	@ToString
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ConnectionInfo {
		private static final int DEFAULT_PORT = 22;
		
		private String host;
		private String username;
		private String password;
		private Integer port;
		private String directory;
		
		public Integer getPort() {
			return port == null ? DEFAULT_PORT : port;
		}
	}
}
