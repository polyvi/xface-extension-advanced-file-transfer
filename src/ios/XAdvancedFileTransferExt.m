
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
//  XAdvancedFileTransferExt.m
//  xFaceLib
//
//

#import "XAdvancedFileTransferExt.h"
#import <XFace/XApplication.h>
#import <XFace/XUtils.h>
#import <XFace/XFileUtils.h>
#import <XFace/XUtils.h>
#import <Cordova/CDVInvokedUrlCommand.h>
#import <Cordova/CDVPluginResult.h>

@implementation XAdvancedFileTransferExt

- (id)initWithWebView:(UIWebView*)theWebView
{
    self = [super initWithWebView:theWebView];
    if (self) {
        self->downloaderManager = [[XFileDownloaderManager alloc] init];
    }
    return self;
}


- (void) download:(CDVInvokedUrlCommand*)command
{
    NSString *source = [command.arguments objectAtIndex:0];
    NSString *filePath = [command.arguments objectAtIndex:1];
    id<XApplication> app = [self ownerApp];
    XFileTransferError errorCode = 0;
    CDVPluginResult *result = nil;

    if (NSNotFound != [filePath rangeOfString:@":"].location)
    {
        errorCode = FILE_NOT_FOUND_ERR;
    }

    NSString *fullPath = [XUtils resolvePath:filePath usingWorkspace:[app getWorkspace]];
    if (!fullPath)
    {
        errorCode = FILE_NOT_FOUND_ERR;
    }

    NSURL *file = [NSURL fileURLWithPath:filePath];
    NSURL *url = [NSURL URLWithString:source];

    if (!url)
    {
        errorCode = INVALID_URL_ERR;
        ALog(@"Advanced File Transfer Error: Invalid server URL");
    }
    else if(![file isFileURL])
    {
        errorCode = FILE_NOT_FOUND_ERR;
        ALog(@"Advanced File Transfer Error: Invalid file path or URL");
    }

    if(errorCode > 0)
    {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary: [XFileUtils createFileTransferError:errorCode andSource:source andTarget:filePath]];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    [downloaderManager addDownloaderWithMessageHandler:[[self ownerApp] jsEvaluator] callbackId:command.callbackId application:app url:source filePath:fullPath];
}

- (void) pause:(CDVInvokedUrlCommand*)command
{
    NSString *source = [command.arguments objectAtIndex:0];
    id<XApplication> app = [self ownerApp];
    [downloaderManager pauseWithAppId:[app getAppId] url:source];
}

- (void) cancel:(CDVInvokedUrlCommand*)command
{
    NSString *url = [command.arguments objectAtIndex:0];
    NSString *filePath = [command.arguments objectAtIndex:1];
    id<XApplication> app = [self ownerApp];
    filePath = [XUtils resolvePath:filePath usingWorkspace:[app getWorkspace]];
    [downloaderManager cancelWithAppId:[app getAppId] url:url filePath:filePath];
}

- (void)dealloc
{
    // 退出app时暂停该app中的所有下载任务
    // TODO:移除appId
    [self->downloaderManager stopAllWithAppId:nil];
}

@end
