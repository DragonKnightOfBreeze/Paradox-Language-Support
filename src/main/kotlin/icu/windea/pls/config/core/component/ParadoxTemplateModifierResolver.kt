package icu.windea.pls.config.core.component

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.script.psi.*

/**
 * 通过模版表达式生成修饰符。（如：`job_<job>_add` -> `job_researcher_add`）
 */
class ParadoxTemplateModifierResolver: ParadoxModifierResolver {
    companion object {
        @JvmField val referencesKey = Key.create<List<ParadoxInTemplateExpressionReference>>("paradox.modifierElement.references")
        @JvmField val definitionTypesKey = Key.create<List<String>>("paradox.parameterElement.definitionTypes")
    }
    
    override fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int) : Boolean {
        val isStatic = BitUtil.isSet(matchType, CwtConfigMatchType.STATIC)
        if(isStatic) return false
        //要求生成源必须已定义
        return configGroup.generatedModifiers.values.any { config ->
            config.template.matches(name, configGroup, matchType)
        }
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        //要求生成源必须已定义
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return null
        var generatedModifierConfig: CwtModifierConfig? = null
        val references = configGroup.generatedModifiers.values.firstNotNullOfOrNull { config ->
            ProgressManager.checkCanceled()
            val resolvedReferences = config.template.resolveReferences(element, name, configGroup).takeIfNotEmpty()
            if(resolvedReferences != null) generatedModifierConfig = config
            resolvedReferences
        }.orEmpty()
        if(generatedModifierConfig == null) return null
        val result = ParadoxModifierElement(element, name, generatedModifierConfig, gameType, project)
        result.putUserData(referencesKey, references)
        return result
    }
}
