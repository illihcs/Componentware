package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
@EntityListeners({PersistencyLog.class})
public abstract class BaseEntity {
	//IDs für alle Entities
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long uuID;

	//Created und Updated für alle Entities
	@Temporal(TemporalType.TIMESTAMP)
	@Basic(optional = false)
	@Column(nullable = true)
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Basic(optional = false)
	@Column(nullable = true)
	private Date updatedAt;
	//Created und Updated für alle Entities end

	//getter and setter
	public Date getCreatedAt() {return createdAt;}
	public void setCreatedAt(Date createdAt) {this.createdAt = createdAt;}

	public Date getUpdatedAt() {return updatedAt;}
	public void setUpdatedAt(Date updatedAt) {this.updatedAt = updatedAt;}

	public long getUUID() {return uuID;}
	//getter and setter end
}
