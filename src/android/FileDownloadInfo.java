package com.polyvi.xface.extension.advancedfiletransfer;

public class FileDownloadInfo {

    private int mTotalSize;

    private int mCompleteSize;

    private String mUrl;

    public FileDownloadInfo(int totalSize, int completeSize, String url) {
        super();
        mTotalSize = totalSize;
        mCompleteSize = completeSize;
        mUrl = url;
    }

    public int getCompleteSize() {
        return mCompleteSize;
    }

    public void setCompleteSize(int completeSize) {
        mCompleteSize = completeSize;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public int getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(int totalSize) {
        mTotalSize = totalSize;
    }

    public boolean isDownloadCompleted() {
        return mTotalSize == mCompleteSize;
    }

    @Override
    public String toString() {
        return "DownloadInfo [mTotalSize=" + mTotalSize + ", mCompeleteSize="
                + mCompleteSize + ", mUrl=" + mUrl + "]";
    }

}
