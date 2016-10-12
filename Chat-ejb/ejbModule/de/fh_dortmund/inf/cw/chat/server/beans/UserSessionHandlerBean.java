package de.fh_dortmund.inf.cw.chat.server.beans;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerRemote;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;

@Stateful
public class UserSessionHandlerBean implements UserSessionHandlerLocal, UserSessionHandlerRemote {

	User user;
	
	
	@EJB
	private UserManagementLocal userManagement;
	
	@PostConstruct
	public void init(){
		user = new User();
	}
	
	@Override
	public void login(String userName, String password)  throws LoginException{
		userManagement.login(userName, password);
		
		user.setUserName(userName);
		user.setPasswordHash(password);
		user.setOnline(true);
	}

	//Logout and destroy Bean
	@Override
	public void logout() {
		userManagement.logout(user);
		user.setOnline(false);
		disconnect();
	}

	//Destroy Bean
	@Remove
	@Override
	public void disconnect() {}

	@Override
	public String getUserName() {
		return user.getUserName();
	}

	@Override
	public void delete(String password) {
		if(user.getPasswordHash().equals(password))
			userManagement.delete(user);
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		if(user.getPasswordHash().equals(oldPassword))
			userManagement.changePassword(user, newPassword);
	}

	
	
	
	
	
}
