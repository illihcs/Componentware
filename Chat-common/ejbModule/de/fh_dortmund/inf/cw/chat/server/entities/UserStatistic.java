package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class UserStatistic extends Statistic {
	private static final long serialVersionUID = 1L;
	
	@Basic(optional = true)
//	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
}
