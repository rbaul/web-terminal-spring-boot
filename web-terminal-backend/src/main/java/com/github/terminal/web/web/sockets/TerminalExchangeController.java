package com.github.terminal.web.web.sockets;

import com.github.terminal.web.services.ssh.ShellConsoleSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class TerminalExchangeController {
	
	private final ShellConsoleSessionManager shellConsoleSessionManager;
	
//	@MessageMapping("/terminal-exchange/{sessionId}")
//	@SendTo("/topic/messages/{sessionId}")
//	public OutputMessage send(@DestinationVariable final String sessionId, final String input) throws Exception {
//
//		return new OutputMessage(message.getFrom(), message.getText(), time);
//	}
}
