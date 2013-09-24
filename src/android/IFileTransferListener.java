package com.polyvi.xface.extension.advancedfiletransfer;

public interface IFileTransferListener {
    /**
     * 文件传输成功回调
     */
    public void onSuccess();

    /**
     * 文件输出失败回调
     *
     * @param errorCode
     *            失败错误码
     */
    public void onError(int errorCode);

    /**
     * 更新传输进度
     *
     * @param completeSize
     *            已输出的大小
     * @param totalSize
     *            要传输的总大小(下载时没有用到该参数，可以直接传0)
     */
    public void onProgressUpdated(int completeSize, long totalSize);
}
