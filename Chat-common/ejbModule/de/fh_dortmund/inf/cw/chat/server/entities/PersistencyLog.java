package de.fh_dortmund.inf.cw.chat.server.entities;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class PersistencyLog {
	
	@PrePersist
	public void newEntity(BaseEntity entity)
	{	
		Date currentDate = new Date();
		entity.setCreatedAt(currentDate);
		entity.setUpdatedAt(currentDate);
	}
	
	@PreUpdate
	private void updateEntity(BaseEntity entity) {
		entity.setUpdatedAt(new Date());
	}
}
