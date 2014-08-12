package com.fourinone;

import com.fourinone.CoolHashMap.CoolKeySet;

interface CoolHash{
	public Object get(String key) throws CoolHashException;
	public <T> T get(String key, Class<T> valueType) throws CoolHashException;
	public <T> T get(String key, Class<T> valueType, boolean point, String... pointSubKey) throws CoolHashException;
	public <T> Object put(String key, T value) throws CoolHashException;
	public Object remove(String key) throws CoolHashException;
	public String putPoint(String keyPoint, String key) throws CoolHashException;
	public Object getPoint(String keyPoint, String... pointSubKey) throws CoolHashException;
	public <T> T getPoint(String keyPoint, Class<T> valueType, String... pointSubKey) throws CoolHashException;
	public CoolHashMap get(CoolKeySet<String> keys);
	public CoolHashMap get(CoolKeySet<String> keys, Filter filter);
	public CoolHashMap get(CoolKeySet<String> keys, Filter filter, boolean point, String... pointSubKey);
	public int put(CoolHashMap keyvalue);
	public int remove(CoolKeySet<String> keys);
	public CoolKeyResult findKey(String keywild);
	public CoolKeyResult findKey(String keywild, Filter filter);
	public CoolKeyResult findKey(String keywild, Filter filter, boolean point, String... pointSubKey);
	public CoolHashResult find(String keywild);
	public CoolHashResult find(String keywild, Filter filter);
	public CoolHashResult find(String keywild, Filter filter, boolean point, String... pointSubKey);
}