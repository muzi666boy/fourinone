package com.fourinone;

import com.fourinone.FileAdapter.ByteReadParser;
import com.fourinone.FileAdapter.ByteWriteParser;
import java.util.List;
import java.io.File;

class CoolHashWorker extends DumpWorker{
	ByteWriteParser bwp = DumpAdapter.getByteWriteParser();
	ByteReadParser brp = DumpAdapter.getByteReadParser();
	ConstantBit.Target ct=ConstantBit.Target.POINT;
	
	Object dump(String d, int i, String k, byte[] v, byte c, boolean p, String... psk){
		byte[] vl=null;
		int s=0;
		int j=0;
		DumpAdapter dal=new DumpAdapter(d);
		boolean l=c>0?dal.writeLock():dal.readLock();
		DumpAdapter da=dal.getLockMeta();
		if(da.exists()&&da.length()!=0){
			String[] metagroup = da.getGroupMeta();
			for(;j<metagroup.length;j++){
				DumpAdapter dar = new DumpAdapter(metagroup[j]);
				s = brp.reset(dar.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
				byte[] btall = dar.getReader(DumpAdapter.ConstBit[2],s).readAll();
				if(btall!=null&&btall.length>0){
					brp.reset(btall);
					while(brp.reading()){
						short ks = brp.readShort();
						if(ks>0){
							String kcs = brp.readChars(ks);
							int vs = brp.readInt();
							if(vs>0){
								vl = brp.read(vs);
								if(k.equals(kcs)){
									if(p&&vl[vl.length-1]==(byte)DumpAdapter.ConstBit[8]){
										StringBuilder pk = (new StringBuilder(ct.getTargetObject(vl, String.class))).append(psk!=null&&pl.size()<psk.length?psk[pl.size()]:"");
										if(!pl.contains(pk.toString())){
											pl.add(pk.toString());
											DumpAdapter pkda = dar.getKeyMeta(pk.toString());
											dal.releaseLock();
											byte[] plvl = (byte[])dump(pkda.toString(),i,pk.toString(),v,c,p,psk);
											pl.remove(pk.toString());
											return plvl!=null?plvl:vl;
										}else chex.pointLoopException();
									}
									break;
								}
							}
						}
						vl=null;
					}
					if(vl!=null&&c!=0){
						DumpAdapter daw = new DumpAdapter(dar.toString());
						int index = brp.getReadIndex();
						byte[] bts = brp.read((int)dar.length()-index);
						if(bts!=null)
							daw.getWriter(index-k.length()-vl.length-DumpAdapter.ConstBit[1], bts.length).write(bts);
						s=s-k.length()-vl.length-DumpAdapter.ConstBit[1]-DumpAdapter.ConstBit[2];
						bwp.reset().writeInt(s);
						daw.getWriter(0,DumpAdapter.ConstBit[2]).write(bwp.getBytes());
						daw.close();
					}
				}
				dar.close();
				/*if(vl!=null&&c==0)
					break;
				else vl=null;*/
				if(vl!=null)
					break;
			}
		}
		if(c>=0x12){
			while(true){
				if(j>0){
					DumpAdapter dar = new DumpAdapter(da,i);
					if(dar.exists()&&dar.length()>0){
						s = brp.reset(dar.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
						dar.close();
					}
				}
				int ns = k.length()+v.length+DumpAdapter.ConstBit[1]+DumpAdapter.ConstBit[2];
				if(ns+DumpAdapter.ConstBit[2]>DumpAdapter.ms){
					chex.exceedException();
					return null;
				}
				ns+=s;
				if(ns+DumpAdapter.ConstBit[2]<=DumpAdapter.ms){
					byte[] btn = bwp.reset().writeInt(ns).getBytes();
					byte[] bts = bwp.reset(ns).writeShort((short)k.length()).writeChars(k).writeInt(v.length).writeBytes(v).getBytes();
					DumpAdapter daw = new DumpAdapter(da,i);
					daw.getWriter(0,DumpAdapter.ConstBit[2]).write(btn);
					daw.getWriter(DumpAdapter.ConstBit[2]+s,bts.length).write(bts);
					daw.close();
					break;
				}else i++;
				s=0;
			}
		}
		boolean rl=dal.releaseLock();
		return vl;
	}
	
	public WareHouse doTask(WareHouse inhouse)
	{
		WareHouse wh = new WareHouse();
		try{
			byte c=inhouse.getByte(0x0);
			if(c>=0x40){
				String w=inhouse.getString(0x3c);
				List<File> df=dumpAdapter.getWalkTree(w);
				wh.put(0x1e,df);
			}else wh=super.doTask(inhouse);
		}catch(Exception ex){
			LogUtil.info("[CoolHashWorker]", "[doTask]", ex.toString());
		}
		return wh;
	}
	
	public static void main(String[] args)
	{
		CoolHashWorker cw = new CoolHashWorker();
		cw.waitWorking(args);
	}
}