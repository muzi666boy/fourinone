package com.fourinone;

public interface CoolHashClient extends CoolHash{
	public void begin();
	public void rollback();
	public void commit();
	public void exit();
}