package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxTextColorManager {
    fun getId(element: PsiElement): String? {
        return when (element) {
            is ParadoxLocalisationColorfulText -> element.idElement?.text
            is ParadoxLocalisationArgumentAwareElement -> element.argumentElement?.let { getId(it) }
            is ParadoxLocalisationParameterArgument -> element.idElement?.let { doGetIdInArgument(it) }
            is ParadoxLocalisationCommandArgument -> element.idElement?.let { doGetIdInArgument(it) }
            else -> null
        }
    }

    private fun doGetIdInArgument(element: PsiElement): String? {
        return element.text.find { isIdInArgument(it) }?.toString()
    }

    fun getIdElementAndOffset(element: PsiElement): Tuple2<PsiElement, Int>? {
        return when (element) {
            is ParadoxLocalisationColorfulText -> element.idElement?.let { it to 0 }
            is ParadoxLocalisationArgumentAwareElement -> element.argumentElement?.let { getIdElementAndOffset(it) }
            is ParadoxLocalisationParameterArgument -> element.idElement?.let { doGetIdOffset(element) }
            is ParadoxLocalisationCommandArgument -> element.idElement?.let { doGetIdOffset(element) }
            else -> null
        }
    }

    private fun doGetIdOffset(element: PsiElement): Tuple2<PsiElement, Int>? {
        return element to element.text.indexOfFirst { isIdInArgument(it) }
    }

    fun getInfo(element: PsiElement): ParadoxTextColorInfo? {
        if (element is ParadoxScriptDefinitionElement) {
            val info = doGetInfoFromCache(element)
            if (info != null) return info
        }

        val id = getId(element)
        if (id.isNullOrEmpty()) return null
        return getInfo(id, element.project, element)
    }

    fun getInfo(name: String, project: Project, contextElement: PsiElement? = null): ParadoxTextColorInfo? {
        val selector = selector(project, contextElement).definition().contextSensitive()
        val definition = ParadoxDefinitionSearch.search(name, "text_color", selector).find()
        if (definition == null) return null
        return doGetInfoFromCache(definition)
    }

    private fun doGetInfoFromCache(definition: ParadoxScriptDefinitionElement): ParadoxTextColorInfo? {
        if (definition !is ParadoxScriptProperty) return null
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedTextColorInfo) {
            val value = doGetInfo(definition)
            value.withDependencyItems(definition)
        }
    }

    private fun doGetInfo(definition: ParadoxScriptDefinitionElement): ParadoxTextColorInfo? {
        if (definition !is ParadoxScriptProperty) return null
        //要求输入的name必须是单个字母或数字
        val name = definition.name
        if (name.singleOrNull()?.let { isId(it) } != true) return null
        val gameType = selectGameType(definition) ?: return null
        val rgbList = definition.block?.valueList?.mapNotNull { it.intValue() } ?: return null
        val value = ParadoxTextColorInfo(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
        return value
    }

    fun getInfos(project: Project, contextElement: PsiElement? = null): List<ParadoxTextColorInfo> {
        val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
        val definitions = ParadoxDefinitionSearch.search("text_color", selector).findAll()
        if (definitions.isEmpty()) return emptyList()
        return definitions.mapNotNull { definition -> doGetInfoFromCache(definition) } //it.name == it.definitionInfo.name
    }

    fun isId(c: Char): Boolean {
        return c.isExactWord()
    }

    fun isIdInArgument(c: Char): Boolean {
        return c.isExactLetter()
    }
}

