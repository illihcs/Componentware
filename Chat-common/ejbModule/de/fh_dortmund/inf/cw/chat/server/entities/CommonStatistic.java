package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries({
	@NamedQuery(name = CommonStatistic.GET_COMMONSTATISTIC_QUERY, query = "select c from CommonStatistic c where c.uuID = :uuID"),
	@NamedQuery(name = CommonStatistic.GET_COMMONSTATISTIC_SIZE_QUERY, query = "count * from CommonStatistic c"),
	@NamedQuery(name = CommonStatistic.GET_COMMONSTATISTIC_ALL_QUERY, query = "select s from CommonStatistic s order by s.startingDate asc"),
	
})

@Entity
public class CommonStatistic extends Statistic {
	private static final long serialVersionUID = 1L;
	
	public static final String GET_COMMONSTATISTIC_QUERY = "CommonStatistic$getCommonStatistic";
	public static final String GET_COMMONSTATISTIC_SIZE_QUERY = "CommonStatistic$getCommonStatisticSize";
	public static final String GET_COMMONSTATISTIC_ALL_QUERY = "CommonStatistic$getCommonStatisticAll";
	//public static final String GET_COMMONSTATISTIC_SIZE_QUERY = "CommonStatistic$getCommonStatisticSize";
		
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private UUID uuID;
	@Basic(optional = false)
	@Column(nullable = false)
	private Date startingDate;
	@Basic(optional = false)
	@Column(nullable = false)
	private Date endDate;
	
	public Date getStartingDate() {
		return startingDate;
	}
	public void setStartingDate(Date startingDate) {
		this.startingDate = startingDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
