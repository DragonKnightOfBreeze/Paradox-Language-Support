package icu.windea.pls.config.config

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

var CwtMemberConfig<*>.inlineableConfig: CwtInlineableConfig<CwtMemberElement, CwtMemberConfig<*>>? by createKeyDelegate(CwtMemberConfig.Keys)

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
        if(option == null) {
            //如果没有注明且类型是常量，则推断为 1..1
            if(expression.type == CwtDataTypes.Constant) {
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
        if(option == null) return@action null
        val options1 = option.options ?: return@action null
        buildMap {
            for(option1 in options1) {
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
        if(r.isNullOrEmpty()) ParadoxScopeManager.anyScopeIdSet else r
    }

var CwtMemberConfig<*>.originalConfig: CwtMemberConfig<CwtMemberElement>? by createKeyDelegate(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.overriddenProvider: CwtOverriddenConfigProvider? by createKeyDelegate(CwtMemberConfig.Keys)

var CwtMemberConfig<*>.declarationConfigContext: CwtDeclarationConfigContext? by createKeyDelegate(CwtMemberConfig.Keys)
var CwtMemberConfig<*>.declarationConfigCacheKey: String? by createKeyDelegate(CwtMemberConfig.Keys)
