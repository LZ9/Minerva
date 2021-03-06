package com.lodz.android.minerva.recorder;

import android.Manifest;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.lodz.android.minerva.fftlib.FftFactory;
import com.lodz.android.minerva.mp3.Mp3EncodeThread;
import com.lodz.android.minerva.contract.OnRecordingDataListener;
import com.lodz.android.minerva.contract.OnRecordingFftDataListener;
import com.lodz.android.minerva.contract.OnRecordingFinishListener;
import com.lodz.android.minerva.contract.OnRecordingSoundSizeListener;
import com.lodz.android.minerva.contract.OnRecordingStateListener;
import com.lodz.android.minerva.utils.FileUtils;
import com.lodz.android.minerva.wav.WavUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * @author zhaolewei on 2018/7/10.
 */
public class RecordHelper {
    private static final String TAG = RecordHelper.class.getSimpleName();
    private volatile static RecordHelper instance;
    private volatile RecordingState state = RecordingState.IDLE;
    private static final int RECORD_AUDIO_BUFFER_TIMES = 1;

    private OnRecordingStateListener mOnRecordingStateListener;
    private OnRecordingDataListener mOnRecordingDataListener;
    private OnRecordingSoundSizeListener mOnRecordingSoundSizeListener;
    private OnRecordingFinishListener mOnRecordingFinishListener;
    private OnRecordingFftDataListener mOnRecordingFftDataListener;
    private RecordConfig currentConfig;
    private AudioRecordThread audioRecordThread;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private File resultFile = null;
    private File tmpFile = null;
    private List<File> files = new ArrayList<>();
    private Mp3EncodeThread mp3EncodeThread;

    private RecordHelper() {
    }

    static RecordHelper getInstance() {
        if (instance == null) {
            synchronized (RecordHelper.class) {
                if (instance == null) {
                    instance = new RecordHelper();
                }
            }
        }
        return instance;
    }

    RecordingState getState() {
        return state;
    }

    void setOnRecordingStateListener(OnRecordingStateListener recordStateListener) {
        this.mOnRecordingStateListener = recordStateListener;
    }

    void setOnRecordingDataListener(OnRecordingDataListener recordDataListener) {
        this.mOnRecordingDataListener = recordDataListener;
    }

    void setOnRecordingSoundSizeListener(OnRecordingSoundSizeListener recordSoundSizeListener) {
        this.mOnRecordingSoundSizeListener = recordSoundSizeListener;
    }

    void setOnRecordingFinishListener(OnRecordingFinishListener recordResultListener) {
        this.mOnRecordingFinishListener = recordResultListener;
    }

    public void setOnRecordingFftDataListener(OnRecordingFftDataListener recordFftDataListener) {
        this.mOnRecordingFftDataListener = recordFftDataListener;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void start(String filePath, RecordConfig config) {
        this.currentConfig = config;
        if (state != RecordingState.IDLE && state != RecordingState.STOP) {
            Log.e(TAG, "状态异常当前状态： " + state.name());
            return;
        }
        resultFile = new File(filePath);
        String tempFilePath = getTempFilePath();

        Log.d(TAG, "----------------开始录制 ------------------------"+ currentConfig.getFormat().name());
        Log.d(TAG, "参数： "+ currentConfig.toString());
        Log.i(TAG, "pcm缓存 tmpFile: "+ tempFilePath);
        Log.i(TAG, "录音文件 resultFile: "+ filePath);


        tmpFile = new File(tempFilePath);
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    public void stop() {
        if (state == RecordingState.IDLE) {
            Log.e(TAG, "状态异常当前状态： "+ state.name());
            return;
        }

        if (state == RecordingState.PAUSE) {
            makeFile();
            state = RecordingState.IDLE;
            notifyState();
            stopMp3Encoded();
        } else {
            state = RecordingState.STOP;
            notifyState();
        }
    }

    void pause() {
        if (state != RecordingState.RECORDING) {
            Log.e(TAG, "状态异常当前状态： "+ state.name());
            return;
        }
        state = RecordingState.PAUSE;
        notifyState();
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    void resume() {
        if (state != RecordingState.PAUSE) {
            Log.e(TAG, "状态异常当前状态： "+ state.name());
            return;
        }
        String tempFilePath = getTempFilePath();
        Log.i(TAG, "tmpPCM File: "+ tempFilePath);
        tmpFile = new File(tempFilePath);
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    private void notifyState() {
        if (mOnRecordingStateListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnRecordingStateListener.onStateChange(state);
            }
        });

        if (state == RecordingState.STOP || state == RecordingState.PAUSE) {
            if (mOnRecordingSoundSizeListener != null) {
                mOnRecordingSoundSizeListener.onSoundSize(0);
            }
        }
    }

    private void notifyFinish() {
        Log.d(TAG, "录音结束 file: "+ resultFile.getAbsolutePath());

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnRecordingStateListener != null) {
                    mOnRecordingStateListener.onStateChange(RecordingState.FINISH);
                }
                if (mOnRecordingFinishListener != null) {
                    mOnRecordingFinishListener.onFinish(resultFile);
                }
            }
        });
    }

    private void notifyError(final String error) {
        if (mOnRecordingStateListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mOnRecordingStateListener.onError(error);
            }
        });
    }

    private FftFactory fftFactory = new FftFactory(FftFactory.Level.Original);

    private void notifyData(final byte[] data) {
        if (mOnRecordingDataListener == null && mOnRecordingSoundSizeListener == null && mOnRecordingFftDataListener == null) {
            return;
        }
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnRecordingDataListener != null) {
                    mOnRecordingDataListener.onData(data);
                }

                if (mOnRecordingFftDataListener != null || mOnRecordingSoundSizeListener != null) {
                    byte[] fftData = fftFactory.makeFftData(data);
                    if (fftData != null) {
                        if (mOnRecordingSoundSizeListener != null) {
                            mOnRecordingSoundSizeListener.onSoundSize(getDb(fftData));
                        }
                        if (mOnRecordingFftDataListener != null) {
                            mOnRecordingFftDataListener.onFftData(fftData);
                        }
                    }
                }
            }
        });
    }

    private int getDb(byte[] data) {
        double sum = 0;
        double ave;
        int length = Math.min(data.length, 128);
        int offsetStart = 0;
        for (int i = offsetStart; i < length; i++) {
            sum += data[i] * data[i];
        }
        ave = sum / (length - offsetStart) ;
        return (int) (Math.log10(ave) * 20);
    }

    private void initMp3EncoderThread(int bufferSize) {
        try {
            mp3EncodeThread = new Mp3EncodeThread(resultFile, bufferSize);
            mp3EncodeThread.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private class AudioRecordThread extends Thread {
        private AudioRecord audioRecord;
        private int bufferSize;

        @RequiresPermission(Manifest.permission.RECORD_AUDIO)
        AudioRecordThread() {
            bufferSize = AudioRecord.getMinBufferSize(currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig()) * RECORD_AUDIO_BUFFER_TIMES;
            Log.d(TAG, "record buffer size = "+ bufferSize);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, currentConfig.getSampleRate(),
                    currentConfig.getChannelConfig(), currentConfig.getEncodingConfig(), bufferSize);
            if (currentConfig.getFormat() == RecordingFormat.MP3) {
                if (mp3EncodeThread == null) {
                    initMp3EncoderThread(bufferSize);
                } else {
                    Log.e(TAG, "mp3EncodeThread != null, 请检查代码");
                }
            }
        }

        @Override
        public void run() {
            super.run();

            switch (currentConfig.getFormat()) {
                case MP3:
                    startMp3Recorder();
                    break;
                default:
                    startPcmRecorder();
                    break;
            }
        }

        private void startPcmRecorder() {
            state = RecordingState.RECORDING;
            notifyState();
            Log.d(TAG, "开始录制 Pcm");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tmpFile);
                audioRecord.startRecording();
                byte[] byteBuffer = new byte[bufferSize];

                while (state == RecordingState.RECORDING) {
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    notifyData(byteBuffer);
                    fos.write(byteBuffer, 0, end);
                    fos.flush();
                }
                audioRecord.stop();
                files.add(tmpFile);
                if (state == RecordingState.STOP) {
                    makeFile();
                } else {
                    Log.i(TAG, "暂停！");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                notifyError("录音失败");
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (state != RecordingState.PAUSE) {
                state = RecordingState.IDLE;
                notifyState();
                Log.d(TAG, "录音结束");
            }
        }

        private void startMp3Recorder() {
            state = RecordingState.RECORDING;
            notifyState();

            try {
                audioRecord.startRecording();
                short[] byteBuffer = new short[bufferSize];

                while (state == RecordingState.RECORDING) {
                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
                    if (mp3EncodeThread != null) {
                        mp3EncodeThread.addChangeBuffer(new Mp3EncodeThread.ChangeBuffer(byteBuffer, end));
                    }
                    notifyData(toByteArray(byteBuffer));
                }
                audioRecord.stop();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                notifyError("录音失败");
            }
            if (state != RecordingState.PAUSE) {
                state = RecordingState.IDLE;
                notifyState();
                stopMp3Encoded();
            } else {
                Log.d(TAG, "暂停");
            }
        }
    }

    /**
     * short[] 转 byte[]
     */
    private byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }

        return dest;
    }

    private void stopMp3Encoded() {
        if (mp3EncodeThread != null) {
            mp3EncodeThread.stopSafe(new Mp3EncodeThread.EncordFinishListener() {
                @Override
                public void onFinish() {
                    notifyFinish();
                    mp3EncodeThread = null;
                }
            });
        } else {
            Log.e(TAG, "mp3EncodeThread is null, 代码业务流程有误，请检查！！ ");
        }
    }

    private void makeFile() {
        switch (currentConfig.getFormat()) {
            case MP3:
                return;
            case WAV:
                mergePcmFile();
                makeWav();
                break;
            case PCM:
                mergePcmFile();
                break;
            default:
                break;
        }
        notifyFinish();
        Log.i(TAG, "录音完成！ path: "+resultFile.getAbsoluteFile()+" ； 大小："+ resultFile.getAbsoluteFile()+ resultFile.length());
    }

    /**
     * 添加Wav头文件
     */
    private void makeWav() {
        if (!FileUtils.isFile(resultFile) || resultFile.length() == 0) {
            return;
        }
        byte[] header = WavUtils.generateHeader((int) resultFile.length(), currentConfig.getSampleRate(), (short) currentConfig.getChannelCount(), (short) currentConfig.getEncoding());
        WavUtils.writeHeader(resultFile, header);
    }

    /**
     * 合并文件
     */
    private void mergePcmFile() {
        boolean mergeSuccess = mergePcmFiles(resultFile, files);
        if (!mergeSuccess) {
            notifyError("合并失败");
        }
    }

    /**
     * 合并Pcm文件
     *
     * @param recordFile 输出文件
     * @param files      多个文件源
     * @return 是否成功
     */
    private boolean mergePcmFiles(File recordFile, List<File> files) {
        if (recordFile == null || files == null || files.size() <= 0) {
            return false;
        }

        FileOutputStream fos = null;
        BufferedOutputStream outputStream = null;
        byte[] buffer = new byte[1024];
        try {
            fos = new FileOutputStream(recordFile);
            outputStream = new BufferedOutputStream(fos);

            for (int i = 0; i < files.size(); i++) {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(files.get(i)));
                int readCount;
                while ((readCount = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readCount);
                }
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < files.size(); i++) {
            files.get(i).delete();
        }
        files.clear();
        return true;
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private String getTempFilePath() {
//        String fileDir = String.format(Locale.getDefault(), "%s/Record/", Environment.getExternalStorageDirectory().getAbsolutePath());
//        if (!FileUtils.createOrExistsDir(fileDir)) {
//            Log.e(TAG, "文件夹创建失败：%s", fileDir);
//        }
        String fileName = String.format(Locale.getDefault(), "record_tmp_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s.pcm", resultFile, fileName);
    }

}
