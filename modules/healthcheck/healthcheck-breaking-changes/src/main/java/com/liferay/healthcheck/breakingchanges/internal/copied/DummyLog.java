package com.liferay.healthcheck.breakingchanges.internal.copied;

import java.util.LinkedList;

public class DummyLog {
	public void error(String message) {
		_messages.add(message);
	}
	public void error(String message, Exception e) {
		_messages.add(message + " " + e.getClass().getName() + " " + e.getMessage());
	}
//	public void warn(String message) {
//		_messages.add(message);
//	}
	public void warn(String message, Exception e) {
		_messages.add(message + " " + e.getClass().getName() + " " + e.getMessage());
	}
	public boolean isWarnEnabled() {
		return true;
	}
	public LinkedList<String> popMessages() {
		 LinkedList<String> result = _messages;
		 _messages = new  LinkedList<String>();
		 return result;
	}
	private LinkedList<String> _messages = new LinkedList<String>();
}
