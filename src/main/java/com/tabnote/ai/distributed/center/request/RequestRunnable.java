package com.tabnote.ai.distributed.center.request;

import com.tabnote.ai.distributed.center.QueueRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class RequestRunnable implements Runnable {
    private Socket socket;
    private Queue<QueueRequest> queue;

    public RequestRunnable(Socket socket,Queue<QueueRequest> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        OutputStream os = null;
        try{
            bis = new BufferedInputStream(this.socket.getInputStream());
            byte[] bytes = bis.readAllBytes();
            String s = new String(bytes, StandardCharsets.UTF_8);
            os = new BufferedOutputStream(this.socket.getOutputStream());
            QueueRequest queueRequest = new QueueRequest(socket,bis,os);
            queueRequest.setRequestContent(s);
            System.out.println("请求内容："+s);
            queue.add(queueRequest);
            System.out.println("加入队列,队列大小："+queue.size());
        } catch (SocketException e) {
            System.out.println("reset");
        } catch (Exception e){
            e.printStackTrace();
            try {
                os.write("failed".getBytes());
                socket.close();
                if (bis != null) {
                    bis.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
