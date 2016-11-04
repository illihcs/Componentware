package de.fh_dortmund.inf.cw.chat.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.fh_dortmund.inf.cw.chat.client.ServiceHandlerImpl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSessionHandlerTest {

	private static ServiceHandlerImpl serviceHandler;
	private static String testUsername;
	private static String testPassword;
	private static int registeredUser;
	private static int onlineUser;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
		testUsername = UUID.randomUUID().toString();
		testPassword = UUID.randomUUID().toString();
		registeredUser = serviceHandler.getNumberOfRegisteredUsers();
		onlineUser = serviceHandler.getNumberOfOnlineUsers();
	}

	@Test
	public void test001_register() throws Exception {
		serviceHandler.register(testUsername, testPassword);
		registeredUser++;
		assertEquals(registeredUser, serviceHandler.getNumberOfRegisteredUsers());
	}

	@Test
	public void test002_login() throws Exception {
		boolean included = false;
		serviceHandler.login(testUsername, testPassword);
		onlineUser++;

		for (String item : serviceHandler.getOnlineUsers()) {
			if (item.equals(testUsername)) {
				included = true;
				break;
			}
		}

		assertEquals(onlineUser, serviceHandler.getNumberOfOnlineUsers());
		assertEquals(included, true);
	}

	@Test
	public void test003_getUserName() {
		assertEquals(testUsername, serviceHandler.getUserName());
	}

	@Test
	public void test004_getOnlineUsers() {		
		assertEquals(onlineUser, serviceHandler.getOnlineUsers().size());
	}

	@Test
	public void test005_getNumberOfRegisteredUsers() {
		assertEquals(registeredUser, serviceHandler.getNumberOfRegisteredUsers());
	}

	@Test
	public void test006_getNumberOfOnlineUsers() {
		assertNotNull(serviceHandler.getNumberOfOnlineUsers());
		assertEquals(onlineUser, serviceHandler.getNumberOfOnlineUsers());
	}

	@Test
	public void test007_changePassword() throws Exception {
		String newPassword = UUID.randomUUID().toString();
		serviceHandler.changePassword(testPassword, newPassword);
		testPassword = newPassword;
	}

	@Test
	public void test008_logout() throws Exception {
		serviceHandler.logout();
		onlineUser--;
		assertEquals(onlineUser, serviceHandler.getNumberOfOnlineUsers());
	}

	@Test
	public void test009_delete() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
		serviceHandler.login(testUsername, testPassword);
		serviceHandler.delete(testPassword);
	}

	@Test
	public void test010_disconnect() {
		serviceHandler = ServiceHandlerImpl.getInstance();
		serviceHandler.disconnect();
	}
	
	public static void tearDownAfterClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
	}
}
