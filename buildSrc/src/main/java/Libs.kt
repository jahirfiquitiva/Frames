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
    private const val activityKtx = "androidx.activity:activity-ktx:${Versions.activityKtx}"
    private const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"

    // ViewModel and LiveData
    private const val lifecycle = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}"
    private const val livedataKtx =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    private const val viewmodelKtx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"

    // Room Database
    private const val room = "androidx.room:room-ktx:${Versions.room}"
    private const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"

    // Work Manager
    private const val work = "androidx.work:work-runtime-ktx:${Versions.work}"

    // Network & Serialization
    private const val gson = "com.google.code.gson:gson:${Versions.gson}"
    private const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    private const val retrofitGsonConverter =
        "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    private const val retrofitScalarsConverter =
        "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"

    // Image loading and Touch Image View
    private const val coil = "io.coil-kt:coil:${Versions.coil}"
    private const val touchImageView =
        "com.github.MikeOrtiz:TouchImageView:${Versions.touchImageView}"

    // Harmonic colors
    private const val harmonicColors =
        "com.github.LeonardoSM04:HarmonicColorExtractor:${Versions.harmonicColors}"

    // Sectioned RecyclerView
    private const val sectionedRecyclerView =
        "com.jahirfiquitiva:sectioned-recyclerview:${Versions.sectionedRecyclerView}@aar"

    // FastScroll RecyclerView
    private const val fastScrollRecyclerView =
        "com.github.jahirfiquitiva:RecyclerView-FastScroll:${Versions.fastScrollRecyclerView}"

    // Permissions
    private const val permissions = "com.github.fondesa:kpermissions:${Versions.permissions}"

    // License checker
    private const val licenseChecker =
        "com.github.javiersantos:PiracyChecker:${Versions.licenseChecker}"

    // In-App Billing
    private const val inAppBilling = "com.android.billingclient:billing:${Versions.inAppBilling}"

    // MultiDex
    private const val multidex = "androidx.multidex:multidex:${Versions.multidex}"

    // OneSignal
    const val oneSignal = "com.onesignal:OneSignal:${Versions.oneSignal}"

    // Muzei API
    const val muzei = "com.google.android.apps.muzei:muzei-api:${Versions.muzei}"

    // Dependencies (must use api for they to work)
    val dependencies = arrayOf(
        kotlin,
        coroutines,
        coroutinesAndroid,
        appcompat,
        cardView,
        recyclerView,
        swipeRefreshLayout,
        palette,
        preference,
        materialComponents,
        activityKtx,
        fragmentKtx,
        lifecycle,
        livedataKtx,
        viewmodelKtx,
        room,
        work,
        gson,
        retrofit,
        retrofitGsonConverter,
        retrofitScalarsConverter,
        coil,
        touchImageView,
        harmonicColors,
        sectionedRecyclerView,
        fastScrollRecyclerView,
        permissions,
        licenseChecker,
        inAppBilling,
        multidex,
        muzei
    )

    // Kapt dependencies
    val kaptDependencies = arrayOf(roomCompiler)
}