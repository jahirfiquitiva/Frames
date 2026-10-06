# Consumer rules: applied to every app that depends on Frames.
# Only keep what R8 cannot trace by itself. Everything an app calls, extends,
# or declares in its manifest or layouts is kept automatically, and libraries
# (Gson, Retrofit, Room, Billing, Material, ...) bundle their own rules.

# Gson reads and writes these fields by name (wallpapers JSON, purchase records)
-keepclassmembers class dev.jahir.frames.data.models.Wallpaper,
    dev.jahir.frames.data.models.PseudoDetailedPurchaseRecord,
    dev.jahir.frames.data.models.DetailedPurchaseRecord {
    <init>(...);
    <fields>;
}
