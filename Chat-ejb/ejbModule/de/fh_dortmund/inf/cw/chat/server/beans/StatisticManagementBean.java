package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Topic;

import com.sun.xml.internal.ws.assembler.jaxws.HandlerTubeFactory;

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

	// Attribute
	HashMap<String, UserStatistic> userStatisticMap;
	LinkedList<CommonStatistic> commonStatistic;
	// Attribute Ende

	@PostConstruct
	public void init() {
		userStatisticMap = new HashMap();
		commonStatistic = new LinkedList<CommonStatistic>();
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

			//muss noch von 2 in 60 geändert werden
			timerService.createIntervalTimer(initialExpirationCalendar.getTime(), 1000*60*60, timerConfig);
			System.out.println("INTERVALLTIMER GESTARTET!!!");

		}
	}

	//Löst Intervall-Timer und notify aus
	@Timeout
	public void timeout(Timer timer) {
		if (HALF_HOUR_TIMER.equals(timer.getInfo())) {
			if(commonStatistic.size() == 0){
				createAndInsertCommonStatistic();
			}
			notifyOfStatistic("Statistik der letzten halben Stunde", commonStatistic.getLast());
		}
	}
	//INTERVALLTIMER ENDE
	
	//ANDERER TIMER
	@Schedule(second = "10", minute="*", hour = "*", info = FULL_HOUR_TIMER, persistent=false)
	private void timerFullHour(){
		System.out.println("----FULLHOUR TIMER!!!");
		if(commonStatistic.size() == 0){
			createAndInsertCommonStatistic();
		}
		notifyOfStatistic("Statistik der letzten Stunde", commonStatistic.getLast());
		
		createAndInsertCommonStatistic();
	}
	//ANDERER TIMER ENDE
	// TimerMethoden Ende

	// Methoden
	@Override
	public UserStatistic getUserStatistic(String UserName) {
		System.out.println(UserName + " | getUserStatistic(String UserName)");
		UserStatistic us = userStatisticMap.get(UserName);
		System.out.println(us);
		System.out.println(us.getLogins());
		System.out.println(us.getLogouts());
		System.out.println(us.getLastLogin());
		return userStatisticMap.get(UserName);
	}

	@Override
	public List<CommonStatistic> getCommonStatistic() {
		System.out.println(commonStatistic.getFirst() + " | getCommonStatistic SMB");
		return commonStatistic;
	}
	// Methoden Ende

	// Methoden Local Bean
	// create UserStatistic for given user if it is not existing already
	@Override
	public void createUserStatisticIfNotExisting(User user) {
		System.out.println(user.getUserName() + "CREATE USER STATISTIC IF NOT EXIST!!!");
		if (userStatisticMap.get(user.getUserName()) == null) {
			UserStatistic us = new UserStatistic();
			us.setLastLogin(new Date());
			us.setLogins(0);
			us.setLogouts(0);
			us.setMessages(0);
			userStatisticMap.put(user.getUserName(), us);
			System.out.println("create UserStatistic");
		}
	}

	// increment login count for user
	@Override
	public void incrementLoginCount(User user, Date date) {
		System.out.println(user.getUserName() + "login");
		UserStatistic us = userStatisticMap.get(user.getUserName());
		if (us == null) {
			System.out.println("Du Wichser bist behindert!!!!!!!!!!!!!!");
			return;
		}
		us.setLogins(us.getLogins() + 1);
		us.setLastLogin(date);
		userStatisticMap.put(user.getUserName(), us);
		System.out.println("increment Login count!");

		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(1, 0, 0);
	}

	// increment logout count for user
	@Override
	public void incrementLogoutCount(User user) {
		System.out.println(user.getUserName() + "logout");
		UserStatistic us = userStatisticMap.get(user.getUserName());
		us.setLogouts(us.getLogouts() + 1);
		userStatisticMap.put(user.getUserName(), us);
		System.out.println("increment Logout count!");
		
		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(0, 1, 0);
	}

	// increment message count for user
	@Override
	public void incrementMessageCount(User user) {
		UserStatistic us = userStatisticMap.get(user.getUserName());
		us.setMessages(us.getMessages() + 1);
		userStatisticMap.put(user.getUserName(), us);
		
		// IncrementLogin in der aktuellen CommonStatistic
		// Falls noch nicht vorhanden wird sie erstellt
		incrementCommonStatistic(0, 0, 1);
	}
	// Methoden Local Bean Ende

	// Helper Methods CommonStatistic
	//Create and inserts common statistic
	public void createAndInsertCommonStatistic() {
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
		commonStatistic.add(cs);
	}

	public CommonStatistic returnActualCommonStatistic() {
		return commonStatistic.getLast();
	}

	public void incrementCommonStatistic(int login, int logout, int message) {
		CommonStatistic cs = null;
		if (commonStatistic.size() == 0) {
			createAndInsertCommonStatistic();
			cs = commonStatistic.getLast();
		}
		else{
			cs = commonStatistic.getLast();
		}
		cs.setLogins(cs.getLogins() + login);
		cs.setLogouts(cs.getLogouts() + logout);
		cs.setMessages(cs.getMessages() + message);
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
