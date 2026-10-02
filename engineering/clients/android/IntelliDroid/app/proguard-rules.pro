# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\CODE\android-sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-injars      libs
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*
# Obfuscation parameters:
#-dontobfuscate
-useuniqueclassmembernames
-keepattributes SourceFile,LineNumberTable
-allowaccessmodification

# Keep INTELLIBITZ
-keep class intellibitz.** { *; }
-keepclasseswithmembers class * {
    @intellibitz.** *;
}
-keepclassmembers class * {
    @intellibitz.** *;
}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }

-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keepnames class com.google.firebase.** { *; }
-keepclassmembers class * {@com.google.firebase.** *;}
-keepclasseswithmembers class * {@com.google.firebase.** *;}


#If you're using additional Google APIs, you'll have to specify those as well, for instance:
# -libraryjars /usr/local/android-sdk/add-ons/google_apis-7_r01/libs/maps.jar

#If you're using Google's optional License Verification Library, you can obfuscate its code along
#with your own code. You do have to preserve its ILicensingService interface for the library to work:
# -keep public interface com.android.vending.licensing.ILicensingService

#If you're using the Android Compatibility library, you should add the following line,
# to let ProGuard know it's ok that the library references some classes that are not available in
# all versions of the API:
-dontwarn android.support.**
-dontwarn com.google.android.**
-dontwarn com.google.firebase.**


-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

# Only necessary if you downloaded the SDK jar directly instead of from maven.
-keep class com.shaded.fasterxml.jackson.** { *; }
-keep class com.amazonaws.** { *; }
-keep interface com.amazonaws.** { *; }
-keepnames class com.amazonaws.** { *; }
-keepclassmembers class * {@com.amazonaws.** *;}
-keepclasseswithmembers class * {@com.amazonaws.** *;}
-dontwarn com.amazonaws.**
-dontwarn com.fasterxml.**

# Ignore warnings:
-dontwarn org.mockito.**
-dontwarn org.junit.**
-dontwarn com.robotium.**
-dontwarn org.joda.convert.**

# Ignore warnings: We are not using DOM model
-dontwarn com.fasterxml.jackson.databind.ext.DOMSerializer
# Ignore warnings: https://github.com/square/okhttp/wiki/FAQs
-dontwarn com.squareup.okhttp.internal.huc.**
# Ignore warnings: https://github.com/square/okio/issues/60
-dontwarn okio.**
# Ignore warnings: https://github.com/square/retrofit/issues/435
-dontwarn com.google.appengine.api.urlfetch.**

# Keep the pojos used by GSON or Jackson
-keep class com.futurice.project.models.pojo.** { *; }

# Keep GSON stuff
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep Jackson stuff
-keep class org.codehaus.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }

# Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Keep Retrofit
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.** *;
}
-keepclassmembers class * {
    @retrofit.** *;
}

# Keep Picasso
-keep class com.squareup.picasso.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.picasso.** *;
}
-keepclassmembers class * {
    @com.squareup.picasso.** *;
}
