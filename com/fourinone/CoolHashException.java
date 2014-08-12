package com.fourinone;

import java.util.regex.Pattern;

public class CoolHashException extends ServiceException {
	Pattern pk=null,pkw=null;
	
	public CoolHashException(){
		super();
		pk=Pattern.compile("^[a-z0-9A-Z_]+(\\.[a-z0-9A-Z_]+)*$");
		pkw=Pattern.compile("^([a-z0-9A-Z_]+|\\*?)(\\.([a-z0-9A-Z_]+|\\*?))*$");
	}
	
	public CoolHashException(String msg){
		super(msg);
	}

	public CoolHashException(String msg, Throwable cause){
		super(msg, cause);
	}
	
	public CoolHashException(Throwable cause){
		super(cause);
	}
	
	<T> String checkTargetLog(Class<T> value){
		return "this class type cant be provided "+value;
	}
	
	<T> void checkTargetMatch(Class<T> value) throws CoolHashException{
		try{
			 ConstantBit.Target.valueOf(value.getSimpleName().toUpperCase());
		}catch(Exception e){
			throw new CoolHashException(checkTargetLog(value));
		}
	}
	
	void checking(String str) throws CoolHashException{
		if(str==null||pk.matcher(str).matches()==false)
			throw new CoolHashException("[Syntax Error] the keyname '"+str+"' must be composed of 'a-z' or '0-9' or '_' and split by '.'");
		if(str.length()>DumpAdapter.mk)
			throw new CoolHashException("[Exceed Error] the size of the keyname exceed");
		
	}
	
	void checking(byte[] bts) throws CoolHashException{
		if(bts==null||bts.length<=1||bts.length>DumpAdapter.mv)
			throw new CoolHashException("[Value Error] value be null or size of the value exceed");
	}
	
	boolean checkKey(String key){
		try{
			checking(key);
		}catch(Exception ex){
			LogUtil.info("[CoolHashException]", "[checkKey]", ex);
			return false;
		}
		return true;
	}
	
	boolean checkWild(String str){
		if(str==null||pkw.matcher(str).matches()==false){
			LogUtil.fail("[CoolHashException]","[Syntax Error]","the findname '"+str+"' must be composed of 'a-z' or '0-9' or '_' or '*' and split by '.'");
			return false;
		}
		return true;
	}
	
	boolean checkKeyValue(Object key, Object value){
		try{
			checkingType(key,value);
			checking((String)key,(byte[])value);
		}catch(Exception ex){
			LogUtil.info("[CoolHashException]", "[checkKeyValue]", ex);
			return false;
		}
		return true;
	}
	
	void checkingType(Object key, Object value)throws CoolHashException{
		if(key==null||value==null)
			throw new CoolHashException("[NullPointerException] key or value cant be null!");
		if(!(key instanceof String&&value.getClass().isArray()&&value.getClass().getComponentType().equals(byte.class)))
			throw new CoolHashException("[ClassCastException] key type or value type error!");
	}
	
	void checkingRegex(String regex)throws CoolHashException{
		try{
			Pattern.compile(regex);
		}catch(Exception e){
			throw new CoolHashException(e);
		}
	}
	
	void exceedException(){
		LogUtil.fail("[CoolHashException]", "[ExceedException]", "the size of the key/value exceed!");
	}
	
	void checking(String str, byte[] bts) throws CoolHashException{
		checking(str);
		checking(bts);
	}
	
	void checkKeyPoint(String keyPoint) throws CoolHashException
	{
		checking(keyPoint);
		if(!keyPoint.endsWith("\u002E\u006B\u0065\u0079"))
			throw new CoolHashException("[Syntax Error] keypoint must be end with .key");
	}
	
	void pointLoopException(){
		LogUtil.fail("[CoolHashException]", "[PointLoopException]", "key point loop be canceled, return point key string maybe cause unpredictable data format conversion error!");
	}
	
	void rollback(){
		LogUtil.fail("[CoolHashException]", "[TransactionException]", "something exception, transaction roll back");
	}
	
	void commitException(Exception ex){
		LogUtil.fail("[CoolHashException]", "[CommitException]", ex.toString());
	}
	
	void rollbackException(Exception ex){
		LogUtil.info("[CoolHashException]", "[RollbackException]", "roll back failed", ex);
	}
}