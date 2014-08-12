import com.fourinone.MigrantWorker;
import com.fourinone.WareHouse;

public class WorkerLocalDemo extends MigrantWorker
{
	public String name;
	
	public WorkerLocalDemo(String name){
		this.name = name;
	}
	
	public WareHouse doTask(WareHouse inhouse)
	{
		System.out.println(name+":"+inhouse);//输出本工人名字和获取到的任务
		inhouse.put("task",inhouse.get("task")+" done.");//完成任务返回
		return inhouse;
	}
}