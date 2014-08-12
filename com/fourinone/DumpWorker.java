package com.fourinone;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import com.fourinone.FileAdapter.ByteReadParser;
import com.fourinone.FileAdapter.ByteWriteParser;

class DumpWorker extends MigrantWorker implements CoolHashBase
{
	ByteWriteParser bwp = DumpAdapter.getByteWriteParser();
	ByteReadParser brp = DumpAdapter.getByteReadParser();
	ConstantBit.Target ct=ConstantBit.Target.POINT;
	
	List<String> pl = new ArrayList<String>();
	Object dump(String d, int i, String k, byte[] v, byte c, boolean p, String... psk){
		byte[] vl=null;
		int s=0;
		if(c<0x14){
			DumpAdapter da = new DumpAdapter(d,i);
			if(da.exists()&&da.length()>0){
				s = brp.reset(da.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
				byte[] btall = da.getReader(DumpAdapter.ConstBit[2],s).readAll();
				da.close();
				if(btall!=null&&btall.length>0){
					brp.reset(btall);
					while(!isInterrupted()&&brp.reading()){
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
											DumpAdapter pkda = da.getKeyMeta(pk.toString());
											pkda.readLock();//pkda=pkda.getLockMeta();
											int g=pkda.getGroupMeta().length;
											for(int n=0;n<g;n++){
												vl = (byte[])dump(pkda.toString(),n,pk.toString(),v,c,p,psk);
												if(vl!=null)
													break;
											}
											pkda.releaseLock();
											pl.remove(pk.toString());
										}else chex.pointLoopException();
									}
									break;
								}
							}
						}
						vl=null;
					}
					if(vl!=null&&c!=0){
						DumpAdapter daw = new DumpAdapter(da.toString());
						int index = brp.getReadIndex();
						byte[] bts = brp.read((int)da.length()-index);
						if(bts!=null)
							daw.getWriter(index-k.length()-vl.length-DumpAdapter.ConstBit[1], bts.length).write(bts);
						s=s-k.length()-vl.length-DumpAdapter.ConstBit[1]-DumpAdapter.ConstBit[2];
						bwp.reset().writeInt(s);
						daw.getWriter(0,DumpAdapter.ConstBit[2]).write(bwp.getBytes());
						daw.close();
					}
				}
			}
		}
		if(c>=0x12){
			if(c==0x16){
				DumpAdapter da = new DumpAdapter(d,i);
				if(da.exists()&&da.length()>0){
					s = brp.reset(da.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
					da.close();
				}
			}
			int ns = k.length()+v.length+DumpAdapter.ConstBit[1]+DumpAdapter.ConstBit[2];
			if(ns+DumpAdapter.ConstBit[2]>DumpAdapter.ms){
				chex.exceedException();
				return null;
			}
			ns+=s;
			if(ns+DumpAdapter.ConstBit[2]>DumpAdapter.ms){
				return dump(d,i+1,k,v,c,p,psk);
			}
			byte[] btn = bwp.reset().writeInt(ns).getBytes();
			byte[] bts = bwp.reset(ns).writeShort((short)k.length()).writeChars(k).writeInt(v.length).writeBytes(v).getBytes();
			DumpAdapter daw = new DumpAdapter(d,i);
			daw.getWriter(0,DumpAdapter.ConstBit[2]).write(btn);
			daw.getWriter(DumpAdapter.ConstBit[2]+s,bts.length).write(bts);
			daw.close();
		}
		return vl;
	}
	
	private List<String[]> pla = new ArrayList<String[]>();
	private Object dump(String d, String[] k, Object[] v, Filter f, byte c, boolean p, String... psk){
		int r=0;
		CoolHashMap cm = new CoolHashMap();
		HashSet kl=new HashSet(Arrays.asList(k));
		DumpAdapter dal=new DumpAdapter(d);
		boolean l=c>0x28?dal.writeLock():dal.readLock();
		DumpAdapter da=dal.getLockMeta();
		if(da.exists()&&da.length()!=0){
			CoolHashMap cmp = new CoolHashMap();
			String[] metagroup = da.getGroupMeta();
			for(int i=0;i<metagroup.length;i++){
				DumpAdapter dar = new DumpAdapter(metagroup[i]);
				brp.reset(dar.getReader(0,DumpAdapter.ConstBit[2]).readAll());
				int s=brp.readInt();
				if(s>0){
					byte[] btall = dar.getReader(DumpAdapter.ConstBit[2],s).readAll();
					brp.reset(btall);
					bwp.reset();
					int index=-1;
					while(brp.reading()){
						short ks = brp.readShort();
						if(ks>0){
							String kcs = brp.readChars(ks);
							int vs = brp.readInt();
							if(vs>0){
								byte[] vbt = brp.read(vs);
								if(kl.contains(kcs)){
									if(c>0x28&&index==-1)
										index = brp.getReadIndex()-ks-vs-DumpAdapter.ConstBit[1]-DumpAdapter.ConstBit[2];
									if(p&&vbt[vbt.length-1]==(byte)DumpAdapter.ConstBit[8]){
										StringBuilder pk = (new StringBuilder(ct.getTargetObject(vbt, String.class))).append(psk!=null&&pla.size()<psk.length?psk[pla.size()]:"");
										cmp.put(kcs, pk.toString());
									}
									else{
										try{
											if(f==null||f.match(vbt))
												cm.putValue(kcs, vbt);
										}catch(Exception ex){
											LogUtil.info("[Filter]", "[match]", "[Error Exception:]", ex);
										}
									}
								}else if(c>0x28&&index>-1){
									bwp.writeShort(ks).writeChars(kcs).writeInt(vs).writeBytes(vbt);
								}
							}
						}
					}
					if(c>0x28&&index>-1){
						byte[] bts = bwp.getBytes();
						byte[] btn = bwp.reset().writeInt(index+bts.length).getBytes();
						DumpAdapter daw = new DumpAdapter(dar.toString());
						daw.getWriter(0,DumpAdapter.ConstBit[2]).write(btn);
						daw.getWriter(index+DumpAdapter.ConstBit[2],bts.length).write(bts);
						daw.close();
						r=cm.size();
					}
				}
				dar.close();
				if(cm.size()==kl.size()||(cm.containsKey("0x1e")&&(int)cm.get("0x1e")==kl.size()))
					break;
			}
			if(cmp.size()>0){
				dal.releaseLock();
				String[] klstr = (String[])cmp.keySet().toArray(new String[0]);
				String[] vlstr = (String[])cmp.values(String.class).toArray(new String[0]);
				WareHouse wh = da.getKeyMetaStr(vlstr);
				Set<Map.Entry> meset=(Set<Map.Entry>)wh.entrySet();
				for(Entry me:meset){
					String cd=(String)me.getKey();
					String[] cfks=((List<String>)me.getValue()).toArray(new String[0]);
					boolean contains=false;
					for(String[] ps:pla){
						if(Arrays.equals(ps,cfks)){
							contains=true;
							break;
						}
					}
					if(!contains){
						pla.add(cfks);
						CoolHashMap cmcf = (CoolHashMap)dump(cd,cfks,v,f,c,p,psk);
						for(int i=0;i<vlstr.length;i++)
							if(cmcf.containsKey(vlstr[i]))
								cmp.putValue(klstr[i],cmcf.getValue(vlstr[i]));
						pla.remove(cfks);
					}else chex.pointLoopException();
				}
				meset=(Set<Map.Entry>)cmp.getEntrySet();
				for(Entry me:meset)
					if(f==null||f.match((byte[])me.getValue()))
						cm.putValue(me.getKey(),me.getValue());
				return cm;
			}
		}
		if(c>0x2a){
			int i=0,j=0;
			r=0;
			while(j<kl.size()){
				DumpAdapter dar = new DumpAdapter(da,i++);
				int s=0;
				if(dar.exists()&&dar.length()>0)
					s=brp.reset(dar.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
				bwp.reset();
				while(j<kl.size()){
					int ns = k[j].length()+((byte[])v[j]).length+DumpAdapter.ConstBit[1]+DumpAdapter.ConstBit[2];
					if(ns+bwp.getBytesLength()+s+DumpAdapter.ConstBit[2]<=DumpAdapter.ms){
						bwp.writeShort((short)k[j].length()).writeChars(k[j]).writeInt(((byte[])v[j]).length).writeBytes((byte[])v[j]);
						r++;
					}else break;
					j++;
				}
				if(bwp.getBytesLength()>0){
					byte[] nbts=bwp.getBytes();
					byte[] btn=bwp.reset(DumpAdapter.ConstBit[2]).writeInt(nbts.length+s).getBytes();
					DumpAdapter daw = new DumpAdapter(dar.toString());
					daw.getWriter(0,DumpAdapter.ConstBit[2]).write(btn);
					daw.getWriter(DumpAdapter.ConstBit[2]+s,nbts.length).write(nbts);
					daw.close();
				}
				dar.close();
			}
		}
		dal.releaseLock();
		if(c>0x28)
			cm.put("0x1e",r);
		return cm;
	}

	private Object dump(String d,String w,Filter f,byte c,boolean p,String... psk){
		Object dumpobj=c>0x1e?new CoolHashMap():CoolHashMap.newKeySet();
		DumpAdapter dal = new DumpAdapter(d);
		if(dal.being()){
			dal.readLock();
			DumpAdapter da=dal.getLockMeta();
			CoolHashMap cmp = new CoolHashMap();
			String[] metagroup = da.getGroupMeta();
			for(int i=0;i<metagroup.length;i++){
				DumpAdapter dar = new DumpAdapter(metagroup[i]);
				int s=brp.reset(dar.getReader(0,DumpAdapter.ConstBit[2]).readAll()).readInt();
				if(s>0){
					brp.reset(dar.getReader(DumpAdapter.ConstBit[2],s).readAll());
					while(brp.reading()){
						short ks = brp.readShort();
						if(ks>0){
							String kcs = brp.readChars(ks);
							int vs = brp.readInt();
							if(vs>0){
								byte[] vbt = brp.read(vs);
									if(p&&vbt[vbt.length-1]==(byte)DumpAdapter.ConstBit[8]){
										StringBuilder pk = (new StringBuilder(ct.getTargetObject(vbt, String.class))).append(psk!=null?psk[0]:"");
										cmp.put(kcs, pk.toString());
									}
									else{
										try{
											if(f==null||f.match(vbt)){
												if(checkMatch(kcs,w)){
													if(c>0x1e) ((CoolHashMap)dumpobj).putValue(kcs, vbt);
													else ((CoolHashMap.CoolKeySet)dumpobj).addKey(kcs);
												}
											}
										}catch(Exception ex){
											LogUtil.info("[Filter]", "[match]", "[Error Exception:]", ex);
										}
									}
							}
						}
					}
				}
				dar.close();
			}
			dal.releaseLock();
			if(cmp.size()>0){
				String[] klstr = (String[])cmp.keySet().toArray(new String[0]);
				String[] vlstr = (String[])cmp.values(String.class).toArray(new String[0]);
				if(psk!=null)
					psk=psk.length>1?Arrays.copyOfRange(psk,1,psk.length):null;//psk=Arrays.copyOfRange(psk,1,psk.length-1);
				WareHouse wh = da.getKeyMetaStr(vlstr);
				Set<Map.Entry> meset=(Set<Map.Entry>)wh.entrySet();
				for(Entry me:meset){
					String cd=(String)me.getKey();
					String[] cfks=((List<String>)me.getValue()).toArray(new String[0]);
					CoolHashMap cmcf=(CoolHashMap)dump(cd,cfks,null,f,(byte)0x28,p,psk);
					for(int i=0;i<vlstr.length;i++){
						if(cmcf.containsKey(vlstr[i])){
							if(c>0x1e) ((CoolHashMap)dumpobj).putValue(klstr[i],cmcf.getValue(vlstr[i]));
							else ((CoolHashMap.CoolKeySet)dumpobj).addKey(klstr[i]);
						}
					}
				}
			}
		}
		return dumpobj;
	}
	
	private boolean checkMatch(String kn, String fw){
		ArrayList<String> knlist=new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(kn,"\u002E");
		while(tokenizer.hasMoreTokens())
			knlist.add(tokenizer.nextToken());
		ArrayList<String> fwlist=new ArrayList<String>();
		tokenizer = new StringTokenizer(fw,"\u002E");
		while(tokenizer.hasMoreTokens())
			fwlist.add(tokenizer.nextToken());
		
		if(knlist.size()<fwlist.size())
			return false;
		else if(knlist.size()>fwlist.size()&&!fwlist.get(fwlist.size()-1).equals("\u002A"))
			return false;
		else for(int i=0;i<knlist.size();i++)
				if(!knlist.get(i).equals(fwlist.get(Math.min(fwlist.size()-1,i)))&&!fwlist.get(Math.min(fwlist.size()-1,i)).equals("\u002A"))
					return false;
		return true;
	}
	
	Filter getFilter(WareHouse inhouse){
		Filter filter = null;
		try{
			String filterKey = inhouse.getString(0x20);
			if(filterKey!=null){
				Object[] filterValue = (Object[])inhouse.get(0x24);
				filter = new Filter.ValueFilter(filterKey, filterValue);
			}
		}catch(Exception ex){
			LogUtil.info("[Filter]", "[getFilter]", "[Error Exception:]", ex);
		}
		return filter;
	}	
	
	public WareHouse doTask(WareHouse inhouse)
	{
		WareHouse wh = new WareHouse();
		try{
			byte c=inhouse.getByte(0x0);
		 	String d=inhouse.getSeparator(0xa);
		 	boolean p=inhouse.getBoolean(0x28);
			String[] psk=p?(String[])inhouse.get(0x32):null;
		 	if(c<0x1e){
		 		int i=inhouse.getInt(0xc);
		 		String k=inhouse.getString(0x14);
				byte[] v=(byte[])inhouse.get(0x1e);
				byte[] gv=(byte[])dump(d,i,k,v,c,p,psk);
				if(gv!=null&&(c==0x0||c==0xa))
			 		wh.put("-interrupted", "true");
			 	wh.put(0x1e, gv);
			}else{
				Filter f=getFilter(inhouse);
				if(c<0x28){
					String w=inhouse.getString(0x3c);
					Object dumpobj=dump(d,w,f,c,p,psk);
				 	if(c>0x1e){
				 		wh.put(0x14,((CoolHashMap)dumpobj).keySet().toArray(new String[0]));
				 		wh.put(0x1e,((CoolHashMap)dumpobj).getValues().toArray());
				 	}else wh.put(0x14,((CoolHashMap.CoolKeySet)dumpobj).toArray(new String[0]));
				}else if(c<0x3c){
					String[] k=(String[])inhouse.get(0x14);
					Object[] v=(Object[])inhouse.get(0x1e);
					CoolHashMap chm=(CoolHashMap)dump(d,k,v,f,c,p,psk);
					if(c>0x28){
				 		wh.put(0x1e,chm.get("0x1e"));
				 	}else{
				 		v=new Object[k.length];
				 		for(int i=0;i<k.length;i++)
				 			v[i]=chm.getValue(k[i]);
				 		wh.put(0x1e,v);
				 	}
				}
			}
		}catch(Exception ex){
			LogUtil.info("[DumpWorker]", "[doTask]", ex.toString());
		}
		return wh;
	}
	
	void waitWorking(String[] args){
		if(args!=null&&args.length==2)
			waitWorking(args[0],Integer.parseInt(args[1]),"DataWorker");
		else if(args!=null&&args.length==4)
			waitWorking(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]),"DataWorker");
	}
	
	public static void main(String[] args)
	{
		DumpWorker dw = new DumpWorker();
		dw.waitWorking(args);
	}
}