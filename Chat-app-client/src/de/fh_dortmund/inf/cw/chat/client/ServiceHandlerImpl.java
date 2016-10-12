package de.fh_dortmund.inf.cw.chat.client;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.chat.client.shared.ServiceHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.UserSessionHandler;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerRemote;

public class ServiceHandlerImpl extends ServiceHandler implements UserSessionHandler {

	private Context ctx;
	private UserSessionHandlerRemote userSessionHandler;
	private UserManagementRemote userManagement;

	// Singleton Instance
	private static ServiceHandlerImpl instance;

	// Singleton Constructor
	private ServiceHandlerImpl() {
		try {
			ctx = new InitialContext();

			userSessionHandler = (UserSessionHandlerRemote) ctx.lookup(
					"java:global/Chat-ear/Chat-ejb/UserSessionHandlerBean!de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerRemote");
			userManagement = (UserManagementRemote) ctx.lookup(
					"java:global/Chat-ear/Chat-ejb/UserManagementBean!de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	};

	// getInstance method
	public static ServiceHandlerImpl getInstance() {
		if (instance == null)
			instance = new ServiceHandlerImpl();

		return instance;
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) throws Exception {
		userSessionHandler.changePassword(oldPassword, newPassword);
	}

	@Override
	public void delete(String password) throws Exception {
		userSessionHandler.delete(password);
		
	}

	@Override
	public void disconnect() {
		userSessionHandler.disconnect();
	}

	@Override
	public int getNumberOfOnlineUsers() {
		return userManagement.getNumberOfOnlineUsers();
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		return userManagement.getNumberOfRegisteredUsers();
	}

	@Override
	public List<String> getOnlineUsers() {
		return userManagement.getOnlineUsers();
	}

	@Override
	public String getUserName() {
			return userSessionHandler.getUserName();
	}

	@Override
	public void login(String userName, String password) throws Exception {
		userSessionHandler.login(userName, password);
	}

	@Override
	public void logout() throws Exception {
		userSessionHandler.logout();
	}

	@Override
	public void register(String userName, String password) throws Exception {
		userManagement.register(userName, password);
	}
}
