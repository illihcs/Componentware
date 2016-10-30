package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;

public class Statistic implements Serializable {
	private static final long serialVersionUID = 1L;
	@Basic(optional = false)
	@Column(nullable = false)
	private int logins;
	@Basic(optional = false)
	@Column(nullable = false)
	private int logouts;
	@Basic(optional = false)
	@Column(nullable = false)
	private int messages;

	public int getLogins() {
		return logins;
	}
	public void setLogins(int logins) {
		this.logins = logins;
	}
	
	public int getLogouts() {
		return logouts;
	
	}
	public void setLogouts(int logouts) {
		this.logouts = logouts;
	}

	public int getMessages() {
		return messages;
	}
	public void setMessages(int messages) {
		this.messages = messages;
	}
}
