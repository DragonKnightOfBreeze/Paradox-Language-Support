package icu.windea.pls.config.config

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    val value: String
    val valueType: CwtType
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?
    val documentation: String?

    var parentConfig: CwtMemberConfig<*>?

    val valueExpression: CwtDataExpression
    override val expression: CwtDataExpression get() = valueExpression

    override fun toString(): String

    object Keys : KeyRegistry()
}

val CwtMemberConfig<*>.booleanValue: Boolean? get() = if (valueType == CwtType.Boolean) value.toBooleanYesNo() else null
val CwtMemberConfig<*>.intValue: Int? get() = if (valueType == CwtType.Int) value.toIntOrNull() ?: 0 else null
val CwtMemberConfig<*>.floatValue: Float? get() = if (valueType == CwtType.Float) value.toFloatOrNull() ?: 0f else null
val CwtMemberConfig<*>.stringValue: String? get() = if (valueType == CwtType.String) value else null

val CwtMemberConfig<*>.values: List<CwtValueConfig>? get() = configs?.filterIsInstance<CwtValueConfig>()
val CwtMemberConfig<*>.properties: List<CwtPropertyConfig>? get() = configs?.filterIsInstance<CwtPropertyConfig>()

val CwtMemberConfig<*>.options: List<CwtOptionConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionConfig>()
val CwtMemberConfig<*>.optionValues: List<CwtOptionValueConfig>? get() = optionConfigs?.filterIsInstance<CwtOptionValueConfig>()

fun CwtMemberConfig<*>.getOptionValue(): String? {
    return stringValue
}

fun CwtMemberConfig<*>.getOptionValues(): Set<String>? {
    return optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue?.intern() }
}

fun CwtMemberConfig<*>.getOptionValueOrValues(): Set<String>? {
    return getOptionValue()?.toSingletonSet() ?: getOptionValues()
}

fun CwtMemberConfig<*>.findOption(key: String): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtMemberConfig<*>.findOption(predicate: (CwtOptionConfig) -> Boolean): CwtOptionConfig? {
    return optionConfigs?.find { it is CwtOptionConfig && predicate(it) }?.cast()
}

fun CwtMemberConfig<*>.findOptions(key: String): List<CwtOptionConfig>? {
    return optionConfigs?.filter { it is CwtOptionConfig && it.key == key }?.cast()
}

inline fun CwtMemberConfig<*>.findOptions(predicate: (CwtOptionConfig) -> Boolean): List<CwtOptionConfig> {
    return optionConfigs?.filter { it is CwtOptionConfig && predicate(it) }.orEmpty().cast()
}

fun CwtMemberConfig<*>.findOptionValue(value: String): CwtOptionValueConfig? {
    return optionConfigs?.find { it is CwtOptionValueConfig && it.value == value }?.cast()
}

val <T : CwtMemberElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

val CwtMemberConfig<*>.isRoot: Boolean
    get() = when (this) {
        is CwtPropertyConfig -> this.parentConfig == null
        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
    }

val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

val CwtValueConfig.isTagConfig: Boolean
    get() = findOptionValue("tag") != null

fun <T : CwtMemberElement> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
    val cardinality = this.cardinality ?: return Occurrence(0, null, null, false)
    val cardinalityMinDefine = this.cardinalityMinDefine
    val cardinalityMaxDefine = this.cardinalityMaxDefine
    val occurrence = Occurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin)
    run {
        if (cardinalityMinDefine == null) return@run
        val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMinDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
        occurrence.min = defineValue
        occurrence.minDefine = cardinalityMinDefine
    }
    run {
        if (cardinalityMaxDefine == null) return@run
        val defineValue = ParadoxDefineManager.getDefineValue(cardinalityMaxDefine, contextElement, project)?.castOrNull<Int>() ?: return@run
        occurrence.max = defineValue
        occurrence.maxDefine = cardinalityMaxDefine
    }
    return occurrence
}

//Accessors

val CwtMemberConfig.Keys.cardinality by createKey<CwtCardinalityExpression>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.cardinalityMinDefine by createKey<String>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.cardinalityMaxDefine by createKey<String>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.hasScopeOption by createKey<Boolean>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.scopeContext by createKey<ParadoxScopeContext>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.replaceScopes by createKey<Map<String, String>>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.pushScope by createKey<String>(CwtMemberConfig.Keys)
val CwtMemberConfig.Keys.supportedScopes by createKey<Set<String>>(CwtMemberConfig.Keys)

//may on:
// * a config expression in declaration config
// * a config expression in subtype structure config
val CwtMemberConfig<*>.cardinality: CwtCardinalityExpression?
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinality, CwtCardinalityExpression.EmptyExpression) action@{
        val option = findOption("cardinality")
        if (option == null) {
            //如果没有注明且类型是常量，则推断为 1..1
            if (expression.type == CwtDataTypes.Constant) {
                return@action CwtCardinalityExpression.resolve("1..1")
            }
        }
        option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
    }
val CwtMemberConfig<*>.cardinalityMinDefine: String?
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinalityMinDefine, "") action@{
        val option = findOption("cardinality_min_define")
        option?.stringValue
    }
val CwtMemberConfig<*>.cardinalityMaxDefine
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinalityMaxDefine, "") action@{
        val option = findOption("cardinality_max_define")
        option?.stringValue
    }

val CwtMemberConfig<*>.scopeContext: ParadoxScopeContext?
    get() = getOrPutUserData(CwtMemberConfig.Keys.scopeContext, ParadoxScopeContext.Empty) action@{
        val replaceScopes = replaceScopes
        val pushScope = pushScope
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }
        scopeContext?.resolveNext(pushScope) ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
    }

//ignore case for both system scopes and scopes (to lowercase)
//may on:
//* a data expression
//* a declaration config
//* a type config
//* a subtype config
//* an extended (definition / game_rule / on_action / inline_script / parameter) config

val CwtMemberConfig<*>.replaceScopes: Map<String, String>?
    get() = getOrPutUserData(CwtMemberConfig.Keys.replaceScopes, emptyMap()) action@{
        val option = findOption { it.key == "replace_scope" || it.key == "replace_scopes" }
        if (option == null) return@action null
        val options1 = option.options ?: return@action null
        buildMap {
            for (option1 in options1) {
                val k = option1.key.lowercase()
                val v = option1.stringValue?.let { ParadoxScopeManager.getScopeId(it) } ?: continue
                put(k, v)
            }
        }
    }
val CwtMemberConfig<*>.pushScope: String?
    get() = getOrPutUserData(CwtMemberConfig.Keys.pushScope, "") action@{
        val option = findOption { it.key == "push_scope" }
        option?.getOptionValue()?.let { v -> ParadoxScopeManager.getScopeId(v) }
    }

//ignore case for scopes (to lowercase)
//may on:
//* a data expression

val CwtMemberConfig<*>.supportedScopes: Set<String>
    get() = getOrPutUserData(CwtMemberConfig.Keys.supportedScopes) action@{
        val option = findOption { it.key == "scope" || it.key == "scopes" }
        val r = option?.getOptionValueOrValues()?.mapTo(mutableSetOf()) { ParadoxScopeManager.getScopeId(it) }
        if (r.isNullOrEmpty()) ParadoxScopeManager.anyScopeIdSet else r
    }

var CwtPropertyConfig.singleAliasConfig: CwtSingleAliasConfig? by createKey(CwtMemberConfig.Keys)
var CwtPropertyConfig.aliasConfig: CwtAliasConfig? by createKey(CwtMemberConfig.Keys)
var CwtPropertyConfig.inlineConfig: CwtInlineConfig? by createKey(CwtMemberConfig.Keys)

var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMemberElement>? by createKey(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by createKey(CwtMemberConfig.Keys)

var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by createKey(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by createKey(CwtMemberConfig.Keys)

//Resolve Methods

fun CwtMemberConfig<*>.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtMemberConfig<*> {
    return when (this) {
        is CwtPropertyConfig -> this.delegated(configs, parentConfig)
        is CwtValueConfig -> this.delegated(configs, parentConfig)
    }
}
