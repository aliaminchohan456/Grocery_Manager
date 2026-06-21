# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.example.grocerymanager.**$$serializer { *; }
-keepclassmembers class com.example.grocerymanager.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.grocerymanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}
