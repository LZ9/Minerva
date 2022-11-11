package com.lodz.android.minerva;


import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.lodz.android.minerva.recorder.RecordConfig;
import com.lodz.android.minerva.recorder.RecordService;
import com.lodz.android.minerva.bean.AudioFormats;
import com.lodz.android.minerva.recorder.RecordingState;
import com.lodz.android.minerva.contract.OnRecordingDataListener;
import com.lodz.android.minerva.contract.OnRecordingFftDataListener;
import com.lodz.android.minerva.contract.OnRecordingFinishListener;
import com.lodz.android.minerva.contract.OnRecordingSoundSizeListener;
import com.lodz.android.minerva.contract.OnRecordingStateListener;

/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordManager {
    private static final String TAG = RecordManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static RecordManager instance;
    private Application context;

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        if (instance == null) {
            synchronized (RecordManager.class) {
                if (instance == null) {
                    instance = new RecordManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param application Application
     */
    public void init(Application application) {
        this.context = application;
    }

    public void start() {
        if (context == null) {
            Log.e(TAG, "未进行初始化");
            return;
        }
        Log.i(TAG, "start...");
        RecordService.startRecording(context);
    }

    public void stop() {
        if (context == null) {
            return;
        }
        RecordService.stopRecording(context);
    }

    public void resume() {
        if (context == null) {
            return;
        }
        RecordService.resumeRecording(context);
    }

    public void pause() {
        if (context == null) {
            return;
        }
        RecordService.pauseRecording(context);
    }

    /**
     * 录音状态监听回调
     */
    public void setOnRecordingStateListener(OnRecordingStateListener listener) {
        RecordService.setOnRecordingStateListener(listener);
    }

    /**
     * 录音数据监听回调
     */
    public void setOnRecordingDataListener(OnRecordingDataListener listener) {
        RecordService.setOnRecordingDataListener(listener);
    }

    /**
     * 录音可视化数据回调，傅里叶转换后的频域数据
     */
    public void setOnRecordingFftDataListener(OnRecordingFftDataListener recordFftDataListener) {
        RecordService.setOnRecordingFftDataListener(recordFftDataListener);
    }

    /**
     * 录音文件转换结束回调
     */
    public void setOnRecordingFinishListener(OnRecordingFinishListener listener) {
        RecordService.setOnRecordingFinishListener(listener);
    }

    /**
     * 录音音量监听回调
     */
    public void setOnRecordingSoundSizeListener(OnRecordingSoundSizeListener listener) {
        RecordService.setOnRecordingSoundSizeListener(listener);
    }


    public boolean changeFormat(AudioFormats recordFormat) {
        return RecordService.changeFormat(recordFormat);
    }


    public boolean changeRecordConfig(RecordConfig recordConfig) {
        return RecordService.changeRecordConfig(recordConfig);
    }

    public RecordConfig getRecordConfig() {
        return RecordService.getRecordConfig();
    }

    /**
     * 修改录音文件存放路径
     */
    public void changeRecordDir(String recordDir) {
        RecordService.changeRecordDir(recordDir);
    }

    /**
     * 获取当前的录音状态
     *
     * @return 状态
     */
    public RecordingState getState() {
        return RecordService.getState();
    }

}
