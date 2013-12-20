using System;
using System.Collections.Generic;
using System.IO;

using xFaceLib.Log;
using xFaceLib.runtime;
using xFaceLib.Util;
using WPCordovaClassLib.Cordova;

namespace xFaceLib.extensions.advancedFileTransfer
{
    public class XFileTransferManager
    {
        private static String COMMAND_DOWNLOAD = "download";

        /// <summary>
        /// 为每个app创建一个Dictionary<String, XIFileTransfer>,第一个key代表WorkSpace, Dictionary<String,
        /// XIFileTransfer>表示为每个source创建一个XIFileTransfer,
        /// 这里的key在下载时表示服务器地址，上传是表示要上传的文件地址
        /// </summary>
        private Dictionary<String, Dictionary<String, XIFileTransfer>> hashMapFileTransfers = new Dictionary<String, Dictionary<String, XIFileTransfer>>();
        public Dictionary<String, Dictionary<String, XIFileTransfer>> HashMapFileTransfers
        {
            get { return hashMapFileTransfers; }
        }

        /// <summary>
        /// 为每个app创建一个XFileTransferRecorder，因为每个应用有自己单独的配置文件，这里的key代表WorkSpace
        /// </summary>
        private Dictionary<String, XFileTransferRecorder> fileTransferRecorders = new Dictionary<String, XFileTransferRecorder>();
        public Dictionary<String, XFileTransferRecorder> FileTransferRecorders
        {
            get { return fileTransferRecorders; }
        }

        public XFileTransferManager() {
        }

        /// <summary>
        /// 当文件传输完成后移除XIFileTransfer
        /// </summary>
        /// <param name="appWorkSpace">指定app的WorkSpace</param>
        /// <param name="source">下载时表示服务器地址，上传时表示要上传的文件地址</param>
        public void RemoveFileTranferTask(String appWorkSpace, String source)
        {
            Dictionary<String, XIFileTransfer> fileTransfers = null;
            HashMapFileTransfers.TryGetValue(appWorkSpace, out fileTransfers);

            if (fileTransfers != null)
            {
                fileTransfers.Remove(source);
            }
        }

        /// <summary>
        /// 当有文件传输任务发起时，增加一个传输任务
        /// </summary>
        /// <param name="source">下载时表示服务器地址，上传时表示要上传的文件地址</param>
        /// <param name="target">下载时表示存储下载文件的本地地址，上传时表示要上传的服务器地址</param>
        /// <param name="appWorkSpace">app的WorkSpace</param>
        /// <param name="DispatchPluginResult">事件派发</param>
        /// <param name="type">传输的类型(上传或下载两种)</param>
        public void AddFileTranferTask(String source, String target, String appWorkSpace,
                EventHandler<PluginResult> DispatchPluginResult, String type)
        {
            Dictionary<String, XIFileTransfer> fileTransfers = null;
            HashMapFileTransfers.TryGetValue(appWorkSpace, out fileTransfers);
            if (fileTransfers == null)
            {
                fileTransfers = new Dictionary<String, XIFileTransfer>();
            }
            XIFileTransfer fileTransfer = GetFileTransfer(fileTransfers, source, target, appWorkSpace, type);

            fileTransfer.DispatchPluginResult += DispatchPluginResult;
            if (!HashMapFileTransfers.ContainsValue(fileTransfers))
            {
                HashMapFileTransfers.Add(source, fileTransfers);
            }
            fileTransfer.Transfer();
        }

        /// <summary>
        /// 获取XIFileTransfer对象，如果Dictionary<String, XIFileTransfer>中有就直接获取，没有就创建
        /// </summary>
        /// <param name="fileTransfers">存储XIFileTransfer对象的Dictionary</param>
        /// <param name="source">下载时表示服务器地址，上传时表示要上传的文件地址</param>
        /// <param name="target">下载时表示存储下载文件的本地地址，上传时表示要上传的服务器地址</param>
        /// <param name="appWorkSpace">app的WorkSpace</param>
        /// <param name="type">传输的类型(上传或下载两种)</param>
        /// <returns></returns>
        private XIFileTransfer GetFileTransfer(Dictionary<String, XIFileTransfer> fileTransfers, String source, String target, String appworkSpace, String type)
        {
            XFileTransferRecorder fileTransferRecorder = null;
            FileTransferRecorders.TryGetValue(appworkSpace, out fileTransferRecorder);
            if (fileTransferRecorder == null)
            {
                fileTransferRecorder = new XFileTransferRecorder(appworkSpace);
                FileTransferRecorders.Add(appworkSpace, fileTransferRecorder);
            }

            XIFileTransfer fileTransfer = null;
            fileTransfers.TryGetValue(source, out fileTransfer);
            if (fileTransfer == null)
            {
                if (type.Equals(COMMAND_DOWNLOAD))
                {
                    fileTransfer = new XFileDownloader(source, target, appworkSpace, fileTransferRecorder, this);
                }
                fileTransfers.Add(source, fileTransfer);
            }
            return fileTransfer;
        }

        /// <summary>
        /// 暂停指定的文件传输任务
        /// </summary>
        /// <param name="appWorkSpace">app的WorkSpace</param>
        /// <param name="source">下载时表示服务器地址，上传时表示要上传的文件地址</param>
        public void Pause(String appWorkSpace, String source)
        {
            Dictionary<String, XIFileTransfer> fileTransfers = null;
            HashMapFileTransfers.TryGetValue(appWorkSpace, out fileTransfers);
            if (fileTransfers != null)
            {
                XIFileTransfer fileTransfer = null;
                fileTransfers.TryGetValue(source, out fileTransfer);
                if (fileTransfer != null)
                {
                    fileTransfer.Pause();
                }
            }
        }

        /// <summary>
        /// 停止某个app中的所有文件传输任务
        /// </summary>
        /// <param name="appId">指定app的WorkSpace</param>
        public void StopAllByApp(String appWorkSpace)
        {
            Dictionary<String, XIFileTransfer> fileTransfers = null;
            HashMapFileTransfers.TryGetValue(appWorkSpace, out fileTransfers);
            if (fileTransfers != null)
            {
                foreach (XIFileTransfer fileTransfer in fileTransfers.Values)
                {
                    fileTransfer.Pause();
                }
            }
        }

        /// <summary>
        /// 停止所有app中的所有任务
        /// </summary>
        public void StopAll()
        {
            foreach (Dictionary<String, XIFileTransfer> fileTransfers in HashMapFileTransfers.Values)
            {
                foreach (XIFileTransfer fileTransfer in fileTransfers.Values)
                {
                    fileTransfer.Pause();
                }
            }
        }

        /// <summary>
        /// 取消指定的文件传输任务
        /// </summary>
        /// <param name="appWorkSpace">指定app的WorkSpace</param>
        /// <param name="source">下载时表示服务器地址，上传时表示要上传的文件地址</param>
        /// <param name="target">下载时表示存储下载文件的本地地址，上传时表示要上传的服务器地址</param>
        /// <param name="type">传输的类型(上传或下载两种)</param>
        public void Cancel(String appWorkSpace,String source, String target, String type)
        {
            Pause(appWorkSpace,source);
            RemoveFileTranferTask(appWorkSpace,source);
            XFileTransferRecorder recorder = null;
            FileTransferRecorders.TryGetValue(appWorkSpace, out recorder);
            if (null == recorder)
                return;
            if (type.Equals(COMMAND_DOWNLOAD))
            {
                recorder.DeleteDownloadInfo(source);

                if (File.Exists(target))
                {
                    File.Delete(target);
                }
            }
        }

        /// <summary>
        /// 取消某个app中的所有文件传输任务
        /// </summary>
        /// <param name="appWorkSpace">指定app的WorkSpace</param>
        public void CancelAllByApp(String appWorkSpace)
        {
            // 卸载app时移除FileTransferManager中该app对应的Map<String, XIFileTransfer>
            Dictionary<String, XIFileTransfer> fileTransfers = null;
            HashMapFileTransfers.TryGetValue(appWorkSpace, out fileTransfers);
            if (null != fileTransfers)
            {
                HashMapFileTransfers.Remove(appWorkSpace);
            }

            // 卸载app时移除FileTransferManager中该app对应的XFileTransferRecorder
            FileTransferRecorders.Remove(appWorkSpace);
        }
    }
}
