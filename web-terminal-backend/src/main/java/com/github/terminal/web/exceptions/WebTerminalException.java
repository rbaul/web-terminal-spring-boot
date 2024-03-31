package com.github.terminal.web.exceptions;

import lombok.Getter;

@Getter
public class WebTerminalException extends RuntimeException {

    public WebTerminalException(String message) {
        super(message);
    }

    public WebTerminalException(String message, Throwable e) {
        super(message, e);
    }
}
