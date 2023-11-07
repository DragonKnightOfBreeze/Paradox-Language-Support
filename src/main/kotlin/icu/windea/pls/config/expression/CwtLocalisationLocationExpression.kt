package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

private val validValueTypes = arrayOf(
    CwtDataTypes.Localisation,
    CwtDataTypes.SyncedLocalisation,
    CwtDataTypes.InlineLocalisation
)

/**
 * CWT本地化位置表达式。
 *
 * 用于定位定义的相关本地化。
 *
 * 如果包含占位符`$`，将其替换成定义的名字后，尝试得到对应名字的本地化，否则尝试得到对应名字的属性的值对应的本地化。
 *
 * 示例：`"$"`, `"$_desc"`, `"$_DESC|u"` , `"title"`
 *
 * @property placeholder 占位符文本。其中的`"$"`会在解析时被替换成定义的名字。
 * @property propertyName 属性名，用于获取本地化的名字。
 * @property upperCase 本地化的名字是否强制大写。
 */
class CwtLocalisationLocationExpression private constructor(
    expressionString: String,
    val placeholder: String? = null,
    val propertyName: String? = null,
    val upperCase: Boolean = false
) : AbstractExpression(expressionString), CwtExpression {
    fun resolvePlaceholder(name: String): String? {
        if(placeholder == null) return null
        return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
            .letIf(upperCase) { it.uppercase() }
    }
    
    data class ResolveResult(
        val name: String,
        val element: ParadoxLocalisationProperty?,
        val message: String? = null
    )
    
    /**
     * @return (localisationKey, localisation, message)
     */
    fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveResult? {
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            val name = resolvePlaceholder(definitionInfo.name)!!
            val resolved = ParadoxLocalisationSearch.search(name, selector).find()
            return ResolveResult(name, resolved)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = CwtConfigHandler.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveResult("", null, PlsBundle.message("dynamic"))
            }
            if(propertyValue !is ParadoxScriptString) {
                return null
            }
            if(propertyValue.text.isParameterized()) {
                return ResolveResult("", null, PlsBundle.message("parameterized"))
            }
            if(config.expression.type == CwtDataTypes.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveResult("", null, PlsBundle.message("inlined"))
            }
            val name = propertyValue.value
            val resolved = ParadoxLocalisationSearch.search(name, selector).find()
            return ResolveResult(name, resolved)
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }
    
    data class ResolveAllResult(
        val name: String,
        val elements: Set<ParadoxLocalisationProperty>,
        val message: String? = null
    )
    
    fun resolveAll(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveAllResult? {
        if(placeholder != null) {
            if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            
            val name = resolvePlaceholder(definitionInfo.name)!!
            val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
            return ResolveAllResult(name, resolved)
        } else if(propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = CwtConfigHandler.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if(config.expression.type !in validValueTypes) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("dynamic"))
            }
            if(propertyValue !is ParadoxScriptString) {
                return null
            }
            if(propertyValue.text.isParameterized()) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("parameterized"))
            }
            if(config.expression.type == CwtDataTypes.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("inlined"))
            }
            val name = propertyValue.value
            val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
            return ResolveAllResult(name, resolved)
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }
    
    companion object Resolver {
        val EmptyExpression = CwtLocalisationLocationExpression("", propertyName = "")
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) }
        
        fun resolve(expressionString: String): CwtLocalisationLocationExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
            return when {
                expressionString.isEmpty() -> EmptyExpression
                expressionString.contains('$') -> {
                    val placeholder = expressionString.substringBefore('|').intern()
                    val upperCase = expressionString.substringAfter('|', "") == "u"
                    CwtLocalisationLocationExpression(expressionString, placeholder = placeholder, upperCase = upperCase)
                }
                else -> {
                    val propertyName = expressionString
                    CwtLocalisationLocationExpression(expressionString, propertyName = propertyName)
                }
            }
        }
    }
}

