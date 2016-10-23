package de.fh_dortmund.inf.cw.chat.server.entities;

import java.io.Serializable;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;
	private String userName;
	private String passwordHash;
	private boolean online;
	
	public User(){}
	
	
	public User(String userName, String passwordHash)
	{
		this.userName = userName;
		this.passwordHash = passwordHash;
		this.online = false;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
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
