package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import javax.ejb.Remote;

@Remote
public interface UserSessionHandlerRemote extends UserSessionHandler {

	void changePassword(String oldPassword, String newPassword);

	
}
