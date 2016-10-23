package de.fh_dortmund.inf.cw.chat.server.beans;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
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
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

@Singleton
@Startup
public class UserManagementBean implements UserManagementLocal, UserManagementRemote {

	// hashverfahren
	@Resource(name = "hashverfahren")
	private String hashverfahren;
	// end hashverfahren

	
	//JMS Zeugs
	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:global/jms/ObserverTopic")
	private Topic chatMessageTopic;
	
	
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
		tmpUser5.setUserName("s");
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
	public void register(String userName, String password) throws IllegalArgumentException {
		if (userName == null || userName == "")
			throw new IllegalArgumentException("userName cannot be null or empty!");
		if (password == null || password == "")
			throw new IllegalArgumentException("password cannot be null or empty!");

		User user = new User(userName, generateHash(password));
		users.add(user);
		
		//Alle Nutzer benachrichten, dass ein neuer Nutzer registriert wurde.
		ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
		try {
			jmsChatMessage.setObject(new ChatMessage(ChatMessageType.REGISTER, user.getUserName(), "Registered", new Date()));
		} catch (JMSException e) {
			e.printStackTrace();
		}
		jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
		//Registrierungsbenachrichtigung Ende
	}

	@Lock(LockType.WRITE)
	@Override
	public void changePassword(User user, String newPassword) {
		for (User user2 : users) {
			if(user.getUserName().equals(user2.getUserName()) && user.getPasswordHash().equals(user2.getPasswordHash())){
				user2.setPasswordHash(generateHash(newPassword));
			}
		}
	}

	@Lock(LockType.WRITE)
	@Override
	public void login(String userName, String password) throws LoginException  {
		for (User user2 : users) {
			if(user2.getUserName().equals(userName)&& user2.getPasswordHash().equals(generateHash(password)))
			{
				//wenn der Nutzer sich zum zweiten Mal anmeldet, muss der andere Client geschlossen werden.
				if(user2.isOnline())
				{
					ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
					try {
						jmsChatMessage.setObject(new ChatMessage(ChatMessageType.DISCONNECT, user2.getUserName(), "twiceLogin", new Date()));
						jmsChatMessage.setStringProperty(ChatMessage.USER_PROPERTY_ID, user2.getUserName());
					} catch (JMSException e) {
						e.printStackTrace();
					}
					jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
					//Benachrichtigung zweiter Login Ende
				}else{
				//Bei erfolgreicher Anmeldung wird der User online gesetzt und der online liste hinzugefügt.
				//läuft hier nur rein, wenn der user nicht schon online ist
					user2.setOnline(true);
					onlineUserList.add(user2);
				}				
				
				//Alle anderen nutzer werden informiert, dass sich ein neuer Nutzer angemeldet hat.
				ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
				try {
					jmsChatMessage.setObject(new ChatMessage(ChatMessageType.LOGIN, user2.getUserName(), "Login", new Date()));
				} catch (JMSException e) {
					e.printStackTrace();
				}
				jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
				//Benachrichtigung Login Ende
				
				return;
			}
		}
		throw new LoginException("userName oder password sind falsch!");
	}

	@Lock(LockType.WRITE)
	@Override
	public void logout(User user) {
		for (User userTmp : onlineUserList) {
			if (userTmp.getUserName().equals(user.getUserName())) {
				//User aus der online-Liste austragen und offline setzen
				userTmp.setOnline(false);
				onlineUserList.remove(userTmp);
				
				
				//Benachrichtung aller Nutzer über das Logout
				ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
				try {
					jmsChatMessage.setObject(new ChatMessage(ChatMessageType.LOGOUT, userTmp.getUserName(), "Logout", new Date()));
				} catch (JMSException e) {
					e.printStackTrace();
				}

				jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
				//Benachrichtung Logout Ende
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
	@Override
	public String generateHash(String plaintext) {
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
