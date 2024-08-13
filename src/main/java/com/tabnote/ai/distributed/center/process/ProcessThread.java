package com.tabnote.ai.distributed.center.process;

import com.tabnote.ai.distributed.center.QueueRequest;
import com.tabnote.ai.distributed.center.request.RequestRunnable;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.*;

public class ProcessThread extends Thread {
    private Queue<QueueRequest> queue = new ConcurrentLinkedQueue<>();

    public ProcessThread(Queue<QueueRequest> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //创建serverSocket对象，同时为服务器注册端口
                ServerSocket serverSocket = new ServerSocket(11714);
                //创建线程池负责通信管道的任务
                ThreadPoolExecutor pool = new ThreadPoolExecutor(10, //核心线程数
                        1000,//最大线程数
                        0, //最大空闲时间
                        TimeUnit.SECONDS,//时间单位
                        new ArrayBlockingQueue<>(8), //阻塞队列
                        Executors.defaultThreadFactory(), //线程工程
                        new ThreadPoolExecutor.AbortPolicy());//拒绝策略

                while (true) {
                    try {
                        //使用serverSocket对象调用accept方法等待客户端连接
                        Socket socket = serverSocket.accept();
                        System.out.println("[Process Request from: " + socket.getRemoteSocketAddress() + "]");

                        //把通信管道交给独立的线程处理
                        pool.execute(new ProcessRunnable(socket,queue));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}