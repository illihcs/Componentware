package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import java.util.List;

public interface UserManagement {

	public List<String> getOnlineUsers();

	public int getNumberOfRegisteredUsers();

	public int getNumberOfOnlineUsers();

	public void register(String userName, String password) throws IllegalArgumentException;

	public String generateHash(String plaintext);
}
