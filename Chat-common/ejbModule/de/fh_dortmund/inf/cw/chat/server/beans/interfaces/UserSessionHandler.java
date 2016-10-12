package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;

public interface UserSessionHandler {

	public void login(String userName, String password) throws LoginException;

	public void logout();

	public void disconnect();

	public String getUserName();

	public void delete(String password);
	

}
