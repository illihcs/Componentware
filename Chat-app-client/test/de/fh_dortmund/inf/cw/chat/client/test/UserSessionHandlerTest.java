package de.fh_dortmund.inf.cw.chat.client.test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fh_dortmund.inf.cw.chat.client.ServiceHandlerImpl;

public class UserSessionHandlerTest {

	private static ServiceHandlerImpl serviceHandler;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
	}

	@Test
	public void test_getUserName() {
		
	}

	@Test
	public void test_disconnect() {
		
	}

	@Test
	public void getOnlineUsers() {
		assertNotNull(serviceHandler.getNumberOfOnlineUsers());
	}

	@Test
	public void getNumberOfRegisteredUsers() {
		assertThat(serviceHandler.getNumberOfRegisteredUsers(), greaterThan(1));
	}

	@Test
	public void getNumberOfOnlineUsers() {
		assertNotNull(serviceHandler.getNumberOfOnlineUsers());
	}

	
}
