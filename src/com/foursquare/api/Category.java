package com.foursquare.api;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Category  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7386971940440892340L;

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String pluralName;
	
	@Key
	public String icon;
	
	@Key
	public String[] parents;
	
	@Key
	public boolean primary;
	
}
