plugins {
    alias(libs.plugins.test)
}

android {
    namespace = "com.example.macrobenchmark"
    compileSdk = 34

    defaultConfig {
        minSdk = 28
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}
dependencies {
    implementation(libs.benchmark)
    implementation(libs.androidx.junit)
    implementation(libs.uiautomator)
}