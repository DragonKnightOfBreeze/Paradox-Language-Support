package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

private val validValueTypes = arrayOf(
    CwtDataType.Localisation,
    CwtDataType.SyncedLocalisation,
    CwtDataType.InlineLocalisation
)

/**
 * CWT本地化的位置表达式。
 *
 * 用于推断定义的相关本地化（relatedLocation）的位置。
 *
 * 示例：`"$"`, `"$_desc"`, `"#title"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成定义的名字，如果定义是匿名的，则忽略此表达式）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后的子字符串，可以为空字符串）。
 */
class CwtLocalisationLocationExpression(
    expressionString: String,
    val placeholder: String? = null,
    val propertyName: String? = null
) : AbstractExpression(expressionString), CwtExpression {
    companion object Resolver {
        val EmptyExpression = CwtLocalisationLocationExpression("", "")
        
        val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) } }
        
        fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
        
        private fun doResolve(expressionString: String) = when {
            expressionString.isEmpty() -> EmptyExpression
            expressionString.startsWith('#') -> {
                val propertyName = expressionString.substring(1)
                CwtLocalisationLocationExpression(expressionString, null, propertyName)
            }
            else -> CwtLocalisationLocationExpression(expressionString, expressionString)
        }
    }
    
    operator fun component1() = placeholder
    
    operator fun component2() = propertyName
    
    fun resolvePlaceholder(name: String): String? {
        if(placeholder == null) return null
        return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
    }
    
    data class ResolveResult(
        val key: String,
        val localisation: ParadoxLocalisationProperty?,
        val message: String? = null
    )
    
    /**
     * @return (localisationKey, localisation, message)
     */
    fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveResult? {
        if(placeholder != null) {
            //如果定义是匿名的，则直接忽略
            if(definitionInfo.isAnonymous) return null
            
            val key = resolvePlaceholder(definitionInfo.name)!!
            val localisation = ParadoxLocalisationSearch.search(key, project, selector = selector).find()
            return ResolveResult(key, localisation)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxCwtConfigHandler.resolveValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveResult("", null, PlsDocBundle.message("dynamic"))
            }
            if(config.expression.type == CwtDataType.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveResult("", null, PlsDocBundle.message("inlined"))
            }
            val key = propertyValue.value
            val localisation = ParadoxLocalisationSearch.search(key, project, selector = selector).find()
            return ResolveResult(key, localisation)
        } else {
            return null //不期望的结果
        }
    }
    
    data class ResolveAllResult(
        val key: String,
        val localisations: Set<ParadoxLocalisationProperty>,
        val message: String? = null
    )
    
    fun resolveAll(definitionName: String, definition: ParadoxScriptDefinitionElement, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveAllResult? {
        if(placeholder != null) {
            val key = resolvePlaceholder(definitionName)!!
            val localisations = ParadoxLocalisationSearch.search(key, project, selector = selector).findAll()
            return ResolveAllResult(key, localisations)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxCwtConfigHandler.resolveValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveAllResult("", emptySet(), PlsDocBundle.message("dynamic"))
            }
            if(config.expression.type == CwtDataType.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveAllResult("", emptySet(), PlsDocBundle.message("inlined"))
            }
            val key = propertyValue.value
            val localisations = ParadoxLocalisationSearch.search(key, project, selector = selector).findAll()
            return ResolveAllResult(key, localisations)
        } else {
            return null //不期望的结果
        }
    }
}

