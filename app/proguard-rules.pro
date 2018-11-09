#指定代码的压缩级别  压缩比率 0 ~ 7
-optimizationpasses 5

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

#包明不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses

#不优化输入的类文件
-dontoptimize

#预校验
-dontpreverify

#混淆时是否记录日志
-verbose

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.** #忽略警告

# 混淆时所采用的算法
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

#保护注解
-keepattributes *Annotation*

#忽略警告
-ignorewarnings

##记录生成的日志数据,gradle build时在本项目根目录输出##

#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt

########记录生成的日志数据，gradle build时 在本项目根目录输出-end######

# 保持哪些类不被混淆
-dontwarn android.app.**
-keep class android.app.** {*;}

#-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends com.unity3d.player.UnityPlayerActivity
-keep public class * extends android.app.Service
-keep public class * extends android.app.View
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.vending.licensing.ILicensingService

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

#如果有引用v4包可以添加下面这行
#-keep public class * extends android.support.v4.app.Fragment
#-keep class android.support.v4.** {*;}
-dontwarn android.support.v4.**
-keep class android.support.v4.** {*;}

-keep class android.support.v7.widget.RoundRectDrawable { *; }
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
#-keep class android.support.v7.** {*;}

-keepattributes InnerClasses,SourceFile,LineNumberTable,*Annotation*

-keep public @interface *

-keep public class * extends com.xiaoenai.app.annotation.json.BaseJsonModel {
    *;
}

-keep class android.net.http.SslError
-keep class android.webkit.**{*;}
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
#-keep class m.framework.**{*;}

-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void openFileChooser(...);
}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class cn.sharesdk.R {*;}
-keep class cn.sharesdk.R$* {*;}
-keep public class com.xiaoenai.app.R$*{
    public static final int *;
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

#EventBus 混淆配置
#keep 定义的事件接口
-keepnames interface * implements com.shizhefei.eventbus.IEvent
#keep apt生成定义的事件接口的Proxy类
-keepnames class **.*Proxy implements com.shizhefei.eventbus.IEvent