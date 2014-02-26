
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
//  XFileDownloader.m
//  xFaceLib
//
//


#import <XFace/XApplication.h>
#import <XFace/XFileUtils.h>
#import <Cordova/CDVInvokedUrlCommand.h>
#import <Cordova/CDVPluginResult.h>

#import "XFileDownloaderDelegate.h"
#import "XFileDownloader.h"
#import "CDVFile.h"

@implementation XFileDownloader

- (id) initWithCommandDelegate:(id <CDVCommandDelegate>)cmdDelegate url:(NSString *)aUrl filePath:(NSString *)filePath downloadInfoRecorder:(XFileDownloadInfoRecorder *)recorder downloaderManager:(XFileDownloaderManager *)manager filePlugin:afilePlugin
{
    self = [super init];
    if(self)
    {
        self->commandDelegate = cmdDelegate;
        self->state = INIT;
        self->completeSize = 0;
        self->url = aUrl;
        self->totalSize = 0;
        self->localFilePath = filePath;
        self->downloadInfoRecorder = recorder;
        self->downloaderManager = manager;
        self->filePlugin = afilePlugin;
    }
    return self;
}

/**
    判断是否是第一次下载.
 */
- (BOOL) isFirst:(NSString *)aUrl
{
    return ![downloadInfoRecorder hasInfo:aUrl];
}

/**
    初始化下载信息.
 */
- (void) initDownloadInfo
{
        if([self isFirst:self->url])
        {
            self->downloadInfo = [[XFileDownloadInfo alloc] initWithURL:self->url andTotalSize:0 andCompleteSize:0];
            [self->downloadInfoRecorder saveDownloadInfo:self->downloadInfo];
            //第一次 下载 存在同名的temp文件 则删除
            [XFileUtils removeItemAtPath:[self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX] error:nil];
        }
        else
        {
            self->downloadInfo = [downloadInfoRecorder getDownloadInfo:url];
            self->totalSize = [downloadInfo totalSize];
            //读取temp文件 大小
            NSFileManager* fileMg = [NSFileManager defaultManager];
            NSDictionary* fileAttrs = [fileMg attributesOfItemAtPath:[self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX] error:nil];
            self->completeSize = [fileAttrs fileSize];
        }
}

- (void) download:(NSString *)callbackId
{
    //如果文件正在下载再请求下载时直接返回
    if(self->state == DOWNLOADING)
    {
        return;
    }
    self->_callbackId = callbackId;
    self->state = DOWNLOADING;

    [self initDownloadInfo];

    NSURL *nsUrl = [NSURL URLWithString:self->url];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:nsUrl];
    XFileDownloaderDelegate* delegate = [[XFileDownloaderDelegate alloc] initWithDownloader:self];
    NSString *range = [NSString stringWithFormat:@"%@%d%@", @"bytes=",self->completeSize, @"-"];
    [request setValue:range forHTTPHeaderField:@"Range"];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    connection = [NSURLConnection connectionWithRequest:request delegate:delegate];
}

- (void) pause
{
    @synchronized(self)
    {
        [connection cancel];
        connection = nil;
        state = PAUSE;
    }
}

- (BOOL) isPaused
{
    return state == PAUSE;
}

- (void) onProgressUpdated:(NSInteger) aTotalSize withData:(NSData *) data
{
    NSString *parentPath = [self->localFilePath stringByDeletingLastPathComponent];

    // 判断父目录是否存在，不存在则创建
    NSFileManager* fileMgr = [NSFileManager defaultManager];
    if(![fileMgr fileExistsAtPath:parentPath ])
    {
        [fileMgr createDirectoryAtPath:parentPath withIntermediateDirectories:YES attributes:nil error:nil];
    }

    //下载数据先存temp文件
    if(![fileMgr fileExistsAtPath:[self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX]])
    {
        [[NSFileManager defaultManager] createFileAtPath:[self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX] contents:nil attributes:nil];
    }
    FILE *file = fopen([[self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX] UTF8String], [@"ab+" UTF8String]);

    if(file != NULL)
    {
        fseek(file, 0, SEEK_END);
    }

    int readSize = [data length];
    self->completeSize += readSize;
    [self->downloadInfo setCompleteSize:self->completeSize];
    fwrite((const void *)[data bytes], readSize, 1, file);
    fclose(file);
    if(self->totalSize == 0)
    {
        self->totalSize = aTotalSize;
        [downloadInfoRecorder setTotalSize:self->totalSize withUrl:self->url];
    }

    NSMutableDictionary* message = [NSMutableDictionary dictionaryWithCapacity:2];
    [message setObject:[NSNumber numberWithInt:self->totalSize] forKey:@"total"];
    [message setObject:[NSNumber numberWithInt:self->completeSize] forKey:@"loaded"];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];

    // 由于还需执行后续的onSuccess或onError,故需做此标记以通知js端保留之前设置的回调
    [result setKeepCallbackAsBool:YES];
    [self->commandDelegate sendPluginResult:result callbackId:self->_callbackId];
}

- (void) onSuccess
{
    //重命名temp文件
    __autoreleasing NSError *error;
    NSString *tempFilePath = [self->localFilePath stringByAppendingString:TEMP_FILE_SUFFIX];
    NSAssert(([tempFilePath length] > 0), @"Temp file path should not be nil!");

    BOOL ret = [XFileUtils moveItemAtPath:tempFilePath toPath:self->localFilePath error:&error];
    if(!ret && error)
    {
        ALog(@"Failed to move temp file at path:%@ to path:%@ with error:%@", tempFilePath, self->localFilePath, [error localizedDescription]);
    }

    //FIXME:我们认为在上述情况下不应出现重命名失败的问题，如果实际应用中的确还会发生ret为NO的情况，则考虑添加自定义错误码
    NSAssert(ret, @"Rename temp file should not be failed!");

    [downloaderManager removeDownloaderWithUrl:self->url];
    [downloadInfoRecorder deleteDownloadInfo:self->url];
    self->state = INIT;

    NSURL *targetUrl = [self->filePlugin fileSystemURLforLocalPath:self->localFilePath].url;
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self->filePlugin makeEntryForURL:targetUrl]];
    [self->commandDelegate sendPluginResult:result callbackId:self->_callbackId];
}

- (void) onError
{
    self->state = INIT;
    [downloaderManager removeDownloaderWithUrl:self->url];
    [downloadInfoRecorder deleteDownloadInfo:self->url];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary: [XFileUtils createFileTransferError:CONNECTION_ERR andSource:self->url andTarget:self->localFilePath]];
    [self->commandDelegate sendPluginResult:result callbackId:self->_callbackId];
}

@end

