# President Simulator — R8 configuration
# Kotlinx-serialization: keep generated serializers for all @Serializable models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.presidentsimulator.game.**$$serializer { *; }
-keepclassmembers class com.presidentsimulator.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.presidentsimulator.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coil works out of the box; no rules needed.
