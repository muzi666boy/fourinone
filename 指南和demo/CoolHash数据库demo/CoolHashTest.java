import com.fourinone.BeanContext;
import com.fourinone.CoolHashClient;
import com.fourinone.CoolHashMap;
import com.fourinone.CoolHashMap.CoolKeySet;
import com.fourinone.CoolKeyResult;
import com.fourinone.CoolHashResult;
import com.fourinone.Filter.ValueFilter;
import com.fourinone.CoolHashException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Comparator;
import java.util.Arrays;

public class CoolHashTest
{
	public static void singleTest(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			long m=1000;
			long j=Integer.parseInt(args[2])*m;
			for(long i=j;i<j+m;i++)
				chc.put(i+"",i+"");
			System.out.println("put time taken in MS--"+(System.currentTimeMillis()-start));
						
			start = System.currentTimeMillis();
			int n=0;
			for(long i=j;i<j+m;i++){
				if(chc.get(i+"")!=null)
					n++;
			}
			System.out.println("get time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("get total:"+n);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();		
	}
		
	public static void batchTest(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		long m=100000;//每次批量写入十万条并读出
		long j=Integer.parseInt(args[2])*m;
		try{
			long start = System.currentTimeMillis();
			CoolHashMap hm=new CoolHashMap();
			for(long i=j;i<j+m;i++){
				hm.put(i+"",i+"");
			}
			System.out.println("load time taken in MS--"+(System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			int n=chc.put(hm);
			System.out.println("putBatch time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("putBatch total:"+n);
			
			start = System.currentTimeMillis();
			CoolHashMap chm = chc.get(hm.getKeys());
			System.out.println("getBatch time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("getBatch total:"+chm.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();	
	}
	
	public static void findTest(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			CoolHashResult hr = chc.find("*", ValueFilter.contains("888888"), true);//模糊查询
			CoolHashMap chmb = hr.nextBatch(50);//分页获取前50条
			System.out.println("find:"+chmb);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void main(String[] args){
		batchTest(args);//批量测试
		//findTest(args);//查询测试
		//singleTest(args);//单条测试
	}
}