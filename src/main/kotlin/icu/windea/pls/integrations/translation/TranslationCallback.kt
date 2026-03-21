package icu.windea.pls.integrations.translation

fun interface TranslationCallback {
    fun call(result: String?, error: Throwable?)
}
