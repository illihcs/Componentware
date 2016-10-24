package de.fh_dortmund.inf.cw.chat.client;

import java.util.Date;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.fh_dortmund.inf.cw.chat.client.shared.ChatMessageHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.ServiceHandler;
import de.fh_dortmund.inf.cw.chat.client.shared.UserSessionHandler;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementRemote;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserSessionHandlerRemote;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

public class ServiceHandlerImpl extends ServiceHandler 
		implements UserSessionHandler, ChatMessageHandler, MessageListener {

	private Context ctx;
	private UserSessionHandlerRemote userSessionHandler;
	private UserManagementRemote userManagement;
	
	private JMSContext jmsContext;
	private Topic chatMessageTopic;
	private Queue chatMessageQueue;
	private JMSConsumer chatMessageConsumer;

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
		unregisterConsumer();
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
		registerOnConsumer();
	}

	@Override
	public void logout() throws Exception {
		unregisterConsumer();
		userSessionHandler.logout();
	}

	@Override
	public void register(String userName, String password) throws Exception {
		userManagement.register(userName, password);
	}

	//Chat Message Handler
	//Hört auf Nachrichten, die von Server gesendet werden.
	@Override
	public void onMessage(Message jmsMessage) {
		try {
			if (jmsMessage.isBodyAssignableTo(ChatMessage.class)) {
				ChatMessage chatMessage = jmsMessage.getBody(ChatMessage.class);
				
				setChanged();
				notifyObservers(chatMessage);

				System.out.println(chatMessage.getText());
			} else {
				System.out.println("Falscher jmsMessageType" + jmsMessage);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}		
	}

	//Sendet übergebene Nachricht an den Server
	@Override
	public void sendChatMessage(String message) {
		System.out.println("sendChatMessage(" + message+")");
		
		try {
			TextMessage textMessage = jmsContext.createTextMessage(message);
			textMessage.setStringProperty("Name", getUserName());
			
			jmsContext.createProducer().send(chatMessageQueue, textMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}		
	}
	
	//initialize JMS Context 
	private void registerOnConsumer(){
		try {
			ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("java:comp/DefaultJMSConnectionFactory");
			jmsContext = connectionFactory.createContext();
	
			// Message Beans
			//empfängt
			chatMessageTopic = (Topic) ctx.lookup("java:global/jms/ObserverTopic");
			//sendet
			chatMessageQueue = (Queue) ctx.lookup("java:global/jms/ObserverQueue");
			//Topic als Consumer anmelden und den Listener auf aktuelles Objekt setzen
			//Filter für die Usererkennung
			String filter = String.format("%1$s IS NULL OR %1$s = \'%2$s\'", ChatMessage.USER_PROPERTY_ID, getUserName());
			jmsContext.createConsumer(chatMessageTopic,filter).setMessageListener(this);		
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	public void unregisterConsumer(){
		if(chatMessageConsumer == null){
			return;
		}else{
			chatMessageConsumer.close();
		}
		chatMessageConsumer = null;
	}
}
