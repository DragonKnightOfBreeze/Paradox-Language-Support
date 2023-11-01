package icu.windea.pls

import com.intellij.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*

class PlsDocumentationBundle(bundleName: String) : DynamicBundle(bundleName) {
    companion object {
        private val bundles = mutableMapOf<String, PlsDocumentationBundle>().withDefault {
            val bundleName = "messages.documentation.${it}DocumentationBundle"
            PlsDocumentationBundle(bundleName)
        }
        
        private fun get(key: String) = bundles.getValue(key)
        
        private fun get(key: ParadoxGameType?) = bundles.getValue(key?.title ?: "Core")
        
        @Nls
        @JvmStatic
        fun message(gameType: ParadoxGameType?, definitionName: String, definitionType: String): String? {
            val key = "$definitionType.$definitionName"
            return get(gameType).messageOrNull(key) ?: get(null).messageOrNull(key)
        }
    }
}