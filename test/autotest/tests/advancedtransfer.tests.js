describe('AdvancedFileTransfer (xFace.AdvancedFileTransfer)', function () {
	var download_source = "http://apollo.polyvi.com/develop/FileTransfer/test.rar";
	var download_target = "test.rar";
	var upload_source = "test_upload2.rar";
	var upload_target = "http://polyvi.net:8091/mi/UploadServer";

    var getMalformedUrl = function() {
        if (isAndroid()) {
            // bad protocol causes a MalformedUrlException on Android
            return "httpssss://example.com";
        } else {
            // iOS doesn't care about protocol, space in hostname causes error
            return "httpssss://exa mple.com";
        }
    };
    // deletes file, if it exists, then invokes callback
    var deleteFile = function(rootEntry, fileName, callback) {
        rootEntry.getFile(fileName, null,
            // remove file system entry
            function(entry) {
                entry.remove(callback, function() { console.log('[ERROR] deleteFile cleanup method invoked fail callback.'); });
            },
            // doesn't exist
            callback);
    };

    it("advancedtransfer.spec.1 should exist xFace.AdvancedFileTransfer object", function() {
        expect(xFace.AdvancedFileTransfer).toBeDefined();
    });

    it("advancedtransfer.spec.2 should exist and be constructable", function() {
        var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
        expect(aft).toBeDefined();
    });

    it("advancedtransfer.spec.3 should contain a download function", function() {
        var download_aft = new xFace.AdvancedFileTransfer(download_source,download_target);
        expect(typeof download_aft.download).toBe('function');
    });

    it("advancedtransfer.spec.4 should contain a pause function", function() {
        var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
        expect(typeof aft.pause).toBe('function');
    });

    it("advancedtransfer.spec.5 should contain a cancel function", function() {
        var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
        expect(typeof aft.cancel).toBe('function');
    });

    describe('download method', function() {
        var fail = createDoNotCallSpy('downloadFail');
        var lastProgressEvent = null;
        var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
            expect(entry.name).toBe(download_target);
            expect(lastProgressEvent.loaded).toBeGreaterThan(1);
        });

        it("advancedtransfer.spec.6 should be able to download a file using http", function() {
            this.after(function() {
                deleteFile(workspace_root, download_target);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
                aft.onprogress = function(e) {
                    lastProgressEvent = e;
                };
                aft.download(downloadWin, fail);
            });
            waitsForAny(downloadWin, fail, 10000);
         });

        it("advancedtransfer.spec.7 should be able to download a file using http after pause", function() {
            this.after(function() {
                deleteFile(workspace_root, download_target);
            });
            runs(function() {
                var download_win = createDoNotCallSpy('download_win');
                var download_fail = createDoNotCallSpy('download_fail');
                var fail = createDoNotCallSpy('downloadFail');
                var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
                aft.onprogress = function(e) {
                    lastProgressEvent = e;
                };
                aft.download(download_win, download_fail);
                aft.pause();
                aft.download(downloadWin, fail);
            });
            waitsForAny(downloadWin, fail, 10000);
         });

        it("advancedtransfer.spec.8 should be stopped by pause() ", function() {
            var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
            var downloadWin = createDoNotCallSpy('downloadWin');
            var downloadFail = jasmine.createSpy().andCallFake(function(e) {
            });
            this.after(function() {
                deleteFile(workspace_root, download_target+".temp");
            });
            runs(function() {
                aft.pause(); // should be a no-op.
                aft.download(downloadWin, downloadFail);
                aft.pause();
                aft.pause(); // should be a no-op.
            });
         });

        it("advancedtransfer.spec.9 should be stopped by cancel()", function() {
            var aft = new xFace.AdvancedFileTransfer(download_source,download_target);
            var downloadWin = createDoNotCallSpy('downloadWin');
            var downloadFail = jasmine.createSpy().andCallFake(function(e) {
            });
            this.after(function() {
                deleteFile(workspace_root, download_target+".temp");
            });
            runs(function() {
                aft.cancel(); // should be a no-op.
                aft.download(downloadWin, downloadFail);
                aft.cancel();
                aft.cancel(); // should be a no-op.
            });
         });

        it("advancedtransfer.spec.10 should get http status on failure", function() {
            var downloadWin = createDoNotCallSpy('downloadWin');
            var remoteFile = download_source + "/404";
            var downloadFail = jasmine.createSpy().andCallFake(function(error) {
                expect(error.code).toBe(FileTransferError.CONNECTION_ERR);
            });
            this.after(function() {
                deleteFile(workspace_root, download_target);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(remoteFile,download_target);
                aft.download(downloadWin, downloadFail);
            });
            waitsForAny(downloadWin, downloadFail);
        });

        it("advancedtransfer.spec.11 should handle malformed urls", function() {
            var downloadWin = createDoNotCallSpy('downloadWin');
            var remoteFile = getMalformedUrl();
            var downloadFail = jasmine.createSpy().andCallFake(function(error) {
                // Note: Android needs the bad protocol to be added to the access list
                // <access origin=".*"/> won't match because ^https?:// is prepended to the regex
                // The bad protocol must begin with http to avoid automatic prefix
                expect(error.code).toBe(FileTransferError.INVALID_URL_ERR);
            });
            this.after(function() {
                deleteFile(workspace_root, download_target);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(remoteFile,download_target);
                aft.download(downloadWin, downloadFail);
            });
            waitsForAny(downloadWin, downloadFail);
        });

        it("advancedtransfer.spec.12 should handle unknown host", function() {
            var downloadWin = createDoNotCallSpy('downloadWin');
            var remoteFile = "http://192.168.3.123/index.html";
            var localFileName = remoteFile.substring(remoteFile.lastIndexOf('/')+1);
            var downloadFail = jasmine.createSpy().andCallFake(function(error) {
                expect(error.code).toBe(FileTransferError.CONNECTION_ERR);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(remoteFile,localFileName);
                aft.download(downloadWin, downloadFail);
            });
           // iOS 的网络链接超时为60秒，故等待时间要大于60秒
            waitsForAny(downloadWin, downloadFail, 62000);
        });

        it("advancedtransfer.spec.13 should handle bad file path", function() {
            var downloadWin = createDoNotCallSpy('downloadWin');
            var badFilePath = "c:\\54321";
            var downloadFail = jasmine.createSpy().andCallFake(function(error) {
            expect(error.code).toBe(FileTransferError.FILE_NOT_FOUND_ERR);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(download_source,badFilePath);
                aft.download(downloadWin, downloadFail);
            });
            waitsForAny(downloadWin, downloadFail);
        });

        if(isAndroid()) {
	        it("advancedtransfer.spec.14 should contain a upload function(iOS not support upload function now! please ignore!!!)", function() {
	            var upload_aft = new xFace.AdvancedFileTransfer(upload_source,upload_target,true);
	            expect(typeof upload_aft.upload).toBe('function');
	        });
        }
    });

    describe('File Path', function () {
        it("advancedtransfer.spec.15 success callback should be called with relative file path", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            };

            this.after(function() {
                deleteFile(workspace_root, download_target);
            });
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(download_source, download_target);
                aft.onprogress = function(e) {
                    lastProgressEvent = e;
                };
                aft.download(downloadWin, fail);
            });

            waitsForAny(fileWin, fail, fileFail);
        });

        it("advancedtransfer.spec.16 success callback should be called with absolute appworkspace file path", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = workspace_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(workspace_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                cordova.exec(function(localPath) {
                    var aft = new xFace.AdvancedFileTransfer(download_source, localPath);
	                aft.onprogress = function(e) {
	                    lastProgressEvent = e;
	                };
	                aft.download(downloadWin, fail);
                }, unsupportedOperation, 'File', '_getLocalFilesystemPath', [cdvfileURL]);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                if (!unsupportedOperation.wasCalled) {
                    expect(downloadWin).toHaveBeenCalled();
                    expect(fail).not.toHaveBeenCalled();
                }
            });
        });

        it("advancedtransfer.spec.17 success callback should be called with absolute persisent file path", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = persistent_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(persistent_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                cordova.exec(function(localPath) {
                    var aft = new xFace.AdvancedFileTransfer(download_source, localPath);
	                aft.onprogress = function(e) {
	                    lastProgressEvent = e;
	                };
	                aft.download(downloadWin, fail);
                }, unsupportedOperation, 'File', '_getLocalFilesystemPath', [cdvfileURL]);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                if (!unsupportedOperation.wasCalled) {
                    expect(downloadWin).toHaveBeenCalled();
                    expect(fail).not.toHaveBeenCalled();
                }
            });
        });

        it("advancedtransfer.spec.18 success callback should be called with appworkspace file url", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = workspace_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(workspace_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                cordova.exec(function(localPath) {
                    var aft = new xFace.AdvancedFileTransfer(download_source, "file://" + localPath);
	                aft.onprogress = function(e) {
	                    lastProgressEvent = e;
	                };
	                aft.download(downloadWin, fail);
                }, unsupportedOperation, 'File', '_getLocalFilesystemPath', [cdvfileURL]);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                if (!unsupportedOperation.wasCalled) {
                    expect(downloadWin).toHaveBeenCalled();
                    expect(fail).not.toHaveBeenCalled();
                }
            });
        });

        it("advancedtransfer.spec.19 success callback should be called with persisent file url", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = persistent_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(persistent_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                cordova.exec(function(localPath) {
                    var aft = new xFace.AdvancedFileTransfer(download_source, "file://" + localPath);
	                aft.onprogress = function(e) {
	                    lastProgressEvent = e;
	                };
	                aft.download(downloadWin, fail);
                }, unsupportedOperation, 'File', '_getLocalFilesystemPath', [cdvfileURL]);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                if (!unsupportedOperation.wasCalled) {
                    expect(downloadWin).toHaveBeenCalled();
                    expect(fail).not.toHaveBeenCalled();
                }
            });
        });

        it("advancedtransfer.spec.20 success callback should be called with appworkspace cdvfile url", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = workspace_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(persistent_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(download_source, cdvfileURL);
                aft.onprogress = function(e) {
                    lastProgressEvent = e;
                };
                aft.download(downloadWin, fail);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                expect(downloadWin).toHaveBeenCalled();
                expect(fail).not.toHaveBeenCalled();
            });
        });

        it("advancedtransfer.spec.21 callback should be called with persisent cdvfile url", function () {
            var fail = createDoNotCallSpy('downloadFail');
            var fileFail = createDoNotCallSpy('downloadFail');
            var lastProgressEvent = null;
            var cdvfileURL = persistent_root.toURL() + download_target;

            var fileWin = jasmine.createSpy().andCallFake(function(blob) {
                expect(lastProgressEvent.loaded).not.toBeGreaterThan(blob.size);
            });

            var downloadWin = jasmine.createSpy().andCallFake(function(entry) {
                expect(entry.name).toBe(download_target);
                expect(lastProgressEvent.loaded).toBeGreaterThan(1);
                entry.file(fileWin, fileFail);
            });

            this.after(function() {
                deleteFile(persistent_root, download_target);
            });

            var unsupportedOperation = jasmine.createSpy("Operation not supported");
            runs(function() {
                var aft = new xFace.AdvancedFileTransfer(download_source, cdvfileURL);
                aft.onprogress = function(e) {
                    lastProgressEvent = e;
                };
                aft.download(downloadWin, fail);
            });
            waitsForAny(fileWin, fail, fileFail);
            runs(function() {
                expect(downloadWin).toHaveBeenCalled();
                expect(fail).not.toHaveBeenCalled();
            });
        });
    });
});
