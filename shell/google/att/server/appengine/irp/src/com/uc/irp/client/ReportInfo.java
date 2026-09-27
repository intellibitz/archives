package com.uc.irp.client;

import java.io.Serializable;
import java.util.Date;

public class ReportInfo implements Serializable {
	private Long id;
	private String subscriberId;
	private String event;
	private String location;
	private Date captureTime;
	private String content;

	public ReportInfo() {
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}

	/**
	 * @param subscriberId
	 *            the subscriberId to set
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
	 * @param event
	 *            the event to set
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
	 * @param location
	 *            the location to set
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
	 * @param captureTime
	 *            the captureTime to set
	 */
	public void setCaptureTime(Date captureTime) {
		this.captureTime = captureTime;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

}
