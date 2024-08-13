package com.tabnote.ai.distributed.center;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class QueueRequest {
    private long startTime;
    private String requestContent;
    private Socket socket;
    private BufferedInputStream bis;
    private OutputStream os;

    public QueueRequest(Socket socket, BufferedInputStream bis, OutputStream os) {
        startTime = System.currentTimeMillis();
        this.socket = socket;
        this.bis = bis;
        this.os = os;
    }

    public QueueRequest() {
        startTime = System.currentTimeMillis();
    }

    public String getRequestContent() {
        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    //默认定义从超时等待时间为180秒，如果超过120秒就没有必要再处理了
    public boolean isValid() throws IOException {
        if ((System.currentTimeMillis() - startTime)<120000){
            return true;
        }else {
            write("wait time too long\n");
            closeAll();
            return false;
        }
    }

    public void closeAll() throws IOException {
        socket.close();
        bis.close();
        os.close();
    }

    public void write(String s) throws IOException {
        os.write(s.getBytes());
        os.flush();
    }
}
