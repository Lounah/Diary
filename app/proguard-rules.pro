
-keepattributes Exceptions
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3. { *; }
-keep interface okhttp3. { *; }
-dontwarn okhttp3.
-dontnote okhttp3.
-dontwarn okhttp3.internal.platform.*
#-keep class com.lounah.yarealty.data.entity. { *; }
# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream. { *; }
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn javax.annotation.concurrent.GuardedBy

-dontwarn com.google.errorprone.annotations.**

#-keep class com.lounah.yarealty.data.entity.** { *; }
#-keep class com.lounah.yarealty.data.network.** { *; }

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}