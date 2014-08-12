package com.fourinone;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;

class DumpAdapter extends FileAdapter
{
	final static long mk = ConfigContext.getKEYLENTH();
	final static long mv = m(ConfigContext.getVALUELENGTH())+ConstBit[8];
	final static long ms = mk+m(ConfigContext.getREGIONLENGTH())+ConstBit[8]+ConstBit[5];
	final static long ml = m(ConfigContext.getLOADLENGTH());
	final static String src = ConfigContext.getDATAROOT();
	static boolean windows=BeanContext.getWindows();
	
	DumpAdapter(String dump){
		super(dump);
	}
	
	DumpAdapter(String pdump, String dump){
		super(pdump, dump);
	}
	
	DumpAdapter(String dump, int i){
		//super(i>0?dump.substring(0,dump.length()-1)+i:dump);
		this(new DumpAdapter(dump),i);
	}
	
	DumpAdapter(DumpAdapter dump, int i){
		super(i>0?dump.getFirstMeta()+i:dump.toString());
	}
	
	String[] getGroupMeta(String k){
		DumpAdapter da = getKeyMeta(k);
		return da.getGroupMeta();
	}
	
	String[] getGroupMeta(){
		ArrayList<String> al = new ArrayList<String>();
		File f=this;
		int i=0;
		while(f.exists()&&f.isFile()){
			al.add(f.toString());
			i++;
			f=new File(getParentFile(),getFirstMetaName()+i);
		}
		return al.toArray(new String[0]);
	}

	String getFirstMeta(){
		return getParentFile()+separator+getFirstMetaName();
	}
	
	String getFirstMetaName(){
		return getName().substring(0,3);
	}

	WareHouse getKeyMetaStr(String[] ks){
		WareHouse dm = new WareHouse();
		for(String k:ks){
			String fa = getKeyMeta(k).toString();
			List<String> al = (List)dm.get(fa);
			al = al!=null?al:new ArrayList<String>();
			al.add(k);
			dm.put(fa,al);
		}
		return dm;
	}

	WareHouse getKeyMeta(String[] ks){
		WareHouse dm = new WareHouse();
		for(int i=0;i<ks.length;i++){
			String da = getKeyMeta(ks[i]).toString();
			List<Integer> al = (List)dm.get(da);
			al = al!=null?al:new ArrayList<Integer>();
			al.add(i);
			dm.put(da,al);
		}
		return dm;
	}

	DumpAdapter getKeyMeta(String k){
		return getKeyMeta(src, k);
	}
	
	String getMetaString(String k){
		int h=0,off=0;
    	char val[]=k.toCharArray();
   		int len=k.length();
		for(int i=0;i<len;i++)
            h=31*h+val[off++];
        return Integer.toHexString((h&0x7FFFFFFF)%0x3e8+0x3e8);
	}
	
	DumpAdapter getKeyMeta(String src, String k){
		StringBuilder fb=new StringBuilder(src);
		if(k!=null&&k.length()>0){
			StringTokenizer tokenizer = new StringTokenizer(k,"\u002E");
			while(tokenizer.hasMoreTokens()){
				String ck = tokenizer.nextToken();
				fb.append(separator).append(ck.equals("\u002A")?ck:getMetaString(ck));
			}
			if(fb.lastIndexOf("\u002A")!=fb.length()-1)
				fb.append("\u0030");
		}
		return new DumpAdapter(fb.toString());
	}
	
	String[] getKeyWildArr(String k){
		String[] ks = k.split("\\u002E");
		for(int i=0;i<ks.length;i++)
			ks[i]=ks[i].equals("\u002A")?ks[i]:getMetaString(ks[i]);
		ks[ks.length-1]=ks[ks.length-1].equals("\u002A")?ks[ks.length-1]:ks[ks.length-1]+"\u0030";
		return ks;
	}
	
	private class DumpWalk{
		private List<File> df=null;
		private String[] fwarr=null;
		private int deep=-1;
		
		private DumpWalk(String filewild){
			df = new ArrayList<File>();
			fwarr = getKeyWildArr(filewild);
			walkTree(getKeyMeta(""));
		}
	
		private void walkTree(File path){
			deep++;
			if(fwarr[Math.min(fwarr.length-1,deep)].equals("\u002A")){
				if(path.isDirectory()){
					File[] childs = path.listFiles();
					for(File f:childs)
						walkTree(f);
				}else if(path.isFile()&&path.getName().endsWith("\u0030")&&deep>=fwarr.length-1)//or inlock?
					df.add(path);
			}else{
				path = new File(path,fwarr[deep]);
				if(deep<fwarr.length-1){
					if(path.isDirectory())
						walkTree(path);
				}else if(deep==fwarr.length-1){
					if(path.isFile())
						df.add(path);
				}
			}
			deep--;
		}
	}
	
	List<File> getWalkTree(String filewild){
		DumpWalk dw = new DumpWalk(filewild);
		return dw.df;
	}
	
	private DumpAdapter lockMeta;
	
	private void setLockMeta(){
		lockMeta = new DumpAdapter(getParent(), getName()+"\u002E\u006C\u006F\u0063\u006B");
	}
	
	public boolean readLock(){
		return windows?super.readLock():writeLock();
	}

	public boolean writeLock(){
		if(windows)
			return super.writeLock();
		else setLockMeta();
		try{
			while(true){
				if(exists()&&!lockMeta.exists()){
					if(renameTo(lockMeta))
						break;
				}else if(!exists()&&!lockMeta.exists()){
					createFile();
				}
			}
			return true;
		}catch(Exception ex){
			LogUtil.info("[DumpLock]", "", ex.toString());
			releaseLock();
			return false;
		}
	}
	
	public boolean releaseLock(){
		try{
			return windows?super.releaseLock():lockMeta.renameTo(this);
		}catch(Exception ex){
			LogUtil.info("[DumpReleaseLock]", "", ex.toString());
			return false;
		}
	}
	
	DumpAdapter getLockMeta(){
		return windows?this:(exists()?this:lockMeta);
	}
	
	boolean being(){
		if(!windows) setLockMeta();
		return (exists()&&length()>0)||(lockMeta.exists()&&lockMeta.length()>0);
	}
}