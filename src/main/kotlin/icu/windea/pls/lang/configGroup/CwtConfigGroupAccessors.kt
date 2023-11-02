package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

inline val CwtConfigGroup.foldingSettings get() = get(CwtConfigGroup.Keys.foldingSettings) 
inline val CwtConfigGroup.postfixTemplateSettings get() = get(CwtConfigGroup.Keys.postfixTemplateSettings) 

inline val CwtConfigGroup.systemLinks get() = get(CwtConfigGroup.Keys.systemLinks) 
inline val CwtConfigGroup.localisationLocalesById get() = get(CwtConfigGroup.Keys.localisationLocalesById) 
inline val CwtConfigGroup.localisationLocalesByCode get() = get(CwtConfigGroup.Keys.localisationLocalesByCode) 
inline val CwtConfigGroup.localisationPredefinedParameters get() = get(CwtConfigGroup.Keys.localisationPredefinedParameters) 

inline val CwtConfigGroup.folders get() = get(CwtConfigGroup.Keys.folders)

inline val CwtConfigGroup.types get() = get(CwtConfigGroup.Keys.types) 
inline val CwtConfigGroup.swappedTypes get() = get(CwtConfigGroup.Keys.swappedTypes) 
inline val CwtConfigGroup.type2ModifiersMap get() = get(CwtConfigGroup.Keys.type2ModifiersMap) 

inline val CwtConfigGroup.declarations get() = get(CwtConfigGroup.Keys.declarations) 

inline val CwtConfigGroup.values get() = get(CwtConfigGroup.Keys.values) 
inline val CwtConfigGroup.enums get() = get(CwtConfigGroup.Keys.enums) 
inline val CwtConfigGroup.complexEnums get() = get(CwtConfigGroup.Keys.complexEnums) 

inline val CwtConfigGroup.links get() = get(CwtConfigGroup.Keys.links) 
inline val CwtConfigGroup.linksAsScopeNotData get() = get(CwtConfigGroup.Keys.linksAsScopeNotData) 
inline val CwtConfigGroup.linksAsScopeWithPrefix get() = get(CwtConfigGroup.Keys.linksAsScopeWithPrefix) 
inline val CwtConfigGroup.linksAsScopeWithoutPrefix get() = get(CwtConfigGroup.Keys.linksAsScopeWithoutPrefix) 
inline val CwtConfigGroup.linksAsValueNotData get() = get(CwtConfigGroup.Keys.linksAsValueNotData) 
inline val CwtConfigGroup.linksAsValueWithPrefix get() = get(CwtConfigGroup.Keys.linksAsValueWithPrefix) 
inline val CwtConfigGroup.linksAsValueWithoutPrefix get() = get(CwtConfigGroup.Keys.linksAsValueWithoutPrefix) 

inline val CwtConfigGroup.localisationLinks get() = get(CwtConfigGroup.Keys.localisationLinks) 
inline val CwtConfigGroup.localisationCommands get() = get(CwtConfigGroup.Keys.localisationCommands) 

inline val CwtConfigGroup.scopes get() = get(CwtConfigGroup.Keys.scopes) 
inline val CwtConfigGroup.scopeAliasMap get() = get(CwtConfigGroup.Keys.scopeAliasMap) 
inline val CwtConfigGroup.scopeGroups get() = get(CwtConfigGroup.Keys.scopeGroups) 

inline val CwtConfigGroup.singleAliases get() = get(CwtConfigGroup.Keys.singleAliases) 
inline val CwtConfigGroup.aliasGroups get() = get(CwtConfigGroup.Keys.aliasGroups) 
inline val CwtConfigGroup.inlineConfigGroup get() = get(CwtConfigGroup.Keys.inlineConfigGroup) 

inline val CwtConfigGroup.gameRules get() = get(CwtConfigGroup.Keys.gameRules) 
inline val CwtConfigGroup.onActions get() = get(CwtConfigGroup.Keys.onActions) 

inline val CwtConfigGroup.modifierCategories get() = get(CwtConfigGroup.Keys.modifierCategories) 
inline val CwtConfigGroup.modifiers get() = get(CwtConfigGroup.Keys.modifiers) 
inline val CwtConfigGroup.predefinedModifiers get() = get(CwtConfigGroup.Keys.predefinedModifiers) 
inline val CwtConfigGroup.generatedModifiers get() = get(CwtConfigGroup.Keys.generatedModifiers) 

inline val CwtConfigGroup.aliasKeysGroupConst get() = get(CwtConfigGroup.Keys.aliasKeysGroupConst) 
inline val CwtConfigGroup.aliasKeysGroupNoConst get() = get(CwtConfigGroup.Keys.aliasKeysGroupNoConst) 

inline val CwtConfigGroup.linksAsScopeWithPrefixSorted get() = get(CwtConfigGroup.Keys.linksAsScopeWithPrefixSorted) 
inline val CwtConfigGroup.linksAsValueWithPrefixSorted get() = get(CwtConfigGroup.Keys.linksAsValueWithPrefixSorted) 
inline val CwtConfigGroup.linksAsScopeWithoutPrefixSorted get() = get(CwtConfigGroup.Keys.linksAsScopeWithoutPrefixSorted) 
inline val CwtConfigGroup.linksAsValueWithoutPrefixSorted get() = get(CwtConfigGroup.Keys.linksAsValueWithoutPrefixSorted) 
inline val CwtConfigGroup.linksAsVariable get() = get(CwtConfigGroup.Keys.linksAsVariable) 

inline val CwtConfigGroup.aliasNamesSupportScope get() = get(CwtConfigGroup.Keys.aliasNamesSupportScope) 
inline val CwtConfigGroup.definitionTypesSupportScope get() = get(CwtConfigGroup.Keys.definitionTypesSupportScope) 
inline val CwtConfigGroup.definitionTypesIndirectSupportScope get() = get(CwtConfigGroup.Keys.definitionTypesIndirectSupportScope) 
inline val CwtConfigGroup.definitionTypesSkipCheckSystemLink get() = get(CwtConfigGroup.Keys.definitionTypesSkipCheckSystemLink) 
inline val CwtConfigGroup.definitionTypesSupportParameters get() = get(CwtConfigGroup.Keys.definitionTypesSupportParameters) 
