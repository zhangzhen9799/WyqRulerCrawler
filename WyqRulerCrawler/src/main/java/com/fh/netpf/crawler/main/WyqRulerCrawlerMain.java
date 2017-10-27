package com.fh.netpf.crawler.main;

import com.fh.netpf.crawler.conf.SystemConfigure;
import com.fh.netpf.crawler.job.GetMailInfoJob;
import com.fh.netpf.crawler.thread.WorkThread;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class WyqRulerCrawlerMain {

    public static void main(String[] args) {

        // 加载配置文件
        init();

        // 接收邮件
        receiveMail();

        // 执行任务
        work();

    }

    /**
     * 加载配置文件
     */
    private static void init() {

        SystemConfigure.getInstance();

    }

    /**
     * 接收邮件
     */
    private static void receiveMail() {

        int withIntervalInHours = Integer.parseInt(SystemConfigure.getInstance().getProperty("withIntervalInHours"));

        //通过schedulerFactory获取一个调度器
        SchedulerFactory schedulerfactory = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            // 通过schedulerFactory获取一个调度器
            scheduler = schedulerfactory.getScheduler();

            // 创建jobDetail实例，绑定Job实现类
            // 指明job的名称，所在组的名称，以及绑定job类
            JobDetail job = JobBuilder
                    .newJob(GetMailInfoJob.class)
                    .withIdentity("JobName", "JobGroupName")
                    .build();

            // 定义调度触发规则
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("SimpleTrigger", "SimpleTriggerGroup")
                    .withSchedule(SimpleScheduleBuilder
                            .simpleSchedule()
                            .withIntervalInSeconds(60)
//                            .withIntervalInHours(1)
                            .repeatForever())
                    .startNow()
                    .build();

            // 把作业和触发器注册到任务调度中
            scheduler.scheduleJob(job, trigger);

            // 启动调度
            scheduler.start();
        } catch(SchedulerException e){
            log.error("{}", e);
        }
    }

    /**
     * 执行任务
     */
    private static void work() {

        int threadPoolSize = Integer.parseInt(SystemConfigure.getInstance().getProperty("threadPoolSize"));
        ExecutorService workThreadPool = Executors.newFixedThreadPool(threadPoolSize);

        for (int i = 0; i < threadPoolSize; i ++) {

            log.info("Thread:{}", i + 1);
            workThreadPool.execute(new WorkThread());

        }

        workThreadPool.shutdown();

    }

}
