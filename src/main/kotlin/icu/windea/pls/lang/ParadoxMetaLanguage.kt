package icu.windea.pls.lang

import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

import com.intellij.lang.*
import com.intellij.openapi.components.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.serviceContainer.*
import com.intellij.util.containers.*
import com.intellij.util.xmlb.annotations.*

@Suppress("NAME_SHADOWING")
class ParadoxMetaLanguage: MetaLanguage("PARADOX") {
    private val EP_NAME = ExtensionPointName<ParadoxLanguageEP>("icu.windea.pls.paradoxLanguage")
    private val LANGS = ExtensionPointUtil.dropLazyValueOnChange(
        ClearableLazyValue.create { ContainerUtil.map2Set(EP_NAME.extensionList) { it.language!! } }, EP_NAME, null)
    
    override fun matchesLanguage(language: Language): Boolean {
        var language = language
        val langs = LANGS.value
        while(true) {
            if(langs.contains(language.id)) return true
            language = language.baseLanguage ?: break
        }
        return false
    }
    
    class ParadoxLanguageEP : BaseKeyedLazyInstance<String?>() {
        @Attribute("language")
        var language: String? = null
        
        override fun getImplementationClassName(): String? {
            return null
        }
        
        override fun createInstance(componentManager: ComponentManager, pluginDescriptor: PluginDescriptor): String {
            return language!!
        }
    }
}