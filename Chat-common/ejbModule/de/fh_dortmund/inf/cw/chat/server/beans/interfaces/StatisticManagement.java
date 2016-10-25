package de.fh_dortmund.inf.cw.chat.server.beans.interfaces;

import java.util.List;

import de.fh_dortmund.inf.cw.chat.server.entities.CommonStatistic;
import de.fh_dortmund.inf.cw.chat.server.entities.UserStatistic;

public interface StatisticManagement {
	UserStatistic getUserStatistic(String userName);
	List<CommonStatistic> getCommonStatistic();
}
