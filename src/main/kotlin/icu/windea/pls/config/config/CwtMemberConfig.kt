package icu.windea.pls.config.config

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.ep.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*

sealed interface CwtMemberConfig<out T : CwtMemberElement> : UserDataHolder, CwtConfig<T>, CwtValueAware, CwtConfigsAware, CwtDocumentationAware, CwtOptionsAware {
    override val configs: List<CwtMemberConfig<*>>?
    var parentConfig: CwtMemberConfig<*>?
    var inlineableConfig: CwtInlineableConfig<@UnsafeVariance T, CwtMemberConfig<@UnsafeVariance T>>?
    
    val valueExpression: CwtValueExpression
    override val expression: CwtDataExpression
    
    override fun resolved(): CwtMemberConfig<T> = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>() ?: this
    
    override fun resolvedOrNull(): CwtMemberConfig<T>? = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>()
    
    override fun toString(): String
    
    object Keys : KeyRegistry("CwtMemberConfig")
}

fun CwtMemberConfig<*>.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtMemberConfig<*> {
    return when(this) {
        is CwtPropertyConfig -> this.delegated(configs, parentConfig)
        is CwtValueConfig -> this.delegated(configs, parentConfig)
    }
}

val <T : CwtMemberElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

val CwtMemberConfig<*>.isRoot: Boolean
    get() = when(this) {
        is CwtPropertyConfig -> this.parentConfig == null
        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
    }

val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when(this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

val CwtValueConfig.isTagConfig: Boolean
    get() = findOptionValue("tag") != null

val CwtMemberConfig.Keys.cardinality by createKey<CwtCardinalityExpression>("cwt.memberConfig.cardinality")
val CwtMemberConfig.Keys.cardinalityMinDefine by createKey<String>("cwt.memberConfig.cardinalityMinDefine")
val CwtMemberConfig.Keys.cardinalityMaxDefine by createKey<String>("cwt.memberConfig.cardinalityMaxDefine")
val CwtMemberConfig.Keys.hasScopeOption by createKey<Boolean>("cwt.memberConfig.hasScopeOption")
val CwtMemberConfig.Keys.scopeContext by createKey<ParadoxScopeContext>("cwt.memberConfig.scopeContext")
val CwtMemberConfig.Keys.replaceScopes by createKey<Map<String, String>>("cwt.memberConfig.replaceScopes")
val CwtMemberConfig.Keys.pushScope by createKey<String>("cwt.memberConfig.pushScope")
val CwtMemberConfig.Keys.supportedScopes by createKey<Set<String>>("cwt.memberConfig.supportedScopes")
val CwtMemberConfig.Keys.overriddenProvider by createKey<CwtOverriddenConfigProvider>("cwt.memberConfig.overriddenProvider")

//may on:
// * a config expression in declaration config
// * a config expression in subtype structure config
val CwtMemberConfig<*>.cardinality
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinality, CwtCardinalityExpression.EmptyExpression) action@{
        val option = findOption("cardinality")
        if(option == null) {
            //如果没有注明且类型是常量，则推断为 1..1
            if(expression.type == CwtDataTypes.Constant) {
                return@action CwtCardinalityExpression.resolve("1..1")
            }
        }
        option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
    }
val CwtMemberConfig<*>.cardinalityMinDefine
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinalityMinDefine, "") action@{
        val option = findOption("cardinality_min_define")
        option?.stringValue
    }
val CwtMemberConfig<*>.cardinalityMaxDefine
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinalityMaxDefine, "") action@{
        val option = findOption("cardinality_max_define")
        option?.stringValue
    }

val CwtMemberConfig<*>.hasScopeOption
    get() = getOrPutUserData(CwtMemberConfig.Keys.hasScopeOption) action@{
        val option = findOption { it.key == "replace_scope" || it.key == "replace_scopes" || it.key == "push_scope" || it.key == "scope" || it.key == "scopes" }
        option != null
    }
val CwtMemberConfig<*>.scopeContext
    get() = getOrPutUserData(CwtMemberConfig.Keys.scopeContext, ParadoxScopeContext.Empty) action@{
        val map = replaceScopes ?: return@action null
        ParadoxScopeContext.resolve(map)
    }
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
//* a definition / game type / on action config
val CwtMemberConfig<*>.replaceScopes
    get() = getOrPutUserData(CwtMemberConfig.Keys.replaceScopes, emptyMap()) action@{
        val option = findOption { it.key == "replace_scope" || it.key == "replace_scopes" }
        if(option == null) return@action null
        val options1 = option.findOptions() ?: return@action null
        buildMap {
            for(option1 in options1) {
                val k = option1.key.lowercase()
                val v = option1.stringValue?.let { ParadoxScopeHandler.getScopeId(it) } ?: continue
                put(k, v)
            }
        }
    }
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtMemberConfig<*>.pushScope
    get() = getOrPutUserData(CwtMemberConfig.Keys.pushScope, "") action@{
        val option = findOption { it.key == "push_scope" }
        option?.getOptionValue()?.let { v -> ParadoxScopeHandler.getScopeId(v) }
    }
//may on:
// * a config expression in declaration config
val CwtMemberConfig<*>.supportedScopes
    get() = getOrPutUserData(CwtMemberConfig.Keys.supportedScopes) action@{
        val option = findOption { it.key == "scope" || it.key == "scopes" }
        val r = option?.getOptionValueOrValues()?.mapTo(mutableSetOf()) { ParadoxScopeHandler.getScopeId(it) }
        if(r.isNullOrEmpty()) ParadoxScopeHandler.anyScopeIdSet else r
    }

fun <T : CwtMemberElement> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
    val cardinality = this.cardinality ?: return Occurrence(0, null, null, false)
    val cardinalityMinDefine = this.cardinalityMinDefine
    val cardinalityMaxDefine = this.cardinalityMaxDefine
    val occurrence = Occurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin)
    if(cardinalityMinDefine != null) {
        val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMinDefine, Int::class.java)
        if(defineValue != null) {
            occurrence.min = defineValue
            occurrence.minDefine = cardinalityMinDefine
        }
    }
    if(cardinalityMaxDefine != null) {
        val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMaxDefine, Int::class.java)
        if(defineValue != null) {
            occurrence.max = defineValue
            occurrence.maxDefine = cardinalityMaxDefine
        }
    }
    return occurrence
}

var CwtMemberConfig<*>.overriddenProvider by CwtMemberConfig.Keys.overriddenProvider
