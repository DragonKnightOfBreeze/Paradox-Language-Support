package icu.windea.pls.config.config

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.expression.CwtDataType.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.model.*

sealed interface CwtMemberConfig<out T : PsiElement> : UserDataHolder, CwtConfig<T>, CwtValueAware, CwtOptionsAware, CwtDocumentationAware {
    val configs: List<CwtMemberConfig<*>>?
    
    var parent: CwtMemberConfig<*>?
    var inlineableConfig: CwtInlineableConfig<@UnsafeVariance T>?
    
    val values: List<CwtValueConfig>?
    val properties: List<CwtPropertyConfig>?
    
    override val expression: CwtDataExpression
    
    override fun resolved(): CwtMemberConfig<T> = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>() ?: this
    
    override fun resolvedOrNull(): CwtMemberConfig<T>? = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>()
    
    object Keys
}

val <T : PsiElement> CwtMemberConfig<T>.isBlock: Boolean
    get() = configs != null

val CwtMemberConfig<*>.isRoot: Boolean
    get() = when {
        this is CwtPropertyConfig -> this.parent == null
        this is CwtValueConfig -> this.parent == null && this.propertyConfig == null
        else -> false
    }

val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<PsiElement>
    get() = when {
        this is CwtPropertyConfig -> this
        this is CwtValueConfig -> propertyConfig ?: this
        else -> this
    }

fun <T : PsiElement> CwtMemberConfig<T>.deepCopyConfigs(): List<CwtMemberConfig<*>>? {
    if(configs.isNullOrEmpty()) return configs
    return configs?.mapFast { config ->
        when(config) {
            is CwtPropertyConfig -> config.copyDelegated(config.parent, config.deepCopyConfigs())
            is CwtValueConfig -> config.copyDelegated(config.parent, config.deepCopyConfigs())
        }
    }
}

fun <T : PsiElement> CwtMemberConfig<T>.deepMergeConfigs(configContext: CwtConfigContext): List<CwtMemberConfig<*>> {
    //因为之后可能需要对得到的声明规则进行注入，需要保证当注入时所有规则列表都是可变的
    
    val mergedConfigs: MutableList<CwtMemberConfig<*>>? = if(configs != null) mutableListOf() else null
    configs?.forEachFast { config ->
        val childConfigList = config.deepMergeConfigs(configContext)
        if(childConfigList.isNotEmpty()) {
            for(childConfig in childConfigList) {
                mergedConfigs?.add(childConfig)
            }
        }
    }
    when(this) {
        is CwtValueConfig -> {
            val mergedConfig = this.copyDelegated(parent, mergedConfigs)
            if(configContext.injectors.isNotEmpty()) return mutableListOf(mergedConfig)
            return mergedConfig.toSingletonList()
        }
        is CwtPropertyConfig -> {
            val subtypeExpression = key.removeSurroundingOrNull("subtype[", "]")
            if(subtypeExpression == null) {
                val mergedConfig = this.copyDelegated(parent, mergedConfigs)
                if(configContext.injectors.isNotEmpty()) return mutableListOf(mergedConfig)
                return mergedConfig.toSingletonList()
            } else {
                val subtypes = configContext.definitionSubtypes
                if(subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                    mergedConfigs?.forEachFast { mergedConfig ->
                        mergedConfig.parent = parent
                    }
                    if(configContext.injectors.isNotEmpty()) return mergedConfigs ?: mutableListOf()
                    return mergedConfigs.orEmpty()
                } else {
                    if(configContext.injectors.isNotEmpty()) return mutableListOf()
                    return emptyList()
                }
            }
        }
    }
}

val CwtMemberConfig.Keys.cardinality by lazy { Key.create<CwtCardinalityExpression>("paradox.cwtDataConfig.cardinality") }
val CwtMemberConfig.Keys.cardinalityMinDefine by lazy { Key.create<String>("paradox.cwtDataConfig.cardinalityMinDefine") }
val CwtMemberConfig.Keys.cardinalityMaxDefine by lazy { Key.create<String>("paradox.cwtDataConfig.cardinalityMaxDefine") }
val CwtMemberConfig.Keys.hasScopeOption by lazy { Key.create<Boolean>("paradox.cwtDataConfig.hasScopeOption") }
val CwtMemberConfig.Keys.scopeContext by lazy { Key.create<ParadoxScopeContext>("paradox.cwtDataConfig.scopeContext") }
val CwtMemberConfig.Keys.replaceScopes by lazy { Key.create<Map<String, String?>>("paradox.cwtDataConfig.replaceScopes") }
val CwtMemberConfig.Keys.pushScope by lazy { Key.create<String>("paradox.cwtDataConfig.pushScope") }
val CwtMemberConfig.Keys.supportedScopes by lazy { Key.create<Set<String>>("paradox.cwtDataConfig.supportedScopes") }

//may on:
// * a config expression in declaration config
// * a config expression in subtype structure config
val CwtMemberConfig<*>.cardinality
    get() = getOrPutUserData(CwtMemberConfig.Keys.cardinality, CwtCardinalityExpression.EmptyExpression) action@{
        val option = findOption("cardinality")
        if(option == null) {
            //如果没有注明且类型是常量，则推断为 1..1
            if(expression.type == Constant) {
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
    get() = getOrPutUserData(CwtMemberConfig.Keys.scopeContext, ParadoxScopeContext.EMPTY) action@{
        val map = replaceScopes ?: return@action null
        ParadoxScopeContext.resolve(map)
    }
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtMemberConfig<*>.replaceScopes
    get() = getOrPutUserData(CwtMemberConfig.Keys.replaceScopes, emptyMap()) action@{
        val option = findOption { it.key == "replace_scope" || it.key == "replace_scopes" }
        if(option == null) return@action null
        val options = option.findOptions() ?: return@action null
        options.associateBy({ it.key.lowercase() }, { it.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) } })
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

fun <T : PsiElement> CwtMemberConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
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

val CwtMemberConfig.Keys.overriddenProvider by lazy { Key.create<ParadoxOverriddenConfigProvider>("paradox.cwtDataConfig.overriddenProvider") }

var CwtMemberConfig<*>.overriddenProvider by CwtMemberConfig.Keys.overriddenProvider
