package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Table(name = "users")
@NamedQueries({
	@NamedQuery(name = User.GET_USER_QUERY, query = "select u from User u where u.userName = :userName AND u.passwordHash = :passwordHash"),
	@NamedQuery(name = User.COUNT_REGISTERED_USER, query = "count * from User")
})

@Entity
public class User implements Serializable{
	private static final long serialVersionUID = 1L;

	public static final String GET_USER_QUERY = "User$getUser";
	public static final String COUNT_REGISTERED_USER = "User$countRegisteredUser";
		
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private UUID uuID;
	@Basic(optional = false)
	@Column(nullable = false)
	private String userName;
	@Basic(optional = false)
	@Column(nullable = false)
	private String passwordHash;
//	@Basic(optional = false)
//	@Column(nullable = false)
//	private boolean online;
	
	public User(){}
	
	
	public User(String userName, String passwordHash)
	{
		this.userName = userName;
		this.passwordHash = passwordHash;
		//this.online = false;
	}
	
	public UUID getUUID() {
		return uuID;
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
}
