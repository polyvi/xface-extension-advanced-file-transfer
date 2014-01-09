
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
