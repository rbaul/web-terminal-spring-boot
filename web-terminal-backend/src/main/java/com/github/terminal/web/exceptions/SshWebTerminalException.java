package com.github.terminal.web.exceptions;

import lombok.Getter;

@Getter
public class SshWebTerminalException extends WebTerminalException {

    public SshWebTerminalException(String message) {
        super(message);
    }

    public SshWebTerminalException(String message, Throwable e) {
        super(message, e);
    }
}
