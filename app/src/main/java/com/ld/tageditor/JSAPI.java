package com.ld.tageditor;

import android.nfc.tech.NfcA;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.io.IOException;

public class JSAPI {
    MainActivity activity;
    WebView web;

    public JSAPI(MainActivity paramActivity, WebView paramWeb) {
        this.activity = paramActivity;
        this.web = paramWeb;
    }

    // Reuse one open connection for the whole tap. Opened lazily, closed on
    // failure or when MainActivity.onNewIntent sees the next tap.
    private NfcA connect() throws IOException {
        NfcA nfcA = this.activity.nfcA;
        if (nfcA == null) {
            nfcA = NfcA.get(this.activity.tag);
            this.activity.nfcA = nfcA;
        }
        if (!nfcA.isConnected()) {
            nfcA.connect();
            nfcA.setTimeout(250);
        }
        return nfcA;
    }

    private void closeConn() {
        if (this.activity.nfcA != null) {
            try {
                this.activity.nfcA.close();
            } catch (IOException ignored) {
            }
            this.activity.nfcA = null;
        }
    }

    @JavascriptInterface
    public String readTag(byte page) {
        try {
            NfcA nfcA = connect();
            byte[] message = new byte[] {
                    0x30,
                    (byte) (page & 0xFF)
            };
            byte[] payload = nfcA.transceive(message);
            String encodeToString = Base64.encodeToString(payload, 0, 16, 0);
            Log.i("JSAPI", "read p" + (page & 0xFF) + " -> " + encodeToString);
            return encodeToString;
        } catch (IOException e) {
            Log.e("JSAPI", "read failed p" + (page & 0xFF), e);
            closeConn();
            return null;
        }
    }

    @JavascriptInterface
    public boolean writeTag(byte page, String payload) {
        byte[] data = Base64.decode(payload, 0);
        try {
            NfcA nfcA = connect();
            byte[] message = new byte[] {
                    (byte) 0xA2,
                    (byte) (page & 0xFF),
                    data[0], data[1], data[2], data[3]
            };
            nfcA.transceive(message);
            Log.i("JSAPI", "wrote p" + (page & 0xFF));
            return true;
        } catch (IOException e) {
            Log.e("JSAPI", "write failed p" + (page & 0xFF), e);
            closeConn();
            return false;
        }
    }

    private void callJavaScript(String methodName, Object... params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{(window.");
        stringBuilder.append(methodName);
        stringBuilder.append("||console.warn.bind(console,'UNHANDLED','");
        stringBuilder.append(methodName);
        stringBuilder.append("'))(");
        for (Object param : params) {
            Object param2 = "";
            if (!(param instanceof String)) {
                param2 = param.toString();
            }
            stringBuilder.append("'");
            stringBuilder.append(param2);
            stringBuilder.append("'");
            stringBuilder.append(",");
        }
        stringBuilder.append("''");
        stringBuilder.append(")}catch(error){console.error('ANDROID APP ERROR',error);}");
        this.web.loadUrl(stringBuilder.toString());
        Log.i("CallJS", stringBuilder.toString());
    }
}
