package com.hexaware.Exception;

@SuppressWarnings("serial")
public class CustomerNotFoundException extends Exception {
	
    public CustomerNotFoundException(String message) {
    	
        super(message);
    }
}