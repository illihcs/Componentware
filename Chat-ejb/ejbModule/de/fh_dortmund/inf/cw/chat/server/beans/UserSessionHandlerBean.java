package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.StatisticManagementLocal;
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
	@EJB
	private StatisticManagementLocal statisticManagement;

	@PostConstruct
	public void init(){
		user = new User();
	}
	
	@Override
	public void login(String userName, String password)  throws LoginException{
		userManagement.login(userName, password);
		
		user.setUserName(userName);
		user.setPasswordHash(userManagement.generateHash(password));
		user.setOnline(true);
		
		statisticManagement.createUserStatisticIfNotExisting(user);
		statisticManagement.incrementLoginCount(user,new Date());
	}

	//Logout and destroy Bean
	@Override
	public void logout() {
		if(user.isOnline()){
			userManagement.logout(user);
			user.setOnline(false);
			statisticManagement.incrementLogoutCount(user);
		}

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
		if(user.getPasswordHash().equals(userManagement.generateHash(password)))
		{
			userManagement.delete(user);
			logout();
		}
	}

	//oldPassword überflüssig???
	@Override
	public void changePassword(String oldPassword, String newPassword) {
		userManagement.changePassword(user, newPassword);
	}
}
