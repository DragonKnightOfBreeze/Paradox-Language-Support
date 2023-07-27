package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxTextColorHandler {
    fun getInfo(name: String, project: Project, contextElement: PsiElement? = null): ParadoxTextColorInfo? {
        val selector = definitionSelector(project, contextElement).contextSensitive()
        val definition = ParadoxDefinitionSearch.search(name, "textcolor", selector).find()
        if(definition == null) return null
        return doGetInfoFromCache(definition)
    }
    
    private fun doGetInfoFromCache(definition: ParadoxScriptDefinitionElement): ParadoxTextColorInfo? {
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedTextColorInfo) {
            val value = doGetInfo(definition)
            CachedValueProvider.Result.create(value, definition)
        }
    }
    
    private fun doGetInfo(definition: ParadoxScriptDefinitionElement): ParadoxTextColorInfo? {
        if(definition !is ParadoxScriptProperty) return null
        //要求输入的name必须是单个字母或数字
        val name = definition.name
        if(name.singleOrNull()?.let { it.isExactLetter() || it.isExactDigit() } != true) return null
        val gameType = selectGameType(definition) ?: return null
        val rgbList = definition.valueList.mapNotNull { it.intValue() }
        val value = ParadoxTextColorInfo(name, gameType, definition.createPointer(), rgbList[0], rgbList[1], rgbList[2])
        return value
    }
    
    fun getInfos(project: Project, contextElement: PsiElement? = null): List<ParadoxTextColorInfo> {
        val selector = definitionSelector(project, contextElement).contextSensitive().distinctByName()
        val definitions = ParadoxDefinitionSearch.search("textcolor", selector).findAll()
        if(definitions.isEmpty()) return emptyList()
        return definitions.mapNotNull { definition -> doGetInfoFromCache(definition) } //it.name == it.definitionInfo.name
    }
}

