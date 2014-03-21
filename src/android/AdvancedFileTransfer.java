
/*
 Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
 This program is distributed under the terms of the GNU General Public License.

 This file is part of xFace.

 xFace is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 xFace is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with xFace.  If not, see <http://www.gnu.org/licenses/>.
*/

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

import com.polyvi.xface.util.XLog;
import com.polyvi.xface.util.XPathResolver;
import com.polyvi.xface.view.XAppWebView;

public class AdvancedFileTransfer extends CordovaPlugin {
    private static final String CLASS_NAME = AdvancedFileTransfer.class
            .getSimpleName();

    private static final String COMMAND_DOWNLOAD = "download";
    private static final String COMMAND_UPLOAD = "upload";
    private static final String COMMAND_PAUSE = "pause";
    private static final String COMMAND_CANCEL = "cancel";

    private static final int FILE_NOT_FOUND_ERR = 1;
    private static final int INVALID_URL_ERR = 2;
    private static final int CONNECTION_ERR = 3;

    private FileTransferManager mFileTransferManager;

    private interface AdvancedFileTransferOp {
        void run() throws Exception;
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
    public boolean execute(String action, final JSONArray args,
            final CallbackContext callbackCtx) throws JSONException {
        if (action.equals(COMMAND_DOWNLOAD)) {
            threadhelper(new AdvancedFileTransferOp() {

                @Override
                public void run() throws Exception {
                    download(args.getString(0), args.getString(1), callbackCtx,
                            webView);
                }
            }, callbackCtx, args.getString(0), args.getString(1));
        } else if (action.equals(COMMAND_UPLOAD)) {
            threadhelper(new AdvancedFileTransferOp() {

                @Override
                public void run() throws Exception {
                    upload(args.getString(0), args.getString(1), callbackCtx);
                }
            }, callbackCtx, args.getString(0), args.getString(1));
        } else if (action.equals(COMMAND_PAUSE)) {
            threadhelper(new AdvancedFileTransferOp() {

                @Override
                public void run() throws Exception {
                    mFileTransferManager.pause(args.getString(0));
                }
            }, callbackCtx, args.getString(0), "");
        } else if (action.equals(COMMAND_CANCEL)) {
            threadhelper(new AdvancedFileTransferOp() {

                @Override
                public void run() throws Exception {
                    mFileTransferManager.cancel(args.getString(0),
                            args.getString(1), callbackCtx, COMMAND_DOWNLOAD);
                }
            }, callbackCtx, args.getString(0), args.getString(1));
        } else {
            return false;
        }
        return true;
    }

    /**
     * 异步执行扩展功能，并处理结果
     *
     * @param transferOp
     * @param callbackContext
     * @param action
     */
    private void threadhelper(final AdvancedFileTransferOp transferOp,
            final CallbackContext callbackContext, final String source,
            final String target) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    transferOp.run();
                } catch (Exception e) {
                    XLog.e(CLASS_NAME, e.getMessage());
                    e.printStackTrace();
                    if (e instanceof FileNotFoundException) {
                        JSONObject error = createFileTransferError(
                                FILE_NOT_FOUND_ERR, source, target);
                        callbackContext.error(error);
                    } else if (e instanceof IllegalArgumentException) {
                        JSONObject error = createFileTransferError(
                                INVALID_URL_ERR, source, target);
                        callbackContext.error(error);
                    } else if (e instanceof JSONException) {
                        callbackContext.sendPluginResult(new PluginResult(
                                PluginResult.Status.JSON_EXCEPTION));
                    } else if (e instanceof IOException) {
                        JSONObject error = createFileTransferError(
                                CONNECTION_ERR, source, target);
                        callbackContext.error(error);
                    } else {
                        callbackContext.error("Unknown Error");
                    }
                }
            }
        });
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
     * @param webView
     */
    public void download(String url, String filePath,
            CallbackContext callbackCtx, CordovaWebView webView)
            throws FileNotFoundException, IOException {
        // 目前下载目的地址只支持http协议
        if (!url.startsWith("http://")) {
            throw new IllegalArgumentException();
        }
        XPathResolver pathResolver = new XPathResolver(filePath,
                ((XAppWebView) this.webView).getOwnerApp().getWorkSpace());
        String path = pathResolver.resolve(this.webView.getResourceApi());
        if (null == path) {
            throw new FileNotFoundException();
        }
        File file = new File(path);
        file.getParentFile().mkdirs();
        mFileTransferManager.addFileTranferTask(url, file.getCanonicalPath(),
                callbackCtx, COMMAND_DOWNLOAD, webView);
    }

    public void upload(String filePath, String server,
            CallbackContext callbackCtx) throws FileNotFoundException,
            IllegalArgumentException {

        // 目前上传目的地址只支持http协议
        if (!server.startsWith("http://")) {
            throw new IllegalArgumentException();
        }
        // 在此处检测文件是否存在，防止由于文件不存在运行很多不该执行的代码
        XPathResolver pathResolver = new XPathResolver(filePath,
                ((XAppWebView) this.webView).getOwnerApp().getWorkSpace());
        String absoluteFilePath = pathResolver.resolve(this.webView
                .getResourceApi());
        if (null != absoluteFilePath) {
            mFileTransferManager.addFileTranferTask(absoluteFilePath, server,
                    callbackCtx, COMMAND_UPLOAD, webView);
        } else {
            throw new FileNotFoundException();
        }
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
            XLog.e(CLASS_NAME, e.getMessage(), e);
        }
        return error;
    }
}
