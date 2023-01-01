package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.config.setting.*
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.model.*

interface CwtConfigGroup {
	val gameType: ParadoxGameType?
	val project: Project
	val info: CwtConfigGroupInfo
	
	val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSetting>> //EXTENDED BY PLS
	val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSetting>> //EXTENDED BY PLS
	
	val systemScopes: Map<@CaseInsensitive String, CwtSystemScopeConfig> //EXTENDED BY PLS
	val localisationLocales: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationLocalesNoDefault: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationLocalesByCode: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationPredefinedParameters: Map<String, CwtLocalisationPredefinedParameterConfig> //EXTENDED BY PLS
	
	val onActions: Map<String, ParadoxOnActionInfo>
	
	val folders: Set<String>
	val types: Map<String, CwtTypeConfig>
	val values: Map<String, CwtEnumValueConfig>
	//enumValue可以是int、float、bool类型，统一用字符串表示
	val enums: Map<String, CwtEnumValueConfig>
	//基于enum_name进行定位，对应的可能是key/value
	val complexEnums: Map<String, CwtComplexEnumConfig>
	
	val links: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsScopeNotData: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsScopeWithPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsScopeWithoutPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsValueNotData: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsValueWithPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
	val linksAsValueWithoutPrefix: Map<@CaseInsensitive String, CwtLinkConfig>
	val localisationLinks: Map<@CaseInsensitive String, CwtLocalisationLinkConfig>
	val localisationCommands: Map<@CaseInsensitive String, CwtLocalisationCommandConfig>
	
	val modifierCategories: Map<String, CwtModifierCategoryConfig>
	val modifiers: Map<String, CwtModifierConfig>
	val scopes: Map<@CaseInsensitive String, CwtScopeConfig>
	val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig>
	val scopeGroups: Map<String, CwtScopeGroupConfig>
	//同名的single_alias可以有多个
	val singleAliases: Map<String, List<CwtSingleAliasConfig>>
	//同名的alias可以有多个
	val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>>
	//inline_script
	val inlineConfigGroup: Map<String, List<CwtInlineConfig>>
	
	val declarations: MutableMap<String, CwtDeclarationConfig>
	
	//目前版本的CWT配置已经不再使用
	val modifierCategoryIdMap: Map<String, CwtModifierCategoryConfig>
	//常量字符串的别名的组名的映射
	val aliasKeysGroupConst: Map<String, Map<@CaseInsensitive String, String>>
	//非常量字符串的别名的组名的映射
	val aliasKeysGroupNoConst: Map<String, Set<String>>
	val linksAsScopeWithPrefixSorted: List<CwtLinkConfig>
	val linksAsValueWithPrefixSorted: List<CwtLinkConfig>
	val linksAsScopeWithoutPrefixSorted: List<CwtLinkConfig>
	val linksAsValueWithoutPrefixSorted: List<CwtLinkConfig>
	
	//支持作用域上下文的CWT别名规则
	val aliasNameSupportScope: Set<String>
	//支持作用域上下文的定义类型
	val definitionTypesSupportScope: Set<String>
	//支持参数的定义类型
	val definitionTypesSupportParameters: Set<String>
}
