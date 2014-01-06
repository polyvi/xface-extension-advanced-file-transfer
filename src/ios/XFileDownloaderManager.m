
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
//  XFileDownloaderManager.m
//  xFaceLib
//
//

#import "XFileDownloaderManager.h"
#import <XFace/XApplication.h>
#import "XFileDownloader.h"
#import <XFace/XFileUtils.h>

@implementation XFileDownloaderManager

- (void) removeDownloaderWithUrl:(NSString *)url
{
    [self->dictDownloaders removeObjectForKey:url];
}

- (void) addDownloaderWithCommandDelegate:(id <CDVCommandDelegate>)cmdDelegate callbackId:(NSString *)callbackId application:(id<XApplication>)application url:(NSString *)aUrl filePath:(NSString *)filePath
{
    if (nil == downloadInfoRecorder)
    {
        downloadInfoRecorder = [[XFileDownloadInfoRecorder alloc] initWithApp:application];
    }
    XFileDownloader *downloader = nil;
    if(nil == self->dictDownloaders)
    {
        self->dictDownloaders = [NSMutableDictionary dictionaryWithCapacity:1];
        downloader = [[XFileDownloader alloc] initWithCommandDelegate:cmdDelegate url:aUrl filePath:filePath downloadInfoRecorder:downloadInfoRecorder downloaderManager:self];
        [self->dictDownloaders setObject:downloader forKey:aUrl];
    }
    else
    {
        downloader = [self->dictDownloaders objectForKey:aUrl];
        if(nil == downloader)
        {
            downloader = [[XFileDownloader alloc] initWithCommandDelegate:cmdDelegate url:aUrl filePath:filePath downloadInfoRecorder:downloadInfoRecorder downloaderManager:self];
            [self->dictDownloaders setObject:downloader forKey:aUrl];
        }
    }
    [downloader download:callbackId];
}

- (void) pauseWithUrl:(NSString *)url
{
    XFileDownloader *downloader = [self->dictDownloaders valueForKey:url];
    if(nil != downloader)
    {
        [downloader pause];
    }
}

- (void) stopAll
{
    NSArray *downloaders = [self->dictDownloaders allValues];
    if(nil != downloaders)
    {
        for(XFileDownloader *downloader in [downloaders objectEnumerator])
        {
            [downloader pause];
        }
    }
}

- (void) cancelWithUrl:(NSString *)url filePath:(NSString *)filePath
{
    [self pauseWithUrl:url];
    [self removeDownloaderWithUrl:url];
    [self->downloadInfoRecorder deleteDownloadInfo:url];

    //删掉已下载的temp文件
    [XFileUtils removeItemAtPath:[filePath stringByAppendingString:TEMP_FILE_SUFFIX] error:nil];
}

@end

