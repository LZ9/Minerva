package com.lodz.android.minervademo.ui;

import android.media.AudioFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lodz.android.minerva.RecordManager;
import com.lodz.android.minerva.bean.AudioFormats;
import com.lodz.android.minerva.contract.OnRecordingFftDataListener;
import com.lodz.android.minerva.contract.OnRecordingFinishListener;
import com.lodz.android.minerva.contract.OnRecordingSoundSizeListener;
import com.lodz.android.minervademo.App;
import com.lodz.android.minervademo.R;
import com.lodz.android.minervademo.utils.FileManager;
import com.lodz.android.minervademo.widget.AudioView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.util.Locale;

/**
 * @author zhouL
 * @date 2021/10/18
 */
public class MainActivity2  extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = MainActivity2.class.getSimpleName();

    private Button btRecord;
    private Button btStop;
    private TextView tvState;
    private TextView tvSoundSize;
    private RadioGroup rgAudioFormat;
    private RadioGroup rgSimpleRate;
    private RadioGroup tbEncoding;
    private AudioView audioView;
    private Spinner spUpStyle;
    private Spinner spDownStyle;
    private Button jumpTestActivity;

    private boolean isStart = false;
    private boolean isPause = false;
    final RecordManager recordManager = RecordManager.getInstance();
    private static final String[] STYLE_DATA = new String[]{"STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btRecord = findViewById(R.id.btRecord);
        btStop = findViewById(R.id.btStop);
        tvState = findViewById(R.id.tvState);
        tvSoundSize = findViewById(R.id.tvSoundSize);
        rgAudioFormat = findViewById(R.id.rgAudioFormat);
        rgSimpleRate = findViewById(R.id.rgSimpleRate);
        tbEncoding = findViewById(R.id.tbEncoding);
        audioView = findViewById(R.id.audioView);
        spUpStyle = findViewById(R.id.spUpStyle);
        spDownStyle = findViewById(R.id.spDownStyle);
        jumpTestActivity = findViewById(R.id.jumpTestActivity);
        initAudioView();
        initEvent();
        initRecord();
        onViewClicked();
        AndPermission.with(this)
                .runtime()
                .permission(new String[]{Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO})
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doStop();
        initRecordEvent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doStop();
    }

    private void initAudioView() {
        tvState.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, STYLE_DATA);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUpStyle.setAdapter(adapter);
        spDownStyle.setAdapter(adapter);
        spUpStyle.setOnItemSelectedListener(this);
        spDownStyle.setOnItemSelectedListener(this);
    }

    private void initEvent() {
        rgAudioFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbPcm:
                        recordManager.changeFormat(AudioFormats.PCM);
                        break;
                    case R.id.rbMp3:
                        recordManager.changeFormat(AudioFormats.MP3);
                        break;
                    case R.id.rbWav:
                        recordManager.changeFormat(AudioFormats.WAV);
                        break;
                    default:
                        break;
                }
            }
        });

        rgSimpleRate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb8K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(8000));
                        break;
                    case R.id.rb16K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(16000));
                        break;
                    case R.id.rb44K:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setSampleRate(44100));
                        break;
                    default:
                        break;
                }
            }
        });

        tbEncoding.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb8Bit:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT));
                        break;
                    case R.id.rb16Bit:
                        recordManager.changeRecordConfig(recordManager.getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initRecord() {
        recordManager.init(App.get());
        recordManager.changeFormat(AudioFormats.WAV);
        String recordDir = FileManager.getContentFolderPath();
        recordManager.changeRecordDir(recordDir);
        initRecordEvent();
    }

    private void initRecordEvent() {
//        recordManager.setOnRecordingStateListener(new OnRecordingStateListener() {
//            @Override
//            public void onStateChange(RecordingState state) {
//                Log.i(TAG, "onStateChange %s" + state.name());
//
//                switch (state) {
//                    case PAUSE:
//                        tvState.setText("暂停中");
//                        break;
//                    case IDLE:
//                        tvState.setText("空闲中");
//                        break;
//                    case RECORDING:
//                        tvState.setText("录音中");
//                        break;
//                    case STOP:
//                        tvState.setText("停止");
//                        break;
//                    case FINISH:
//                        tvState.setText("录音结束");
//                        tvSoundSize.setText("---");
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//            @Override
//            public void onError(String error) {
//                Log.i(TAG, "onError %s" + error);
//            }
//        });
        recordManager.setOnRecordingSoundSizeListener(new OnRecordingSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                tvSoundSize.setText(String.format(Locale.getDefault(), "声音大小：%s db", soundSize));
            }
        });
        recordManager.setOnRecordingFinishListener(new OnRecordingFinishListener() {
            @Override
            public void onFinish(@NonNull File result) {
                Toast.makeText(MainActivity2.this, "录音文件： " + result.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        });
        recordManager.setOnRecordingFftDataListener(new OnRecordingFftDataListener() {
            @Override
            public void onFftData(@NonNull byte[] data) {
                audioView.setWaveData(data);
            }
        });
    }

    public void onViewClicked() {
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPlay();
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop();
            }
        });

        jumpTestActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity2.this, TestHzActivity.class));
            }
        });
    }

    private void doStop() {
        recordManager.stop();
        btRecord.setText("开始");
        isPause = false;
        isStart = false;
    }

    private void doPlay() {
        if (isStart) {
            recordManager.pause();
            btRecord.setText("开始");
            isPause = true;
            isStart = false;
        } else {
            if (isPause) {
                recordManager.resume();
            } else {
                recordManager.start();
            }
            btRecord.setText("暂停");
            isStart = true;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spUpStyle:
                audioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[position]), audioView.getDownStyle());
                break;
            case R.id.spDownStyle:
                audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[position]));
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing
    }
}
