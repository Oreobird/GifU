# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\AndroidSdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn okio.**
-dontwarn rx.internal.**

-keepattributes *Annotation*

-keep public class android.support.**{
    *;
}

-keep class com.alibaba.sdk.android.feedback.impl.FeedbackServiceImpl {*;}
-keep class com.alibaba.sdk.android.feedback.impl.FeedbackAPI {*;}
-keep class com.alibaba.sdk.android.feedback.util.IWxCallback {*;}
-keep class com.alibaba.sdk.android.feedback.util.IUnreadCountCallback{*;}
-keep class com.alibaba.sdk.android.feedback.FeedbackService{*;}
-keep public class com.alibaba.mtl.log.model.LogField {public *;}
-keep class com.taobao.securityjni.**{*;}
-keep class com.taobao.wireless.security.**{*;}
-keep class com.ut.secbody.**{*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}
-keep class com.ta.utdid2.device.**{*;}

-printmapping mapping.txt
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.taobao.android.**{*;}
-keep class com.ta.utdid2.device.**{*;}


# JavaCV
-keep @org.bytedeco.javacpp.annotation interface * {
    *;
}

-keep @org.bytedeco.javacpp.annotation.Platform public class *

-keepclasseswithmembernames class * {
    @org.bytedeco.* <fields>;
}

-keepclasseswithmembernames class * {
    @org.bytedeco.* <methods>;
}


-keep @interface org.bytedeco.javacpp.annotation.*,javax.inject.*

-keep class org.bytedeco.javacpp.** {*;}
-dontwarn java.awt.**
-dontwarn org.bytedeco.javacv.**
-dontwarn org.bytedeco.javacpp.**

# end javacv