package icu.windea.pls.config.option

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.RegistedKey
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.core.util.copy
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeContext

abstract class CwtOptionDataHolderBase : UserDataHolderBase(), CwtOptionDataHolder {
    object Keys : KeyRegistry() {
        val optionConfigs by registerKey<List<CwtOptionMemberConfig<*>>>(this, emptyList())
        val apiStatus by registerKey<CwtApiStatus?>(this)
        val cardinality by registerKey<CwtCardinalityExpression?>(this)
        val cardinalityMinDefine by registerKey<String?>(this)
        val cardinalityMaxDefine by registerKey<String?>(this)
        val predicate by registerKey<Map<String, ReversibleValue<String>>?>(this)
        val replaceScopes by registerKey<Map<String, String>?>(this)
        val pushScope by registerKey<String?>(this)
        val scopeContext by registerKey<ParadoxScopeContext?>(this)
        val supportedScopes by registerKey(this, ParadoxScopeManager.anyScopeIdSet)
        val type by registerKey<String?>(this)
        val hint by registerKey<String?>(this)
        val eventType by registerKey<String?>(this)
        val contextKey by registerKey<String?>(this)
        val contextConfigsType by registerKey(this, "single")
        val group by registerKey<String?>(this)
        val typeKeyFilter by registerKey<ReversibleValue<Set<@CaseInsensitive String>>?>(this)
        val typeKeyRegex by registerKey<Regex?>(this)
        val startsWith by registerKey<String?>(this)
        val onlyIfNot by registerKey<Set<String>?>(this)
        val graphRelatedTypes by registerKey<Set<String>?>(this)
        val severity by registerKey<String?>(this)
        val fileExtensions by registerKey<Set<String>?>(this)
        val modifierCategories by registerKey<Set<String>?>(this)
        val colorType by registerKey<String?>(this)
        val inject by registerKey<String?>(this)
        val required by registerKey(this, false)
        val primary by registerKey(this, false)
        val inherit by registerKey(this, false)
        val tag by registerKey(this, false)
        val caseInsensitive by registerKey(this, false)
        val perDefinition by registerKey(this, false)
    }

    // region Accessors

    // see: icu.windea.pls.inject.injectors.opt.InlinedDelegateFieldCodeInjectors.CwtOptionDataHolderBase

    final override var optionConfigs by Keys.optionConfigs
    final override var apiStatus by Keys.apiStatus
    final override var cardinality by Keys.cardinality
    final override var cardinalityMinDefine by Keys.cardinalityMinDefine
    final override var cardinalityMaxDefine by Keys.cardinalityMaxDefine
    final override var predicate by Keys.predicate
    final override var replaceScopes by Keys.replaceScopes
    final override var pushScope by Keys.pushScope
    final override var scopeContext by Keys.scopeContext
    final override var supportedScopes by Keys.supportedScopes
    final override var type: String? by Keys.type
    final override var hint: String? by Keys.hint
    final override var eventType: String? by Keys.eventType
    final override var contextKey: String? by Keys.contextKey
    final override var contextConfigsType by Keys.contextConfigsType
    final override var group by Keys.group
    final override var typeKeyFilter by Keys.typeKeyFilter
    final override var typeKeyRegex by Keys.typeKeyRegex
    final override var startsWith by Keys.startsWith
    final override var onlyIfNot by Keys.onlyIfNot
    final override var graphRelatedTypes by Keys.graphRelatedTypes
    final override var severity by Keys.severity
    final override var fileExtensions by Keys.fileExtensions
    final override var modifierCategories by Keys.modifierCategories
    final override var colorType by Keys.colorType
    final override var inject by Keys.inject
    final override var required by Keys.required
    final override var primary by Keys.primary
    final override var inherit by Keys.inherit
    final override var tag by Keys.tag
    final override var caseInsensitive by Keys.caseInsensitive
    final override var perDefinition by Keys.perDefinition

    // endregion

    final override fun copyTo(target: CwtOptionDataHolder) {
        val keys = userMap.keys
        for (key in keys) {
            if (key !is RegistedKey || key.registry != Keys) continue
            key.copy(this, target, ifPresent = true)
        }
    }
}
