package com.facsu.publicartmap.bean;

public class ExecutionStatus {
	
	public String ErrorDesc;
	public String HasError;
	public String ID;

	public boolean hasError() {
		boolean hasError = true;
		try {
			hasError = Boolean.valueOf(HasError);
		} catch (Exception e) {
		}
		return hasError;
	}
}
