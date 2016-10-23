package de.fh_dortmund.inf.cw.chat.server.beans;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateful;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.ChatLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.ChatRemote;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;

@Stateful
public class ChatBean implements ChatLocal, ChatRemote {

	@EJB
	private UserManagementLocal userManagement;

	// Chat-Nutzer
	private User user;

	public ChatBean() {
	}

	@PostConstruct
	private void init() {
		user = null;
	}

	@Override
	public User login(User user) throws Exception {
		if (user == null) {
			throw new Exception("Invalid user", new IllegalArgumentException("User should not be null."));
		}
		System.out.println(String.format("login('%s')", user.toString()));

		User loggedinUser;
		try {
			loggedinUser = userManagement.login(user.getUserName(), user.getPasswordHash());
		} catch (LoginException e) {
			throw new Exception(e.getMessage(), e);
		}
		this.user = loggedinUser;

		return loggedinUser;
	}

	@Override
	public void logout() throws Exception {
		System.out.println(String.format("logout"));
		try {
			userManagement.logout(user);
			user = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public User getUser() {
		System.out.println(String.format("getUser"));
		return user;
	}

	@Override
	public User changePassword(String oldPasswordHash, String newPasswordHash) throws Exception {
		System.out.println("changePassword from" + oldPasswordHash + "to" + newPasswordHash);

		user = userManagement.changePassword(user, newPasswordHash);
		return user;
	}

	@Override
	public void deleteUser(String passwordHash) throws Exception {
		System.out.println("deleteUser");

		userManagement.delete(user);
		user = null;
	}

	@Override
	public void close() {
		System.out.println("close");

		user = null;
	}
}
