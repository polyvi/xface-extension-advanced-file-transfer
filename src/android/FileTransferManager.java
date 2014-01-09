
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
import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;

import android.content.Context;

public class FileTransferManager {
    private static final String COMMAND_DOWNLOAD = "download";

    /** 标示temp文件后缀 */
    private static final String TEMP_FILE_SUFFIX = ".temp";

    /**
     * 一个FileTransferManager管理一个app的下载上传任务 Map<String,
     * IFileTransfer>,第一个key代表上传是表示要上传的文件地址
     */
    private Map<String, IFileTransfer> mHashMapFileTransfers = null;

    private FileTransferRecorder mFileTransferRecorder = null;

    private Context mContext;

    public FileTransferManager(Context context, String configRoot) {
        mContext = context;
        mHashMapFileTransfers = new HashMap<String, IFileTransfer>();
        mFileTransferRecorder = new FileTransferRecorder(configRoot);
    }

    public FileTransferManager() {
    }

    /**
     * 当文件传输完成后移除IFileTransfer
     *
     * @param source
     *            下载时表示服务器地址，上传是表示要上传的文件地址
     */
    public void removeFileTranferTask(String source) {
        if (mHashMapFileTransfers.containsKey(source)) {
            mHashMapFileTransfers.remove(source);
        }
    }

    /**
     * 当有文件传输任务发起时，增加一个传输任务
     *
     * @param source
     *            下载时表示服务器地址，上传是表示要上传的文件地址
     * @param target
     *            下载时表示存储下载文件的本地地址，上传是表示要上传的服务器地址
     * @param callbackCtx
     *            回调上下文环境
     * @param commandTtype
     *            传输的类型(上传或下载两种)
     */
    public void addFileTranferTask(String source, String target,
            CallbackContext callbackCtx, String commandTtype) {
        IFileTransfer fileTransfer = getFileTransfer(source, target,
                commandTtype);
        if (!mHashMapFileTransfers.containsValue(fileTransfer)) {
            mHashMapFileTransfers.put(source, fileTransfer);
        }
        fileTransfer.transfer(callbackCtx);
    }

    /**
     * 获取IFileTransfer对象，如果Map<String, IFileTransfer>中有就直接获取，没有就创建
     *
     * @param source
     *            下载时表示服务器地址，上传是表示要上传的文件地址
     * @param target
     *            下载时表示存储下载文件的本地地址，上传是表示要上传的服务器地址
     * @param extensionContext
     *            XExtensionContext对象
     * @param webContext
     *            当前应用
     * @param commandType
     *            传输的类型(上传或下载两种)
     */
    private IFileTransfer getFileTransfer(String source, String target,
            String commandType) {
        IFileTransfer fileTransfer = mHashMapFileTransfers.get(source);
        if (fileTransfer == null) {
            if (commandType.equals(COMMAND_DOWNLOAD)) {
                fileTransfer = new FileDownloader(mContext, source, target,
                        mFileTransferRecorder, this);
            }
            else {
                fileTransfer = new FileUploader(mContext, source, target,
                        mFileTransferRecorder, this);
            }
            mHashMapFileTransfers.put(source, fileTransfer);
        }
        return fileTransfer;
    }

    /**
     * 暂停指定的文件传输任务
     *
     * @param source
     *            下载时表示服务器地址，上传是表示要上传的文件地址
     */
    public void pause(String source) {
        IFileTransfer fileTransfer = mHashMapFileTransfers.get(source);
        if (fileTransfer != null) {
            fileTransfer.pause();
        }
    }

    /**
     * 停止该app所有文件传输任务
     */
    public void stopAll(CallbackContext callbackCtx) {
        if (mHashMapFileTransfers != null) {
            for (IFileTransfer fileTransfer : mHashMapFileTransfers.values()) {
                fileTransfer.pause();
            }
        }
    }

    /**
     * 取消指定的文件传输任务
     *
     * @param source
     *            下载时表示服务器地址，上传是表示要上传的文件地址
     * @param target
     *            下载时表示存储下载文件的本地地址，上传是表示要上传的服务器地址
     * @param commandType
     *            传输的类型(上传或下载两种)
     */
    public void cancel(String source, String target,
            CallbackContext callbacktxt, String commandType) {
        pause(source);
        removeFileTranferTask(source);
        if (commandType.equals(COMMAND_DOWNLOAD)) {
            if (null != mFileTransferRecorder) {
                mFileTransferRecorder.deleteDownloadInfo(source);
            }
            File file = new File(target + TEMP_FILE_SUFFIX);
            if (file.exists()) {
                file.delete();
            }
        } else {
            if (null != mFileTransferRecorder) {
                mFileTransferRecorder.deleteUploadInfo(source);
            }
        }
    }

    public Map<String, IFileTransfer> getHashMapFileTransfers() {
        return mHashMapFileTransfers;
    }

    public FileTransferRecorder getFileTransferRecorders() {
        return mFileTransferRecorder;
    }

}
