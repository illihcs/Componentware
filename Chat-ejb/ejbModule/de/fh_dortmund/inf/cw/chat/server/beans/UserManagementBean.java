package de.fh_dortmund.inf.cw.chat.server.beans;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.exceptions.LoginException;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

@Stateless
public class UserManagementBean implements UserManagementLocal, UserManagementRemote {

	// hashverfahren
	@Resource(name = "hashverfahren")
	private String hashverfahren;
	// end hashverfahren

	// JMS Zeugs
	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:global/jms/ObserverTopic")
	private Topic chatMessageTopic;

	// DB-Context
	@PersistenceContext(unitName = "ChatDB")
	private EntityManager entityManager;

	// private ArrayList<User> users;

	private ArrayList<User> onlineUserList;

	@PostConstruct
	private void init() {
		// users = new ArrayList<User>();
		onlineUserList = new ArrayList<User>();

		// User tmpUser = new User();
		// tmpUser.setUserName("Jan");
		// tmpUser.setPasswordHash(generateHash("jan"));
		// users.add(tmpUser);
		//
		// User tmpUser2 =new User();
		// tmpUser2.setUserName("Stefan");
		// tmpUser2.setPasswordHash(generateHash("stefan"));
		// users.add(tmpUser2);
		//
		// User tmpUser3 = new User();
		// tmpUser3.setUserName("Marvin");
		// tmpUser3.setPasswordHash(generateHash("marvin"));
		// users.add(tmpUser3);
		//
		// User tmpUser4 = new User();
		// tmpUser4.setUserName("Bla");
		// tmpUser4.setPasswordHash(generateHash("bla"));
		// users.add(tmpUser4);
		//
		// User tmpUser5 = new User();
		// tmpUser5.setUserName("s");
		// tmpUser5.setPasswordHash(generateHash("s"));
		// users.add(tmpUser5);
	}

	@Override
	public List<String> getOnlineUsers() {
		List<String> tmp = new ArrayList<String>();
		for (User user : onlineUserList) {
			tmp.add(user.getUserName());
		}

		return tmp;
	}

	@Override
	public int getNumberOfRegisteredUsers() {
		TypedQuery<Integer> query = entityManager.createNamedQuery(User.COUNT_REGISTERED_USER, Integer.class);
		int count = query.getSingleResult();
		return count;
	}

	@Override
	public int getNumberOfOnlineUsers() {
		return onlineUserList.size();
	}

	@Override
	public void register(String userName, String password) throws IllegalArgumentException {
		if (userName == null || userName == "")
			throw new IllegalArgumentException("userName cannot be null or empty!");
		if (password == null || password == "")
			throw new IllegalArgumentException("password cannot be null or empty!");

		// Usererstellung und Speicherung in der DB
		User user = new User(userName, generateHash(password));
		entityManager.persist(user);

		// Alle Nutzer benachrichten, dass ein neuer Nutzer registriert wurde.
		ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
		try {
			jmsChatMessage
					.setObject(new ChatMessage(ChatMessageType.REGISTER, user.getUserName(), "Registered", new Date()));
		} catch (JMSException e) {
			e.printStackTrace();
		}
		jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
		// Registrierungsbenachrichtigung Ende
	}

	@Override
	public void changePassword(User user, String newPassword) {
		if (user == null) {
			throw new IllegalArgumentException("user cannot be null!");
		}

		// set password and merge with db
		user.setPasswordHash(generateHash(newPassword));
		entityManager.merge(user);
	}

	@Override
	public void login(String userName, String password) throws LoginException {
		TypedQuery<User> query = entityManager.createNamedQuery(User.GET_USER_QUERY, User.class);
		query.setParameter("userName", userName);
		query.setParameter("passwordHash", password);
		User user = query.getSingleResult();

		if (user != null) {
			// wenn der Nutzer sich zum zweiten Mal anmeldet, muss der andere
			// Client geschlossen werden.
			if (isOnline(user)) {
				ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
				try {
					jmsChatMessage.setObject(
							new ChatMessage(ChatMessageType.DISCONNECT, user.getUserName(), "twiceLogin", new Date()));
					jmsChatMessage.setStringProperty(ChatMessage.USER_PROPERTY_ID, user.getUserName());
				} catch (JMSException e) {
					e.printStackTrace();
				}
				jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
				// Benachrichtigung zweiter Login Ende
			} else {
				// Bei erfolgreicher Anmeldung wird der User online gesetzt und
				// der online liste hinzugefügt.
				// läuft hier nur rein, wenn der user nicht schon online ist
				//user.setOnline(true);
				onlineUserList.add(user);
			}

			// Alle anderen nutzer werden informiert, dass sich ein neuer Nutzer
			// angemeldet hat.
			ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
			try {
				jmsChatMessage
						.setObject(new ChatMessage(ChatMessageType.LOGIN, user.getUserName(), "Login", new Date()));
			} catch (JMSException e) {
				e.printStackTrace();
			}
			jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
			// Benachrichtigung Login Ende

			return;
		}
		throw new LoginException("userName oder password sind falsch!");
	}

	@Override
	public void logout(User user) {
		for (User userTmp : onlineUserList) {
			if (userTmp.getUUID().equals(user.getUUID())) {
				// User aus der online-Liste austragen und offline setzen
				//userTmp.setOnline(false);
				onlineUserList.remove(userTmp);

				// Benachrichtung aller Nutzer über das Logout
				ObjectMessage jmsChatMessage = jmsContext.createObjectMessage();
				try {
					jmsChatMessage.setObject(
							new ChatMessage(ChatMessageType.LOGOUT, userTmp.getUserName(), "Logout", new Date()));
				} catch (JMSException e) {
					e.printStackTrace();
				}

				jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
				// Benachrichtung Logout Ende
				break;
			}
		}
	}

	@Override
	public void delete(User user) {
		for (User userTmp : onlineUserList) {
			if (userTmp.getUserName().equals(user.getUserName())) {
				onlineUserList.remove(userTmp);
				break;
			}
		}

		// remove user out of db
		entityManager.remove(user);
	}

	// helper
	private boolean isOnline(User user){
		for (User userTmp : onlineUserList) {
			if (userTmp.getUUID().equals(user.getUUID())) {
				return true;
			}
		}
		return false;
	}
	
	
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
