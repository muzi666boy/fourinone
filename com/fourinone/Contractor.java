package com.fourinone;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Array;
import java.util.Random;

public abstract class Contractor extends ContractorParallel
{
	private Contractor ctor;
	WorkerLocal[] wks = null; 
	
	public Contractor toNext(Contractor ctor)
	{
		this.ctor = ctor;
		return ctor;
	}
	
	public final WareHouse giveTask(WareHouse inhouse, boolean chainProcess)
	{
		WareHouse outhouse = giveTask(inhouse);
		if(chainProcess&&ctor!=null)
			return ctor.giveTask(outhouse, chainProcess);
		return outhouse;
	}
	
	final <T> T[] getLocals(String localType, Class<T> local){
		T[] locals = null;
		WorkerLocal[] wkls = getWaitingWorkers(localType);
		if(wkls!=null){
			locals = (T[])Array.newInstance(local, wkls.length);
			for(int i=0;i<wkls.length;i++){
				locals[i]=(T)wkls[i];
			}
		}
		return locals;
	}
	/*
	final WorkerLocal[] getWorkerLocals(Class[] locals){
		WorkerLocal[] wkls = null;
		if(locals!=null){
			wkls = new WorkerLocal[locals.length];
			for(int i=0;i<locals.length;i++){
				wkls[i]=(WorkerLocal)locals[i];
			}
		}
		return wkls;
	}*/
	
	public void doProject(WareHouse inhouse)
	{
		/*WareHouse outhouse = giveTask(inhouse);
		if(ctor!=null)
			ctor.doProject(outhouse);*/
		giveTask(inhouse, true);
	}
	
	WorkerLocal[] getWaitingWorkersFromService(String workerType)
	{
		return getWaitingWorkersFromService(workerType,null);
	}
	
	WorkerLocal[] getWaitingWorkersFromService(String workerType, MigrantWorker mw)
	{
		//get host:port from ParkLocal and get WorkerService from host:port
		LogUtil.fine("", "", "getWaitingWorkersFromService:"+workerType+",MigrantWorker:"+mw);
		/*List<ObjectBean> oblist = ParkPatternExector.getWorkerTypeList(workerType);
		List<WorkerLocal> wklist = new ArrayList<WorkerLocal>();
		for(ObjectBean ob:oblist)
		{
			String[] hostport = ((String)ob.toObject()).split(":");
			wklist.add(BeanContext.getWorkerLocal(hostport[0], Integer.parseInt(hostport[1]), workerType));
		}
		return wklist.toArray(new WorkerLocal[wklist.size()]);*/
		//if(wks==null){
			List<String[]> wslist = getWorkersService(workerType);
			List<WorkerLocal> wklist = new ArrayList<WorkerLocal>();
			for(String[] wsinfo:wslist)
				wklist.add(BeanContext.getWorkerLocal(wsinfo[0], Integer.parseInt(wsinfo[1]), wsinfo[2]));
				
			/*if(mw!=null){
				BeanContext.startInetServer();
				for(WorkerLocal wl:wklist)
					((WorkerProxy)wl).setWorker(mw);
			}*/
			wks=wklist.toArray(new WorkerLocal[wklist.size()]);
		//}
		return wks;
	}
	
	WorkerLocal[] getWaitingWorkersFromPark(String workerType)
	{
		LogUtil.fine("", "", "getWaitingWorkersFromPark:"+workerType);
		//if(wks==null){
			List<ObjectBean> oblist = ParkPatternExector.getWorkerTypeList(workerType);
			//System.out.println("getWaitingWorkersFromPark oblist:"+oblist);
			List<WorkerLocal> wklist = new ArrayList<WorkerLocal>();
			for(ObjectBean ob:oblist)
				wklist.add(BeanContext.getWorkerLocal(ob.getName()));
			wks=wklist.toArray(new WorkerLocal[wklist.size()]);
		//}
		return wks;
	}
	
	WorkerLocal[] getWaitingWorkers(String parkhost, int parkport, String workerType){
		List<String[]> wslist =  getWorkersServicePark(parkhost, parkport, workerType);
		List<WorkerLocal> wklist = new ArrayList<WorkerLocal>();
		for(String[] wsinfo:wslist)
			wklist.add(BeanContext.getWorkerLocal(wsinfo[0], Integer.parseInt(wsinfo[1]), wsinfo[2]));
		wks=wklist.toArray(new WorkerLocal[wklist.size()]);
		return wks;
	}
	
	protected WorkerLocal[] getLocalWorkers(int num)
	{
		WorkerLocal[] wkls = new WorkerLocal[num];
		for(int j=0;j<wkls.length;j++)
			wkls[j]=BeanContext.getWorkerLocal();
		return wkls;
	}
	
	protected final WareHouse[] doTaskBatch(WareHouse wh){
		return doTaskBatch(wks, wh);
	}
	
	protected final WareHouse[] doTaskBatch(WorkerLocal[] wks, WareHouse wh){
		WareHouse[] hmarr = new WareHouse[wks.length];
		for(int i=0,j=0;j<hmarr.length;){
			if(hmarr[i]==null){
				hmarr[i] = wks[i].doTask(wh);
			}
			else if(hmarr[i].isReady()&&hmarr[i].getMark()){
				hmarr[i].setMark(false);
				j++;
			}
			i=i+1==hmarr.length?0:i+1;
		}
		return hmarr;
	}
	
	/*protected final WareHouse[] doTaskCompete(WorkerLocal[] wks, WareHouse[] tasks){
		WareHouse[] tasks_result = new WareHouse[tasks.length];
		WareHouse[] wks_result = new WareHouse[wks.length];
		int[] itask = new int[wks.length];
		
		for(int i=0,task=0,done=0;done<tasks_result.length;){
			if(wks_result[i]==null){
				if(task<tasks.length){
					int t=tasks[task].get("timeout")!=null?tasks[task].getStringInt("timeout"):0;
					wks_result[i] = wks[i].doTask(tasks[task],t);
					itask[i]=task++;
				}
			}
			else if(wks_result[i].getStatus()!=WareHouse.NOTREADY){
				tasks_result[itask[i]]=wks_result[i];
				wks_result[i]=null;
				done++;
			}
			i=i+1==wks_result.length?0:i+1;
		}
		
		return tasks_result;
	}*/
	
	//synchronized
	protected final WareHouse[] doTaskCompete(WorkerLocal[] wks, WareHouse[] tasks){
//2013.8.29
		/*WorkerLocal[] wks = null; 
		if(tasks.length<wkls.length){
			wks = new WorkerLocal[tasks.length];
			System.arraycopy(wkls, 0, wks, 0, tasks.length);
		}else wks = wkls;*/
		if(tasks.length<wks.length){
			WorkerLocal[] wkls = new WorkerLocal[tasks.length];
			//System.arraycopy(wks, 0, wkls, 0, tasks.length);
			System.arraycopy(wks, new Random().nextInt(wks.length-tasks.length+1), wkls, 0, tasks.length);
			wks = wkls;
		}
		//System.out.println("wks.length:"+wks.length);
		
		WareHouse[] tasks_result = new WareHouse[tasks.length];
		WareHouse[] wks_result = new WareHouse[wks.length];
		int[] itask = new int[wks.length];
		boolean allfault = false;
		
		for(int i=0,done=0;done<tasks_result.length;){
			if(wks_result[i]==null){
				for(int job=0;job<tasks_result.length;job++){
					if(tasks_result[job]==null||(!allfault&&tasks_result[job].getStatus()==WareHouse.EXCEPTION)){
						int t=tasks[job].get("-timeout")!=null?tasks[job].getStringInt("-timeout"):0;
						wks_result[i] = wks[i].doTask(tasks[job],t);
						tasks_result[job]=wks_result[i];
						itask[i]=job;
						break;
					}
				}
			}
			else if(wks_result[i].getStatus()!=WareHouse.NOTREADY){
				if(wks_result[i].getStatus()==WareHouse.EXCEPTION){
					int n=0;
					for(;n<wks_result.length;n++)
						if(wks_result[n]==null||wks_result[n].getStatus()!=WareHouse.EXCEPTION)
							break;
					if(n==wks_result.length)
						allfault = true;
				}
				
				if(wks_result[i].getStatus()==WareHouse.READY||allfault){
					//if interrupted then stop other workers and clear worker result and cancel remaining task
					if(wks_result[i].getStringBool("-interrupted")){
						for(int k=0;k<wks.length;k++){
							if(k!=i)
								wks[k].interrupt();
							wks_result[k]=null;
						}
						break;
					}
					wks_result[i]=null;
					done++;
				}
			}
			i=i+1==wks_result.length?0:i+1;
		}
		
		return tasks_result;
	}
}