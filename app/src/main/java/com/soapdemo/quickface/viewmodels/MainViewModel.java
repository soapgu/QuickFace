package com.soapdemo.quickface.viewmodels;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.lifecycle.MutableLiveData;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.soapdemo.quickface.activitys.RecognizeActivity;
import com.soapdemo.quickface.util.ErrorCodeUtil;
import com.soapdemo.quickface.util.Execute;
import com.soapdemo.quickface.util.MessageHelper;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends ObservableViewModel {
    private final String DEFAULT_AUTH_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "active_result.dat";
    private String activateStatus;
    private MutableLiveData<Class<?>> targetActivity;


    /**
     * 离线激活所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS_OFFLINE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};

    public MainViewModel(@NonNull Application application) {
        super(application);
        String activeMsg = checkPermissions(NEEDED_PERMISSIONS) ? ( isActivated()  ? "已激活": "未激活" ) : "READ_PHONE_STATE 权限未获取";
        String ip = getLocalIpAddress();
        this.setActivateStatus( String.format( "IP:%s 状态:%s" ,  ip, activeMsg ) );
    }

    public MutableLiveData<Class<?>> getTargetActivity() {
        if( targetActivity == null )
            targetActivity = new MutableLiveData<>();
        return targetActivity;
    }

    @Bindable
    public String getActivateStatus() {
        return activateStatus;
    }

    public void setActivateStatus(String activateStatus) {
        this.activateStatus = activateStatus;
        this.notifyPropertyChanged(BR.activateStatus);
    }

    private boolean isActivated() {
        return FaceEngine.getActiveFileInfo(this.getApplication(), new ActiveFileInfo()) == ErrorInfo.MOK;
    }

    public void ActiveOffline()
    {
        if (checkPermissions(NEEDED_PERMISSIONS_OFFLINE)) {
            Execute.getInstance().BeginOnSubThread( ()->{
                int result = FaceEngine.activeOffline(this.getApplication(), DEFAULT_AUTH_FILE_PATH);
                String notice;
                switch (result) {
                    case ErrorInfo.MOK:
                        notice = "成功";
                        break;
                    case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED:
                        notice = "已激活";
                        break;
                    case ErrorInfo.MERR_ASF_ACTIVEKEY_ACTIVEKEY_ACTIVATED:
                        notice = "该激活码已被其他设备使用";
                        break;
                    default:
                        notice = "其他错误" + ErrorCodeUtil.arcFaceErrorCodeToFieldName(result);
                        break;
                }
                MessageHelper.ShowToast(this.getApplication(), notice);
            } );
        }
        else {
            MessageHelper.ShowToast( this.getApplication(),"No Auth!!!!");
        }
    }

    public void GoToRecognizeActivity()
    {
        this.targetActivity.setValue(RecognizeActivity.class);
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 是否全部被允许
     */
    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplication(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    /**
     * 获取本地IP地址
     *
     * @return IP String
     */
    private static String getLocalIpAddress() {
        try {
            String ipv4;
            List<NetworkInterface> nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nilist) {
                List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress()) {
                        ipv4 = address.getHostAddress();
                        return ipv4;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
