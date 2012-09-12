package com.foursquare.api;

import java.io.Serializable;

import com.google.api.client.util.Key;

public class Statistics  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8685233190024069767L;
	
	@Key
	public int checkinsCount;
	@Key
	public int usersCount;
	@Key
	public int tipCount;
}
