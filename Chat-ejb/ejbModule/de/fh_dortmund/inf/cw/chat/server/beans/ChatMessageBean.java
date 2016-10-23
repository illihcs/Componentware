package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;


@MessageDriven(mappedName = "java:global/jms/ObserverQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class ChatMessageBean implements MessageListener {

	//Schmipfworteinstellungen / -vorlagen
	private static final String FORBIDDEN_WORDS_REPLACEMENT = "***";
	private static final List<String> FORBIDDEN_WORDS = new ArrayList<String>(Arrays.asList((new String[] { "Wichser", "Hurensohn", "Hure", "Arschloch" })));
	//Schmipfworteinstellungen / -vorlagen
	

	//Inject JMS Context
	@Inject
	private JMSContext jmsContext;

	//get Topic
	@Resource(lookup = "java:global/jms/ObserverTopic")
	private Topic chatMessageTopic;

	//Client sendet an den Server und der Server leitet die in die Topic weiter, die wiederum die Clients benachrichtigt.
	@Override
	public void onMessage(Message msg) {
		try {
			//Empfangene Nachricht, als Parameter gekommen.
			TextMessage textMsg = (TextMessage) msg;

			//Schimpfwörter aussortieren bzw. umbenennen
			String text = filterForbiddenWords(textMsg.getText());
			
			User sender = new User();
			sender.setUserName(textMsg.getStringProperty("Name"));

			ChatMessage chatMessage = new ChatMessage(ChatMessageType.TEXT, sender.getUserName(), text, new Date());
			notifyViaChatMessageTopic(chatMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private String filterForbiddenWords(String text) {
		for (String forbiddenWord : FORBIDDEN_WORDS) {
			text = text.replaceAll(forbiddenWord, FORBIDDEN_WORDS_REPLACEMENT);
		}
		return text;
	}

	//notified alle Clients
	private void notifyViaChatMessageTopic(ChatMessage msg) {
		if (msg == null) {
			return;
		}

		try {
			ObjectMessage jmsChatMessage = jmsContext.createObjectMessage(msg);
			jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
		} catch (Exception e) {
			System.out.println("Nachricht konnte nicht versendet werden: " + e.getMessage());
		}
	}
}
