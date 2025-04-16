package com.hexaware.Exception;

@SuppressWarnings("serial")
public class ProductNotFoundException extends Exception {
	
    public ProductNotFoundException(String message) {
    	
        super(message);
    }
}