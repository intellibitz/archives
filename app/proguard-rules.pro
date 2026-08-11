# Blukit Production ProGuard Rules

# Google Nearby Connections
-keep class com.google.android.gms.nearby.** { *; }
-keep interface com.google.android.gms.nearby.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class *_*Impl { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# AndroidX Navigation
-keep class androidx.navigation.** { *; }

# Datastore & Security
-keep class androidx.datastore.** { *; }
-keep class androidx.security.crypto.** { *; }

# Internal Crypto & Networking (Ensure handshakes work)
-keep class cc.thevar.blukit.data.crypto.** { *; }
-keep class cc.thevar.blukit.network.p2p.** { *; }
-keep class cc.thevar.blukit.domain.model.** { *; }

# Android Keystore
-keep class android.security.keystore.** { *; }
