package com.fh.netpf.crawler.main;

import com.fh.netpf.crawler.task.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 线程上下文
 */
public class AppContext {

    /**
     * 任务队列
     */
    public static BlockingQueue<Task> blockingQueueFile = new LinkedBlockingQueue<Task>(100);

}
