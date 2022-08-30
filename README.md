# Midi_Player

请使用com.genshin.Playback

使用纯HashMap处理消息 不在原序列上处理

完美避免了```数据字节必须在 0..127 范围内```的异常问题（适用于黑乐谱MIDI）

在处理消息阶段就拦截所有的元消息Tempo，自适应任意变化bpm的MIDI（不会出现一段非常快，一段非常慢的情况）

**下载并安装 [JDK8](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)**

1. 导入Maven工程

2. 编译项目，运行com.genshin.Playback.main()

或者

下载根目录的play.jar和run.bat到D:\下的任意位置 直接运行run.bat
