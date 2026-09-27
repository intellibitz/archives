package com.androidrocks.bex.client;

import java.io.Serializable;
import java.util.Date;

public class ReportInfo implements Serializable {
	private Long id;
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
