package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries({
	@NamedQuery(name = UserStatistic.GET_USERSTATISTIC_QUERY, query = "select u from UserStatistic u where u.userName = :userName")
})


@Entity
public class UserStatistic extends Statistic {
	public static final String GET_USERSTATISTIC_QUERY = "UserStatistic$getUserStatistic";

	
	private static final long serialVersionUID = 1L;
	
	private Date lastLogin;

	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
}
