package com.foursquare.api;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Location  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6298050148255958092L;

	@Key
	public String address;
	
	@Key
	public String city;
	
	@Key
	public String state;
	
	@Key
	public String postalCode;
	
	@Key
	public String country;
	
	@Key
	public double lat;
	
	@Key
	public double lng;
	
	@Key
	public double distance;
	
}
