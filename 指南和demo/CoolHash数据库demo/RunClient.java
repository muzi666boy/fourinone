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

public class RunClient
{	
	public static void singleDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			chc.put("name","zhang");//写入字符
			chc.put("age",20);//写入整数
			chc.put("weight",50.55f);//写入浮点数
			chc.put("price",100.5588d);//写入double数
			chc.put("user.001.id",10000000l);//写入长整数
			chc.put("user.001.birthday",new Date());//写入日期对象
			chc.put("user.001.pet",new ArrayList());//写入集合对象
			
			System.out.println((String)chc.get("name"));//读取字符
			System.out.println((int)chc.get("age"));//读取整数
			System.out.println((float)chc.get("weight"));//读取浮点数
			System.out.println((double)chc.get("price"));//读取double数
			System.out.println((long)chc.get("user.001.id"));//读取长整数
			System.out.println((Date)chc.get("user.001.birthday"));//读取日期对象
			System.out.println((ArrayList)chc.get("user.001.pet"));//读取集合对象
			System.out.println("time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void batchDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			CoolHashMap hm=new CoolHashMap();
			for(long i=0;i<1000000;i++){
				hm.put(i+"",i+"");//载入1百万条k/v数据到CoolHashMap缓存
			}
			System.out.println("load time taken in MS--"+(System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			int n=chc.put(hm);//批量写入1百万条k/v数据
			System.out.println("putBatch time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("putBatch total:"+n);
			
			start = System.currentTimeMillis();
			CoolHashMap chm = chc.get(hm.getKeys());//批量读取1百万条k/v数据
			System.out.println("getBatch time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("getBatch total:"+chm.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void findDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			CoolHashMap hm=new CoolHashMap();//CoolHashMap默认最大容量为1百万条数据，可根据内存大小调整HASHCAPACITY配置项
			for(long i=0;i<100000;i++){
				hm.put("user."+i+".name",i+"name");
				hm.put("user."+i+".age",i);
			}
			System.out.println("load time taken in MS--"+(System.currentTimeMillis()-start));
			start = System.currentTimeMillis();
			int n=chc.put(hm);//批量写入需要查询的数据
			System.out.println("put time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("put total:"+n);
			
			start = System.currentTimeMillis();
			CoolHashResult hr100 = chc.find("user.100.*");//查询用户100的所有属性
			CoolHashMap user100 = hr100.nextBatch(50);//指定数量分页获取，可多次调用直到全部读出
			System.out.println("find:"+user100);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			//ValueFilter包括对字符类型、数字类型、日期类型等的常用过滤操作，如字符串"开始,结束,包括,被包括"，数字日期的"最大,最小,区间"等
			CoolHashResult hr = chc.find("user.*.name", ValueFilter.contains("8888"), true);//查询名字包含88888的所有用户（like%88888%）
			CoolHashMap chmb = hr.nextBatch(50);
			System.out.println("find:"+chmb);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
			
			start = System.currentTimeMillis();
			CoolKeyResult kr = chc.findKey("user.*.age", ValueFilter.less(20l), true);//查询年龄小于20岁的用户key
			CoolKeySet ks = kr.nextBatchKey(50);
			System.out.println("findKey:"+ks);
			ks = kr.nextBatchKey(50);
			System.out.println("findKey:"+ks);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void pointDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			chc.put("aaa","aaa");
			chc.putPoint("bbb","aaa");
			chc.putPoint("ccc","bbb");
			System.out.println(chc.getPoint("ccc"));//指向aaa

			chc.put("order.0.price", 158.68d);
			chc.putPoint("order.0.buyer", "user.0");//指向模糊key
			chc.put("user.0.name", "zhangsan");
			chc.putPoint("user.0.order.0", "order.0");
			System.out.println(chc.get("order.0.buyer"));//获取指向key
			System.out.println(chc.getPoint("order.0.buyer",".name"));//补充name属性，指向user.0.name
			System.out.println(chc.getPoint("order.0.buyer",".order.0",".price"));//指向user.0.order.0再指向order.0.price
			
			System.out.println("time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void findPointDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			CoolHashMap hm=new CoolHashMap();
			for(long i=0;i<100000;i++){
				hm.put("order."+i+".price", i);
				hm.putPoint("order."+i+".buyer", "user."+i);//order通过key指针关联user
				hm.put("user."+i+".name",i+"name");
				hm.putPoint("user."+i+".order."+i, "order."+i);//user通过key指针关联order
			}
			System.out.println("load time taken in MS--"+(System.currentTimeMillis()-start));
			start = System.currentTimeMillis();
			int n=chc.put(hm);//批量写入测试数据
			System.out.println("put time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("put total:"+n);
			
			start = System.currentTimeMillis();
			//查询order价格少于10的所有user
			CoolKeyResult kr = chc.findKey("user.*.order.*", ValueFilter.less(10l), true, ".price");
			CoolKeySet ks = kr.nextBatchKey(50);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("findKey:"+ks);
			System.out.println("findKey total:"+ks.size());
			
			start = System.currentTimeMillis();
			//查询buyer名字以8888name结束的所有order
			CoolHashResult hr = chc.find("order.*.buyer", ValueFilter.endsWith("8888name"), true, ".name");
			CoolHashMap chmb = hr.nextBatch(50);
			System.out.println("find time taken in MS--"+(System.currentTimeMillis()-start));
			System.out.println("find:"+chmb);
			System.out.println("find total:"+chmb.size());
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void andOrDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			long start = System.currentTimeMillis();
			CoolHashMap hm=new CoolHashMap();
			for(int i=0;i<100;i++){
				hm.put("person."+i+".name",i+"name");
				hm.put("person."+i+".age",i);
			}
			int n=chc.put(hm);//写入100条测试数据
			
			//查询名字以88name结束的person
			CoolKeyResult kn = chc.findKey("person.*", ValueFilter.endsWith("88name"), true);
			CoolKeySet ksn = kn.nextBatchKey(50);
			System.out.println("findKey:"+ksn);
			System.out.println("findKey total:"+ksn.size());
			
			//查询年龄大于60的person
			CoolKeyResult ka = chc.findKey("person.*", ValueFilter.greater(60), true);
			CoolKeySet ksa = ka.nextBatchKey(50);
			System.out.println("findKey:"+ksa);
			System.out.println("findKey total:"+ksa.size());
			
			//getSuperKeys(1)表示获取到第二级的父key
			System.out.println("and:"+ksn.getSuperKeys(1).and(ksa.getSuperKeys(1)));//取并集
			System.out.println("or:"+ksn.getSuperKeys(1).or(ksa.getSuperKeys(1)));//取或集
			System.out.println("except:"+ksa.getSuperKeys(1).except(ksn.getSuperKeys(1)));//取差集
			System.out.println("time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void sortDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			CoolHashMap hm=new CoolHashMap();
			for(int i=0;i<100;i++){
				hm.put(i+"", i);
			}
			int n=chc.put(hm);//批量写入100条测试数据
			hm = chc.get(hm.getKeys());//批量读出100条测试数据
			System.out.println(hm);//按写入顺序输出
			
			long start = System.currentTimeMillis();
			//默认按key字母排序
			Map.Entry[] enarr = hm.sort();
			System.out.println(Arrays.toString(enarr));
			
			//自定义按key字母倒排
			CoolKeySet<String> ks = hm.getKeys();
			String[] ksarr=ks.sort(new Comparator<String>(){
				public int compare(String o1, String o2){
					return o2.compareTo(o1);
				}
			});
			System.out.println(Arrays.toString(ksarr));
			
			//自定义按value数字大小排序
			Map.Entry[] arr=hm.sort(new Comparator<Map.Entry>(){
				public int compare(Map.Entry o1, Map.Entry o2){
					return (int)o1.getValue()-(int)o2.getValue();
				}
			});
			System.out.println(Arrays.toString(arr));
			System.out.println("sort time taken in MS--"+(System.currentTimeMillis()-start));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		chc.exit();
	}
	
	public static void transDemo(String[] args){
		CoolHashClient chc = BeanContext.getCoolHashClient(args[0],Integer.parseInt(args[1]));
		try{
			chc.put("a","100");//a初始值
			chc.put("b","100");//b初始值
			chc.begin();//开始事务操作
			chc.put("a","80");
			chc.remove("b");
			System.out.println("a:"+chc.get("a"));//回滚前输出a，已经改变
			System.out.println("b:"+chc.get("b"));//回滚前输出b，已经改变
			chc.put("b",new RunClient());//一个错误操作触发回滚
			chc.commit();//提交事务
		}catch(Exception ex){
			System.out.println(ex);
			chc.rollback();//回滚事务
		}
		
		try{
			System.out.println("a:"+chc.get("a"));//回滚后输出a，已经还原
			System.out.println("b:"+chc.get("b"));//回滚后输出b，已经还原
		}catch(Exception ex){
			System.out.println(ex);
		}
		chc.exit();
	}
	
	public static void main(String[] args){
		//运行方式（2个参数为需要连接的CoolHashServer的ip和端口）: java  -cp fourinone.jar; RunClient localhost 2014
		singleDemo(args);//单条读写demo
		//batchDemo(args);//批量读写demo
		//findDemo(args);//查询数据demo
		//pointDemo(args);//key指针demo
		//findPointDemo(args);//key指针查询demo
		//andOrDemo(args);//and、or、except操作demo
		//sortDemo(args);//排序demo
		//transDemo(args);//事务demo
	}
}