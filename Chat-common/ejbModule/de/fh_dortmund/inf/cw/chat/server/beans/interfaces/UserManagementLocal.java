package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import javax.ejb.Local;

import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;

@Local
public interface UserManagementLocal extends UserManagement {

	public void login(String userName, String password) throws LoginException;

	public void logout(User user);

	public void delete(User user);

	public void changePassword(User user, String newPassword);

}
