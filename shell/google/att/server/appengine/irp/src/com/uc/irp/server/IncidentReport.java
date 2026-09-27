package com.uc.irp.server;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class IncidentReport implements Serializable {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;
    @Persistent
    private String subscriberId;
    @Persistent
    private String event;
    @Persistent
    private String location;
    @Persistent
    private Date captureTime;

    @Persistent(serialized = "true")
    private Text content;

    public IncidentReport(Text content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Text getContent() {
        return content;
    }

    public void setContent(Text content) {
        this.content = content;
    }

	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}

	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	/**
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the captureTime
	 */
	public Date getCaptureTime() {
		return captureTime;
	}

	/**
	 * @param captureTime the captureTime to set
	 */
	public void setCaptureTime(Date captureTime) {
		this.captureTime = captureTime;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
    
    
}
