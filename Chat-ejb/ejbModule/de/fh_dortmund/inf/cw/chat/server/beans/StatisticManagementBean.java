package de.fh_dortmund.inf.cw.chat.server.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;

import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.StatisticManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.StatisticManagementRemote;
import de.fh_dortmund.inf.cw.chat.server.beans.interfaces.UserManagementLocal;
import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.entities.User;
import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

@Stateless
public class StatisticManagementBean implements StatisticManagementLocal, StatisticManagementRemote {
	@EJB
	private UserManagementLocal userManagement;

	@Inject
	private JMSContext jmsCtx;

	@Resource(lookup = "java:global/jms/ObserverTopic")
	private Topic chatMessageTopic;
	
	@Resource
	private TimerService timerService;
	
	//Attribute
	HashMap<String, UserStatistic> userStatisticMap;
	LinkedList<CommonStatistic> commonStatistic;
	//Attribute Ende
	
	@PostConstruct
	public void init(){
		userStatisticMap = new HashMap();
		commonStatistic = new LinkedList<CommonStatistic>();
	}
	
	
	//Methoden
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
	//Methoden Ende

	
	//Methoden Local Bean
	//create UserStatistic for given user if it is not existing already 
	@Override
	public void createUserStatisticIfNotExisting(User user){
		System.out.println(user.getUserName() + "CREATE");
		if(userStatisticMap.get(user.getUserName()) == null)
		{
			UserStatistic us = new UserStatistic();
			us.setLastLogin(new Date());
			us.setLogins(0);
			us.setLogouts(0);
			us.setMessages(0);
			userStatisticMap.put(user.getUserName(), us);
			System.out.println("create UserStatistic");
		}
	}

	//increment login count for user
	@Override
	public void incrementLoginCount(User user, Date date) {
		System.out.println(user.getUserName() + "login");
		UserStatistic us = userStatisticMap.get(user.getUserName());
		if(us == null){
			System.out.println("Du Wichser bist behindert!!!!!!!!!!!!!!");
			return;
		}
		us.setLogins(us.getLogins()+1);
		us.setLastLogin(date);
		userStatisticMap.put(user.getUserName(), us);
		System.out.println("increment Login count!");
	}

	//increment logout coutn for user
	@Override
	public void incrementLogoutCount(User user) {
		System.out.println(user.getUserName() + "logout");
		UserStatistic us = userStatisticMap.get(user.getUserName());
		us.setLogouts(us.getLogouts()+1);
		userStatisticMap.put(user.getUserName(), us);
		System.out.println("increment Logout count!");
	}

	//increment message count for user
	@Override
	public void incrementMessageCount(User user) {
		UserStatistic us = userStatisticMap.get(user.getUserName());
		us.setMessages(us.getMessages()+1);
		userStatisticMap.put(user.getUserName(), us);
	}
	//Methoden Local Bean Ende
	
	//Helper Methods
	public void createCommonStatistic()
	{
		CommonStatistic cs = new CommonStatistic();
		cs.setLogins(0);
		cs.setLogouts(0);
		cs.setMessages(0);
		cs.setStartingDate(new Date());
		cs.setEndDate(new Date());
		commonStatistic.add(new CommonStatistic());
	}
	
	public CommonStatistic returnActualCommonStatistic()
	{
		return commonStatistic.getLast();
	}
	
	public void incrementCommonStatistic(int login, int logout, int message)
	{
		CommonStatistic cs = commonStatistic.getLast();
		cs.setLogins(cs.getLogins()+login);
		cs.setLogouts(cs.getLogouts()+logout);
		cs.setMessages(cs.getMessages()+message);
	}
	//Helper Methods End
}
