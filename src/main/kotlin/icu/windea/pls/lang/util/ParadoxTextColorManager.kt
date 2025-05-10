package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxTextColorManager {
    fun getInfo(name: String, project: Project, contextElement: PsiElement? = null): ParadoxTextColorInfo? {
        val selector = selector(project, contextElement).definition().contextSensitive()
        val definition = ParadoxDefinitionSearch.search(name, "textcolor", selector).find()
        if (definition == null) return null
        return doGetInfoFromCache(definition)
    }

    fun getInfo(element: PsiElement): ParadoxTextColorInfo? {
        if (element is ParadoxScriptDefinitionElement) {
            val info = doGetInfoFromCache(element)
            if (info != null) return info
        }

        val colorIdText = when {
            element is ParadoxLocalisationColorfulText -> element.idElement?.text
            element is ParadoxLocalisationPropertyReference -> element.argumentElement?.text
            element is ParadoxLocalisationCommand -> element.argumentElement?.text
            else -> null
        }
        val colorId = colorIdText?.findLast { ParadoxLocalisationArgumentManager.isTextColorChar(it) }?.toString()
        if (colorId.isNullOrEmpty()) return null
        return getInfo(colorId, element.project, element)
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
        if (name.singleOrNull()?.let { isColorId(it) } != true) return null
        val gameType = selectGameType(definition) ?: return null
        val rgbList = definition.block?.valueList?.mapNotNull { it.intValue() } ?: return null
        val value = ParadoxTextColorInfo(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
        return value
    }

    fun getInfos(project: Project, contextElement: PsiElement? = null): List<ParadoxTextColorInfo> {
        val selector = selector(project, contextElement).definition().contextSensitive().distinctByName()
        val definitions = ParadoxDefinitionSearch.search("textcolor", selector).findAll()
        if (definitions.isEmpty()) return emptyList()
        return definitions.mapNotNull { definition -> doGetInfoFromCache(definition) } //it.name == it.definitionInfo.name
    }

    fun isColorId(c: Char): Boolean {
        return c.isExactWord()
    }
}

