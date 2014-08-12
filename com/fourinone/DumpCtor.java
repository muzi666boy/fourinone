package com.fourinone;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class DumpCtor extends Contractor implements CoolHashBase,CoolHashClient
{
	private String parkhost;
	private int parkport;
	
	WorkerLocal[] wks;
	private ConstantBit.Target ct=ConstantBit.Target.POINT;
	DumpCtor(String parkhost, int parkport){
		writeAhead.setMark(false);
		wks=getWaitingWorkers(parkhost, parkport, "DataWorker");
		LogUtil.info("", "", "DataWorker Number:"+wks.length);
	}
	
	private byte[] dump(String d, int g, String k, byte[] v, byte c, boolean p, String... psk){
		WareHouse[] wh = new WareHouse[g];
		for(int i=0;i<g;i++){
			wh[i] = new WareHouse(0x0,c);
			wh[i].put(0xa,d);
			wh[i].put(0xc,i);
			wh[i].put(0x14,k);
			if(p){
				wh[i].put(0x28,p);
				if(psk!=null&&psk.length!=0)
					wh[i].put(0x32,psk);
			}
			if(v!=null)
				wh[i].put(0x1e,v);
		}
		WareHouse[] result = doTaskCompete(wks, wh);
		for(WareHouse r:result){
			if(r.containsKey(0x1e)){
				byte[] bts=(byte[])r.get(0x1e);
				if(bts!=null)
					return bts;
			}
		}
		return null;
	}
	
	byte[] dump(String k, byte[] v, byte c, boolean p, String... psk){
		byte[] rb=null;
		DumpAdapter da = dumpAdapter.getKeyMeta(k);
		boolean l=c>0?da.writeLock():da.readLock();
		da=da.getLockMeta();
		if(da.exists()&&da.length()>0){
			int g=da.getGroupMeta().length;
			if(c==0x14){
				if(g==1) rb=dump(da.toString(),g,k,v,(byte)0x12,p,psk);
				else{
					dump(da.toString(),g,k,v,(byte)0xa,p,psk);
					rb=dump(da.toString(),1,k,v,(byte)0x16,p,psk);
				}
			}else rb=dump(da.toString(),g,k,v,c,p,psk);
		}else if(c==0x14) rb=dump(da.toString(),1,k,v,(byte)0x16,p,psk);
		boolean rl=da.releaseLock();
		return rb;
	}
	
	private WareHouse[] dump(String[] k, Object[] v, Filter f, byte c, boolean p, String... psk){
		List<WareHouse> tasklist = new ArrayList<WareHouse>();
		WareHouse wh = dumpAdapter.getKeyMeta(k);//k,v
		for(Entry me:(Set<Map.Entry>)wh.entrySet()){
			String cd=(String)me.getKey();
			List<Integer> kiarr = (List<Integer>)me.getValue();
			List karr=kiarr.size()>0?new ArrayList():null;
			List varr=kiarr.size()>0&&v!=null?new ArrayList():null;
			for(int i:kiarr){
				karr.add(k[i]);
				if(varr!=null)
					varr.add(v[i]);
			}
			WareHouse task = new WareHouse(0x0,c);
			task.put(0xa,cd);
			task.put(0x14,karr.toArray(new String[0]));
			if(p){
				task.put(0x28,p);
				if(psk!=null&&psk.length!=0)
					task.put(0x32,psk);
			}
			if(varr!=null)
				task.put(0x1e,varr.toArray());
			if(f!=null){
				task.put(0x20,f.getFilterKey());
				task.put(0x24,f.getFilterValue());
			}
			tasklist.add(task);
		}
		WareHouse[] tasks=tasklist.toArray(new WareHouse[0]);
		WareHouse[] whr=doTaskCompete(wks, tasks);
		if(c==0x28){
			for(int i=0;i<whr.length;i++)
				whr[i].put(0x14,(String[])tasks[i].get(0x14));
		}
		return whr;
	}
	
	private WareHouse getKeyMetaDump(CoolHashMap dump){
		int num=100000,i=0;
		List<WareHouse> tasklist=new ArrayList<WareHouse>();
		List<String> kl=new ArrayList<String>();
		for(Entry me:(Set<Map.Entry>)dump.getEntrySet()){
			if(kl.size()==num){
				WareHouse task=new WareHouse(0,(byte)0x3c);
				task.put(0x14,kl.toArray(new String[0]));
				tasklist.add(task);
				kl=new ArrayList<String>(num);
				i=0;
			}else kl.add((String)me.getKey());
		}
		if(kl.size()>0){
			WareHouse task=new WareHouse(0,(byte)0x3c);
			task.put(0x14,kl.toArray(new String[0]));
			tasklist.add(task);
		}
		WareHouse[] whr=doTaskCompete(wks, tasklist.toArray(new WareHouse[0]));
		WareHouse wh = new WareHouse();
		for(WareHouse wr:whr){
			for(Entry me:(Set<Map.Entry>)wr.entrySet()){
				String cd=(String)me.getKey();
				kl=wh.containsKey(cd)?(List<String>)wh.get(cd):new ArrayList<String>();
				kl.addAll((List<String>)me.getValue());
				wh.put(cd,kl);
			}
		}
		return wh;
	}

	private WareHouse writeAhead = new WareHouse();
	private ObjectBean lockbean;
	public void begin(){
		lockbean=BeanContext.getLock();//String parkhost, int parkport
		writeAhead.setMark(true);
	}
	
	public void rollback(){
		chex.rollback();
		writeAhead.setMark(false);
		writeAhead.clear();
		BeanContext.unLock(lockbean);
	}
	
	public void commit(){
		String[] walk=(String[])writeAhead.keySet().toArray(new String[0]);
		int i=0;
		try{
			for(;i<walk.length;i++){
				writeAhead.setMark(true);
				Object objwa=get(walk[i]);
				writeAhead.setMark(false);
				Object objkv=get(walk[i]);
				if(objwa!=null)
					put(walk[i],objwa);
				else
					remove(walk[i]);
				writeAhead.put(walk[i], objkv);
			}
		}catch(Exception e){
			chex.commitException(e);
			chex.rollback();
			try{
				writeAhead.setMark(false);
				for(int j=0;j<i;j++){
					if(writeAhead.get(walk[j])!=null)
						put(walk[j],writeAhead.get(walk[j]));
					else remove(walk[j]);
				}
			}catch(Exception ex){
				chex.rollbackException(ex);
			}
		}finally{
			writeAhead.setMark(false);
			writeAhead.clear();
			BeanContext.unLock(lockbean);
		}
	}
	
	public Object remove(String key) throws CoolHashException{
		chex.checking(key);
		byte[] bts=writeAhead.getMark()?(byte[])writeAhead.put(key,null):dump(key,null,(byte)0xa,false);
		return bts!=null?ct.getTargetObject(bts):null;
	}
	
	public Object get(String key) throws CoolHashException{
		return get(key, null);
	}
	
	public <T> T get(String key, Class<T> valueType) throws CoolHashException{
		return get(key, valueType, false);
	}
	
	public <T> T get(String key, Class<T> valueType, boolean point, String... pointSubKey) throws CoolHashException{
		chex.checking(key);
		byte[] bts = writeAhead.getMark()&&writeAhead.containsKey(key)?(byte[])writeAhead.get(key):dump(key,null,(byte)0x0,point,pointSubKey);
		return bts!=null?ct.getTargetObject(bts, valueType):null;
	}
	
	public <T> Object put(String key, T value) throws CoolHashException{
		byte[] bts = value!=null?ct.getTargetBytes(value):null;
		chex.checking(key,bts);
		bts = writeAhead.getMark()?(byte[])writeAhead.put(key,bts):dump(key,bts,(byte)0x14,false);
		return bts!=null?ct.getTargetObject(bts):null;
	}
	
	public String putPoint(String keyPoint, String key) throws CoolHashException{
		chex.checking(keyPoint);
		chex.checking(key);
		byte[] bts = writeAhead.getMark()?(byte[])writeAhead.put(keyPoint,ct.getBytes(key)):dump(keyPoint,ct.getBytes(key),(byte)0x14,false);
		return bts!=null?ct.getTargetObject(bts,String.class):null;
	}
	
	public Object getPoint(String keyPoint, String... pointSubKey) throws CoolHashException{
		return getPoint(keyPoint,null,pointSubKey);
	}
	
	public <T> T getPoint(String keyPoint, Class<T> valueType, String... pointSubKey) throws CoolHashException{
		chex.checking(keyPoint);
		return get(keyPoint,valueType,true,pointSubKey);
	}
		
	public int remove(CoolHashMap.CoolKeySet<String> keys){
		WareHouse[] wh=dump(keys.toArray(new String[0]),null,null,(byte)0x2a,false);
		return getResultNum(wh);
	}
	
	public int put(CoolHashMap keyvalue){
		WareHouse[] wh=dump((String[])keyvalue.keySet().toArray(new String[0]),keyvalue.getValues().toArray(),null,(byte)0x2c,false);
		return getResultNum(wh);
	}
	
	public CoolHashMap get(CoolHashMap.CoolKeySet<String> keys){
		CoolHashMap hm = get(keys,null,false);
		return hm;
	}
	
	public CoolHashMap get(CoolHashMap.CoolKeySet<String> keys, Filter filter){
		return get(keys,filter,false);
	}
	
	public CoolHashMap get(CoolHashMap.CoolKeySet<String> keys, Filter filter, boolean point, String... pointSubKey){
		WareHouse[] wh=dump(keys.toArray(new String[0]),null,filter,(byte)0x28,point,pointSubKey);
		CoolHashMap chm=new CoolHashMap();
		for(int i=0;i<wh.length;i++){
			if(wh[i]!=null&&wh[i].containsKey(0x1e)){
				Object[] vs=(Object[])wh[i].get(0x1e);
				String[] ks=(String[])wh[i].get(0x14);
				for(int j=0;j<ks.length;j++){
					if(vs[j]!=null)
						chm.putValue(ks[j],vs[j]);
				}
			}
		}
		return chm;
	}
	
	private int getResultNum(WareHouse[] wh){
		int rb = 0;
		for(int i=0;i<wh.length;i++)
			rb+=(Integer)wh[i].get(0x1e);
		return rb;
	}
	
	public CoolKeyResult findKey(String keywild){
		return findKey(keywild, null);
	}
	
	public CoolKeyResult findKey(String keywild, Filter filter){
		return findKey(keywild,filter,false);
	}
	
	public CoolKeyResult findKey(String keywild, Filter filter, boolean point, String... pointSubKey){
		return (CoolKeyResult)find(keywild,filter,point,pointSubKey);
	}
	
	public CoolHashResult find(String keywild){
		return find(keywild,null);
	}
	
	public CoolHashResult find(String keywild, Filter filter){
		return find(keywild,filter,false);
	}
	
	public CoolHashResult find(String keywild, Filter filter, boolean point, String... pointSubKey){
		return chex.checkWild(keywild)?new CoolResult(keywild, filter, point, pointSubKey):null;
	}
	
	class CoolResult implements CoolKeyResult,CoolHashResult{
		private int j=0;
		private List<File> df=null;
		private ArrayList<String> ks=null;
		private ArrayList vs=null;
		private String filewild=null;
		private Filter filter=null;
		private boolean point=false;
		private String[] pointSubKey=null;
		
		private CoolResult(String filewild, Filter filter, boolean point, String[] pointSubKey){
			this(filewild,filter,point,pointSubKey,null);
		}
		
		CoolResult(String filewild, Filter filter, boolean point, String[] pointSubKey, List<File> dfr){
			this.filewild=filewild;
			this.filter=filter;
			df=dfr!=null?dfr:dumpAdapter.getWalkTree(filewild);
			ks=new ArrayList<String>();
			vs=new ArrayList();
			if(point){
				this.point=point;
				this.pointSubKey=pointSubKey;
			}
		}
		
		public CoolHashMap nextBatch(int batchLength){
			return (CoolHashMap)nextBatchAction(batchLength,(byte)0x20);
		}
		
		public CoolHashMap.CoolKeySet nextBatchKey(int batchLength){
			return (CoolHashMap.CoolKeySet)nextBatchAction(batchLength,(byte)0x1e);
		}
		
		private Object nextBatchAction(int batchLength,byte code){
			Object batch=null;
			while(ks.size()<batchLength&&j<df.size()){
				List<WareHouse> tasks = new ArrayList<WareHouse>();
				long total=0;
				int i=0;
				for(i=j;i<df.size();i++){
					File f = (File)df.get(i);
					total+=f.length();
					if(total<=DumpAdapter.m(64)){
						WareHouse whtk = new WareHouse(0x0,code);
						whtk.put(0xa,f.toString());
						whtk.put(0x3c,filewild);
						if(filter!=null){
							whtk.put(0x20,filter.getFilterKey());
							whtk.put(0x24,filter.getFilterValue());
						}
						if(point){
							whtk.put(0x28,point);
							if(pointSubKey!=null&&pointSubKey.length!=0)
								whtk.put(0x32,pointSubKey);
						}
						tasks.add(whtk);
					}
					else break;
				}
				j=i;
				if(tasks.size()>0){
					WareHouse[] result = doTaskCompete(wks, tasks.toArray(new WareHouse[0]));
					for(WareHouse r:result){
						if(r!=null&&r.containsKey(0x14)){
							ks.addAll(Arrays.asList((String[])r.get(0x14)));
							if(code>0x1e)
								vs.addAll(Arrays.asList((Object[])r.get(0x1e)));
						}
					}
				}
			}
			int bks=Math.min(ks.size(),batchLength);
			List bk=ks.subList(0,bks);
			if(code>0x1e){
				List bv=vs.subList(0,bks);
				CoolHashMap cm=new CoolHashMap();
				for(int i=0;i<bk.size();i++)
					cm.putValue(bk.get(i),bv.get(i));
				batch=cm;
				bv.clear();
			}else{
				CoolHashMap.CoolKeySet cs=CoolHashMap.newKeySet();
				cs.addCollection(bk);
				batch=cs;
			}
			bk.clear();
			return batch;
		}
	};	
	
	public WareHouse giveTask(WareHouse inhouse)
	{
		return null;
	}
	
	public static void main(String[] args){
	}
}