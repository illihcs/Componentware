package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;


@MessageDriven(mappedName = "java:global/jms/ChatMessageQueue", activationConfig = {
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
	@Resource(lookup = "java:global/jms/ChatMessageTopic")
	private Topic chatMessageTopic;

	@Override
	public void onMessage(Message msg) {
		try {
			TextMessage textMsg = (TextMessage) msg;

			String text = filterForbiddenWords(textMsg.getText());
			
			ChatMessage chatMessage = new ChatMessage(ChatMessageType.TEXT, "Placement", text, new Date());
			notifyViaChatMessageTopic(chatMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String filterForbiddenWords(String text) {
		System.out.println(String.format("filterForbiddenWords('%s')", text));

		for (String forbiddenWord : FORBIDDEN_WORDS) {
			text = text.replaceAll("(?iu)" + Pattern.quote(forbiddenWord), FORBIDDEN_WORDS_REPLACEMENT);
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