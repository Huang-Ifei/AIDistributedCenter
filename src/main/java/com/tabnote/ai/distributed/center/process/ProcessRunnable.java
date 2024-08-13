package com.tabnote.ai.distributed.center.process;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.ai.distributed.center.QueueRequest;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

public class ProcessRunnable implements Runnable {
    private Socket socket;
    private Queue<QueueRequest> queue;

    public ProcessRunnable(Socket socket, Queue<QueueRequest> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {
        OutputStream bos = null;
        BufferedReader br = null;
        QueueRequest queueRequest = null;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            bos = socket.getOutputStream();
            String line1 = br.readLine();
            JSONObject jsonObject = JSONObject.parseObject(line1);
            String computerName = jsonObject.getString("computerName");
            String memory = jsonObject.getString("memory");
            String memoryUsage = jsonObject.getString("memoryUsage");
            String ip = socket.getRemoteSocketAddress().toString();
            System.out.println("电脑/地址：" + computerName + ip + "，内存:" + memory + "，内存使用率:" + memoryUsage);
            int count = 0;
            while (queue.isEmpty()) {
                bos.write("队列为空\n".getBytes(StandardCharsets.UTF_8));
                Thread.sleep(3000);
                count++;
            }
            queueRequest = queue.poll();
            if (queueRequest != null) {
                while (!queueRequest.isValid()) {
                    queueRequest = queue.poll();
                    if (queueRequest == null) {
                        bos.write("队列为空\n".getBytes(StandardCharsets.UTF_8));
                        bos.flush();
                        socket.close();
                        br.close();
                        bos.close();
                        return;
                    }
                }
                JSONObject jsonInfo = new JSONObject();
                jsonInfo.put("computerName", computerName);
                jsonInfo.put("memory", memory);
                jsonInfo.put("memoryUsage", memoryUsage);
                jsonInfo.put("ip", ip);
                queueRequest.write(jsonInfo + "\n");
                bos.write("请求处理\n".getBytes(StandardCharsets.UTF_8));
                bos.flush();
                bos.write(queueRequest.getRequestContent().getBytes(StandardCharsets.UTF_8));
                bos.flush();
                socket.shutdownOutput();
                //向客户端回报
                String temp;
                while (null != (temp = br.readLine())) {
                    JSONObject json = JSONObject.parseObject(temp);
                    System.out.print(json.getString("response"));
                    JSONObject object = new JSONObject();
                    object.put("response", json.getString("response"));
                    queueRequest.write(object + "\n");
                }
                br.close();
                bos.close();
                queueRequest.closeAll();
            } else {
                bos.write("队列为空\n".getBytes(StandardCharsets.UTF_8));
                bos.flush();
            }

        } catch (SocketException e) {
            System.out.println("停止连接");
            if (queueRequest != null) {
                System.out.println("算力贡献中断，等待重新分配");
                queue.offer(queueRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (queueRequest != null) {
                queue.offer(queueRequest);
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (bos != null) {
                    bos.close();
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
