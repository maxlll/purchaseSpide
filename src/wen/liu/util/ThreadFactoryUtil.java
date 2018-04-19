package wen.liu.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryUtil {
	/**
	 * 创建线程池工厂类
	 * 由该方法返回的线程池工厂类生产的线程，其名称前缀由该方法的参数决定
	 * @param namePrefix	线程名前缀
	 * @return
	 */
	public static ThreadFactory newFactory(String namePrefix){
		return new GenNameThreadFactory(namePrefix);
	}
	
	static class GenNameThreadFactory implements ThreadFactory{
		private  String namePrefix;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		
		public GenNameThreadFactory(String namePrefix){
			SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                                  Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
		}
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
			  t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
			  t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}
}
