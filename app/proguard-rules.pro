# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
 -keepclassmembers class * extends androidx.viewbinding.ViewBinding {
   public static *** bind(android.view.View);
}

# ViewBindingKotlinModel finds the binding class from its subclasses' generic superclass
# (ViewBindingKotlinModel<FooBinding>). R8 full mode drops generic signatures of classes that are
# not kept, which made the release build crash on launch (card #66).
-keepattributes Signature
-keep,allowobfuscation,allowoptimization class * extends com.bumbumapps.studytextscan.epoxy.ViewBindingKotlinModel
-keep,allowobfuscation,allowoptimization class com.bumbumapps.studytextscan.epoxy.ViewBindingKotlinModel

# Huawei Ads (hms flavor)
-keep class com.huawei.openalliance.ad.** { *; }
-keep class com.huawei.hms.ads.** { *; }
-dontwarn com.huawei.**
