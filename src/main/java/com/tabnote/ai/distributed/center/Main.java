package com.tabnote.ai.distributed.center;

import com.sun.management.OperatingSystemMXBean;
import com.tabnote.ai.distributed.center.process.ProcessThread;
import com.tabnote.ai.distributed.center.request.RequestThread;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Queue;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        //创建一个线程安全队列，我们要维护这个队列
        Queue<QueueRequest> queue = new ConcurrentLinkedQueue<>();

        Thread r = new RequestThread(queue);
        Thread p = new ProcessThread(queue);
        r.start();
        p.start();
        System.out.println("启动成功");
    }
}