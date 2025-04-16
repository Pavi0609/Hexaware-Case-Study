package com.hexaware.Exception;

@SuppressWarnings("serial")
public class OrderNotFoundException extends Exception {
	
    public OrderNotFoundException(String message) {
    	
        super(message);
    }
}