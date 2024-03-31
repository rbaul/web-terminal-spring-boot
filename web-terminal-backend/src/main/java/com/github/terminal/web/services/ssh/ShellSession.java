package com.github.terminal.web.services.ssh;

import com.jcraft.jsch.ChannelShell;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Slf4j
public class ShellSession {
	
	public static final int PIPE_SIZE = 4096;
	
	private PipedOutputStream poutWrapper;
	
	private PipedInputStream pin = new PipedInputStream(PIPE_SIZE);
	
	private PipedInputStream pinWrapper = new PipedInputStream(PIPE_SIZE);
	
	private PipedOutputStream pout;
	
	private String lastCommand;
	
	private String sessionId;
	
	private SshSessionHolder.ChannelHolder<ChannelShell> channelHolder;
	
	private ShellSessionListener shellSessionListener;
	
	/**
	 * Constructs a new {@code ShellSession} {@code Object}.
	 */
	public ShellSession(SshSessionHolder session, ShellSessionListener shellSessionListener) {
		try {
			this.sessionId = session.getSessionId();
			pout = new PipedOutputStream(pinWrapper);
			poutWrapper = new PipedOutputStream(pin);
			this.shellSessionListener = shellSessionListener;
			start(session);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start this shell session.
	 *
	 * @param session The ssh session.
	 */
	private void start(SshSessionHolder session) {
		try {
			channelHolder = session.openChannel(ChannelShell.class);
			ChannelShell shellChannel = channelHolder.getChannel();
			shellChannel.setInputStream(pin);
			shellChannel.setOutputStream(pout);
			shellChannel.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void checkOutput() throws IOException {
		if (pinWrapper.available() != 0) {
			String response = readResponse();
			System.out.print(response);
			if (shellSessionListener != null) {
				shellSessionListener.handleOutput(response);
			}
		}
	}
	
	/**
	 * Stop this shell session.
	 */
	public void stop() {
		channelHolder.close();
	}
	
	/**
	 * Send a message to the shell.
	 *
	 * @param message The message to send.
	 */
	public void write(String message) {
		lastCommand = String.format("%s\n", message);
		try {
			poutWrapper.write(lastCommand.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read the response from {@code pinWrapper}.
	 *
	 * @return The string.
	 * @throws IOException If it could not read the piped stream.
	 */
	private synchronized String readResponse() throws IOException {
		final StringBuilder s = new StringBuilder();
		while (pinWrapper.available() > 0) {
			s.append((char) pinWrapper.read());
		}
		return s.toString();
	}
}