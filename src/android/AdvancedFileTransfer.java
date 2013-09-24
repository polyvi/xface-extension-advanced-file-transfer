package com.polyvi.xface.extension.advancedfiletransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.polyvi.xface.util.XPathResolver;
import com.polyvi.xface.view.XAppWebView;

import android.util.Log;

public class AdvancedFileTransfer extends CordovaPlugin {
    private static final String CLASS_NAME = AdvancedFileTransfer.class
            .getSimpleName();

    private static final String ILLEGAL_ARGUMENT_EXCEPTION_NOT_IN_ROOT_DIR = "filePath is not in root directory";
    private static final String ILLEGAL_ARGUMENT_EXCEPTION_NAME_CONTAINS_COLON = "This file has a : in its name";

    private static final String COMMAND_DOWNLOAD = "download";
    private static final String COMMAND_PAUSE = "pause";
    private static final String COMMAND_CANCEL = "cancel";

    private static final int FILE_NOT_FOUND_ERR = 1;
    private static final int INVALID_URL_ERR = 2;
    private static final int CONNECTION_ERR = 3;

    private FileTransferManager mFileTransferManager;

    public AdvancedFileTransfer() {
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        XAppWebView xAppWebView = (XAppWebView) webView;
        if (null == mFileTransferManager) {
            mFileTransferManager = new FileTransferManager(cordova
                    .getActivity().getApplicationContext(), xAppWebView
                    .getOwnerApp().getDataDir());
        }
    }

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackCtx) throws JSONException {
        String source = null;
        String target = null;
        try {
            if (action.equals(COMMAND_DOWNLOAD)) {
                source = args.getString(0);
                target = args.getString(1);
                download(source, target, callbackCtx);
                return true;
            } else if (action.equals(COMMAND_PAUSE)) {
                source = args.getString(0);
                mFileTransferManager.pause(source);
                callbackCtx.sendPluginResult(new PluginResult(
                        PluginResult.Status.OK));
                return true;
            } else if (action.equals(COMMAND_CANCEL)) {
                source = args.getString(0);
                target = args.getString(1);
                mFileTransferManager.cancel(source, target, callbackCtx,
                        COMMAND_DOWNLOAD);
                return true;
            }
        } catch (FileNotFoundException e) {
            JSONObject error = createFileTransferError(FILE_NOT_FOUND_ERR,
                    source, target);
            callbackCtx.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, error));
            Log.e(CLASS_NAME, e.getMessage());
        } catch (IllegalArgumentException e) {
            JSONObject error = createFileTransferError(INVALID_URL_ERR, source,
                    target);
            callbackCtx.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, error));
            Log.e(CLASS_NAME, e.getMessage());
        } catch (IOException e) {
            JSONObject error = createFileTransferError(CONNECTION_ERR, source,
                    target);
            callbackCtx.sendPluginResult(new PluginResult(
                    PluginResult.Status.ERROR, error));
            Log.e(CLASS_NAME, e.getMessage());
        } catch (JSONException e) {
            callbackCtx.sendPluginResult(new PluginResult(
                    PluginResult.Status.JSON_EXCEPTION));
            Log.e(CLASS_NAME, e.getMessage(), e);
        }
        return false;
    }

    /**
     * 发起一个下载请求
     *
     * @param app
     *            当前应用
     * @param url
     *            服务器的URL
     * @param filePath
     *            设备上的路径
     * @param callbackCtx
     *            回调上下文环境
     */
    public void download(String url, String filePath,
            CallbackContext callbackCtx) throws FileNotFoundException,
            IOException {
        // 目前下载目的地址只支持http协议
        if (!url.startsWith("http://")) {
            throw new IllegalArgumentException();
        }
        XPathResolver pathResolver = new XPathResolver(filePath,
                ((XAppWebView) this.webView).getOwnerApp().getWorkSpace());
        String path = pathResolver.resolve();
        File file = new File(path);
        file.getParentFile().mkdirs();
        mFileTransferManager.addFileTranferTask(url, file.getCanonicalPath(),
                callbackCtx, COMMAND_DOWNLOAD);
    }

    /**
     * 创建FileTransferError对象
     *
     * @param errorCode
     *            错误码
     * @return JSONObject 包含错误的JSON对象
     */
    private JSONObject createFileTransferError(int errorCode, String source,
            String target) {
        JSONObject error = null;
        try {
            error = new JSONObject();
            error.put("code", errorCode);
            error.put("source", source);
            error.put("target", target);
        } catch (JSONException e) {
            Log.e(CLASS_NAME, e.getMessage(), e);
        }
        return error;
    }
}
