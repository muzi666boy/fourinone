import com.fourinone.BeanContext;

public class RunServer
{
	public static void main(String[] args){
		//运行方式（3个参数为ip、端口、工人数量）：java  -cp fourinone.jar; RunServer localhost 2014 8
		BeanContext.startCoolHashServer(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]),args.length==4?args[3]:null);
	}
}