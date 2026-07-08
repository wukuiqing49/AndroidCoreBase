# core_base shared keep rules
# Generic metadata is read by GenericTypeResolver at runtime.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# In R8 full mode, Signature is retained only on explicitly matched classes. These soft keep rules
# preserve generic metadata throughout the base Activity/Fragment hierarchy without making pages
# roots or preventing optimization, shrinking, or obfuscation.
-keep,allowoptimization,allowshrinking,allowobfuscation class com.wkq.base.activity.BaseActivity
-keep,allowoptimization,allowshrinking,allowobfuscation class * extends com.wkq.base.activity.BaseActivity
-keep,allowoptimization,allowshrinking,allowobfuscation class com.wkq.base.fragment.BaseFragment
-keep,allowoptimization,allowshrinking,allowobfuscation class * extends com.wkq.base.fragment.BaseFragment

# BaseActivity/BaseTitleActivity/BaseFragment resolve generated bindings from generic signatures and
# invoke inflate(...) by name. Binding classes must stay live and those overload names must not be
# changed, but unrelated generated members may still be optimized.
-keep,allowoptimization,allowobfuscation class * implements androidx.viewbinding.ViewBinding
-keepclassmembers,allowoptimization class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(android.view.LayoutInflater);
    public static *** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# BaseVM Activity/Fragment variants resolve ViewModel classes from generic signatures.
# ViewModelProvider constructs them indirectly, so keep the class and every supported constructor.
-keep,allowoptimization,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# DialogKit is the public dialog facade exported by core_base. XPopup is created from code, but
# keeping its runtime classes avoids integration issues when host apps enable aggressive shrinking.
-keep class com.lxj.xpopup.** { *; }
-dontwarn com.lxj.xpopup.**

# Keep dialog facade method names stable for Java callers and binary consumers of published AARs.
-keep class com.wkq.base.dialog.DialogKit { *; }
-keep class com.wkq.base.dialog.CommonDialog { *; }
-keep class com.wkq.base.dialog.LoadingDialog { *; }
-keep class com.wkq.base.dialog.PopupHandle { *; }
