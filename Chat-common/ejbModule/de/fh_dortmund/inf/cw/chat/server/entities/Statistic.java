package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
//@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Statistic extends BaseEntity implements Serializable {
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
