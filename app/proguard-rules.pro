# Demo app keeps its own ViewBinding entry points because BaseActivity resolves generated binding
# inflate(...) methods by reflection. core_base exports the same rules for host apps via
# consumer-rules.pro; keeping them here makes minified demo builds self-explanatory.
-keep,allowoptimization,allowobfuscation class * implements androidx.viewbinding.ViewBinding
-keepclassmembers,allowoptimization class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(android.view.LayoutInflater);
    public static *** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}
