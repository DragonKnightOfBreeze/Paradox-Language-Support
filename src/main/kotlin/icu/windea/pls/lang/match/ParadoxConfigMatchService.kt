package icu.windea.pls.lang.match

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.floatValue
import icu.windea.pls.config.config.intValue
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.toOccurrence
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.isIncomplete
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isPropertyValue
import icu.windea.pls.script.psi.properties
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.values

object ParadoxConfigMatchService {
    private val CwtConfigGroup.typeConfigsCache by createKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtTypeConfig>> { path ->
            types.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }
    private val CwtConfigGroup.complexEnumConfigsCache by createKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtComplexEnumConfig>> { path ->
            complexEnums.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }

    // region Type Config & Subtype Config

    fun getMatchedTypeConfig(
        element: ParadoxScriptDefinitionElement,
        configGroup: CwtConfigGroup,
        path: ParadoxPath,
        elementPath: ParadoxElementPath,
        typeKey: String,
        typeKeyPrefix: Lazy<String?>?
    ): CwtTypeConfig? {
        // 优先从基于文件路径的缓存中获取
        val configs = configGroup.typeConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesType(element, config, null, elementPath, typeKey, typeKeyPrefix) }
    }

    fun getMatchedTypeConfig(
        node: LighterASTNode,
        tree: LighterAST,
        configGroup: CwtConfigGroup,
        path: ParadoxPath,
        elementPath: ParadoxElementPath,
        typeKey: String,
        typeKeyPrefix: Lazy<String?>?
    ): CwtTypeConfig? {
        // 优先从基于文件路径的缓存中获取
        val configs = configGroup.typeConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesType(node, tree, config, null, elementPath, typeKey, typeKeyPrefix) }
    }

    fun matchesType(
        element: ParadoxScriptDefinitionElement,
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxElementPath?,
        typeKey: String?,
        typeKeyPrefix: Lazy<String?>?
    ): Boolean {
        // 判断definition是否需要是scriptFile还是scriptProperty
        run {
            if (typeConfig.typePerFile) {
                if (element !is ParadoxScriptFile) return false
            } else {
                if (element !is ParadoxScriptProperty) return false
            }
        }

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, typeKey, typeKeyPrefix)
        if (fastResult != null) return fastResult

        // 判断definition的propertyValue是否需要是block
        run {
            val configGroup = typeConfig.configGroup
            val declarationConfig = configGroup.declarations.get(typeConfig.name)?.configForDeclaration ?: return@run
            val propertyValue = element.castOrNull<ParadoxScriptProperty>()?.propertyValue ?: return@run
            // 兼容进行代码补全时用户输入未完成的情况
            val isIncomplete = propertyValue.elementType == STRING
                && propertyValue.text == PlsConstants.dummyIdentifier
                && propertyValue.isIncomplete()
            if (isIncomplete) return@run
            val isBlock = propertyValue.elementType == BLOCK
            val isBlockConfig = declarationConfig.valueExpression.type == CwtDataTypes.Block
            if (isBlockConfig != isBlock) return false
        }

        return true
    }

    fun matchesType(
        node: LighterASTNode,
        tree: LighterAST,
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxElementPath?,
        typeKey: String?,
        typeKeyPrefix: Lazy<String?>?
    ): Boolean {
        // 判断definition是否需要是scriptFile还是scriptProperty
        run {
            val elementType = node.tokenType
            if (typeConfig.typePerFile) {
                if (elementType != ParadoxScriptFile.ELEMENT_TYPE) return false
            } else {
                if (elementType != PROPERTY) return false
            }
        }

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, typeKey, typeKeyPrefix)
        if (fastResult != null) return fastResult

        // 判断definition的propertyValue是否需要是block
        run {
            val configGroup = typeConfig.configGroup
            val declarationConfig = configGroup.declarations.get(typeConfig.name)?.configForDeclaration ?: return@run
            val propertyValue = node.firstChild(tree, ParadoxScriptTokenSets.VALUES) ?: return@run
            val isBlock = propertyValue.tokenType == BLOCK
            val isBlockConfig = declarationConfig.valueExpression.type == CwtDataTypes.Block
            if (isBlockConfig != isBlock) return false
        }

        return true
    }

    fun matchesTypeByUnknownDeclaration(
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxElementPath?,
        typeKey: String?,
        typeKeyPrefix: Lazy<String?>?
    ): Boolean {
        // 判断element是否需要是scriptFile还是scriptProperty
        if (typeConfig.typePerFile) return false

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, typeKey, typeKeyPrefix)
        if (fastResult == false) return fastResult

        return true
    }

    fun matchesTypeFast(
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxElementPath?,
        typeKey: String?,
        typeKeyPrefix: Lazy<String?>?
    ): Boolean? {
        // 判断path是否匹配
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(typeConfig, path)) return false
        }

        if (typeKey != null) {
            // 如果选项starts_with存在，则要求type_key匹配这个前缀
            val startsWithConfig = typeConfig.startsWith
            if (!startsWithConfig.isNullOrEmpty()) {
                val result = typeKey.startsWith(startsWithConfig)
                if (!result) return false
            }

            // 如果type_key_regex存在，则要求type_key匹配
            val typeKeyRegexConfig = typeConfig.typeKeyRegex
            if (typeKeyRegexConfig != null) {
                val result = typeKeyRegexConfig.matches(typeKey)
                if (!result) return false
            }

            // 如果选项type_key_filter存在，则需要通过type_key进行过滤（忽略大小写）
            val typeKeyFilterConfig = typeConfig.typeKeyFilter
            if (typeKeyFilterConfig != null && typeKeyFilterConfig.value.isNotEmpty()) {
                val result = typeKeyFilterConfig.withOperator { it.contains(typeKey) }
                if (!result) return false
            }

            // 如果name_field存在，则要求type_key必须是由type_key_filter指定的所有可能的type_key之一，或者没有指定任何type_key
            val nameFieldConfig = typeConfig.nameField
            if (nameFieldConfig != null) {
                val result = typeConfig.possibleTypeKeys.isEmpty() || typeConfig.possibleTypeKeys.contains(typeKey)
                if (!result) return false
            }
        }

        // 如果属性type_key_prefix存在，且有必要校验，则要求其与typeKeyPrefix必须一致（忽略大小写）
        if (typeKeyPrefix != null && typeConfig.name in typeConfig.configGroup.definitionTypesModel.mayWithTypeKeyPrefix) {
            val result = typeConfig.typeKeyPrefix.equals(typeKeyPrefix.value, ignoreCase = true)
            if (!result) return false
        }

        if (elementPath != null) {
            // 如果属性skip_root_key存在，则要判断是否需要跳过rootKey
            // skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
            // skip_root_key可以重复（其中之一匹配即可）
            val skipRootKeyConfig = typeConfig.skipRootKey
            if (skipRootKeyConfig.isNullOrEmpty()) {
                if (elementPath.length > 1) return false
            } else {
                if (elementPath.isEmpty()) return false
                val input = elementPath.subPaths.dropLast(1)
                val result = skipRootKeyConfig.any { PathMatcher.matches(input, it, ignoreCase = true, useAny = true, usePattern = true) }
                if (!result) return false
            }
        }

        return null // 需要进一步匹配
    }

    fun matchesSubtype(
        element: ParadoxScriptDefinitionElement,
        typeKey: String,
        subtypeConfig: CwtSubtypeConfig,
        subtypeConfigs: MutableList<CwtSubtypeConfig>,
        configGroup: CwtConfigGroup,
        matchOptions: Int = ParadoxMatchOptions.Default
    ): Boolean {
        val fastResult = matchesSubtypeFast(typeKey, subtypeConfig, subtypeConfigs)
        if (fastResult != null) return fastResult

        // 根据config对property进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true
        val finalMatchOptions = matchOptions or ParadoxMatchOptions.SkipIndex or ParadoxMatchOptions.SkipScope
        return matchesDefinitionForSubtype(element, elementConfig, configGroup, finalMatchOptions)
    }

    fun matchesSubtypeFast(
        typeKey: String,
        subtypeConfig: CwtSubtypeConfig,
        subtypeConfigs: MutableList<CwtSubtypeConfig>
    ): Boolean? {
        // 如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
        val onlyIfNotConfig = subtypeConfig.onlyIfNot
        if (!onlyIfNotConfig.isNullOrEmpty()) {
            val matchesAny = subtypeConfigs.any { it.name in onlyIfNotConfig }
            if (matchesAny) return false
        }

        // 如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = subtypeConfig.startsWith
        if (!startsWithConfig.isNullOrEmpty()) {
            val result = typeKey.startsWith(startsWithConfig, false)
            if (!result) return false
        }

        // 如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = subtypeConfig.typeKeyRegex
        if (typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(typeKey)
            if (!result) return false
        }

        // 如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
        if (typeKeyFilterConfig != null && typeKeyFilterConfig.value.isNotEmpty()) {
            val filterResult = typeKeyFilterConfig.withOperator { it.contains(typeKey) }
            if (!filterResult) return false
        }

        // 根据config对property进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true

        return null // 需要进一步匹配
    }

    private fun matchesDefinitionForSubtype(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyConfig: CwtPropertyConfig,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        // 这里不能基于内联后的声明结构，否则可能会导致SOE
        // 也不要参数条件表达式中的声明结构判断，
        val childValueConfigs = propertyConfig.values.orEmpty()
        if (childValueConfigs.isNotEmpty()) {
            // 匹配值列表
            val blockElement = definitionElement.block
            if (!matchesValuesForSubtype(blockElement, childValueConfigs, configGroup, matchOptions)) return false // 继续匹配
        }
        val childPropertyConfigs = propertyConfig.properties.orEmpty()
        if (childPropertyConfigs.isNotEmpty()) {
            // 匹配属性列表
            val blockElement = definitionElement.block
            if (!matchesPropertiesForSubtype(definitionElement, blockElement, childPropertyConfigs, configGroup, matchOptions)) return false // 继续匹配
        }
        return true
    }

    private fun matchesPropertyForSubtype(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyElement: ParadoxScriptProperty,
        propertyConfig: CwtPropertyConfig,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        val propValue = propertyElement.propertyValue
        // 对于propertyValue同样这样判断（可能脚本没有写完）
        if (propValue == null) return propertyConfig.optionData { cardinality }?.min == 0

        when {
            // 匹配布尔值
            propertyConfig.booleanValue != null -> {
                if (propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
            }
            // 匹配值
            propertyConfig.stringValue != null -> {
                val expression = ParadoxScriptExpression.resolve(propValue, matchOptions)
                return ParadoxMatchService.matchScriptExpression(propValue, expression, propertyConfig.valueExpression, propertyConfig, configGroup, matchOptions).get(matchOptions)
            }
            // 匹配single_alias
            ParadoxExpressionManager.isSingleAliasEntryConfig(propertyConfig) -> {
                return matchesSingleAliasForSubtype(definitionElement, propertyElement, propertyConfig, configGroup, matchOptions)
            }
            // 匹配alias
            ParadoxExpressionManager.isAliasEntryConfig(propertyConfig) -> {
                return matchesAliasForSubtype(definitionElement, propertyElement, propertyConfig, matchOptions)
            }
            propertyConfig.configs.orEmpty().isNotEmpty() -> {
                val blockElement = propertyElement.block
                // 匹配值列表
                if (!matchesValuesForSubtype(blockElement, propertyConfig.values.orEmpty(), configGroup, matchOptions)) return false
                // 匹配属性列表
                if (!matchesPropertiesForSubtype(definitionElement, blockElement, propertyConfig.properties.orEmpty(), configGroup, matchOptions)) return false
            }
        }
        return true
    }

    private fun matchesPropertiesForSubtype(
        definitionElement: ParadoxScriptDefinitionElement,
        blockElement: ParadoxScriptBlockElement?,
        propertyConfigs: List<CwtPropertyConfig>,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        if (propertyConfigs.isEmpty()) return true
        if (blockElement == null) return false

        val occurrenceMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.toOccurrence(definitionElement, configGroup.project) })

        // NOTE 这里需要兼容内联
        // NOTE propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
        val matched = blockElement.properties().options(inline = true).all p@{ propertyElement ->
            val keyElement = propertyElement.propertyKey
            val expression = ParadoxScriptExpression.resolve(keyElement, matchOptions)
            val propConfigs = propertyConfigs.filter {
                ParadoxMatchService.matchScriptExpression(keyElement, expression, it.keyExpression, it, configGroup, matchOptions).get(matchOptions)
            }

            // 如果没有匹配的规则则忽略
            if (propConfigs.isEmpty()) return@p true

            val matched = propConfigs.any { propConfig ->
                val matched = matchesPropertyForSubtype(definitionElement, propertyElement, propConfig, configGroup, matchOptions)
                if (matched) occurrenceMap.get(propConfig.key)?.let { it.actual++ }
                matched
            }
            matched
        }
        if (!matched) return false

        return occurrenceMap.values.all { it.isValid(relax = true) }
    }

    private fun matchesValuesForSubtype(
        blockElement: ParadoxScriptBlockElement?,
        valueConfigs: List<CwtValueConfig>,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        if (valueConfigs.isEmpty()) return true
        if (blockElement == null) return false

        val occurrenceMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.toOccurrence(blockElement, configGroup.project) })

        // NOTE 这里需要兼容内联
        val matched = blockElement.values().options(inline = true).process p@{ valueElement ->
            // 如果没有匹配的规则则忽略
            val expression = ParadoxScriptExpression.resolve(valueElement, matchOptions)

            val matched = valueConfigs.any { valueConfig ->
                val matched = ParadoxMatchService.matchScriptExpression(valueElement, expression, valueConfig.valueExpression, valueConfig, configGroup, matchOptions).get(matchOptions)
                if (matched) occurrenceMap.get(valueConfig.value)?.let { it.actual++ }
                matched
            }
            matched
        }
        if (!matched) return false

        return occurrenceMap.values.all { it.isValid(relax = true) }
    }

    private fun matchesSingleAliasForSubtype(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyElement: ParadoxScriptProperty,
        propertyConfig: CwtPropertyConfig,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        val singleAliasName = propertyConfig.valueExpression.value ?: return false
        val singleAlias = configGroup.singleAliases[singleAliasName] ?: return false
        return matchesPropertyForSubtype(definitionElement, propertyElement, singleAlias.config, configGroup, matchOptions)
    }

    private fun matchesAliasForSubtype(definitionElement: ParadoxScriptDefinitionElement, propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, matchOptions: Int): Boolean {
        // aliasName和aliasSubName需要匹配
        val aliasName = propertyConfig.keyExpression.value ?: return false
        val key = propertyElement.name
        val quoted = propertyElement.propertyKey.text.isLeftQuoted()
        val configGroup = propertyConfig.configGroup
        val aliasSubName = ParadoxExpressionManager.getMatchedAliasKey(configGroup, aliasName, key, propertyElement, quoted, matchOptions) ?: return false
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
        val aliases = aliasGroup[aliasSubName] ?: return false
        return aliases.any { alias ->
            matchesPropertyForSubtype(definitionElement, propertyElement, alias.config, configGroup, matchOptions)
        }
    }

    // endregion

    // region Complex Enum Config

    // NOTE 这里匹配时并不兼容向下内联的情况

    fun getMatchedComplexEnumConfig(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup, path: ParadoxPath): CwtComplexEnumConfig? {
        // 优先从基于文件路径的缓存中获取
        val configs = configGroup.complexEnumConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesComplexEnum(element, config, null) }
    }

    fun matchesComplexEnum(element: ParadoxScriptStringExpressionElement, complexEnumConfig: CwtComplexEnumConfig, path: ParadoxPath?): Boolean {
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(complexEnumConfig, path)) return false
        }
        for (enumNameConfig in complexEnumConfig.enumNameConfigs) {
            if (matchesEnumNameForComplexEnum(element, enumNameConfig, complexEnumConfig)) return true
        }
        return false
    }

    private fun matchesEnumNameForComplexEnum(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config is CwtPropertyConfig) {
            if (config.key == "enum_name") {
                if (element !is ParadoxScriptPropertyKey) return false
                val valueElement = element.propertyValue ?: return false
                if (!matchesValueForComplexEnum(valueElement, config, complexEnumConfig)) return false
            } else if (config.stringValue == "enum_name") {
                if (element !is ParadoxScriptString || !element.isPropertyValue()) return false
                val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
                if (!matchesKeyForComplexEnum(propertyElement, config)) return false
            } else {
                return false
            }
        } else if (config is CwtValueConfig) {
            if (config.stringValue == "enum_name") {
                if (element !is ParadoxScriptString || !element.isBlockMember()) return false
            } else {
                return false
            }
        }
        return beforeMatchParentForComplexEnum(element, config, complexEnumConfig)
    }

    private fun matchesParentForComplexEnum(element: PsiElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config is CwtPropertyConfig) {
            // match key only
            if (element !is ParadoxScriptProperty) return false
            if (!matchesKeyForComplexEnum(element, config)) return false
        } else if (config is CwtValueConfig) {
            // blockConfig vs blockElement
            if (element !is ParadoxScriptBlockElement) return false
        } else {
            return false
        }
        return beforeMatchParentForComplexEnum(element, config, complexEnumConfig)
    }

    private fun beforeMatchParentForComplexEnum(element: PsiElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        val parentConfig = config.parentConfig ?: return false
        val parentBlockElement = element.parentOfType<ParadoxScriptBlockElement>() ?: return false
        val parentElement = when {
            parentBlockElement is ParadoxScriptRootBlock -> null
            parentBlockElement is ParadoxScriptBlock && !parentBlockElement.isPropertyValue() -> parentBlockElement
            else -> parentBlockElement.parentOfType<ParadoxScriptProperty>()
        }
        if (parentConfig == complexEnumConfig.nameConfig) {
            if (complexEnumConfig.startFromRoot) {
                return parentElement == null
            } else {
                return parentElement != null && parentElement.parents(false)
                    .find { it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockMember()) } == null
            }
        }
        if (parentElement == null) return false
        parentConfig.configs?.forEach { c ->
            ProgressManager.checkCanceled()
            when {
                c is CwtPropertyConfig -> {
                    // ignore same config or enum name config
                    if (c == config || c.key == "enum_name" || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.properties().options(inline = true).none { propElement ->
                        matchesPropertyForComplexEnum(propElement, c, complexEnumConfig)
                    }
                    if (notMatched) return false
                }
                c is CwtValueConfig -> {
                    // ignore same config or enum name config
                    if (c == config || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.values().options(inline = true).none { valueElement ->
                        matchesValueForComplexEnum(valueElement, c, complexEnumConfig)
                    }
                    if (notMatched) return false
                }
            }
        }
        return matchesParentForComplexEnum(parentElement, parentConfig, complexEnumConfig)
    }

    private fun matchesPropertyForComplexEnum(propertyElement: ParadoxScriptProperty, config: CwtPropertyConfig, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        return matchesKeyForComplexEnum(propertyElement, config)
            && matchesValueForComplexEnum(propertyElement.propertyValue ?: return false, config, complexEnumConfig)
    }

    private fun matchesKeyForComplexEnum(propertyElement: ParadoxScriptProperty, config: CwtPropertyConfig): Boolean {
        val key = config.key
        when (key) {
            "enum_name" -> return true
            "scalar" -> return true
        }
        return key.equals(propertyElement.name, true)
    }

    private fun matchesValueForComplexEnum(valueElement: ParadoxScriptValue, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config.valueType == CwtType.Block) {
            val blockElement = valueElement.castOrNull<ParadoxScriptBlockElement>() ?: return false
            if (!matchesBlockForComplexEnum(blockElement, config, complexEnumConfig)) return false
            return true
        } else if (config.stringValue != null) {
            when (config.stringValue) {
                "enum_name" -> return true
                "scalar" -> return true
                "float" -> return valueElement is ParadoxScriptFloat
                "int" -> return valueElement is ParadoxScriptInt
                "bool" -> return valueElement is ParadoxScriptBoolean
            }
            val stringElement = valueElement.castOrNull<ParadoxScriptString>() ?: return false
            return stringElement.value.equals(config.stringValue, true)
        } else if (config.floatValue != null) {
            val floatElement = valueElement.castOrNull<ParadoxScriptFloat>() ?: return false
            return floatElement.floatValue == config.floatValue
        } else if (config.intValue != null) {
            val intElement = valueElement.castOrNull<ParadoxScriptInt>() ?: return false
            return intElement.intValue == config.intValue
        } else if (config.booleanValue != null) {
            val booleanElement = valueElement.castOrNull<ParadoxScriptBoolean>() ?: return false
            return booleanElement.booleanValue == config.booleanValue
        } else {
            return false
        }
    }

    private fun matchesBlockForComplexEnum(blockElement: ParadoxScriptBlockElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        config.properties?.forEach { propConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.properties().options(inline = true).none { propElement ->
                matchesPropertyForComplexEnum(propElement, propConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        config.values?.forEach { valueConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.values().options(inline = true).none { valueElement ->
                matchesValueForComplexEnum(valueElement, valueConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        return true
    }

    // endregion

    // region Row Config

    fun getMatchedRowConfig(configGroup: CwtConfigGroup, path: ParadoxPath): CwtRowConfig? {
        for (rowConfig in configGroup.rows.values) {
            if (!matchesRow(rowConfig, path)) continue
            return rowConfig
        }
        return null
    }

    fun matchesRow(rowConfig: CwtRowConfig, path: ParadoxPath?): Boolean {
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(rowConfig, path)) return false
        }
        return true
    }

    // endregion
}
