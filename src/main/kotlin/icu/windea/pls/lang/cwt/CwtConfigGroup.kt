package icu.windea.pls.lang.cwt

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.util.containers.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.setting.*
import icu.windea.pls.lang.model.*

interface CwtConfigGroup: UserDataHolder {
	val gameType: ParadoxGameType?
	val project: Project
	val info: CwtConfigGroupInfo
	
	val foldingSettings: Map<String, Map<@CaseInsensitive String, CwtFoldingSetting>> //EXTENDED BY PLS
	val postfixTemplateSettings: Map<String, Map<@CaseInsensitive String, CwtPostfixTemplateSetting>> //EXTENDED BY PLS
	
	val systemLinks: Map<@CaseInsensitive String, CwtSystemLinkConfig> //EXTENDED BY PLS
	val localisationLocales: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationLocalesNoDefault: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationLocalesNoDefaultNoPrefix: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationLocalesByCode: Map<String, CwtLocalisationLocaleConfig> //EXTENDED BY PLS
	val localisationPredefinedParameters: Map<String, CwtLocalisationPredefinedParameterConfig> //EXTENDED BY PLS
	
	val folders: Set<String>
	
	val types: Map<String, CwtTypeConfig>
	//typeExpression - swapType
	val typeToBaseTypeMap: BidirectionalMap<String, String>
	//typeExpression - modifierSimpleName - modifierConfig
	//job - job_$_add - <config>
	val typeToModifiersMap: Map<String, Map<String, CwtModifierConfig>>
	
	val declarations: Map<String, CwtDeclarationConfig>
	
	val values: Map<String, CwtEnumConfig>
	//enumValue可以是int、float、bool类型，统一用字符串表示
	val enums: Map<String, CwtEnumConfig>
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
	
	val scopes: Map<@CaseInsensitive String, CwtScopeConfig>
	val scopeAliasMap: Map<@CaseInsensitive String, CwtScopeConfig>
	val scopeGroups: Map<String, CwtScopeGroupConfig>
	
	val singleAliases: Map<String, CwtSingleAliasConfig>
	//同名的alias可以有多个
	val aliasGroups: Map<String, Map<String, List<CwtAliasConfig>>>
	//inline_script
	val inlineConfigGroup: Map<String, List<CwtInlineConfig>>
	
	// key: scalar
	val gameRules: Map<String, CwtGameRuleConfig>
	// key: scalar / template_expression
	val onActions: Map<String, CwtOnActionConfig>
	
	val modifierCategories: Map<String, CwtModifierCategoryConfig>
	val modifierCategoryIdMap: Map<String, CwtModifierCategoryConfig>
	// key: scalar / template_expression
	val modifiers: Map<@CaseInsensitive String, CwtModifierConfig>
	val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
	val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig>
	
	//常量字符串的别名的组名的映射
	val aliasKeysGroupConst: Map<String, Map<@CaseInsensitive String, String>>
	//非常量字符串的别名的组名的映射
	val aliasKeysGroupNoConst: Map<String, Set<String>>
	
	//处理后的连接规则
	val linksAsScopeWithPrefixSorted: List<CwtLinkConfig>
	val linksAsValueWithPrefixSorted: List<CwtLinkConfig>
	val linksAsScopeWithoutPrefixSorted: List<CwtLinkConfig>
	val linksAsValueWithoutPrefixSorted: List<CwtLinkConfig>
	val linksAsVariable: List<CwtLinkConfig>
	
	//必定支持作用域的CWT别名规则
	val aliasNamesSupportScope: Set<String>
	//必定支持作用域的定义类型
	val definitionTypesSupportScope: Set<String>
	//必定间接支持作用域的定义类型
	val definitionTypesIndirectSupportScope: Set<String> 
	//不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）
	val definitionTypesSkipCheckSystemLink: Set<String>
	//支持参数的定义类型
	val definitionTypesSupportParameters: Set<String>
	
	object Keys
}
