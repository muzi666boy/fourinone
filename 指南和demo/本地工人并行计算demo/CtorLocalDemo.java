import com.fourinone.Contractor;
import com.fourinone.WareHouse;
import com.fourinone.WorkerLocal;
import java.util.ArrayList;

public class CtorLocalDemo extends Contractor
{
	public WareHouse giveTask(WareHouse inhouse)
	{
		WorkerLocal[] wks = getLocalWorkers(3);//创建3个本地工人线程
		for(int j=0;j<wks.length;j++)
			wks[j].setWorker(new WorkerLocalDemo("worker"+j));//设置本地工人实现类
		
		WareHouse[] tasks = new WareHouse[20];//创建20个任务
		for(int i=0;i<tasks.length;i++){
			tasks[i] = new WareHouse("task",i+"");
		}
		
		WareHouse[] result = doTaskCompete(wks, tasks);//分配20个任务给3个本地工人并行计算完成
		System.out.println("result:");
		for(WareHouse r:result)
			System.out.println(r);
		
		return inhouse;
	}
	
	public static void main(String[] args)
	{
		CtorLocalDemo cd = new CtorLocalDemo();
		cd.giveTask(null);
		cd.exit();
	}
}