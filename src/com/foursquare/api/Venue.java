package com.foursquare.api;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Venue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1764436065546781668L;

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String itemId;
	
	@Key
	public Location location;
	
	@Key
	public Category[] categories;
	
	@Key
	public boolean verified;
	
	@Key
	public Statistics stats;
	
	@Key
	public HereNow hereNow;

}
