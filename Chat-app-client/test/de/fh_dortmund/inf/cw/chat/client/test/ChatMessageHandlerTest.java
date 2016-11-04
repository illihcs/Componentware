package de.fh_dortmund.inf.cw.chat.client.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.fh_dortmund.inf.cw.chat.client.ServiceHandlerImpl;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ChatMessageHandlerTest implements Observer {

	private static ServiceHandlerImpl serviceHandler;
	private static String testUsername;
	private static String testPassword;

	private CountDownLatch observerLatch;
	private Queue<ChatMessage> chatMsgs;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
		serviceHandler.initializeSession();
		testUsername = UUID.randomUUID().toString();
		testPassword = UUID.randomUUID().toString();
		serviceHandler.register(testUsername, testPassword);
		serviceHandler.login(testUsername, testPassword);
	}

	@Before
	public void setUp() throws Exception {
		serviceHandler.addObserver(this);
		observerLatch = new CountDownLatch(1);
		chatMsgs = new LinkedList<>();
	}

	@Test(timeout = 1000)
	public void test001_sendChatMessage() throws InterruptedException {
		serviceHandler.sendChatMessage("Nachricht");
		observerLatch.await();
		ChatMessage chatMsg = chatMsgs.poll();
		assertEquals("Nachricht", chatMsg.getText());
		assertEquals(ChatMessageType.TEXT, chatMsg.getType());
	}

	@Test(timeout = 2000)
	public void test002_twiceLogin() throws Exception {
		serviceHandler.login(testUsername, testPassword);
		observerLatch.await();
		ChatMessage chatMsg = chatMsgs.poll();
		boolean isDisconnect = (chatMsg.getType() == ChatMessageType.DISCONNECT);
		if(!(isDisconnect))
		{
			for (ChatMessage chatMessage : chatMsgs) {
				if(chatMessage.getType() == ChatMessageType.DISCONNECT)
					isDisconnect = true;
			}
		}
		assertTrue(isDisconnect);
	}

	@Override
	public void update(Observable observalbe, Object message) {
		if (serviceHandler.equals(observalbe) && message instanceof ChatMessage) {
			chatMsgs.add((ChatMessage) message);
			observerLatch.countDown();
		}
	}

	@After
	public void tearDown() throws Exception {
		serviceHandler.deleteObserver(this);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		serviceHandler.delete(testPassword);
	}

}
