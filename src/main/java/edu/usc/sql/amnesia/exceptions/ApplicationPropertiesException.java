package edu.usc.sql.amnesia.exceptions;

public class ApplicationPropertiesException extends Exception {
	public ApplicationPropertiesException(Exception e) {
		this.initCause(e);
	}
	public ApplicationPropertiesException() {
	}
}
