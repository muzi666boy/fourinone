package com.fourinone;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.Buffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Method;
import com.fourinone.FileBatch.TryByteReadAdapter;
import com.fourinone.FileBatch.TryByteWriteAdapter;
import com.fourinone.FileBatch.TryIntReadAdapter;
import com.fourinone.FileBatch.TryIntWriteAdapter;
import java.util.Date;
import java.io.FilenameFilter;
import java.nio.channels.OverlappingFileLockException;

public class FileAdapter extends File
{
	private RandomAccessFile raf = null;
	private FileChannel fc = null;
	private ByteBuffer mbread=null,mbwrite=null;

	public static final short[] ConstBit = new short[]{0<<1,1<<1,1<<2,1<<3,3<<1,5<<1,6<<1,7<<1,1<<0};

	public static long k(long num)
	{
		return 0x400*num;
	}
	
	public static long m(long num)
	{
		return 0x400*k(num);
	}
	
	public static long g(long num)
	{
		return 0x400*m(num);
	}
	
	public FileAdapter(String filePath)
	{
		super(filePath);
		//fl = new File(filePath);
	}
	
	public FileAdapter(String parentPath, String filePath)
	{
		super(parentPath, filePath);
		//fl = parentPath!=null?new File(parentPath, filePath):new File(filePath);
	}
	

	FileLock flk = null;

	public boolean readLock(){
		return readLock(0L, Long.MAX_VALUE);
	}

	public boolean readLock(long beginIndex, long bytesNum){
		return setLock("r", 0L, Long.MAX_VALUE, true);
	}

	public boolean writeLock(){
		return writeLock(0L, Long.MAX_VALUE);
	}

	public boolean writeLock(long beginIndex, long bytesNum){
		if(!this.exists())
			createFile(this.getPath());
		return setLock("rw", 0L, Long.MAX_VALUE, false);
	}

	boolean setLock(String rw, long beginIndex, long bytesNum, boolean s){
		if(!this.exists())
			return false;
		try{
			raf = new RandomAccessFile(this, rw);
			fc = raf.getChannel();
			flk = fc.lock(beginIndex, bytesNum, s);
		}catch(OverlappingFileLockException oe){
			LogUtil.warn("[SetLock]", "[No effect]", "the region has already been locked");
			return false;
		}catch(Exception e){
			LogUtil.info("[SetLock]", "[No effect]", e);
			return false;
		}
		return true;
	}

	public boolean releaseLock(){
		if(!this.exists()||flk==null)
			return false;
		try{
			flk.release();
		}catch(Exception e){
			LogUtil.info("[ReleaseLock]", "[No effect]", e);
			return false;
		}
		return true;
	}

	public static ByteReadParser getByteReadParser(byte[] array){
		if(array==null)
			array=new byte[0];
		FileAdapter fa = new FileAdapter("");
		fa.mbread = ByteBuffer.wrap(array);
		return fa.getReader(Long.MIN_VALUE,Long.MAX_VALUE);
	}
	
	public static ByteReadParser getByteReadParser(){
		return getByteReadParser(null);
	}

	public interface ByteReadParser{
		public byte[] read(int totalnum);
		public byte[] readLine();
		public byte[] read(byte[] split);
		public byte[] readLast(byte[] split);
		public void jump(int num);
		public int getReadIndex();
		public short readShort();
		public int readInt();
		public long readLong();
		public float readFloat();
		public double readDouble();
		public Date readDate();
		public String readString(int length);
		public byte[] readCharsBytes(int length);
		public String readChars(int length);
		public Object readObject(int length);
		public ByteReadParser reset(byte[] array);
		public boolean reading();
	}
	
	public interface ByteReadAdapter extends ByteReadParser,TryByteReadAdapter{
		public byte[] readAll();
		public byte[] readAllSafety() throws FileException;
	}

	public interface IntReadAdapter extends TryIntReadAdapter{
		public int[] readIntAll();
		public int[] readIntAllSafety() throws FileException;
		public int[] readInt(int totalnum);
		public int readInt();
		public List<Integer> readListIntAll();
		public List<Integer> readListInt(int totalnum);
	}
	
	public interface ReadAdapter extends ByteReadAdapter,IntReadAdapter{}
		
	private boolean initRead(long beginIndex, long bytesNum)
	{
		if(beginIndex==Long.MIN_VALUE&&bytesNum==Long.MAX_VALUE)
			return true;
		
		try{
			if(raf==null)
			{
				raf = new RandomAccessFile(this, "r");//fl
				fc = raf.getChannel();
			}
			
			long readnum = Math.min(this.length()-beginIndex, bytesNum);//fl
			//if(readnum==0)return false;
			mbread = fc.map(MapMode.READ_ONLY, beginIndex, readnum);
		}catch(Exception e){
			//System.out.println(e);
			LogUtil.info("[FileAdapter]", "[ReadAdapter]", e.getMessage());
			return false;
		}
		return true;
	}	
	
	public interface IntWriteAdapter extends TryIntWriteAdapter{
		public int writeInt(int[] its);
		public int writeIntSafety(int[] its) throws FileException;
		public int writeListInt(List<Integer> ls);
	}
	
	public interface ByteWriteAdapter extends TryByteWriteAdapter{
		public int write(byte[] bytes);
		public int writeSafety(byte[] bytes) throws FileException;
	}
	
	public interface WriteAdapter extends ByteWriteAdapter,IntWriteAdapter{}

	class NameFilter implements FilenameFilter{
		private String namestr;
		private boolean onlyFile=false;
		public NameFilter(String namestr, boolean onlyFile){
			this.namestr = namestr;
			this.onlyFile = onlyFile;
		}
		public boolean accept(File dir, String name){
			if(name.startsWith(namestr)){
				if(onlyFile){
					File dirfile = new File(dir,name);
					if(dirfile.isFile())
						return true;
					else return false;
				}else return true;
			}else return false;
  		}
	}
	
	public interface ByteWriteParser{
		public ByteWriteParser writeBytes(byte[] bts);
		public ByteWriteParser writeShort(short n);
		public ByteWriteParser writeInt(int n);
		public ByteWriteParser writeDouble(double n);
		public ByteWriteParser writeLong(long n);
		public ByteWriteParser writeFloat(float n);
		public ByteWriteParser writeDate(Date time);
		public ByteWriteParser writeString(String n);
		public ByteWriteParser writeCharsBytes(byte[] bts);
		public ByteWriteParser writeChars(String n);
		public ByteWriteParser writeObject(Object n);
		public int getBytesLength();
		public byte[] getBytes();
		public ByteWriteParser reset();
		public ByteWriteParser reset(int size);
	}
	
	public static ByteWriteParser getByteWriteParser(){
		return getByteWriteParser((int)k(512));
	}
	
	public static ByteWriteParser getByteWriteParser(int size){
		FileAdapter fa = new FileAdapter("");
		return fa.getByteParserWriter(size);
	}
	
	public ByteWriteParser getByteParserWriter(final int size){
		return size<=0?null:new ByteWriteParser(){
			private int bytesLength=0;
			{reset(size);}
			
			public ByteWriteParser writeBytes(byte[] bts){
				if(bts!=null&&bts.length>0){
					autoBuffer(bts.length);
					mbwrite.put(bts);
					bytesLength+=bts.length;
				}
				return this;
			}
			
			public ByteWriteParser writeShort(short n){
				autoBuffer(ConstBit[1]);
				mbwrite.putShort(n);
				bytesLength+=ConstBit[1];
				return this;
			}
			
			public ByteWriteParser writeInt(int n){
				autoBuffer(ConstBit[2]);
				mbwrite.putInt(n);
				bytesLength+=ConstBit[2];
				return this;
			}
			
			public ByteWriteParser writeDouble(double n){
				autoBuffer(ConstBit[3]);
				mbwrite.putDouble(n);
				bytesLength+=ConstBit[3];
				return this;
			}
			
			public ByteWriteParser writeLong(long n){
				autoBuffer(ConstBit[3]);
				mbwrite.putLong(n);
				bytesLength+=ConstBit[3];
				return this;
			}
			
			public ByteWriteParser writeFloat(float n){
				autoBuffer(ConstBit[2]);
				mbwrite.putFloat(n);
				bytesLength+=ConstBit[2];
				return this;
			}
			
			public ByteWriteParser writeDate(Date time){
				return writeLong(time.getTime());
			}
			
			public ByteWriteParser writeString(String n){
				return writeBytes(n.getBytes());
			}
			
			//ObjectBytes objectBytes = new ObjectBytes();
			public ByteWriteParser writeCharsBytes(byte[] bts){
				return writeBytes(ObjectBytes.getCharSequence(bts));
			}
			
			public ByteWriteParser writeChars(String n){
				return writeCharsBytes(n.getBytes());
			}
			
			public ByteWriteParser writeObject(Object n){
				return writeBytes(ObjectBytes.toBytes(n));
			}
			
			public int getBytesLength(){
				return bytesLength;
			}
			
			public byte[] getBytes(){
				byte[] btsnew = new byte[bytesLength];
				System.arraycopy(mbwrite.array(),0,btsnew,0,bytesLength);
				return btsnew;
			}
			
			public ByteWriteParser reset(){
				//return reset(size);
				mbwrite.clear();
				bytesLength=0;
				return this;
			}
			
			public ByteWriteParser reset(int newsize){
				mbwrite = ByteBuffer.wrap(new byte[newsize]);
				bytesLength=0;
				return this;
			}
			
			private void autoBuffer(int length){
				while(mbwrite.remaining()<length){
					byte[] btsnew = new byte[mbwrite.capacity()+size];
					System.arraycopy(mbwrite.array(),0,btsnew,0,bytesLength);
					mbwrite = ByteBuffer.wrap(btsnew,bytesLength,btsnew.length-bytesLength);
				}
			}
		};
	}
//----------------	
	private void initWrite()
	{
		try{
			if(raf==null)
			{
				if(!this.exists())//fl
					createFile(this.getPath());//fl
				raf = new RandomAccessFile(this, "rw");//fl
				fc = raf.getChannel();
			}		
		}catch(Exception e){
			LogUtil.info("[FileAdapter]", "[WriteAdapter]", e.getMessage());
		}
	}
	
	public ByteReadAdapter getByteReader(){
		return getReader();
	}
	
	public IntReadAdapter getIntReader(){
		return getReader();
	}
	
	public ReadAdapter getReader(){
		return getReader(0, this.length());//byte.length
	}
	
	public ByteReadAdapter getByteReader(long beginIndex, long bytesNum){
		return getReader(beginIndex, bytesNum);
	}
	
	public IntReadAdapter getIntReader(long beginIndex, long intNum){
		return getReader(beginIndex*4, intNum*4);
	}
	
	public ReadAdapter getReader(final long beginIndex, final long bytesNum){
		if(!initRead(beginIndex, bytesNum))
			return null;
		else return new ReadAdapter(){
			public void jump(int num){
				int jumpnum = Math.min(mbread.remaining(),num);
				mbread.position(getReadIndex()+jumpnum);
			}
			
			public int getReadIndex(){
				return mbread.position();
			}
			
			public byte[] readAll(){
				return read((int)Math.min(bytesNum, Integer.MAX_VALUE));
			}
			
			public byte[] readAllSafety() throws FileException{
				byte[] rbts = null;
				FileLock fl = null;
				try{
					fl = fc.lock(beginIndex,bytesNum,true);
					rbts = readAll();
					fl.release();
				}catch(Exception ex){
					throw new FileException(ex);
				}
				return rbts;
			}
			
			public Result<byte[]> tryReadAll(){
				return tryReadAll(false);
			}
			
			public Result<byte[]> tryReadAllSafety(){
				return tryReadAll(true);
			}
			
			private Result<byte[]> tryReadAll(final boolean locked){
				final FileResult<byte[]> fr = new FileResult<byte[]>(false);
				PoolExector.tpe().execute(new Runnable(){
					public void run(){
						try{
							byte[] wh = locked?readAllSafety():readAll();
							if(wh!=null)
								fr.setResult(wh);
							fr.setReady(FileResult.READY);
						}catch(Throwable e){
							LogUtil.info("tryReadAll", "exception", e);
							//fr.status = FileResult.EXCEPTION;
							fr.setReady(FileResult.EXCEPTION);
						}
					}
				});
				return fr;
			}
			
			public byte[] read(int totalnum)
			{
				int readnum = Math.min(mbread.remaining(),totalnum);
				byte[] bt = null;
				try{
					if(readnum>0){
						bt = new byte[readnum];
						mbread.get(bt);
					}
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "[read]", e.getMessage());//
				}
				return bt;
			}
			
			public byte[] readLine()
			{
				byte[] bts = new byte[]{0xD,0xA};
				return read(bts);
			}
			
			public byte[] read(byte[] split)
			{
				if(split==null||mbread.remaining()==0)
					return null;
				
				int i=0,p=mbread.position(),n=0;
				while(mbread.hasRemaining()&&i<split.length){
					byte b = mbread.get();
					if(b==split[i]){
						if(i++==0)
							mbread.mark();
					}else{
						if(i>0){
							mbread.reset();
							i=0;
						}
						n++;
					}
				}
				
				if(i<split.length){
					n+=i;
					i=0;
				}
				
				mbread.position(p);
				byte[] rbts = new byte[n];
				mbread.get(rbts);
				mbread.position(mbread.position()+i);
		 		return rbts;
			}
			
			public byte[] readLast(byte[] split){
				byte[] bts = readAll();
				
				if(split==null||bts==null)
					return null;
				
				int i=bts.length-1,j=split.length-1,m=-1;
				while(i>=0&&j>=0){
					if(bts[i--]==split[j]){
						if(j--==split.length-1)
							m=i;
					}else if(m>0){
						i=m-1;
						j=split.length-1;
						m=-1;
					}
				}
				return i>0?Arrays.copyOf(bts, i+1):bts;
			}
			
			public int[] readIntAll(){
				return readInt((int)(bytesNum/4));
			}
			
			public int[] readIntAllSafety() throws FileException{
				int[] rits = null;
				FileLock fl = null;
				try{
					fl = fc.lock(beginIndex,bytesNum,true);
					rits = readIntAll();
					fl.release();
				}catch(Exception ex){
					throw new FileException(ex);
				}
				return rits;
			}
			
			public Result<int[]> tryIntReadAll(){
				return tryIntReadAll(false);
			}
			
			public Result<int[]> tryIntReadAllSafety(){
				return tryIntReadAll(true);
			}
			
			private Result<int[]> tryIntReadAll(final boolean locked){
				final FileResult<int[]> fr = new FileResult<int[]>(false);
				PoolExector.tpe().execute(new Runnable(){
					public void run(){
						try{
							int[] wh = locked?readIntAllSafety():readIntAll();
							if(wh!=null)
								fr.setResult(wh);
							fr.setReady(FileResult.READY);
						}catch(Throwable e){
							LogUtil.info("tryIntReadAll", "exception", e);
							fr.setReady(FileResult.EXCEPTION);
						}
					}
				});
				return fr;
			}
			
			public int[] readInt(int totalnum){
				int readnum = Math.min(mbread.remaining()/4,totalnum);
				int[] its = null;
				if(readnum>0){
					its = new int[readnum];
					for(int i=0;i<its.length;i++)
						its[i]=readInt();
				}
				return its;
			}
		
			public List<Integer> readListIntAll(){
				return readListInt((int)(bytesNum/4));
			}
			
			public List<Integer> readListInt(int totalnum){
				int size = Math.min(mbread.remaining()/4,totalnum);
				List<Integer> ls = null;
				if(size>0){
					ls = new ArrayList<Integer>(size);
					for(int i=0;i<size;i++)
						ls.add(readInt());
				}
				return ls;
			}

			public boolean reading(){
				return mbread.hasRemaining();
			}			
			
			public int readInt(){
				int n = 0;
				try{
					 n = mbread.getInt();
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "convert int data format error", e.toString());//
				}
				return n;
			}

			public short readShort(){
				short n = 0;
				try{
					 n = mbread.getShort();
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "convert short data format error", e.toString());
				}
				return n;
			}
			
			public long readLong(){
				long n = 0;
				try{
					 n = mbread.getLong();
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "convert long data format error", e.toString());
				}
				return n;
			}
			
			public float readFloat(){
				float n = 0;
				try{
					 n = mbread.getFloat();
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "convert float data format error", e.toString());
				}
				return n;
			}
			
			public double readDouble(){
				double n = 0;
				try{
					 n = mbread.getDouble();
				}catch(Exception e){
					LogUtil.info("[ReadAdapter]", "convert double data format error", e.toString());
				}
				return n;
			}
			
			public Date readDate(){
				long t = readLong();
				return t==0?null:new Date(t);
			}
			
			public String readString(int length){
				byte[] bts = read(length);
				return bts!=null?new String(bts):null;
			}
			
			//ObjectBytes objectBytes = new ObjectBytes();
			public byte[] readCharsBytes(int length){
				return ObjectBytes.getCharSequence(read(length));
			}
			
			public String readChars(int length){
				byte[] bts=readCharsBytes(length);
				return bts!=null?new String(bts):null;
			}
			
			public Object readObject(int length){
				byte[] bts = read(length);
				return bts!=null?ObjectBytes.toObject(bts):null;
			}
			
			public ByteReadParser reset(byte[] array){
				mbread = ByteBuffer.wrap(array);
				return this;
			}
		};
	}	
	
	public File createFile()
	{
		return createFile(this.getPath());
	}
	
	public File createDirectory()
	{
		return createFile(this.getPath(),false);
	}
	
	public File createFile(String fileUrl)
	{
		return createFile(fileUrl,true);
	}
	
	public File createFile(String fileUrl, boolean fileflag)
	{
		File newFile = new File(fileUrl);
		try{
			if(fileflag){
				String parentStr = newFile.getParent();
				String fileName = newFile.getName();
				File newDir = parentStr!=null?createFile(parentStr, false):new File("");
				File theFile = new File(newDir,fileName);
				theFile.createNewFile();
				/*if(!theFile.createNewFile())
					System.out.println(fileUrl+" already existed!");*/
			}else{
				if(!newFile.exists())
					newFile.mkdirs();
			}
		}catch(Exception e){
			LogUtil.info("[FileAdapter]", "[createFile]", e.getMessage()+":"+fileUrl);
		}
		return 	newFile;
	}
	
	public ByteWriteAdapter getByteWriter(){
		return getWriter();
	}
	
	public IntWriteAdapter getIntWriter(){
		return getWriter();
	}
	
	public WriteAdapter getWriter(){
		return getWriter(-1, -1);
	}
	
	public ByteWriteAdapter getByteWriter(long beginIndex, long bytesNum){
		return getWriter(beginIndex, bytesNum);
	}	
	
	public IntWriteAdapter getIntWriter(long beginIndex, long intNum){
		return getWriter(beginIndex*4, intNum*4);
	}
	
	public WriteAdapter getWriter(final long beginIndex, final long bytesNum){
		initWrite();
		final long filesize = this.length();
		return new WriteAdapter(){
			private long index,num;
			private void initIndexNum(long t){
				index = beginIndex<0?filesize:beginIndex;
				num = bytesNum<0?t:bytesNum;
			}
			
			private void initWriteBuffer(int total) throws IOException{
				initIndexNum(total);
				mbwrite = fc.map(MapMode.READ_WRITE, index, num);
			}
			
			public int write(byte[] bytes)
			{
				int t=0;
				try{
					initWriteBuffer(bytes.length);
					if(bytes.length>num)//mbread.remaining()
						bytes = Arrays.copyOf(bytes, (int)num);
					mbwrite.put(bytes);
					t=bytes.length;
				}catch(Exception e){
					LogUtil.info("[WriteAdapter]", "[write]", e.getMessage());//
				}
				return t;
			}
			
			/*public int writeSafety(byte[] bytes) throws FileException{
				int t = 0;
				FileLock fl = null;
				try{
					initIndexNum(bytes.length);
					fl = fc.lock(index,num,false);//
					t = write(bytes);//raf.write(1);
					fl.release();//
				}catch(Exception ex){
					throw new FileException(ex);
				}
				return t;
			}*/
			
			public int writeSafety(byte[] bytes) throws FileException{
				return writeSafety(bytes, null);
			}
			
			private int writeSafety(byte[] bytes, int[] its) throws FileException{
				int t = 0;
				FileLock fl = null;
				try{
					if(bytes!=null)
						initIndexNum(bytes.length);
					else
						initIndexNum(its.length*4);
					fl = fc.lock(index,num,false);
					t = bytes!=null?write(bytes):writeInt(its);
					fl.release();
				}catch(Exception ex){
					throw new FileException(ex);
				}
				return t;
			}
			
			private int write(byte[] bytes, int[] its){
				return bytes!=null?write(bytes):writeInt(its);
			}
			
			public Result<Integer> tryWrite(byte[] bytes){
				return tryWrite(bytes, null, false);
			}
			
			public Result<Integer> tryWriteSafety(byte[] bytes){
				return tryWrite(bytes, null, true);
			}
			
			/*private Result<Integer> tryWrite(final byte[] bytes, final boolean locked)
			{
				final FileResult<Integer> fr = new FileResult<Integer>(false);
				PoolExector.tpe().execute(new Runnable(){
					public void run(){
						try{
							int bl = locked?writeSafety(bytes):write(bytes);
							fr.setResult(new Integer(bl));
							//fr.setReady(true);
							fr.setReady(FileResult.READY);
						}catch(Throwable e){
							LogUtil.info("tryWrite", "exception", e);
							//fr.status = FileResult.EXCEPTION;
							fr.setReady(FileResult.EXCEPTION);
						}
					}
				});
				return fr;
			}*/
			
			private Result<Integer> tryWrite(final byte[] bytes, final int[] its, final boolean locked)
			{
				final FileResult<Integer> fr = new FileResult<Integer>(false);
				PoolExector.tpe().execute(new Runnable(){
					public void run(){
						try{
							int bl = locked?writeSafety(bytes, its):write(bytes, its);
							fr.setResult(new Integer(bl));
							//fr.setReady(true);
							fr.setReady(FileResult.READY);
						}catch(Throwable e){
							LogUtil.info("tryWrite", "exception", e);
							//fr.status = FileResult.EXCEPTION;
							fr.setReady(FileResult.EXCEPTION);
						}
					}
				});
				return fr;
			}
			
			public int writeInt(int[] its)
			{
				int i=0;
				try{
					initWriteBuffer(its.length*4);
					int wt = Math.min((int)(num/4),its.length);
					for(;i<wt;i++)
						mbwrite.putInt(its[i]);
				}catch(Exception e){
					LogUtil.info("[WriteAdapter]", "[writeInt]", e.getMessage());
				}
				return i;
			}
			
			/*public int writeIntSafety(int[] its) throws FileException
			{
				int t = 0;
				FileLock fl = null;
				try{
					initIndexNum(its.length*4);
					fl = fc.lock(index,num,false);
					t = writeInt(its);
					fl.release();
				}catch(Exception ex){
					throw new FileException(ex);
				}
				return t;
			}*/
			
			public int writeIntSafety(int[] its) throws FileException{
				return writeSafety(null, its);
			}
			
			public Result<Integer> tryIntWrite(int[] its){
				return tryWrite(null, its, false);
			}
			
			public Result<Integer> tryIntWriteSafety(int[] its){
				return tryWrite(null, its, true);
			}
			
			/*private Result<Integer> tryIntWrite(final int[] its, final boolean locked)
			{
				final FileResult<Integer> fr = new FileResult<Integer>(false);
				PoolExector.tpe().execute(new Runnable(){
					public void run(){
						try{
							int bl = locked?writeIntSafety(its):writeInt(its);
							fr.setResult(new Integer(bl));
							fr.setReady(FileResult.READY);
						}catch(Throwable e){
							LogUtil.info("tryIntWrite", "exception", e);
							fr.setReady(FileResult.EXCEPTION);
						}
					}
				});
				return fr;
			}*/
			
			public int writeListInt(List<Integer> ls)
			{
				int i=0;
				try{
					initWriteBuffer(ls.size()*4);
					int wt = Math.min((int)(num/4),ls.size());
					for(;i<wt;i++)
						mbwrite.putInt(ls.get(i));
				}catch(Exception e){
					LogUtil.info("[WriteAdapter]", "[writeListInt]", e.getMessage());
				}
				return i;
			}
		};
	}
	
	/*public boolean exists()
	{
		return fl.exists();
	}
	
	public boolean isFile()
	{
		return fl.isFile();
	}
	
	public String getPath()
	{
		return fl.getPath();
	}
	
	
	public boolean delete()
	{
		close();
		boolean b = false;
		try{
			b = fl.delete();
		}catch(Exception e){
			System.out.println(e);
		}
		return b;
	}*/
	
	public int copyTo(String toFilePath){
		return copyTo(toFilePath, FileAdapter.m(8));
	}
	
	public int copyTo(String toFilePath, long every){
		int c=0;
		FileAdapter fa = new FileAdapter(toFilePath);
		fa.createFile();
		byte[] bts = null;
		long begin=0;
		while((bts=this.getReader(begin, every).readAll())!=null){
			c+=fa.getWriter().write(bts);
			begin+=bts.length;
		}
		fa.close();
		return c;
	}
	
	public Result<Integer> tryCopyTo(String toFilePath){
		return tryCopyTo(toFilePath, FileAdapter.m(8));
	}
	
	public Result<Integer> tryCopyTo(final String toFilePath, final long every)
	{
		final FileResult<Integer> fr = new FileResult<Integer>(false);
		PoolExector.tpe().execute(new Runnable(){
			public void run(){
				try{
					int bl = copyTo(toFilePath, every);
					fr.setResult(new Integer(bl));
					fr.setReady(FileResult.READY);
				}catch(Throwable e){
					LogUtil.info("tryCopyFile", "exception", e);
					fr.setReady(FileResult.EXCEPTION);
				}
			}
		});
		return fr;
	}
	
	public final void closeBuffer(final Buffer buffer){
		if(null==buffer)
			return;
		AccessController.doPrivileged(new PrivilegedAction<Object>(){
			public Object run() {
				try {
					Method cleanerMethod = buffer.getClass().getMethod("cleaner");
					if (null==cleanerMethod)
						return null;
					cleanerMethod.setAccessible(true);
					Object cleanerObj = cleanerMethod.invoke(buffer);
					if(null==cleanerObj)
						return null;
					Method cleanMethod = cleanerObj.getClass().getMethod("clean");
					if (null==cleanMethod)
						return null;
					cleanMethod.invoke(cleanerObj);
				}catch(Throwable e){}
				return null;
			}
		});
	}
	
	public void closeExit(){
		PoolExector.close();
		close();
	}
	
	public void close()
	{
		try{
			if(fc!=null)fc.close();
			if(raf!=null)raf.close();
			closeBuffer(mbread);
			closeBuffer(mbwrite);
		}catch(Exception e){
			LogUtil.info("[FileAdapter]", "[close]", e.getMessage());
		}
	}
	
	public static void main(String[] args)
	{
	}
}