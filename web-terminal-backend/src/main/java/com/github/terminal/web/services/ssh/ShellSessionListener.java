package com.github.terminal.web.services.ssh;

public interface ShellSessionListener {
	void handleOutput(String output);
}