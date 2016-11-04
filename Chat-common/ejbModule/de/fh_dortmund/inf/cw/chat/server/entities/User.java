package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Table(name = "users")
@NamedQueries({
	@NamedQuery(name = "GET_USER_QUERY", query = "select u from User u where u.userName = :userName AND u.passwordHash = :passwordHash"),
	@NamedQuery(name = "GET_USER_BY_NAME_QUERY", query = "select u from User u where u.userName = :userName"),
	@NamedQuery(name = "COUNT_REGISTERED_USER", query = "select count (u) from User u"),
	@NamedQuery(name = "GET_USERSTATISTIC_QUERY", query = "select u.statistic from User u where u.userName = :userName")	
})

@Entity
public class User extends BaseEntity implements Serializable{
	private static final long serialVersionUID = 1L;

	@Basic(optional = false)
	@Column(nullable = false)
	private String userName;
	@Basic(optional = false)
	@Column(nullable = false)
	private String passwordHash;
	@JoinColumn(nullable=true, name="statistic")
	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	private UserStatistic statistic;
	
//	@Basic(optional = false)
//	@Column(nullable = false)
//	private boolean online;
	
	public User(){
		statistic = new UserStatistic();
	}
	
	
	public User(String userName, String passwordHash)
	{
		this.userName = userName;
		this.passwordHash = passwordHash;
		statistic = new UserStatistic();
		//this.online = false;
	}
	
//	public boolean isOnline() {
//		return online;
//	}
//	
//	public void setOnline(boolean online) {
//		this.online = online;
//	}
//	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}


	public UserStatistic getStatistic() {
		return statistic;
	}


	public void setStatistic(UserStatistic statistic) {
		this.statistic = statistic;
	}	
}
