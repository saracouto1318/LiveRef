package com.ui;

import org.cef.callback.CefCallback;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class OpenedConnection implements ResourceHandlerState {
    public InputStream inputStream;
    public URLConnection connection;
    public OpenedConnection(URLConnection urlConnection) {
        this.connection = urlConnection;
        try {
            this.inputStream = this.connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getResponseHeaders(CefResponse cefResponse, IntRef intRef, StringRef stringRef) {
        String url = this.connection.getURL().toString();
        if(url.contains("css"))
            cefResponse.setMimeType("text/css");
        else if(url.contains("js"))
            cefResponse.setMimeType("text/javascript");
        else if(url.contains("html"))
            cefResponse.setMimeType("text/html");
        else
            cefResponse.setMimeType(connection.getContentType());
        try {
            intRef.set(inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cefResponse.setStatus(200);
    }

    @Override
    public boolean readResponse(byte[] bytes, int i, IntRef intRef, CefCallback cefCallback) {
        try {
            int availableSize = inputStream.available();
            if (availableSize > 0) {
                int maxBytesToRead = Math.min(availableSize, i);
                int realNumberOfReadBytes = inputStream.read(bytes, 0, maxBytesToRead);
                intRef.set(realNumberOfReadBytes);
                return true;
            } else {
                inputStream.close();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
