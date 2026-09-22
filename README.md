# 教师课程表 TeacherTimetable

一个面向教师的 Android 课程表 App，支持：

- 每周循环排课、早读 / 午练 / 晚自习等特殊时段、单双周、周次范围
- 单次调课 / 取消（只影响某一周）
- 班级总备忘录 + 单次课程备忘录（折叠展示），支持拍照 + 手绘标注
- 课前提醒：通知 / 闹钟（仅震动）、全局默认或单课程覆盖、提前时间自由设置
- 开机自动重排提醒（WorkManager + AlarmManager）

## 技术栈

Kotlin + Jetpack Compose + Room + DataStore + AlarmManager + WorkManager + Coil

## 快速开始

1. 用 Android Studio（Hedgehog 或更新版本）打开本目录
2. 等待 Gradle Sync 完成
3. 连接设备或启动模拟器，点击 Run

## 打包 APK

见项目根目录说明或 Android Studio 菜单：
Build -> Build Bundle(s) / APK(s) -> Build APK(s)
