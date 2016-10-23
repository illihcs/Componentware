package de.fh_dortmund.inf.cw.chat.client;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.chat.client.shared.ChatMessageHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.ServiceHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.UserSessionHandler;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.ChatRemote;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerRemote;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;

public class ServiceHandlerImpl extends ServiceHandler implements UserSessionHandler, ChatMessageHandler, MessageListener {

	private Context ctx;
	private UserSessionHandlerRemote userSessionHandler;
	private UserManagementRemote userManagement;
	private ChatRemote chat;
	
	private JMSContext jmsContext;
	private Topic chatMessageTopic;
	private JMSConsumer chatMessageConsumer;
	private Queue chatMessageQueue;

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
			chat = (ChatRemote) ctx.lookup(
					"java:global/Chat-ear/Chat-ejb/ChatBean!de.fh_dortmund.inf.cw.chat.server.beans.interfaces.ChatRemote");
			
			
			// JMS Context
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx
					.lookup("java:comp/DefaultJMSConnectionFactory");
			jmsContext = connectionFactory.createContext();

			// Message Beans
			chatMessageQueue = (Queue) ctx.lookup("java:global/jms/ChatMessageQueue");
			chatMessageTopic = (Topic) ctx.lookup("java:global/jms/ChatMessageTopic");
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

	//Chat Message Handler
	@Override
	public void onMessage(Message jmsMessage) {
		try {
			if (jmsMessage.isBodyAssignableTo(ChatMessage.class)) {
				ChatMessage chatMessage = jmsMessage.getBody(ChatMessage.class);
				
				System.out.println(String.format("onChatMessage(%s)", chatMessage.toString()));

				setChanged();
				notifyObservers(chatMessage);
			} else {
				System.out.println("Falscher jmsMessageType" + jmsMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void sendChatMessage(String message) {
		System.out.println(String.format("sendChatMessage('%s')", message));
		
		try {
			TextMessage textMessage = jmsContext.createTextMessage(message);
			textMessage.setStringProperty("bla","bla");//ChatMessage.USER_PROPERTY_ID, getUserName());
		
			jmsContext.createProducer().send(chatMessageQueue, textMessage);
		} catch (JMSException e) {
			throw new RuntimeException("Nachricht konnte nicht versendet werden: " + e.getMessage(), e);
		}		
	}
}
