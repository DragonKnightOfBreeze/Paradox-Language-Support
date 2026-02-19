package icu.windea.pls.lang.match

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
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.isIncomplete
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.values
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isPropertyValue
import icu.windea.pls.script.psi.propertyValue

object ParadoxConfigMatchService {
    private val CwtConfigGroup.typeConfigCandidatesCache by registerKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtTypeConfig>> { path ->
            types.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }
    private val CwtConfigGroup.complexEnumConfigCandidatesCache by registerKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtComplexEnumConfig>> { path ->
            complexEnums.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }
    private val CwtConfigGroup.rowConfigCandidatesCache by registerKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtRowConfig>> { path ->
            rows.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }

    // region Type Config

    fun getTypeConfigCandidates(context: CwtTypeConfigMatchContext): Collection<CwtTypeConfig> {
        // 优先从基于文件路径的缓存中获取
        val configGroup = context.configGroup
        return if (context.path == null) configGroup.types.values else configGroup.typeConfigCandidatesCache[context.path]
    }

    fun getMatchedTypeConfig(context: CwtTypeConfigMatchContext, element: ParadoxDefinitionElement): CwtTypeConfig? {
        val candicates = getTypeConfigCandidates(context)
        if (candicates.isEmpty()) return null
        context.matchPath = false
        return candicates.find { matchesType(context, element, it) }
    }

    fun getMatchedTypeConfigForInjection(context: CwtTypeConfigMatchContext): CwtTypeConfig? {
        val candicates = getTypeConfigCandidates(context)
        if (candicates.isEmpty()) return null
        context.matchPath = false
        return candicates.find { matchesTypeForInjection(context, it) }
    }

    fun matchesType(context: CwtTypeConfigMatchContext, element: ParadoxDefinitionElement, typeConfig: CwtTypeConfig): Boolean {
        // 判断 definition 需要是 scriptFile 还是 scriptProperty
        run {
            val elementType = element.elementType
            if (typeConfig.typePerFile) {
                if (elementType != ParadoxScriptFile.ELEMENT_TYPE) return false
            } else {
                if (elementType != PROPERTY) return false
            }
        }

        val fastResult = matchesTypeFast(context, typeConfig)
        if (fastResult != null) return fastResult

        // 判断 definition 的 propertyValue 是否需要是 block
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
            val expectBlock = declarationConfig.valueExpression.type == CwtDataTypes.Block
            if (isBlock != expectBlock) return false
        }

        return true
    }

    fun matchesTypeForInjection(context: CwtTypeConfigMatchContext, typeConfig: CwtTypeConfig): Boolean {
        return canApplyForInjection(typeConfig) && matchesTypeByUnknownDeclaration(context, typeConfig)
    }

    fun matchesTypeByUnknownDeclaration(context: CwtTypeConfigMatchContext, typeConfig: CwtTypeConfig): Boolean {
        // 判断 definition 需要是 scriptFile 还是 scriptProperty
        if (typeConfig.typePerFile) return false

        val fastResult = matchesTypeFast(context, typeConfig)
        if (fastResult == false) return fastResult

        return true
    }

    fun matchesTypeFast(context: CwtTypeConfigMatchContext, typeConfig: CwtTypeConfig): Boolean? {
        if (context.matchPath && context.path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(typeConfig, context.path)) return false
        }

        if (context.typeKey != null) {
            // 如果选项 starts_with 存在，则要求 type_key 匹配这个前缀
            val startsWith = typeConfig.startsWith
            if (!startsWith.isNullOrEmpty()) {
                val result = context.typeKey.startsWith(startsWith)
                if (!result) return false
            }

            // 如果选项 type_key_regex 存在，则要求 type_key 匹配
            val typeKeyRegex = typeConfig.typeKeyRegex
            if (typeKeyRegex != null) {
                val result = typeKeyRegex.matches(context.typeKey)
                if (!result) return false
            }

            // 如果选项 type_key_filter 存在，则需要通过 type_key 进行过滤（忽略大小写）
            val typeKeyFilter = typeConfig.typeKeyFilter
            if (typeKeyFilter != null && typeKeyFilter.value.isNotEmpty()) {
                val result = typeKeyFilter.withOperator { it.contains(context.typeKey) }
                if (!result) return false
            }

            // 如果 name_field 存在，则要求 type_key 必须是由 type_key_filter 指定的所有可能的 type_key 之一，或者没有指定任何 type_key
            val nameField = typeConfig.nameField
            if (nameField != null) {
                val result = typeConfig.possibleTypeKeys.isEmpty() || typeConfig.possibleTypeKeys.contains(context.typeKey)
                if (!result) return false
            }
        }

        if (context.rootKeys != null) {
            // 如果属性 skip_root_key 存在，则要判断是否需要跳过 rootKey
            // skip_root_key 可以为列表（如果是列表，其中的每一个 root_key 都要依次匹配）
            // skip_root_key 可以重复（其中之一匹配即可）
            val skipRootKey = typeConfig.skipRootKey
            if (skipRootKey.isEmpty()) {
                if (context.rootKeys.isNotEmpty()) return false
            } else {
                if (context.rootKeys.isEmpty()) return false
                val result = skipRootKey.any { PathMatcher.matches(context.rootKeys, it, ignoreCase = true, usePattern = true, useAny = true) }
                if (!result) return false
            }
        }

        // 如果属性 type_key_prefix 存在，且有必要校验，则要求其与 typeKeyPrefix 必须一致（忽略大小写）
        if (context.typeKeyPrefix != null && typeConfig.name in typeConfig.configGroup.definitionTypesModel.typeKeyPrefixAware) {
            val result = typeConfig.typeKeyPrefix.equals(context.typeKeyPrefix.value, ignoreCase = true)
            if (!result) return false
        }

        return null // 需要进一步匹配
    }

    // endregion

    // region Subtype Config

    fun getFastMatchedSubtypeConfigs(typeConfig: CwtTypeConfig, typeKey: String): List<CwtSubtypeConfig> {
        if (typeConfig.subtypes.isEmpty()) return emptyList()
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in typeConfig.subtypes.values) {
            val fastResult = matchesSubtypeFast(subtypeConfig, result, typeKey)
            if (fastResult == true) result.add(subtypeConfig)
        }
        return result
    }

    fun matchesSubtype(element: ParadoxDefinitionElement, subtypeConfig: CwtSubtypeConfig, subtypeConfigs: List<CwtSubtypeConfig>, typeKey: String, options: ParadoxMatchOptions? = null): Boolean {
        val fastResult = matchesSubtypeFast(subtypeConfig, subtypeConfigs, typeKey)
        if (fastResult != null) return fastResult

        // 根据 config 对 property 进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true
        return matchesDefinitionForSubtype(element, elementConfig, options.normalized())
    }

    fun matchesSubtypeFast(subtypeConfig: CwtSubtypeConfig, subtypeConfigs: List<CwtSubtypeConfig>, typeKey: String): Boolean? {
        // 如果 only_if_not 存在，且已经匹配指定的任意子类型，则不匹配
        val onlyIfNot = subtypeConfig.onlyIfNot
        if (!onlyIfNot.isNullOrEmpty()) {
            val matchesAny = subtypeConfigs.any { it.name in onlyIfNot }
            if (matchesAny) return false
        }

        // 如果 starts_with 存在，则要求 typeKey 匹配这个前缀（不忽略大小写）
        val startsWith = subtypeConfig.startsWith
        if (!startsWith.isNullOrEmpty()) {
            val result = typeKey.startsWith(startsWith, false)
            if (!result) return false
        }

        // 如果 type_key_regex 存在，则要求 typeKey 匹配
        val typeKeyRegex = subtypeConfig.typeKeyRegex
        if (typeKeyRegex != null) {
            val result = typeKeyRegex.matches(typeKey)
            if (!result) return false
        }

        // 如果 type_key_filter 存在，则通过 typeKey 进行过滤（忽略大小写）
        val typeKeyFilter = subtypeConfig.typeKeyFilter
        if (typeKeyFilter != null && typeKeyFilter.value.isNotEmpty()) {
            val result = typeKeyFilter.withOperator { it.contains(typeKey) }
            if (!result) return false
        }

        // 根据 config 对 property 进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true

        return null // 需要进一步匹配
    }

    private fun matchesDefinitionForSubtype(definition: ParadoxDefinitionElement, propertyConfig: CwtPropertyConfig, options: ParadoxMatchOptions): Boolean {
        // 这里不能基于内联后的声明结构，否则可能会导致SOE
        // 也不要参数条件表达式中的声明结构判断，
        val childValueConfigs = propertyConfig.values.orEmpty()
        if (childValueConfigs.isNotEmpty()) {
            // 匹配值列表
            val blockElement = definition.block
            if (!matchesValuesForSubtype(blockElement, childValueConfigs, options)) return false // 继续匹配
        }
        val childPropertyConfigs = propertyConfig.properties.orEmpty()
        if (childPropertyConfigs.isNotEmpty()) {
            // 匹配属性列表
            val blockElement = definition.block
            if (!matchesPropertiesForSubtype(definition, blockElement, childPropertyConfigs, options)) return false // 继续匹配
        }
        return true
    }

    private fun matchesPropertyForSubtype(definition: ParadoxDefinitionElement, property: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, options: ParadoxMatchOptions?): Boolean {
        val configGroup = propertyConfig.configGroup
        val propValue = property.propertyValue

        // 对于 propertyValue 同样这样判断（可能脚本没有写完）
        if (propValue == null) return propertyConfig.optionData.cardinality?.min == 0

        when {
            // 匹配布尔值
            propertyConfig.booleanValue != null -> {
                if (propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
            }
            // 匹配值
            propertyConfig.stringValue != null -> {
                val expression = ParadoxScriptExpression.resolve(propValue, options)
                val configExpression = propertyConfig.valueExpression
                val context = ParadoxScriptExpressionMatchContext(propValue, expression, configExpression, propertyConfig, configGroup, options)
                return ParadoxMatchService.matchScriptExpression(context).get(options)
            }
            // 匹配 single_alias
            isSingleAliasEntryConfig(propertyConfig) -> {
                return matchesSingleAliasForSubtype(definition, property, propertyConfig, options)
            }
            // 匹配 alias
            isAliasEntryConfig(propertyConfig) -> {
                return matchesAliasForSubtype(definition, property, propertyConfig, options)
            }
            propertyConfig.configs.orEmpty().isNotEmpty() -> {
                val blockElement = property.block
                // 匹配值列表
                if (!matchesValuesForSubtype(blockElement, propertyConfig.values.orEmpty(), options)) return false
                // 匹配属性列表
                if (!matchesPropertiesForSubtype(definition, blockElement, propertyConfig.properties.orEmpty(), options)) return false
            }
        }
        return true
    }

    private fun matchesPropertiesForSubtype(definition: ParadoxDefinitionElement, block: ParadoxScriptBlockElement?, propertyConfigs: List<CwtPropertyConfig>, options: ParadoxMatchOptions?): Boolean {
        if (propertyConfigs.isEmpty()) return true
        if (block == null) return false

        // NOTE 这里需要兼容内联
        // NOTE propConfig.key 可能有重复，这种情况下只要有其中一个匹配即可

        val occurrences = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { ParadoxMatchOccurrenceService.evaluate(definition, it) })
        val configGroup = propertyConfigs.first().configGroup
        val matched = definition.properties(inline = true).all p@{ propertyElement ->
            val keyElement = propertyElement.propertyKey
            val expression = ParadoxScriptExpression.resolve(keyElement, options)
            val propConfigs = propertyConfigs.filter { config ->
                val context = ParadoxScriptExpressionMatchContext(keyElement, expression, config.keyExpression, config, configGroup, options)
                ParadoxMatchService.matchScriptExpression(context).get(options)
            }

            // 如果没有匹配的规则则忽略
            if (propConfigs.isEmpty()) return@p true

            val matched = propConfigs.any { propConfig ->
                val matched = matchesPropertyForSubtype(definition, propertyElement, propConfig, options)
                if (matched) occurrences.get(propConfig.key)?.let { it.actual++ }
                matched
            }
            matched
        }
        if (!matched) return false

        return occurrences.values.all { it.isValid(relax = true) }
    }

    private fun matchesValuesForSubtype(block: ParadoxScriptBlockElement?, valueConfigs: List<CwtValueConfig>, options: ParadoxMatchOptions?): Boolean {
        if (valueConfigs.isEmpty()) return true
        if (block == null) return false

        // NOTE 这里需要兼容内联

        val occurrences = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { ParadoxMatchOccurrenceService.evaluate(block, it) })
        val configGroup = valueConfigs.first().configGroup
        val matched = block.values(inline = true).process p@{ valueElement ->
            // 如果没有匹配的规则则忽略
            val expression = ParadoxScriptExpression.resolve(valueElement, options)
            val matched = valueConfigs.any { config ->
                val configExpression = config.valueExpression
                val context = ParadoxScriptExpressionMatchContext(valueElement, expression, configExpression, config, configGroup, options)
                val matched = ParadoxMatchService.matchScriptExpression(context).get(options)
                if (matched) occurrences.get(config.value)?.let { it.actual++ }
                matched
            }
            matched
        }
        if (!matched) return false

        return occurrences.values.all { it.isValid(relax = true) }
    }

    private fun matchesSingleAliasForSubtype(definition: ParadoxDefinitionElement, property: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, options: ParadoxMatchOptions?): Boolean {
        val configGroup = propertyConfig.configGroup
        val singleAliasName = propertyConfig.valueExpression.value ?: return false
        val singleAlias = configGroup.singleAliases[singleAliasName] ?: return false
        return matchesPropertyForSubtype(definition, property, singleAlias.config, options)
    }

    private fun matchesAliasForSubtype(definition: ParadoxDefinitionElement, property: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, options: ParadoxMatchOptions?): Boolean {
        // aliasName 和 aliasSubName 需要匹配
        val configGroup = propertyConfig.configGroup
        val aliasName = propertyConfig.keyExpression.value ?: return false
        val key = property.name
        val quoted = property.propertyKey.text.isLeftQuoted()
        val aliasSubName = ParadoxMatchService.getMatchedAliasKey(property, configGroup, aliasName, key, quoted, options) ?: return false
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
        val aliases = aliasGroup[aliasSubName] ?: return false
        return aliases.any { alias ->
            matchesPropertyForSubtype(definition, property, alias.config, options)
        }
    }

    // endregion

    // region Complex Enum Config

    // NOTE 这里匹配时并不兼容向下内联的情况

    fun getComplexEnumConfigCandidates(context: CwtComplexEnumConfigMatchContext): Collection<CwtComplexEnumConfig> {
        // 优先从基于文件路径的缓存中获取
        val configGroup = context.configGroup
        return if (context.path == null) configGroup.complexEnums.values else configGroup.complexEnumConfigCandidatesCache[context.path]
    }

    fun getMatchedComplexEnumConfig(context: CwtComplexEnumConfigMatchContext, element: ParadoxScriptStringExpressionElement): CwtComplexEnumConfig? {
        val candicates = getComplexEnumConfigCandidates(context)
        if (candicates.isEmpty()) return null
        context.matchPath = false
        return candicates.find { matchesComplexEnum(context, element, it) }
    }

    fun matchesComplexEnum(context: CwtComplexEnumConfigMatchContext, element: ParadoxScriptStringExpressionElement, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (context.matchPath && context.path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(complexEnumConfig, context.path)) return false
        }
        for (enumNameConfig in complexEnumConfig.enumNameConfigs) {
            if (matchesEnumNameForComplexEnum(element, complexEnumConfig, enumNameConfig)) return true
        }
        return false
    }

    private fun matchesEnumNameForComplexEnum(element: ParadoxScriptStringExpressionElement, complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>): Boolean {
        if (config is CwtPropertyConfig) {
            if (config.key == "enum_name") {
                if (element !is ParadoxScriptPropertyKey) return false
                val valueElement = element.propertyValue ?: return false
                if (!matchesValueForComplexEnum(valueElement, complexEnumConfig, config)) return false
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
        return beforeMatchParentForComplexEnum(element, complexEnumConfig, config)
    }

    private fun matchesParentForComplexEnum(element: PsiElement, complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>): Boolean {
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
        return beforeMatchParentForComplexEnum(element, complexEnumConfig, config)
    }

    private fun beforeMatchParentForComplexEnum(element: PsiElement, complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>): Boolean {
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
                    val notMatched = parentBlockElement.properties(inline = true).none { propElement ->
                        matchesPropertyForComplexEnum(propElement, complexEnumConfig, c)
                    }
                    if (notMatched) return false
                }
                c is CwtValueConfig -> {
                    // ignore same config or enum name config
                    if (c == config || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.values(inline = true).none { valueElement ->
                        matchesValueForComplexEnum(valueElement, complexEnumConfig, c)
                    }
                    if (notMatched) return false
                }
            }
        }
        return matchesParentForComplexEnum(parentElement, complexEnumConfig, parentConfig)
    }

    private fun matchesPropertyForComplexEnum(propertyElement: ParadoxScriptProperty, complexEnumConfig: CwtComplexEnumConfig, config: CwtPropertyConfig): Boolean {
        return matchesKeyForComplexEnum(propertyElement, config)
            && matchesValueForComplexEnum(propertyElement.propertyValue ?: return false, complexEnumConfig, config)
    }

    private fun matchesKeyForComplexEnum(propertyElement: ParadoxScriptProperty, config: CwtPropertyConfig): Boolean {
        val key = config.key
        when (key) {
            "enum_name" -> return true
            "scalar" -> return true
        }
        return key.equals(propertyElement.name, true)
    }

    private fun matchesValueForComplexEnum(valueElement: ParadoxScriptValue, complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>): Boolean {
        if (config.valueType == CwtType.Block) {
            val blockElement = valueElement.castOrNull<ParadoxScriptBlockElement>() ?: return false
            if (!matchesBlockForComplexEnum(blockElement, complexEnumConfig, config)) return false
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

    private fun matchesBlockForComplexEnum(blockElement: ParadoxScriptBlockElement, complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>): Boolean {
        config.properties?.forEach { propConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.properties(inline = true).none { propElement ->
                matchesPropertyForComplexEnum(propElement, complexEnumConfig, propConfig)
            }
            if (notMatched) return false
        }
        config.values?.forEach { valueConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.values(inline = true).none { valueElement ->
                matchesValueForComplexEnum(valueElement, complexEnumConfig, valueConfig)
            }
            if (notMatched) return false
        }
        return true
    }

    // endregion

    // region Row Config

    fun getRowConfigCandidates(context: CwtRowConfigMatchContext): Collection<CwtRowConfig> {
        // 优先从基于文件路径的缓存中获取
        val configGroup = context.configGroup
        return if (context.path == null) configGroup.rows.values else configGroup.rowConfigCandidatesCache[context.path]
    }

    fun getMatchedRowConfig(context: CwtRowConfigMatchContext): CwtRowConfig? {
        val candicates = getRowConfigCandidates(context)
        if (candicates.isEmpty()) return null
        context.matchPath = false
        return candicates.find { matchesRow(context, it) }
    }

    fun matchesRow(context: CwtRowConfigMatchContext, rowConfig: CwtRowConfig): Boolean {
        if (context.matchPath && context.path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(rowConfig, context.path)) return false
        }
        return true
    }

    // endregion

    // region Misc Methods

    fun isAliasEntryConfig(config: CwtPropertyConfig): Boolean {
        return config.keyExpression.type == CwtDataTypes.AliasName && config.valueExpression.type == CwtDataTypes.AliasMatchLeft
    }

    fun isSingleAliasEntryConfig(config: CwtPropertyConfig): Boolean {
        return config.valueExpression.type == CwtDataTypes.SingleAliasRight
    }

    fun canApplyForInjection(typeConfig: CwtTypeConfig): Boolean {
        if (typeConfig.skipRootKey.isNotEmpty()) return false
        if (typeConfig.nameField != null) return false
        if (typeConfig.typeKeyPrefix.isNotNullOrEmpty()) return false
        return true
    }

    // endregion
}
