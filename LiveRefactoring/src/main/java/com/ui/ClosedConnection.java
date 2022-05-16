package com.ui;

import org.cef.callback.CefCallback;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class ClosedConnection implements ResourceHandlerState {
    public ClosedConnection(){
    }
    @Override
    public void getResponseHeaders(CefResponse cefResponse, IntRef intRef, StringRef stringRef) {
        cefResponse.setStatus(404);
    }

    @Override
    public boolean readResponse(byte[] bytes, int i, IntRef intRef, CefCallback cefCallback) {
        return false;
    }

    @Override
    public void close() {

    }
}
