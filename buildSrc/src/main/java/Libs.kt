@file:Suppress("unused")

object Libs {
    // Kotlin
    private const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"

    // Coroutines
    private const val coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    private const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    // Android UI
    private const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    private const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
    private const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    private const val swipeRefreshLayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    private const val palette = "androidx.palette:palette:${Versions.palette}"
    private const val preference = "androidx.preference:preference:${Versions.preference}"
    private const val materialComponents =
        "com.google.android.material:material:${Versions.materialComponents}"

    // ViewModel and LiveData
    private const val lifecycle = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    private const val livedataKtx =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    private const val viewmodelKtx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    // Room Database
    private const val room = "androidx.room:room-ktx:${Versions.room}"
    private const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"

    // Network & Serialization
    private const val gson = "com.google.code.gson:gson:${Versions.gson}"
    private const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    private const val retrofitGsonConverter =
        "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    private const val retrofitScalarsConverter =
        "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"

    // Image loading and Photo View
    private const val coil = "io.coil-kt:coil:${Versions.coil}"
    private const val photoView = "com.github.chrisbanes:PhotoView:${Versions.photoView}"

    // Sectioned RecyclerView
    private const val sectionedRecyclerView =
        "com.jahirfiquitiva:sectioned-recyclerview:${Versions.sectionedRecyclerView}@aar"

    // File Downloader
    private const val fetch = "androidx.tonyodev.fetch2:xfetch2:${Versions.fetch}"

    // Permissions
    private const val permissions = "com.github.fondesa:kpermissions:${Versions.permissions}"

    // License checker
    private const val licenseChecker =
        "com.github.javiersantos:PiracyChecker:${Versions.licenseChecker}"

    // In-App Purchases
    private const val inAppPurchases =
        "com.anjlab.android.iab.v3:library:${Versions.inAppPurchases}"

    // Implementation dependencies
    val implementationDependencies = arrayOf(
        kotlin,
        coroutines,
        coroutinesAndroid,
        cardView,
        recyclerView,
        swipeRefreshLayout,
        palette,
        preference,
        materialComponents,
        lifecycle,
        livedataKtx,
        viewmodelKtx,
        room,
        gson,
        retrofit,
        retrofitGsonConverter,
        retrofitScalarsConverter,
        coil,
        photoView,
        sectionedRecyclerView,
        fetch,
        permissions,
        inAppPurchases
    )

    // Kapt dependencies
    val kaptDependencies = arrayOf(roomCompiler)

    // Api dependencies
    val apiDependencies = arrayOf(
        appcompat,
        licenseChecker
    )
}