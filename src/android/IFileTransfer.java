package com.polyvi.xface.extension.advancedfiletransfer;

import org.apache.cordova.CallbackContext;

public interface IFileTransfer {

    public void transfer(CallbackContext callbackContext);

    public void pause();

}
