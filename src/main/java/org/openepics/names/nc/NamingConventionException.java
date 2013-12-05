package org.openepics.names.nc;

public class NamingConventionException extends Exception {
	
	private static final long serialVersionUID = -6393913446169844833L;
	
	private Object ncSubject;
	
	public NamingConventionException(String message, Object ncSubject) {
		super(message);
		this.ncSubject = ncSubject;
	}
	
	public Object getNCSubject() {
		return ncSubject;
	}
}
