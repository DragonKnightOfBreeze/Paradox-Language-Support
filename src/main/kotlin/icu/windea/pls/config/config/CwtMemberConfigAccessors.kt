package icu.windea.pls.config.config

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.config.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

val CwtMemberConfig.Keys.cardinality by createKey<CwtCardinalityExpression>("cwt.memberConfig.cardinality")
val CwtMemberConfig.Keys.cardinalityMinDefine by createKey<String>("cwt.memberConfig.cardinalityMinDefine")
val CwtMemberConfig.Keys.cardinalityMaxDefine by createKey<String>("cwt.memberConfig.cardinalityMaxDefine")
val CwtMemberConfig.Keys.hasScopeOption by createKey<Boolean>("cwt.memberConfig.hasScopeOption")
val CwtMemberConfig.Keys.scopeContext by createKey<ParadoxScopeContext>("cwt.memberConfig.scopeContext")
val CwtMemberConfig.Keys.replaceScopes by createKey<Map<String, String>>("cwt.memberConfig.replaceScopes")
val CwtMemberConfig.Keys.pushScope by createKey<String>("cwt.memberConfig.pushScope")
val CwtMemberConfig.Keys.supportedScopes by createKey<Set<String>>("cwt.memberConfig.supportedScopes")
val CwtMemberConfig.Keys.originalConfig by createKey<CwtMemberConfig<*>>("cwt.memberConfig.originalConfig")
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

val CwtMemberConfig<*>.scopeContext
    get() = getOrPutUserData(CwtMemberConfig.Keys.scopeContext, ParadoxScopeContext.Empty) action@{
        val replaceScopes = replaceScopes
        val pushScope = pushScope
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }
        scopeContext?.resolveNext(pushScope) ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
    }

//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
// * an extended (definition/ game type / on action/ inline_script / parameter) config
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
//* a definition / game type / on action config
// * an extended (definition/ game type / on action/ inline_script / parameter) config
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

val CwtMemberConfig<*>.originalConfig 
    get() = getUserData(CwtMemberConfig.Keys.originalConfig) ?: this 
val CwtMemberConfig<*>.overriddenProvider 
    get() = getUserData(CwtMemberConfig.Keys.overriddenProvider) 