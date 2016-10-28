package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import java.util.Date;

import javax.ejb.Local;

import de.fh_dortmund.inf.cw.chat.server.entities.User;

@Local
public interface StatisticManagementLocal extends StatisticManagement {

	void incrementLoginCount(User user, Date date);

	void createUserStatisticIfNotExisting(User user);

	void incrementLogoutCount(User user);

	void incrementMessageCount(User user);

	void startIntervallHalfHourTimer();

}
