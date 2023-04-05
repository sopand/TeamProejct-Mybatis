package com.project.model;

public enum SetEnum {
	
	UPDATE("UPDATE"),ADD("ADD"),p_img("p_img")
	,p_contentimg("p_contentimg");
	
	private final String type;
	
	private SetEnum(String type) {
		this.type=type;
	}
	public String getType() {
		return this.type;
	}
	
}
