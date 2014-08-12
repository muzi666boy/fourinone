package com.fourinone;

import java.util.Date;
import java.io.Serializable;

interface ConstantBit<T> extends CoolHashBase{
	byte[] getBytes(T value);
	T getObject(byte[] bts);
	Target tg = Target.POINT;
	enum Target implements ConstantBit,Filter.Condition{
		STRING(DumpAdapter.ConstBit[0],DumpAdapter.ConstBit[0]){
			public boolean matchAction(Object key, Object value){
				Filter<String,Object> filter = (Filter)key;
				Filter.Condition cd = Filter.Condition.Action.valueOf(filter.getFilterKey());
				return cd.matchAction(getObject((byte[])value),filter.getFilterValue());
			}
			
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeChars((String)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public String getObject(byte[] bts){
				brp.reset(bts);
				return brp.readChars(bts.length-1);
			}
		},
		SHORT(DumpAdapter.ConstBit[1],DumpAdapter.ConstBit[1]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeShort((short)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Short getObject(byte[] bts){
				brp.reset(bts);
				return brp.readShort();
			}
		},
		INTEGER(DumpAdapter.ConstBit[2],DumpAdapter.ConstBit[2]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeInt((int)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Integer getObject(byte[] bts){
				brp.reset(bts);
				return brp.readInt();
			}
		},
		FLOAT(DumpAdapter.ConstBit[4],DumpAdapter.ConstBit[2]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeFloat((float)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Float getObject(byte[] bts){
				brp.reset(bts);
				return brp.readFloat();
			}
		},
		LONG(DumpAdapter.ConstBit[3],DumpAdapter.ConstBit[3]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeLong((long)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Long getObject(byte[] bts){
				brp.reset(bts);
				return brp.readLong();
			}
		},
		DOUBLE(DumpAdapter.ConstBit[5],DumpAdapter.ConstBit[3]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeDouble((double)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Double getObject(byte[] bts){
				brp.reset(bts);
				return brp.readDouble();
			}
		},
		DATE(DumpAdapter.ConstBit[6],DumpAdapter.ConstBit[3]){
			public boolean matchAction(Object key, Object value){
				return LONG.matchAction(key,value);
			}
			
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeDate((Date)value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Date getObject(byte[] bts){
				brp.reset(bts);
				return brp.readDate();
			}
		},
		STATGOBJECT(DumpAdapter.ConstBit[7],DumpAdapter.ConstBit[0]){
			public byte[] getBytes(Object value){
				bwp.reset();
				bwp.writeObject(value);
				bwp.writeBytes(new byte[]{(byte)codeKey});
				return bwp.getBytes();
			}
			
			public Object getObject(byte[] bts){
				brp.reset(bts);
				return brp.readObject(bts.length-1);
			}
		},
		POINT(DumpAdapter.ConstBit[8],DumpAdapter.ConstBit[0]){
			public boolean matchAction(Object key, Object value){
				return STRING.matchAction(key,value);
			}
			
			public byte[] getBytes(Object value){
				byte[] bts = STRING.getBytes(value);
				bts[bts.length-1]=(byte)codeKey;
				return bts;
			}
			
			public Object getObject(byte[] bts){
				return STRING.getObject(bts);
			}
		};
		
		static Target getTarget(byte codeKey){
			for(Target tg:Target.values())
				if(tg.codeKey==codeKey)
					return tg;
			return null;
		}
		
		private static <T> Target getTarget(Class<T> value, boolean match){
			try{
				return Target.valueOf(value.getSimpleName().toUpperCase());
			}catch(Exception e){
				if(!match&&Serializable.class.isAssignableFrom(value))
					return STATGOBJECT;
				else
					LogUtil.info("[CoolHashException]","[IllegalArgumentException]",chex.checkTargetLog(value));
			}
			return null;
		}
		
		static <T> Target getTarget(Class<T> value){
			return getTarget(value, false);
		}
		
		static <T> Target getTarget(T value){
			Target tg=getTarget(value.getClass());
			return tg;
		}
		
		static <T> Target getTargetMatch(Class<T> value){
			return getTarget(value, true);
		}
		
		static <T> byte[] getTargetBytes(T value){
			ConstantBit<T> cb = ConstantBit.Target.getTarget(value);
			byte[] bts = cb.getBytes(value);
			return bts;
		}
		
		static <T> T getTargetObject(byte[] bts){
			return getTargetObject(bts,null);
		}
		
		static <T> T getTargetObject(byte[] bts, Class<T> value){
			if(bts==null||bts.length==0){
				return null;
			}else{
				ConstantBit<T> cb=value!=null?ConstantBit.Target.getTarget(value):ConstantBit.Target.getTarget(bts[bts.length-1]);
				return cb.getObject(bts);
			}
		}
		
		public boolean matchAction(Object key, Object value){
			Filter<String,Object> filter = (Filter)key;
			byte[] vb = (byte[])value;
			if(vb.length-1==codeValue&&vb[vb.length-1]==codeKey){//+1? type==type
				Filter.Condition cd = Filter.Condition.Action.valueOf(filter.getFilterKey());
				return cd.matchAction(getObject(vb),filter.getFilterValue());
			}
			return false;
		}
		
		int codeKey;
		int codeValue;
		Target(int codeKey, int codeValue){
			this.codeKey = codeKey;
			this.codeValue = codeValue;
		}
	}
}