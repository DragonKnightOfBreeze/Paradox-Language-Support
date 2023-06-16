package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.util.*
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
    fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveResult? {
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            
            val key = resolvePlaceholder(definitionInfo.name)!!
            val localisation = ParadoxLocalisationSearch.search(key, selector).find()
            return ResolveResult(key, localisation)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            //0~5ms
            val config = ParadoxConfigResolver.getValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveResult("", null, PlsBundle.message("dynamic"))
            }
            if(config.expression.type == CwtDataType.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveResult("", null, PlsBundle.message("inlined"))
            }
            val key = propertyValue.value
            //0~5ms
            val localisation = ParadoxLocalisationSearch.search(key, selector).find()
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
    
    fun resolveAll(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveAllResult? {
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            
            val key = resolvePlaceholder(definitionInfo.name)!!
            val localisations = ParadoxLocalisationSearch.search(key, selector).findAll()
            return ResolveAllResult(key, localisations)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxConfigResolver.getValueConfigs(propertyValue, orDefault = false).firstOrNull() ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("dynamic"))
            }
            if(config.expression.type == CwtDataType.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("inlined"))
            }
            val key = propertyValue.value
            val localisations = ParadoxLocalisationSearch.search(key, selector).findAll()
            return ResolveAllResult(key, localisations)
        } else {
            return null //不期望的结果
        }
    }
    
    companion object Resolver {
        val EmptyExpression = CwtLocalisationLocationExpression("", "")
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) }
        
        fun resolve(expressionString: String): CwtLocalisationLocationExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
            return when {
                expressionString.isEmpty() -> EmptyExpression
                expressionString.startsWith('#') -> {
                    val propertyName = expressionString.substring(1).intern()
                    CwtLocalisationLocationExpression(expressionString, null, propertyName)
                }
                else -> CwtLocalisationLocationExpression(expressionString, expressionString)
            }
        }
    }
}

