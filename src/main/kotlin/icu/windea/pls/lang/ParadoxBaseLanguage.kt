package icu.windea.pls.lang

object ParadoxBaseLanguage : ParadoxLanguage("PARADOX") {
    // 3.1.0 from inspection: Serializable object must implement 'readResolve'
    @Suppress("unused")
    private fun readResolve(): Any = ParadoxBaseLanguage
}
