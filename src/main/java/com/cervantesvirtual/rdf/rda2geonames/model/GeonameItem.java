package com.cervantesvirtual.rdf.rda2geonames.model;

public class GeonameItem {
	String uri;
	String place;
	String country;
	String idGeonames;
	double ponderatedValue;
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getIdGeonames() {
		return idGeonames;
	}
	public void setIdGeonames(String idGeonames) {
		this.idGeonames = idGeonames;
	}
	public double getPonderatedValue() {
		return ponderatedValue;
	}
	public void setPonderatedValue(double ponderatedValue) {
		this.ponderatedValue = ponderatedValue;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Override
	public String toString() {
		return "GeonameItem [uri=" + uri + ", place=" + place + ", country="
				+ country + ", idGeonames=" + idGeonames + ", ponderatedValue="
				+ ponderatedValue + "]";
	}

}
