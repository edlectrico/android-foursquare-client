package com.foursquare.api;

import java.io.Serializable;

import com.google.api.client.util.Key;

import fi.foyt.foursquare.api.io.Response;

public class FoursquareResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3720885902461418602L;
	
	@Key
	public Response response;
	
}
