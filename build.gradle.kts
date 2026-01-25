// 项目根目录的 build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // 根目录需要版本号，或者在 libs.versions.toml 中提供版本
    alias(libs.plugins.hilt.android) apply false
    // 移除 kotlin-compose 插件，它包含在 kotlin-android 插件中
    // alias(libs.plugins.kotlin.compose) apply false  // 删除这行
}