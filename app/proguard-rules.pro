# Keyboard HTML release rules (R8).

# The JS<->Android bridge is looked up at runtime by the web layer;
# reflection keeps methods alive only if we do. Without this rule R8
# strips the methods and the keyboard stops sending events silently.
-keep class com.keyboardhtml.KeyboardBridge { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Gson is used through the tree model (JsonParser/JsonElement), which R8
# follows statically; these guards cover reflection-based internals that
# may kick in when manifest fields evolve.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.annotations.SerializedName { *; }
-keepattributes Signature, *Annotation*
