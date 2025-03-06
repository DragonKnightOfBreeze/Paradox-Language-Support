package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.expression.CwtLocalisationLocationExpression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

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
interface CwtLocalisationLocationExpression : CwtExpression {
    val placeholder: String?
    val propertyName: String?
    val upperCase: Boolean

    fun resolvePlaceholder(name: String): String?

    fun resolve(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ): ResolveResult?

    fun resolveAll(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        selector: ChainedParadoxSelector<ParadoxLocalisationProperty>
    ): ResolveAllResult?

    data class ResolveResult(
        val name: String,
        val element: ParadoxLocalisationProperty?,
        val message: String? = null
    )

    data class ResolveAllResult(
        val name: String,
        val elements: Set<ParadoxLocalisationProperty>,
        val message: String? = null
    )

    companion object Resolver {
        val EmptyExpression: CwtLocalisationLocationExpression = doResolveEmpty()

        fun resolve(expressionString: String): CwtLocalisationLocationExpression = cache.get(expressionString)
    }
}

//Implementations (cached & interned)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtLocalisationLocationExpressionImpl("", propertyName = "")

private fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
    return when {
        expressionString.isEmpty() -> Resolver.EmptyExpression
        expressionString.contains('$') -> {
            val placeholder = expressionString.substringBefore('|').intern()
            val upperCase = expressionString.substringAfter('|', "") == "u"
            CwtLocalisationLocationExpressionImpl(expressionString, placeholder = placeholder, upperCase = upperCase)
        }
        else -> {
            val propertyName = expressionString
            CwtLocalisationLocationExpressionImpl(expressionString, propertyName = propertyName)
        }
    }
}

private class CwtLocalisationLocationExpressionImpl : CwtLocalisationLocationExpression {
    override val expressionString: String
    override val placeholder: String?
    override val propertyName: String?
    override val upperCase: Boolean

    constructor(expressionString: String, placeholder: String? = null, propertyName: String? = null, upperCase: Boolean = false) {
        this.expressionString = expressionString.intern()
        this.placeholder = placeholder?.intern()
        this.propertyName = propertyName?.intern()
        this.upperCase = upperCase
    }

    override fun resolvePlaceholder(name: String): String? {
        if (placeholder == null) return null
        return buildString { for (c in placeholder) if (c == '$') append(name) else append(c) }
            .letIf(upperCase) { it.uppercase() }
    }

    override fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveResult? {
        if (placeholder != null) {
            if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
            val name = resolvePlaceholder(definitionInfo.name)!!
            val resolved = ParadoxLocalisationSearch.search(name, selector).find()
            return ResolveResult(name, resolved)
        } else if (propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxExpressionManager.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if (config.expression.type !in CwtDataTypeGroups.LocalisationLocationResolved) {
                return ResolveResult("", null, PlsBundle.message("dynamic"))
            }
            if (propertyValue !is ParadoxScriptString) {
                return null
            }
            if (propertyValue.text.isParameterized()) {
                return ResolveResult("", null, PlsBundle.message("parameterized"))
            }
            if (config.expression.type == CwtDataTypes.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveResult("", null, PlsBundle.message("inlined"))
            }
            val name = propertyValue.value
            val resolved = ParadoxLocalisationSearch.search(name, selector).find()
            return ResolveResult(name, resolved)
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }

    override fun resolveAll(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): ResolveAllResult? {
        if (placeholder != null) {
            if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions

            val name = resolvePlaceholder(definitionInfo.name)!!
            val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
            return ResolveAllResult(name, resolved)
        } else if (propertyName != null) {
            val property = definition.findProperty(propertyName, conditional = true, inline = true) ?: return null
            val propertyValue = property.propertyValue ?: return null
            val config = ParadoxExpressionManager.getConfigs(propertyValue, orDefault = false).firstOrNull() as? CwtValueConfig ?: return null
            if (config.expression.type !in CwtDataTypeGroups.LocalisationLocationResolved) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("dynamic"))
            }
            if (propertyValue !is ParadoxScriptString) {
                return null
            }
            if (propertyValue.text.isParameterized()) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("parameterized"))
            }
            if (config.expression.type == CwtDataTypes.InlineLocalisation && propertyValue.text.isLeftQuoted()) {
                return ResolveAllResult("", emptySet(), PlsBundle.message("inlined"))
            }
            val name = propertyValue.value
            val resolved = ParadoxLocalisationSearch.search(name, selector).findAll()
            return ResolveAllResult(name, resolved)
        } else {
            throw IllegalStateException() //不期望的结果
        }
    }
}

