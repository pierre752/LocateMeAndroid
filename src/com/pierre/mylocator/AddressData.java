package com.pierre.mylocator;

import com.google.android.gms.maps.model.LatLng;

public class AddressData {
	
	public LatLng location;
	public String name;
	public String address1;
	public String address2;
	public String zip;
	public String city;
	public String state;
	public String country;
	
	public AddressData(LatLng location, String name, String address1, String address2, String zip, String city, String state, String country) {
		this.location = location;
		this.name = name;
		this.address1 = address1;
		this.address2 = address2;
		this.zip = zip;
		this.city = city;
		this.state = state;
		this.country = country;
	}

	@Override
	public String toString() {
	    return name;
	}
	
}
