package de.fh_dortmund.inf.cw.chat.server.beans;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;

@Singleton
@Startup
public class UserManagementBean implements UserManagementLocal, UserManagementRemote {

	// hashverfahren
	@Resource(name = "hashverfahren")
	private String hashverfahren;
	// end hashverfahren

	private ArrayList<User> users;
	
	private ArrayList<User> onlineUserList;

	@PostConstruct
	private void init() {
		users = new ArrayList<User>();
		onlineUserList = new ArrayList<User>();

		User tmpUser = new User();
		tmpUser.setUserName("Jan");
		tmpUser.setPasswordHash(generateHash("jan"));
		users.add(tmpUser);
		
		User tmpUser2 =new User();
		tmpUser2.setUserName("Stefan");
		tmpUser2.setPasswordHash(generateHash("stefan"));
		users.add(tmpUser2);
		
		User tmpUser3 = new User();
		tmpUser3.setUserName("Marvin");
		tmpUser3.setPasswordHash(generateHash("marvin"));
		users.add(tmpUser3);
		
		User tmpUser4 = new User();
		tmpUser4.setUserName("Bla");
		tmpUser4.setPasswordHash(generateHash("bla"));
		users.add(tmpUser4);
		
		User tmpUser5 = new User();
		tmpUser5.setUserName("S");
		tmpUser5.setPasswordHash(generateHash("s"));
		users.add(tmpUser5);
	}

	@Lock(LockType.READ)
	@Override
	public List<String> getOnlineUsers() {
		List<String> tmp = new ArrayList<String>();
		for (User user : onlineUserList) {
			tmp.add(user.getUserName());
		}

		return tmp;
	}

	@Lock(LockType.READ)
	@Override
	public int getNumberOfRegisteredUsers() {
		return users.size();
	}

	@Lock(LockType.READ)
	@Override
	public int getNumberOfOnlineUsers() {
		return onlineUserList.size();
	}

	@Lock(LockType.WRITE)
	@Override
	public void register(String userName, String password) {
		if (userName == null || userName == "")
			throw new IllegalArgumentException("userName cannot be null or empty!");
		if (password == null || password == "")
			throw new IllegalArgumentException("password cannot be null or empty!");

		User user = new User(userName, generateHash(password));
		users.add(user);
	}

	@Lock(LockType.WRITE)
	@Override
	public User changePassword(User user, String newPassword) {
		for (User user2 : users) {
			if(user.getUserName().equals(user2.getUserName())){
				user2.setPasswordHash(generateHash(newPassword));
				return user2;
			}
		}
		return null;
	}

	@Lock(LockType.READ)
	@Override
	public User login(String userName, String password) throws LoginException  {
		for (User user2 : users) {
			System.out.println(password);
			System.out.println(user2.getPasswordHash());
			System.out.println(generateHash(password));
			
			if(user2.getUserName().equals(userName)&& user2.getPasswordHash().equals(generateHash(password)))
			{
				user2.setOnline(true);
				onlineUserList.add(user2);
				return user2;
			}
		}
		throw new LoginException("userName oder password sind falsch!");
	}

	@Lock(LockType.WRITE)
	@Override
	public void logout(User user) {
		for (User userTmp : onlineUserList) {
			if (userTmp.getUserName().equals(user.getUserName())) {
				userTmp.setOnline(false);
				onlineUserList.remove(userTmp);
				break;
			}
		}
	}

	@Lock(LockType.WRITE)
	@Override
	public void delete(User user) {
		for (User userTmp : onlineUserList) {
			if (userTmp.getUserName().equals(user.getUserName())) {
				onlineUserList.remove(userTmp);
				break;
			}
		}

		for (User user2 : users) {
			if(user2.getUserName().equals(user.getUserName())){
				users.remove(user2);
				break;
			}
		}
	}

	// helper
	private String generateHash(String plaintext) {
		String hash;

		try {
			MessageDigest encoder = MessageDigest.getInstance(hashverfahren);
			hash = String.format("%040x", new BigInteger(1, encoder.digest(plaintext.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			hash = null;
		}

		return hash;
	}

}
