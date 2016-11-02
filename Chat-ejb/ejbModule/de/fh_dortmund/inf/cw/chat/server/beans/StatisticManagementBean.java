package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.StatisticManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.StatisticManagementRemote;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessage;
import de.fh_dortmund.inf.cw.chat.server.shared.ChatMessageType;

@Stateless
public class StatisticManagementBean implements StatisticManagementLocal, StatisticManagementRemote {
	private static final String FULL_HOUR_TIMER = "STATISTIC_FULL_HOUR_TIMER";
	private static final String HALF_HOUR_TIMER = "STATISTIC_HALF_HOUR_TIMER";

	@EJB
	private UserManagementLocal userManagement;

	@Inject
	private JMSContext jmsContext;

	@Resource(lookup = "java:global/jms/ObserverTopic")
	private Topic chatMessageTopic;

	@Resource
	private TimerService timerService;

	// DB-Context
	@PersistenceContext(unitName = "ChatDB")
	private EntityManager entityManager;

	// Attribute
	// Actual CommonStatistic
	private CommonStatistic newestCommonStatistic;
	// HashMap<String, UserStatistic> userStatisticMap;
	// LinkedList<CommonStatistic> commonStatistic;
	// Attribute Ende

	@PostConstruct
	public void init() {
		newestCommonStatistic = createCommonStatistic();
	}

	// TimerMethoden
	// Startet Intervall-Timer
	public void startIntervallHalfHourTimer() {
		boolean createTimer = true;

		for (Timer timer : timerService.getTimers()) {
			if (HALF_HOUR_TIMER.equals(timer.getInfo())) {
				createTimer = false;
				break;
			}
		}

		if (createTimer) {
			// Create Timer Config
			TimerConfig timerConfig = new TimerConfig();
			timerConfig.setInfo(HALF_HOUR_TIMER);
			timerConfig.setPersistent(false);

			// IntervallTimer
			Calendar initialExpirationCalendar = new GregorianCalendar();
			initialExpirationCalendar.set(Calendar.MINUTE, 30);
			initialExpirationCalendar.set(Calendar.SECOND, 0);

			// muss noch von 2 in 60 geändert werden
			timerService.createIntervalTimer(initialExpirationCalendar.getTime(), 1000 * 60 * 60, timerConfig);
			System.out.println("INTERVALLTIMER GESTARTET!!!");
		}
	}

	// Löst Intervall-Timer und notify aus
	@Timeout
	public void timeout(Timer timer) {
		// TypedQuery<Integer> query =
		// entityManager.createNamedQuery("GET_COMMONSTATISTIC_SIZE_QUERY",
		// Integer.class);
		// TypedQuery<CommonStatistic> queryGetNewest =
		// entityManager.createNamedQuery("GET_COMMONSTATISTIC_QUERY",
		// CommonStatistic.class);
		// query.setParameter("uuID", newestUUID);
		//
		if (HALF_HOUR_TIMER.equals(timer.getInfo())) {
			// if (query.getSingleResult() == 0) {
			// createAndInsertCommonStatistic();
			// }
			notifyOfStatistic("Statistik der letzten halben Stunde", newestCommonStatistic);
		}
	}
	// INTERVALLTIMER ENDE

	// ANDERER TIMER
	@Schedule(second = "10", minute = "*", hour = "*", info = FULL_HOUR_TIMER, persistent = false)
	private void timerFullHour() {
		System.out.println("----FULLHOUR TIMER!!!");
		// TypedQuery<Integer> query =
		// entityManager.createNamedQuery("GET_COMMONSTATISTIC_SIZE_QUERY",
		// Integer.class);
		// TypedQuery<CommonStatistic> queryGetNewest =
		// entityManager.createNamedQuery("GET_COMMONSTATISTIC_QUERY",
		// CommonStatistic.class);
		// query.setParameter("uuID", newestUUID);
		//
		// if (query.getSingleResult() == 0) {
		// createAndInsertCommonStatistic();
		// }
		notifyOfStatistic("Statistik der letzten Stunde", newestCommonStatistic);

		persistAndMakeNewStatistic();
	}

	// ANDERER TIMER ENDE
	// TimerMethoden Ende

	// Methoden
	@Override
	public UserStatistic getUserStatistic(String UserName) {
		System.out.println(UserName + " | getUserStatistic(String UserName)");

		TypedQuery<UserStatistic> query = entityManager.createNamedQuery("GET_USERSTATISTIC_QUERY",
				UserStatistic.class);
		query.setParameter("userName", UserName);
		UserStatistic us = query.getSingleResult();

		return us;
	}

	@Override
	public List<CommonStatistic> getCommonStatistic() {
		System.out.println(" | getCommonStatistic SMB");
		TypedQuery<CommonStatistic> query = entityManager.createNamedQuery("GET_COMMONSTATISTIC_ALL_QUERY",
				CommonStatistic.class);
		List<CommonStatistic> resultList = query.getResultList();
		resultList.add(newestCommonStatistic);
		return resultList;
	}
	// Methoden Ende

	// Methoden Local Bean
	// create UserStatistic for given user if it is not existing already
	@Override
	public void createUserStatisticIfNotExisting(User user) {
		System.out.println(user.getUserName() + "CREATE USER STATISTIC IF NOT EXIST!!!");
		TypedQuery<UserStatistic> query = entityManager.createNamedQuery("GET_USERSTATISTIC_QUERY",
				UserStatistic.class);
		query.setParameter("userName", user.getUserName());
		UserStatistic us = null;

		try {
			us = query.getSingleResult();
		} catch (NoResultException e) {
			System.out.println("---------------No result for USERSTATISTIC!");
		}

		if (us == null) {
			UserStatistic userStatistic = new UserStatistic();
			userStatistic.setLastLogin(new Date());
			userStatistic.setLogins(0);
			userStatistic.setLogouts(0);
			userStatistic.setMessages(0);
			entityManager.persist(userStatistic);
			user.setStatistic(userStatistic);
			entityManager.flush();
			System.out.println("create UserStatistic");
		}
	}

	// increment login count for user
	@Override
	public void incrementLoginCount(User user, Date date) {

		user.getStatistic().setLogins(user.getStatistic().getLogins() + 1);
		user.getStatistic().setLastLogin(date);
		entityManager.merge(user.getStatistic());
		entityManager.flush();
		System.out.println("increment Login count!");

		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(1, 0, 0);
	}

	// increment logout count for user
	@Override
	public void incrementLogoutCount(User user) {
		user.getStatistic().setLogouts(user.getStatistic().getLogouts() + 1);
		entityManager.merge(user.getStatistic());
		entityManager.flush();
		System.out.println("increment Logout count!");

		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(0, 1, 0);
	}

	// increment message count for user
	@Override
	public void incrementMessageCount(User user) {
		user.getStatistic().setMessages(user.getStatistic().getMessages() + 1);
		entityManager.merge(user.getStatistic());
		entityManager.flush();
		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(0, 0, 1);
	}
	// Methoden Local Bean Ende

	// Helper Methods CommonStatistic
	// so wie es heißt
	private void persistAndMakeNewStatistic() {
		entityManager.persist(newestCommonStatistic);
		entityManager.flush();
		newestCommonStatistic = createCommonStatistic();
	}

	// Create and inserts common statistic
	public CommonStatistic createCommonStatistic() {
		Calendar cur = new GregorianCalendar();
		cur.set(Calendar.MINUTE, 0);
		cur.set(Calendar.SECOND, 0);
		cur.set(Calendar.MILLISECOND, 0);
		Date startdate = cur.getTime();

		Calendar curf = new GregorianCalendar();
		curf.set(Calendar.MINUTE, 59);
		curf.set(Calendar.SECOND, 59);
		curf.set(Calendar.MILLISECOND, 59);
		Date enddate = curf.getTime();

		CommonStatistic cs = new CommonStatistic();
		cs.setLogins(0);
		cs.setLogouts(0);
		cs.setMessages(0);
		cs.setStartingDate(startdate);
		cs.setEndDate(enddate);

		return cs;
	}

	public CommonStatistic returnActualCommonStatistic() {
//		TypedQuery<CommonStatistic> queryGetNewest = entityManager.createNamedQuery("GET_COMMONSTATISTIC_QUERY",
//				CommonStatistic.class);
//		queryGetNewest.setParameter("uuID", newestUUID);
//		CommonStatistic cs = null;
//		try {
//			cs = queryGetNewest.getSingleResult();
//		} catch (NoResultException e) {
//			System.out.println("---------------No result for COMMONSTATISTIC!");
//		}
//		return cs;
		return newestCommonStatistic;
	}

	public void incrementCommonStatistic(int login, int logout, int message) {
//		TypedQuery<Long> query = entityManager.createNamedQuery("GET_COMMONSTATISTIC_SIZE_QUERY", Long.class);
//
//		CommonStatistic cs = null;
//		long count = query.getSingleResult();
//		int count2 = (int) count;
//		if (count2 == 0) {
//			createAndInsertCommonStatistic();
//			cs = returnActualCommonStatistic();
//		} else {
//			cs = returnActualCommonStatistic();
//		}
//		cs.setLogins(cs.getLogins() + login);
//		cs.setLogouts(cs.getLogouts() + logout);
//		cs.setMessages(cs.getMessages() + message);
//		entityManager.merge(cs);
		newestCommonStatistic.setLogins(newestCommonStatistic.getLogins() + login);
		newestCommonStatistic.setLogouts(newestCommonStatistic.getLogouts() + logout);
		newestCommonStatistic.setMessages(newestCommonStatistic.getMessages() + message);
	}
	// Helper Methods CommonStatistic End

	// JmsMessages Methods
	// notify Users of send Statistic or actual statistic
	private void notifyOfStatistic(String Titel, CommonStatistic statistic) {
		String msgTxt = Titel + "\nAnzahl der Anmeldungen: " + statistic.getLogins() + "\nAnzahl der Abmeldungen: "
				+ statistic.getLogouts() + "\nAnzahl der geschriebenen Nachrichten: " + statistic.getMessages();
		ChatMessage msg = new ChatMessage(ChatMessageType.STATISTIC, null, msgTxt, new Date());
		try {
			ObjectMessage jmsChatMessage = jmsContext.createObjectMessage(msg);
			jmsContext.createProducer().send(chatMessageTopic, jmsChatMessage);
		} catch (Exception e) {
			System.out.println("Nachricht konnte nicht versendet werden: " + e.getMessage());
		}
	}
	// JmsMessages Methods Ende
}
