package com.github.terminal.web.web.sockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TerminalPushMessages {
	
	private final SimpMessagingTemplate simpMessagingTemplate;
	
	public void sendToTerminal(String sessionId, String output) {
		simpMessagingTemplate.convertAndSend("/topic/terminal-exchange/" + sessionId, output);
	}
}
