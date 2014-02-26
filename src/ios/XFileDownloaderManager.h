
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

//
//  XFileDownloaderManager.h
//  xFaceLib
//
//

#import <Foundation/Foundation.h>
#import "XFileDownloadInfoRecorder.h"
#import <XFace/XJavaScriptEvaluator.h>

@class CDVFile;

/** 该类用于管理所有应用的下载器，当有下载任务发起时创建一个XFileDownloader，
 *  当当前的url对应的下载任务完成时应该删除该下载任务 */
@interface XFileDownloaderManager : NSObject
{
    /** 该对象中的key代表url,value代表XFileDownloader对象*/
    NSMutableDictionary *dictDownloaders;

    /** 文件下载信息记录*/
    XFileDownloadInfoRecorder *downloadInfoRecorder;
}

/**
    当文件下载完成时移除XFileDownloader.
    @param url          下载地址
 */
- (void) removeDownloaderWithUrl:(NSString *)url;

/**
    当有下载任务发起时，添加一个XFileDownloader.
    @param cmdDelegate  消息处理者
    @param callback     callback
    @param application  当前应用
    @param aUrl         下载地址
    @param filePath     保存下载文件的路径
 */
- (void) addDownloaderWithCommandDelegate:(id <CDVCommandDelegate>)cmdDelegate callbackId:(NSString *)callbackId application:(id<XApplication>)application url:(NSString *)aUrl filePath:(NSString *)filePath filePlugin:(CDVFile *)filePlugin;

/**
    暂停当前app下url对应的下载任务.
    @param url          下载地址
 */
- (void) pauseWithUrl:(NSString *)url;

/**
    暂停当前app中的所有下载任务.
 */
- (void) stopAll;

/**
    取消当前app下url对应的下载任务.
    @param url          下载地址
    @param filePath     存放下载文件的本地地址
 */
- (void) cancelWithUrl:(NSString *)url filePath:(NSString *)filePath;

@end
