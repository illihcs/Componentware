package de.fh_dortmund.inf.cw.chat.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.fh_dortmund.inf.cw.chat.client.ServiceHandlerImpl;
import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatisticHandlerTest {

	private static ServiceHandlerImpl serviceHandler;
	private static String testUsername;
	private static String testPassword;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		serviceHandler = ServiceHandlerImpl.getInstance();
		serviceHandler.initializeSession();
		testUsername = UUID.randomUUID().toString();
		testPassword = UUID.randomUUID().toString();
		serviceHandler.register(testUsername, testPassword);
		serviceHandler.login(testUsername, testPassword);
	}

	@Test
	public void test001_getUserStatistic() throws Exception {
		UserStatistic us = serviceHandler.getUserStatistic();
		assertEquals(1, us.getLogins());
		assertEquals(0, us.getLogouts());
		assertEquals(0, us.getMessages());
		assertTrue(us.getLastLogin().compareTo(new Date()) <= 0);
	}

	@Test
	public void test002_getStatistics() throws Exception {
		List<CommonStatistic> csl = serviceHandler.getStatistics();
		CommonStatistic cs = csl.get(0);
		
		assertTrue(csl.size() > 0);
		assertTrue(cs.getLogins() >= 1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		serviceHandler.delete(testPassword);
	}

}
