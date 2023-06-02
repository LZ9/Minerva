# Minerva音频工具类

Minerva是一个便捷的音频工具，支持快速进行录音和VAD端点检测识别，并保存活动语音。内部采用协程进行异步操作，使用者只需订阅监听器回调实现自己的业务逻辑即可。

1. 我参考了[zhaolewei](https://github.com/zhaolewei)的[ZlwAudioRecorder](https://github.com/zhaolewei/ZlwAudioRecorder)库，将其转为了kotlin版本并进行了重构。 
2. 同时参考[gkonovalov](https://github.com/gkonovalov)的[android-vad](https://github.com/gkonovalov/android-vad)的端点检测库，将其转为了kotlin版本并扩展支持保存活动语音。

## 目录
- [1、引用方式](https://github.com/LZ9/Minerva#1引用方式)
- [2、使用教程](https://github.com/LZ9/Minerva#2使用教程)
- [3、展望](https://github.com/LZ9/Minerva#3展望)
- [扩展](https://github.com/LZ9/Minerva#扩展)

## 1、引用方式
由于jcenter删库跑路，请大家添加mavenCentral依赖
```
repositories {
    ...
    mavenCentral()
    ...
}
```
在你需要调用的module里的dependencies中加入以下依赖
```
implementation 'ink.lodz:minerva:1.0.1'
```

## 2、使用教程
### 录音
MinervaAgent.recording()支持PCM、WAV和MP3三种格式的录音，并通过监听器实时回调音频，支持外部进行音频可视化等操作。完整的调用方式如下：
```
val minerva = MinervaAgent.recording() //选择录音模式
    .setChannel(channel) //设置声道
    .setSampleRate(sampleRate) //设置采样率
    .setEncoding(encoding) //设置位宽编码
    .setAudioFormat(audioFormat) //设置音频格式
    .setSaveDirPath(filePath) //设置音频存储路径
    .setOnRecordingStatesListener{
        when (it) {
            is Idle -> {} //进入空闲状态
            is Recording -> {} //录音中，其中it.data是ShortArray格式的音频数据
            is Pause -> {} //录音暂停
            is Stop -> {} //录音停止
            is Finish -> {} //完成录音，其中it.file是保存的音频文件
            is Error -> {} //录音异常回调
            else -> {}
        }
    }
    .build(context)
```
在得到minerva对象后，可以调用以下方法：   
```
    minerva.changeAudioFormat(audioFormat) //改变音频格式
    minerva.changeSampleRate(sampleRate) //改变采样率
    minerva.changeEncoding(encoding) //改变位宽编码
    minerva.start() //启动录音
    minerva.stop() //停止录音
    minerva.pause() //暂停录音
    minerva.getRecordingState() //获取当前录音状态
```
- change相关的方法只能在录音未启动前调用，否则会回调Error错误
- 如果调用pause()后又直接调用stop()，则会在下次start()时把上一次录一半的音频临时文件删掉
- setSaveDirPath()方法一定要调用，否则初始化会抛异常

<div align="center">
    <img src="https://github.com/LZ9/Minerva/blob/master/img/recording/recording_config.jpg?raw=true" height="600"/>
</div>
<div align="center">
    <img src="https://github.com/LZ9/Minerva/blob/master/img/recording/recording.jpg?raw=true" height="600"/>
    <img src="https://github.com/LZ9/Minerva/blob/master/img/recording/recording_finish.jpg?raw=true" height="600"/>
</div>

### VAD端点检测
MinervaAgent.vad()支持进行端点检测，识别活动语音，并将活动语音保存为PCM或WAV格式的文件。
```
val minerva = MinervaAgent.vad() //选择端点检测模式
    .setChannel(channel) //设置声道
    .setSampleRate(sampleRate) //设置采样率
    .setAudioFormat(audioFormat) //设置音频格式
    .setSaveDirPath(filePath) //设置音频存储路径
    .setSaveActivityVoice(isSaveActiveVoice) //设置是否保存语音
    .setFrameSizeType(frameSizeType) //设置帧大小类型
    .setVadMode(vadMode) //设置检测模式
    .setFileMinSize(size) //设置音频文件大小最小判断值，根据实际业务需求调整
    .setSilenceValue(value) //设置活动语音的停顿长度阈值，一般不需要调整
    .setCacheCount(count) //设置语言存储开始前的缓存声音数量，一般不需要调整
    .setVadInterceptor { vad, buffer, end, db -> }//设置端点检测话音判断拦截器
    .setOnRecordingStatesListener{
        when (it) {
            is Idle -> {} //进入空闲状态
            is VadDetect -> {} //端点检测中，其中it.data是ShortArray格式的音频数据，it.db是音量，it.isSpeech是是否检测到活动语音
            is Stop -> {} //检测停止
            is VadFileSave -> {} //每保存一段活动语音会从该方法回调，it.file是本次保存的语音文件
            is Error -> {} //录音异常回调
            else -> {}
        }
    }
    .build(context)
```
在得到minerva对象后，可以调用以下方法：
```
    minerva.setVadConfig(vadConfig) //设置端点检测配置项
    minerva.changeAudioFormat(audioFormat) //改变音频格式
    minerva.changeSampleRate(sampleRate) //改变采样率
    minerva.changeFrameSizeType(frameSizeType) //改变帧大小类型
    minerva.changeVadMode(vadMode) //改变检测模式
    minerva.changeSaveActiveVoice(isSaveActiveVoice) //改变是否保存语音
    minerva.changeFileMinSize(size) //改变音频文件大小最小判断值，根据实际业务需求调整
    minerva.changeSilenceValue(value) //改变活动语音的停顿长度阈值，一般不需要调整
    minerva.changeCacheCount(count) //改变语言存储开始前的缓存声音数量，一般不需要调整
    minerva.start() //启动检测
    minerva.stop() //停止检测
    minerva.pause() //同stop()
    minerva.getRecordingState() //获取当前录音状态
```
- change相关的方法只能在录音未启动前调用，否则会回调Error错误
- 由于VAD只支持16BIT位宽，因此没有修改位宽编码的方法
- 若要保存活动语音，则setSaveDirPath()方法一定要设置，否则初始化会抛异常
- 端点检测只支持PCM和WAV，不支持MP3格式，因此在调用setAudioFormat()方法时，不要传入MP3，否则会抛异常
- VAD默认不保存活动语音，如需保存，请调用setSaveActivityVoice()方法打开
- VAD的采样率目前仅支持8000、16000、32000和48000，若在setSampleRate()里传入其他采样率，系统会抛异常
- 如果担心保存到杂音，可在setFileMinSize()设置最小音频文件值，如果保存的文件小于该值，则会把音频删除
- 用户说一句话时可能中间会有一些停顿，通过setSilenceValue()增大活动语音的停顿长度阈值，也能一定程度兼容停顿保存为一段完整活动音频
- 用户开始说话到端点检测识别到会存在一些时延，通过调用setCacheCount()方法，可以提前缓存一部分语音数据，当检测发生时，写入这部分语音数据，确保一句话的完整性，避免开头部分被截断。
- 如果觉得VAD太过灵敏，可以调用setOnRecordingStatesListener{}方法，在里面增加分贝大小的判断，例如端点检测识别成功且声音大于40分贝再响应vad.isSpeech(buffer) && db > 40

<div align="center">
    <img src="https://github.com/LZ9/Minerva/blob/master/img/vad/vad_config.jpg?raw=true" height="600"/>
    <img src="https://github.com/LZ9/Minerva/blob/master/img/vad/vad_no_speech.jpg?raw=true" height="600"/>
</div>
<div align="center">
    <img src="https://github.com/LZ9/Minerva/blob/master/img/vad/vad_speech.jpg?raw=true" height="600"/>
    <img src="https://github.com/LZ9/Minerva/blob/master/img/vad/vad_save_file.jpg?raw=true" height="600"/>
</div>

## 3、展望
- 增加一个类微信的语音输入的封装
- 探索在vad进行活动语音保存时的回声消除方案
- 增加一个音频可视化的demo

## 扩展

- [更新记录](https://github.com/LZ9/Minerva/blob/master/minerva/readme_update.md)
- [回到顶部](https://github.com/LZ9/Minerva#minerva音频工具类)

## License
- [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Copyright 2022 Lodz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
