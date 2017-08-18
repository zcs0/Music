package com.music.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @ClassName:     TimerTasks.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月27日 下午1:34:15 
 * @Description:   封装定时器
 */
public class TimerTasks {
	private TaskSchedule task;
	private Timer timer = new Timer();
	public TimerTasks(TaskSchedule task){
		this.task = task;
	}
	/**
	 * 在period后执行，只执行一次
	 * @param period 在此时间后执行
	 */
	public void start(long period){
		timer.schedule(new TimerTaskList(), period);
	}
	public void start(long delay, long period){
		timer.purge();
		timer.schedule(new TimerTaskList(),delay, period);
	}
	public void stop(){
		timer.cancel();
		timer.purge();
	}
	/**
	 * 在period后执行，只执行一次
	 * @param period 在此时间后执行
	 * @param task
	 */
	public TimerTasks(long period,TaskSchedule task){
		this.task = task;
		timer.purge();
		timer.schedule(new TimerTaskList(), period);
	}
	/**
	 * 在period后执行，一直执行
	 * @param delay
	 * @param period 从每一次开始，每次执行的间隔时间
	 * @param task
	 */
	public TimerTasks(long delay, long period,TaskSchedule task){
		this.task = task;
		timer.purge();
		timer.schedule(new TimerTaskList(),delay, period);
	}
	public interface TaskSchedule{
		public void schedule();
	}
	private class TimerTaskList extends TimerTask{

		@Override
		public void run() {
			if(task!=null){
				task.schedule();
			}
		}
		
	}
}
