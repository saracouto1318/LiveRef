package com.ui.metrics;

import org.cef.callback.CefCallback;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefResponse;

public interface ResourceHandlerState {
    void getResponseHeaders(CefResponse cefResponse, IntRef intRef, StringRef stringRef);

    boolean readResponse(byte[] bytes, int i, IntRef intRef, CefCallback cefCallback);

    void close();
}
