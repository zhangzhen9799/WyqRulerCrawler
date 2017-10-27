package com.fh.netpf.crawler.thread;

import com.fh.netpf.crawler.conf.SystemConfigure;
import com.fh.netpf.crawler.main.AppContext;
import com.fh.netpf.crawler.task.Task;
import com.fh.netpf.crawler.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class WorkThread implements Runnable {

    @Override
    public void run() {

        int sleepTimes = Integer.parseInt(SystemConfigure.getInstance().getProperty("sleepTimes"));

        while (true) {

            if (AppContext.blockingQueueFile.size() != 0) {

                try {
                    Task task = AppContext.blockingQueueFile.take();
                    List<String> list = HttpUtils.doPost(task);
                    if (list.size() > 0) {
                        int statusCode = HttpUtils.postAddJob(list);
                        log.info("StatusCode:{}", statusCode);
                        log.info("任务提交成功！！！");
                    }
                } catch (InterruptedException e) {
                    log.error("{}", e);
                }

            }
            try {
                Thread.sleep(sleepTimes);
            } catch (InterruptedException e) {
                log.error("{}", e);
            }

        }

    }

}
