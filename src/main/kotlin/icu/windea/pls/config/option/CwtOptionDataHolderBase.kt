package icu.windea.pls.config.option

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.RegistedKey
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.model.scope.ParadoxScopeContext

// region Accessor Implementations

private val CwtOptionDataHolderBase.from get() = this as UserDataHolder

private object CwtOptionDataKeys : KeyRegistry()

private var UserDataHolder.optionConfigs: List<CwtOptionMemberConfig<*>>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.apiStatus: CwtApiStatus?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.cardinality: CwtCardinalityExpression?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.cardinalityMinDefine: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.cardinalityMaxDefine: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.predicate: Map<String, ReversibleValue<String>>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.replaceScopes: Map<String, String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.pushScope: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.scopeContext: ParadoxScopeContext?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.supportedScopes: Set<String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.type: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.hint: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.eventType: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.contextKey: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.contextConfigsType: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.group: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.typeKeyRegex: Regex?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.startsWith: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.onlyIfNot: Set<String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.graphRelatedTypes: Set<String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.severity: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.fileExtensions: Set<String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.modifierCategories: Set<String>?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.colorType: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.inject: String?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.required: Boolean?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.primary: Boolean?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.inherit: Boolean?
    by registerKey(CwtOptionDataKeys)
private var UserDataHolder.tag: Boolean?
    by registerKey(CwtOptionDataKeys)

// endregion

abstract class CwtOptionDataHolderBase : UserDataHolderBase(), CwtOptionDataHolder {
    override fun copyTo(target: CwtOptionDataHolder) {
        val keys = userMap.keys
        for (key in keys) {
            if (key !is RegistedKey || key.registry != CwtOptionDataKeys) continue
            val v = getUserData(key) ?: continue
            @Suppress("UNCHECKED_CAST")
            key as Key<Any>
            target.putUserData(key, v)
        }
    }

    override var optionConfigs: List<CwtOptionMemberConfig<*>>
        get() = from.optionConfigs ?: emptyList()
        set(value) = run { from.optionConfigs = value }
    override var apiStatus: CwtApiStatus?
        get() = from.apiStatus
        set(value) = run { from.apiStatus = value }
    override var cardinality: CwtCardinalityExpression?
        get() = from.cardinality
        set(value) = run { from.cardinality = value }
    override var cardinalityMinDefine: String?
        get() = from.cardinalityMinDefine
        set(value) = run { from.cardinalityMinDefine = value }
    override var cardinalityMaxDefine: String?
        get() = from.cardinalityMaxDefine
        set(value) = run { from.cardinalityMaxDefine = value }
    override var predicate: Map<String, ReversibleValue<String>>?
        get() = from.predicate
        set(value) = run { from.predicate = value }
    override var replaceScopes: Map<String, String>?
        get() = from.replaceScopes
        set(value) = run { from.replaceScopes = value }
    override var pushScope: String?
        get() = from.pushScope
        set(value) = run { from.pushScope = value }
    override var scopeContext: ParadoxScopeContext?
        get() = from.scopeContext
        set(value) = run { from.scopeContext = value }
    override var supportedScopes: Set<String>
        get() = from.supportedScopes.orEmpty()
        set(value) = run { from.supportedScopes = value }
    override var type: String?
        get() = from.type
        set(value) = run { from.type = value }
    override var hint: String?
        get() = from.hint
        set(value) = run { from.hint = value }
    override var eventType: String?
        get() = from.eventType
        set(value) = run { from.eventType = value }
    override var contextKey: String?
        get() = from.contextKey
        set(value) = run { from.contextKey = value }
    override var contextConfigsType: String
        get() = from.contextConfigsType ?: "single"
        set(value) = run { from.contextConfigsType = value }
    override var group: String?
        get() = from.group
        set(value) = run { from.group = value }
    override var typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
        get() = from.typeKeyFilter
        set(value) = run { from.typeKeyFilter = value }
    override var typeKeyRegex: Regex?
        get() = from.typeKeyRegex
        set(value) = run { from.typeKeyRegex = value }
    override var startsWith: String?
        get() = from.startsWith
        set(value) = run { from.startsWith = value }
    override var onlyIfNot: Set<String>?
        get() = from.onlyIfNot
        set(value) = run { from.onlyIfNot = value }
    override var graphRelatedTypes: Set<String>?
        get() = from.graphRelatedTypes
        set(value) = run { from.graphRelatedTypes = value }
    override var severity: String?
        get() = from.severity
        set(value) = run { from.severity = value }
    override var fileExtensions: Set<String>?
        get() = from.fileExtensions
        set(value) = run { from.fileExtensions = value }
    override var modifierCategories: Set<String>?
        get() = from.modifierCategories
        set(value) = run { from.modifierCategories = value }
    override var colorType: String?
        get() = from.colorType
        set(value) = run { from.colorType = value }
    override var inject: String?
        get() = from.inject
        set(value) = run { from.inject = value }
    override var required: Boolean
        get() = from.required ?: false
        set(value) = run { from.required = value }
    override var primary: Boolean
        get() = from.primary ?: false
        set(value) = run { from.primary = value }
    override var inherit: Boolean
        get() = from.inherit ?: false
        set(value) = run { from.inherit = value }
    override var tag: Boolean
        get() = from.tag ?: false
        set(value) = run { from.tag = value }
}
