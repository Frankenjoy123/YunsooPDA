package com.yunsoo.exception;

@SuppressWarnings("serial")
public class LocalGeneralException extends BaseException {	
	
	public LocalGeneralException(String msg)
	{
		super(msg);
	}
	
	public String getErrorMessage()
	{
		return getMessage();
	}
}
