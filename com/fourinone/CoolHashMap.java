package com.fourinone;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.lang.reflect.Array;
import java.util.Comparator;

public class CoolHashMap extends LinkedHashMap implements Map,CoolHashBase{
	private int maxCapacity=ConfigContext.getHASHCAPACITY();
	
	public static CoolKeySet<String> newKeySet(){
		return new CoolHashMap().new CoolKeySet<String>();
	}
	
	public static CoolKeySet<String> newKeySet(int initialCapacity){
		return new CoolHashMap().new CoolKeySet<String>(initialCapacity);
	}
	
	/*static CoolKeySet<String> newKeySet(Collection c){
		return new CoolHashMap().new CoolKeySet(c);
	}*/
	
	private class DataMapEntry implements Map.Entry,Comparable<Map.Entry>{
		private String k;
		private Object v;
		
		DataMapEntry(String k, Object v){
			this.k=k;
			this.v=v;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof Map.Entry){
				Map.Entry entry =(Map.Entry)o;
				if((entry.getKey()==null?k==null:entry.getKey().equals(k))&&(entry.getValue()==null?v==null:entry.getValue().equals(v)))
					return true;
			}
			return false;
		}
		
		@Override
		public String getKey(){
			return k;
		}
		
		@Override
		public Object getValue(){
			return v;
		}
		
		@Override
		public int hashCode(){
			 return (k==null?0:k.hashCode())^(v==null?0:v.hashCode());
		}
		
		@Override
		public Object setValue(Object value){
			this.v = value;
			return v;
		}
		
		public String toString(){
			return String.valueOf(k)+"="+String.valueOf(v);
		}
		
		public int compareTo(Map.Entry o){
			return getKey().compareTo((String)o.getKey());
		}
	}
	
	public final class CoolKeySet<E> extends LinkedHashSet<E> implements Set<E>{
		private CoolKeySet(){
			super();
		}
		
		private CoolKeySet(int initialCapacity){
			super(initialCapacity);
		}
		
		private CoolKeySet(Collection<? extends E> c){
			super(c);
		}
		
		private CoolKeySet(int initialCapacity, float loadFactor){
			super(initialCapacity,loadFactor);
		}
		
		@Override
		public boolean add(E e){
			return chex.checkKey((String)e)&&checkMaxCapacity()?addKey(e):false;
		}
		
		boolean addKey(E e){
			return super.add(e);
		}
		
		@Override
		public boolean addAll(Collection<? extends E> c){
			if(!(c instanceof CoolKeySet)){
				LogUtil.fail("[addAll]", "[IllegalArgumentException]", "add failed, the Collection c is not instance of CoolKeySet");
				return false;
			}
			return addCollection(c);
		}
		
		boolean addCollection(Collection<? extends E> c){
			return checkMaxCapacity()?super.addAll(c):false;
		}
		
		private boolean checkMaxCapacity(){
			if(size()+1>maxCapacity){
				LogUtil.fail("[Exceed Error] ", "[CoolHashMapException]", "add failed, the size of the CoolKeySet exceed maxCapacity!");
				return false;
			}
			return true;
		}
		
		public CoolKeySet<E> and(CoolKeySet<E> other){
			retainAll(other);
			return this;
		}
		
		public CoolKeySet<E> or(CoolKeySet<E> other){
			addCollection(other);
			return this;
		}
		
		public CoolKeySet<E> except(CoolKeySet<E> other){
			removeAll(other);
			return this;
		}
		
		public String[] sort(){
			String[] arr = toArray(new String[0]);
			Arrays.sort(arr);
			return arr;
		}
		
		public String[] sort(Comparator<String> comp){
			String[] arr = toArray(new String[0]);
			Arrays.sort(arr,comp);
			return arr;
		}
		
		public CoolKeySet<E> getSuperKeys(int superIndex){
			String[] ks=toArray(new String[0]);
			for(int i=0;i<ks.length;i++)
				ks[i]=getSuperKey(ks[i], superIndex);
			CoolKeySet cks=new CoolKeySet<String>();
			cks.addCollection(Arrays.asList(ks));
			return cks;
		}
	}
	
	public CoolKeySet<String> getKeys(){
		CoolKeySet<String> cks = newKeySet();
		cks.addCollection(keySet());
		return cks;
	}
	
	Collection getValues(){
		return super.values();
	}
	
	@Override
	public Collection values(){
		return values(null);
	}
	
	public <T> Collection<T> values(Class<T> classType){
		ArrayList<T> vls = new ArrayList<T>();
		for(Iterator<byte[]> i=(Iterator<byte[]>)getValues().iterator();i.hasNext();)
			vls.add(ct.getTargetObject(i.next(),classType));
		return vls;
	}
	
	public <T> T[] getValues(Class<T> classType){
		T[] vls = (T[])Array.newInstance(classType,size());
		int j=0;
		for(Iterator<byte[]> i=(Iterator<byte[]>)getValues().iterator();i.hasNext();){
			vls[j++]=ct.getTargetObject(i.next(),classType);
		}
		return vls;
	}
	
	@Override
	public boolean containsValue(Object value){
		return super.containsValue(ct.getTargetBytes(value));
	}
	
	@Override
	public Set<Map.Entry> entrySet(){
		Set<Map.Entry> es = new LinkedHashSet<Map.Entry>(size());
		Set<Map.Entry> meset = getEntrySet();
		for(Entry me:meset)
			es.add(new DataMapEntry((String)me.getKey(),get(me.getKey())));
		return es;
	}
	
	Set<Map.Entry> getEntrySet(){
		return super.entrySet();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof CoolHashMap){
			CoolHashMap odm =(CoolHashMap)o;
			if(size()==odm.size()){
				Object[] meset = getEntrySet().toArray();
				Object[] odmmeset = odm.getEntrySet().toArray();
				for(int i=0;i<meset.length;i++){
					Map.Entry me = (Map.Entry)meset[i];
					Map.Entry odmme = (Map.Entry)odmmeset[i];
					if(!(me.getKey().equals(odmme.getKey())&&Arrays.equals((byte[])me.getValue(),(byte[])odmme.getValue())))	
					  return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Object get(Object key){
		if(chex.checkKey((String)key)==false)
			return null;
		byte[] vbs=(byte[])getValue(key);
		return vbs!=null?ct.getTargetObject(vbs):vbs;
	}
	
	Object getValue(Object key){
		return super.get(key);
	}
		
	public <T> T get(String key, Class<T> classType){
		return ct.getTargetObject((byte[])getValue(key),classType);
	}
	
	@Override
	public Object put(Object key, Object value){
		return putValue(key, value, true);
	}
	
	public Object putPoint(String keyPoint, String key){
		return chex.checkKey(key)?putValue(keyPoint,ct.getBytes(key)):null;
	}
	
	Object putValue(Object key, Object value){
		return putValue(key, value, false);
	}
	
	private Object putValue(Object key, Object value, boolean t){
		if(key!=null&&value!=null){
			value = t?ct.getTargetBytes(value):value;
			value=(!t||chex.checkKeyValue(key,value))?super.put(key,value):null;
		}else LogUtil.fail("[NullPointerException]", "[put]", "put failed, key or value cant be null!");
		return value;
	}
	
	@Override
	public void putAll(Map m){
		if(m instanceof CoolHashMap)
			super.putAll(m);
		else LogUtil.fail("[putAll]", "[IllegalArgumentException]", "add failed, the Map m is not instance of CoolHashMap!");
	}
	
	@Override
	public Object remove(Object key){
		if(chex.checkKey((String)key)==false)
			return null;
		return super.remove(key);
	}
	
	public CoolHashMap and(CoolHashMap cm){
		keySet().retainAll(cm.keySet());
		return this;
	}
	
	public CoolHashMap or(CoolHashMap cm){
		putAll(cm);
		return this;
	}
	
	public CoolHashMap except(CoolHashMap cm){
		keySet().removeAll(cm.keySet());
		return this;
	}
	
	public String toString(){
		return entrySet().toString();
	}
	
	String forString(){
		return getEntrySet().toString();
	}
	
	protected boolean removeEldestEntry(Map.Entry eldest){
		return size()>maxCapacity;
	}
	
	public Map.Entry[] sort(){
		Map.Entry[] arr=entrySet().toArray(new Map.Entry[0]);
		Arrays.sort(arr);
		return arr;
	}
	
	public Map.Entry[] sort(Comparator<Map.Entry> comp){
		Map.Entry[] arr=entrySet().toArray(new Map.Entry[0]);
		Arrays.sort(arr,comp);
		return arr;
	}
	
	public static String getSuperKey(String key, int superIndex){
		if(key!=null&&key.length()>0&&superIndex>=0){
			StringBuilder keyStr = new StringBuilder(key);
			int f=0,c=-1;
			while(c<superIndex){
				int x = keyStr.indexOf("\u002E",f+1);
				if(x!=-1){
					c++;
					f=x;
				}
				else{
					f=keyStr.length();
					break;
				}
			}
			return keyStr.substring(0,f);
		}
		return key;
	}
	
	public static void main(String[] args){
	}
}