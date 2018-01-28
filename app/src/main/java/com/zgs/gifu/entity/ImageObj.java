package com.zgs.gifu.entity;

import java.io.Serializable;

public class ImageObj implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String path;


	private String date;
	private int frames;	// gif and bitmap frame number

	public ImageObj(String id, String path, String date) {
		this.setId(id);
		this.setPath(path);
		this.date = date;
		this.frames = 1;
	}


	public String getDate() {
		return date;
	}

	public int getFrames() {
		return frames;
	}

	public void setFrames(int frames) {
		this.frames = frames;
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}
}
