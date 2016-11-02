package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@NamedQueries({
	@NamedQuery(name = "GET_COMMONSTATISTIC_QUERY", query = "select c from CommonStatistic c where c.uuID = :uuID"),
	@NamedQuery(name = "GET_COMMONSTATISTIC_SIZE_QUERY", query = "select count(c) from CommonStatistic c"),
	@NamedQuery(name = "GET_COMMONSTATISTIC_ALL_QUERY", query = "select s from CommonStatistic s order by s.startingDate asc"),
	
})

@Entity
public class CommonStatistic extends Statistic {
	private static final long serialVersionUID = 1L;
		
	@Basic(optional = false)
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date startingDate;
	@Basic(optional = false)
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
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
