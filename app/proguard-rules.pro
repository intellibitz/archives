# Blukit Production ProGuard Rules

# Google Nearby Connections
-keep class com.google.android.gms.nearby.** { <fields>; <methods>; }
-keep interface com.google.android.gms.nearby.** { *; }


# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# AndroidX Navigation
-keep class androidx.navigation.** { *; }

# Security
-keep class androidx.security.crypto.** { *; }

# Internal Crypto & Networking (Ensure handshakes work)
-keep class cc.thevar.blukit.data.crypto.** { *; }
-keep class cc.thevar.blukit.network.p2p.** { *; }
-keep class cc.thevar.blukit.domain.model.** { <fields>; <methods>; }

# Android Keystore
-keep class android.security.keystore.** { *; }
