package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.isExactLetter
import icu.windea.pls.core.isExactWord
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.intValue
import icu.windea.pls.lang.psi.values
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationArgumentAwareElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameterArgument
import icu.windea.pls.model.ParadoxTextColorInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

@Optimized
object ParadoxTextColorManager {
    object Keys : KeyRegistry() {
        val cachedTextColorInfo by registerKey<CachedValue<ParadoxTextColorInfo?>>(this)
    }

    fun getInfo(element: PsiElement): ParadoxTextColorInfo? {
        if (element is ParadoxDefinitionElement) {
            val info = getInfoInternal(element)
            if (info != null) return info
        }

        val name = getColorId(element)
        if (name.isNullOrEmpty()) return null
        val selector = ParadoxDefinitionSearch.selector(element.project, element).contextSensitive()
            .withConstraint(ParadoxDefinitionIndexConstraint.TextColor)
        val definition = ParadoxDefinitionSearch.searchProperty(name, ParadoxDefinitionTypes.textColor, selector).find()
        if (definition == null) return null
        return getInfoInternal(definition)
    }

    fun getInfo(name: String, project: Project, contextElement: PsiElement? = null): ParadoxTextColorInfo? {
        val selector = ParadoxDefinitionSearch.selector(project, contextElement).contextSensitive()
            .withConstraint(ParadoxDefinitionIndexConstraint.TextColor)
        val definition = ParadoxDefinitionSearch.searchProperty(name, ParadoxDefinitionTypes.textColor, selector).find()
        if (definition == null) return null
        return getInfoInternal(definition)
    }

    fun getInfos(project: Project, contextElement: PsiElement? = null): List<ParadoxTextColorInfo> {
        val selector = ParadoxDefinitionSearch.selector(project, contextElement).contextSensitive().distinct()
            .withConstraint(ParadoxDefinitionIndexConstraint.TextColor)
        val definitions = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.textColor, selector).findAll()
        if (definitions.isEmpty()) return emptyList()
        return definitions.mapNotNullFast { definition -> getInfoInternal(definition) } // it.name == it.definitionInfo.name
    }

    private fun getInfoInternal(definition: ParadoxDefinitionElement): ParadoxTextColorInfo? {
        if (definition !is ParadoxScriptProperty) return null
        return getCachedValueOnDemand(definition, Keys.cachedTextColorInfo, onDemand = ChronicleCapabilities.Cache.textColor) {
            runSmartReadAction {
                val value = resolveInfo(definition)
                CachedValueProvider.Result.create(value, definition)
            }
        }
    }

    private fun resolveInfo(definition: ParadoxDefinitionElement): ParadoxTextColorInfo? {
        if (definition !is ParadoxScriptProperty) return null
        // 要求输入的名字必须是单个字母或数字
        val name = definition.name
        if (name.singleOrNull()?.let { isColorId(it) } != true) return null
        val gameType = selectGameType(definition) ?: return null
        val rgbList = definition.values().mapNotNull { it.intValue() }.toList()
        if (rgbList.size != 3) return null
        val value = ParadoxTextColorInfo(name, rgbList[0], rgbList[1], rgbList[2], gameType).also { it.element = definition }
        return value
    }

    fun getColorId(element: PsiElement): String? {
        return when (element) {
            is ParadoxLocalisationColorfulText -> element.idElement?.text
            is ParadoxLocalisationArgumentAwareElement -> element.argumentElement?.let { getColorId(it) }
            is ParadoxLocalisationParameterArgument -> element.idElement?.let { getColorIdFromArgument(it) }
            is ParadoxLocalisationCommandArgument -> element.idElement?.let { getColorIdFromArgument(it) }
            else -> null
        }
    }

    private fun getColorIdFromArgument(element: PsiElement): String? {
        return element.text.find { isColorIdInArgument(it) }?.toString()
    }

    fun getIdElementAndOffset(element: PsiElement): Tuple2<PsiElement, Int>? {
        return when (element) {
            is ParadoxLocalisationColorfulText -> element.idElement?.let { it to 0 }
            is ParadoxLocalisationArgumentAwareElement -> element.argumentElement?.let { getIdElementAndOffset(it) }
            is ParadoxLocalisationParameterArgument -> element.idElement?.let { getIdElementAndOffsetFromArgument(it) }
            is ParadoxLocalisationCommandArgument -> element.idElement?.let { getIdElementAndOffsetFromArgument(it) }
            else -> null
        }
    }

    private fun getIdElementAndOffsetFromArgument(element: PsiElement): Tuple2<PsiElement, Int> {
        return element to element.text.indexOfFirst { isColorIdInArgument(it) }
    }

    fun isColorId(c: Char): Boolean {
        return c.isExactWord()
    }

    fun isColorIdInArgument(c: Char): Boolean {
        return c.isExactLetter()
    }
}
