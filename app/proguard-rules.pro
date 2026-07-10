# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Kotlinx Serialization (playlist persistence)
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keep,includedescriptorclasses class com.kaze.player.data.model.Playlist
-keepclassmembers class com.kaze.player.data.model.Playlist {
    *** Companion;
}
-keepclasseswithmembers class com.kaze.player.data.model.Playlist {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**
