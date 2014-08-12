import com.fourinone.BeanContext;
import com.fourinone.StartResult;
import java.io.File;

public class CoolHashTestRun{
	//一次启动5个客户端
	//java -cp fourinone.jar; CoolHashTestRun localhost 2014 5
    public static void main(String[] args){
        long start = System.currentTimeMillis();
    	final StartResult[] starts = new StartResult[Integer.parseInt(args[2])];
    	for(int i=0;i<starts.length;i++){
			starts[i]=BeanContext.tryStart("java","-cp","fourinone.jar"+File.pathSeparator,"CoolHashTest",args[0],args[1],i+"");
			starts[i].print("log/client"+i+".log");
		}
        System.out.println(System.currentTimeMillis()-start);
    }
}