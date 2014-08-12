package com.fourinone;

import java.util.List;

class CoolHashCtor extends DumpCtor implements CoolHashClient{
	CoolHashCtor(String parkhost, int parkport){
		super(parkhost,parkport);
	}
	
	public CoolHashResult find(String keywild, Filter filter, boolean point, String... pointSubKey){
		if(chex.checkWild(keywild)){
			WareHouse whtk = new WareHouse(0x0,(byte)0x40);
			whtk.put(0x3c,keywild);
			WareHouse r=giveTask(whtk);
			if(r!=null&&r.containsKey(0x1e)){
				List cr=(List)r.get(0x1e);
				if(cr!=null)
					return new CoolResult(keywild,filter,point,pointSubKey,cr);
			}
		}
		return null;
	}
	
	byte[] dump(String k, byte[] v, byte c, boolean p, String... psk){
		WareHouse wh = new WareHouse(0x0,c);
		DumpAdapter da = dumpAdapter.getKeyMeta(k);
		wh.put(0xa,da.toString());
		wh.put(0xc,0);
		wh.put(0x14,k);
		if(p){
			wh.put(0x28,p);
			if(psk!=null&&psk.length!=0)
				wh.put(0x32,psk);
		}
		if(v!=null)
			wh.put(0x1e,v);
		
		WareHouse r=giveTask(wh);
		if(r!=null&&r.containsKey(0x1e)){
			byte[] bts=(byte[])r.get(0x1e);
			if(bts!=null)
				return bts;
		}
		return null;
	}
	
	public WareHouse giveTask(WareHouse inhouse)
	{
		try{
			WareHouse[] result=doTaskCompete(wks, new WareHouse[]{inhouse});
			if(result.length>0)
				return result[0];
		}catch(Exception ex){
			LogUtil.info("CoolHashCtor", "giveTask", ex);
		}
		
		return null;
	}
}