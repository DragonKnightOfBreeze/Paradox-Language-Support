package icu.windea.pls.config.option

import icu.windea.pls.config.CwtConfigApiStatus
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.copy
import icu.windea.pls.core.util.get
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.metadata.MetadataMapBase
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext

abstract class CwtOptionMetadataBase : MetadataMapBase(), CwtOptionMetadata {
    // 3.0.1 use explicit code with folding, instead of delegate properties with addon code injector, to make things simple

    final override var optionConfigs: List<CwtOptionMemberConfig<*>> // region
        get() = this[Keys.optionConfigs]
        set(value) = run { this[Keys.optionConfigs] = value } // endregion
    final override var apiStatus: CwtConfigApiStatus? // region
        get() = this[Keys.apiStatus]
        set(value) = run { this[Keys.apiStatus] = value } // endregion
    final override var cardinality: CwtCardinalityExpression? // region
        get() = this[Keys.cardinality]
        set(value) = run { this[Keys.cardinality] = value } // endregion
    final override var cardinalityMinDefine: String? // region
        get() = this[Keys.cardinalityMinDefine]
        set(value) = run { this[Keys.cardinalityMinDefine] = value } // endregion
    final override var cardinalityMaxDefine: String? // region
        get() = this[Keys.cardinalityMaxDefine]
        set(value) = run { this[Keys.cardinalityMaxDefine] = value } // endregion
    final override var predicate: Map<String, ReversibleValue<String>>? // region
        get() = this[Keys.predicate]
        set(value) = run { this[Keys.predicate] = value } // endregion
    final override var pushScope: String? // region
        get() = this[Keys.pushScope]
        set(value) = run { this[Keys.pushScope] = value } // endregion
    final override var replaceScopes: Map<String, String>? // region
        get() = this[Keys.replaceScopes]
        set(value) = run { this[Keys.replaceScopes] = value } // endregion
    final override var scopeContext: ParadoxScopeContext? // region
        get() = this[Keys.scopeContext]
        set(value) = run { this[Keys.scopeContext] = value } // endregion
    final override var supportedScopes: Set<String> // region
        get() = this[Keys.supportedScopes]
        set(value) = run { this[Keys.supportedScopes] = value } // endregion
    final override var type: String? // region
        get() = this[Keys.type]
        set(value) = run { this[Keys.type] = value } // endregion
    final override var hint: String? // region
        get() = this[Keys.hint]
        set(value) = run { this[Keys.hint] = value } // endregion
    final override var eventType: String? // region
        get() = this[Keys.eventType]
        set(value) = run { this[Keys.eventType] = value } // endregion
    final override var contextKey: String? // region
        get() = this[Keys.contextKey]
        set(value) = run { this[Keys.contextKey] = value } // endregion
    final override var contextConfigsType: String? // region
        get() = this[Keys.contextConfigsType]
        set(value) = run { this[Keys.contextConfigsType] = value } // endregion
    final override var group: String? // region
        get() = this[Keys.group]
        set(value) = run { this[Keys.group] = value } // endregion
    final override var typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>? // region
        get() = this[Keys.typeKeyFilter]
        set(value) = run { this[Keys.typeKeyFilter] = value } // endregion
    final override var typeKeyRegex: Regex? // region
        get() = this[Keys.typeKeyRegex]
        set(value) = run { this[Keys.typeKeyRegex] = value } // endregion
    final override var startsWith: String? // region
        get() = this[Keys.startsWith]
        set(value) = run { this[Keys.startsWith] = value } // endregion
    final override var onlyIfNot: Set<String>? // region
        get() = this[Keys.onlyIfNot]
        set(value) = run { this[Keys.onlyIfNot] = value } // endregion
    final override var graphRelatedTypes: Set<String>? // region
        get() = this[Keys.graphRelatedTypes]
        set(value) = run { this[Keys.graphRelatedTypes] = value } // endregion
    final override var declareComplexEnum: String? // region
        get() = this[Keys.declareComplexEnum]
        set(value) = run { this[Keys.declareComplexEnum] = value } // endregion
    final override var severity: String? // region
        get() = this[Keys.severity]
        set(value) = run { this[Keys.severity] = value } // endregion
    final override var modifierCategories: Set<String>? // region
        get() = this[Keys.modifierCategories]
        set(value) = run { this[Keys.modifierCategories] = value } // endregion
    final override var colorType: String? // region
        get() = this[Keys.colorType]
        set(value) = run { this[Keys.colorType] = value } // endregion
    final override var fileExtensions: Set<String>? // region
        get() = this[Keys.fileExtensions]
        set(value) = run { this[Keys.fileExtensions] = value } // endregion
    final override var inject: String? // region
        get() = this[Keys.inject]
        set(value) = run { this[Keys.inject] = value } // endregion
    final override var required: Boolean // region
        get() = this[Keys.required]
        set(value) = run { this[Keys.required] = value } // endregion
    final override var primary: Boolean // region
        get() = this[Keys.primary]
        set(value) = run { this[Keys.primary] = value } // endregion
    final override var inherit: Boolean // region
        get() = this[Keys.inherit]
        set(value) = run { this[Keys.inherit] = value } // endregion
    final override var tag: Boolean // region
        get() = this[Keys.tag]
        set(value) = run { this[Keys.tag] = value } // endregion
    final override var caseInsensitive: Boolean // region
        get() = this[Keys.caseInsensitive]
        set(value) = run { this[Keys.caseInsensitive] = value } // endregion
    final override var perDefinition: Boolean // region
        get() = this[Keys.perDefinition]
        set(value) = run { this[Keys.perDefinition] = value } // endregion

    final override fun copyTo(target: CwtOptionMetadata) {
        if (target !is CwtOptionMetadataBase) throw IllegalStateException()
        val keys = Keys.keys
        keys.forEachFast { key ->
            key.copy(this, target, ifPresent = true)
        }
    }
}

object CwtOptionMetadataKeys : KeyRegistry() {
    val optionConfigs by registerKey<List<CwtOptionMemberConfig<*>>>(this, emptyList())
    val apiStatus by registerKey<CwtConfigApiStatus?>(this)
    val cardinality by registerKey<CwtCardinalityExpression?>(this)
    val cardinalityMinDefine by registerKey<String?>(this)
    val cardinalityMaxDefine by registerKey<String?>(this)
    val predicate by registerKey<Map<String, ReversibleValue<String>>?>(this)
    val pushScope by registerKey<String?>(this)
    val replaceScopes by registerKey<Map<String, String>?>(this)
    val scopeContext by registerKey<ParadoxScopeContext?>(this)
    val supportedScopes by registerKey(this, ParadoxScopeConstants.anyScopes)
    val type by registerKey<String?>(this)
    val hint by registerKey<String?>(this)
    val eventType by registerKey<String?>(this)
    val contextKey by registerKey<String?>(this)
    val contextConfigsType by registerKey<String?>(this)
    val group by registerKey<String?>(this)
    val typeKeyFilter by registerKey<ReversibleValue<Set<@CaseInsensitive String>>?>(this)
    val typeKeyRegex by registerKey<Regex?>(this)
    val startsWith by registerKey<String?>(this)
    val onlyIfNot by registerKey<Set<String>?>(this)
    val graphRelatedTypes by registerKey<Set<String>?>(this)
    val declareComplexEnum by registerKey<String?>(this)
    val severity by registerKey<String?>(this)
    val modifierCategories by registerKey<Set<String>?>(this)
    val colorType by registerKey<String?>(this)
    val fileExtensions by registerKey<Set<String>?>(this)
    val inject by registerKey<String?>(this)
    val required by registerKey(this, false)
    val primary by registerKey(this, false)
    val inherit by registerKey(this, false)
    val tag by registerKey(this, false)
    val caseInsensitive by registerKey(this, false)
    val perDefinition by registerKey(this, false)
}

private typealias Keys = CwtOptionMetadataKeys
