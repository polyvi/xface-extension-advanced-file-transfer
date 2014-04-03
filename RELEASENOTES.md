<!--
#
# Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
# This program is distributed under the terms of the GNU General Public License.
# 
# This file is part of xFace.
# 
# xFace is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# xFace is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with xFace.  If not, see <http://www.gnu.org/licenses/>.
#
-->

# Release Notes
### 1.0.0 Tue Jan 07 2014 16:08:26 GMT+0800 (CST)
 *  [android][Sync xFace3.1]Add Upload method
 *  AdvancedFileTransfer支持多app
 *  [wp8] modify AdvancedFileTransfer  support appWorkspace
 *  modify plugin.xml,delete dependency plugin url config
 *  added jasmine tests unique id
### 1.0.1 Mon Jan 27 2014 15:56:17 GMT+0800 (CST)
 *  batch modify .reviewboard
 *  [android][Sync xFace3.1]Delete redundant codes and change file format from ANSI to UTF-8
 *  [android]Change Log to XLog
 *  [android]Licensed GPLV3 to advanced file transfer plugin
 *  refactor advance_filetransfer
 *   Sync p4 change 17214: Removed unused method 'updateDownloadInfo' from XFileDownloadInfoRecorder
 *  Incremented plugin version on dev branch to 1.0.1-dev

## 1.0.2 (Fri Feb 28 2014)


 *  [Android]Advanced file transfer supports 'file:///android_asset/' protocal
 *  Add auto tests for resolving file path.
 *  Add filesystem property to entry object.
 *  [iOS] Since entry object has more properties, use file plugin's method to make entry.
 *  Fix the incorrect api description.
 *  [Android]Advanced file transfer plugin supports cordova file url 'cdvfile://localhost/<filesystemType>/<path to file>'
 *  [iOS]Invoke file plugin resolveFilePath: method to process file path
 *  Incremented plugin version on dev branch to 1.0.2-dev


## 1.0.3 (Wed Mar 19 2014)


 *  [Android]Compatible with new file entry object
 *  Ensure that nativeURL is used by download
 *  Deleted upload test
 *  Delete unsupport tests in autotest
 *  Incremented plugin version on dev branch to 1.0.3-dev


## 1.0.4 (Thu Apr 03 2014)


 *  [Android]Fix network on mainThread exception in Android 4.0 version or above
 *  Update test page
 *  Incremented plugin version on dev branch to 1.0.4-dev
