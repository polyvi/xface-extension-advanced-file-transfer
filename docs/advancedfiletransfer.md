---
license: Licensed to the Apache Software Foundation (ASF) under one
         or more contributor license agreements.  See the NOTICE file
         distributed with this work for additional information
         regarding copyright ownership.  The ASF licenses this file
         to you under the Apache License, Version 2.0 (the
         "License"); you may not use this file except in compliance
         with the License.  You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

         Unless required by applicable law or agreed to in writing,
         software distributed under the License is distributed on an
         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
         KIND, either express or implied.  See the License for the
         specific language governing permissions and limitations
         under the License.
---

AdvancedFileTransfer
======

> The `AdvancedFileTransfer` object provides the function of download file.

Methods
-------

- xFace.AdvancedFileTransfer.download
- xFace.AdvancedFileTransfer.pause
- xFace.AdvancedFileTransfer.cancel

Permissions
-----------

### Android

#### app/res/xml/config.xml

    <plugin name="AdvancedFileTransfer" value="com.polyvi.xface.extension.advancedfiletransfer.AdvancedFileTransfer" />

#### app/AndroidManifest

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
