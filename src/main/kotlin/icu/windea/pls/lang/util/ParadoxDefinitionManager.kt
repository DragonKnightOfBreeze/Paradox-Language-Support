package icu.windea.pls.lang.util

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.toOccurrence
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.definitionTypesMayWithTypeKeyPrefix
import icu.windea.pls.config.configGroup.singleAliases
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.firstChild
import icu.windea.pls.core.isIncomplete
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.withOperator
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isInlineScriptUsage
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.PlsConstants
import icu.windea.pls.model.paths.ParadoxElementPath
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptLightTreeUtil
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.findProperty
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.properties
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub
import icu.windea.pls.script.psi.values

/**
 * 用于处理定义。
 *
 * @see ParadoxScriptDefinitionElement
 * @see ParadoxDefinitionInfo
 */
object ParadoxDefinitionManager {
    object Keys : KeyRegistry() {
        val cachedDefinitionInfo by createKey<CachedValue<ParadoxDefinitionInfo>>(Keys)
        val cachedDefinitionPrimaryLocalisationKey by createKey<CachedValue<String>>(Keys)
        val cachedDefinitionPrimaryLocalisation by createKey<CachedValue<ParadoxLocalisationProperty>>(Keys)
        val cachedDefinitionPrimaryLocalisations by createKey<CachedValue<Set<ParadoxLocalisationProperty>>>(Keys)
        val cachedDefinitionPrimaryImage by createKey<CachedValue<PsiFile>>(Keys)
    }

    private val CwtConfigGroup.typeConfigsCache by createKey(CwtConfigGroup.Keys) {
        CacheBuilder().build<ParadoxPath, List<CwtTypeConfig>> { path ->
            types.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }.cancelable()
    }

    // get info & match methods

    fun getInfo(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        // 从缓存中获取
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        // invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = runReadActionSmartly { doGetInfo(element, file) }
            value.withDependencyItems(file)
        }
    }

    private fun doGetInfo(element: ParadoxScriptDefinitionElement, file: PsiFile = element.containingFile): ParadoxDefinitionInfo? {
        val typeKey = getTypeKey(element)
        if (element is ParadoxScriptProperty) {
            if (typeKey.isInlineScriptUsage()) return null // 排除是内联脚本调用的情况
            if (typeKey.isParameterized()) return null // 排除可能带参数的情况
        }
        doGetInfoFromStub(element, file)?.let { return it }
        return doGetInfoFromPsi(element, file, typeKey)
    }

    fun doGetInfoFromStub(element: ParadoxScriptDefinitionElement, file: PsiFile): ParadoxDefinitionInfo? {
        val stub = getStub(element) ?: return null
        val name = stub.definitionName
        val type = stub.definitionType
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val typeConfig = configGroup.types[type] ?: return null
        val subtypes = stub.definitionSubtypes
        val subtypeConfigs = subtypes?.mapNotNull { typeConfig.subtypes[it] }
        val typeKey = stub.typeKey
        val elementPath = stub.elementPath
        return ParadoxDefinitionInfo(element, typeConfig, name, subtypeConfigs, typeKey, elementPath, gameType, configGroup)
    }

    private fun doGetInfoFromPsi(element: ParadoxScriptDefinitionElement, file: PsiFile, typeKey: String): ParadoxDefinitionInfo? {
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType // 这里还是基于fileInfo获取gameType
        val elementPath = ParadoxScriptFileManager.getElementPath(element, PlsFacade.getInternalSettings().maxDefinitionDepth) ?: return null
        if (elementPath.path.isParameterized()) return null // 忽略表达式路径带参数的情况
        val configGroup = PlsFacade.getConfigGroup(file.project, gameType) // 这里需要指定 project
        val typeKeyPrefix = if (element is ParadoxScriptProperty) lazy { ParadoxScriptFileManager.getKeyPrefixes(element).firstOrNull() } else null
        val typeConfig = getMatchedTypeConfig(element, configGroup, path, elementPath, typeKey, typeKeyPrefix) ?: return null
        return ParadoxDefinitionInfo(element, typeConfig, null, null, typeKey, elementPath, gameType, configGroup)
    }

    fun getTypeKey(element: ParadoxScriptDefinitionElement): String {
        return when (element) {
            is ParadoxScriptFile -> element.name.substringBeforeLast(".") // 如果是文件名，不要包含扩展名
            else -> element.name // 否则直接使用 PSI 的名字
        }
    }

    fun getName(element: ParadoxScriptDefinitionElement): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionName }
        return element.definitionInfo?.name
    }

    fun getType(element: ParadoxScriptDefinitionElement): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return it.definitionType }
        return element.definitionInfo?.type
    }

    fun getSubtypes(element: ParadoxScriptDefinitionElement): List<String>? {
        // 定义的子类型可能需要通过访问索引获取，不能在索引时就获取
        return element.definitionInfo?.subtypes
    }

    fun getStub(element: ParadoxScriptDefinitionElement): ParadoxScriptPropertyStub.Definition? {
        return element.castOrNull<ParadoxScriptProperty>()?.greenStub?.castOrNull()
    }

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
        if (typeKeyPrefix != null && typeConfig.name in typeConfig.configGroup.definitionTypesMayWithTypeKeyPrefix) {
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
        return doMatchDefinition(element, elementConfig, configGroup, matchOptions)
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

    private fun doMatchDefinition(
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
            if (!doMatchValues(blockElement, childValueConfigs, configGroup, matchOptions)) return false // 继续匹配
        }
        val childPropertyConfigs = propertyConfig.properties.orEmpty()
        if (childPropertyConfigs.isNotEmpty()) {
            // 匹配属性列表
            val blockElement = definitionElement.block
            if (!doMatchProperties(definitionElement, blockElement, childPropertyConfigs, configGroup, matchOptions)) return false // 继续匹配
        }
        return true
    }

    private fun doMatchProperty(
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
                return doMatchSingleAlias(definitionElement, propertyElement, propertyConfig, configGroup, matchOptions)
            }
            // 匹配alias
            ParadoxExpressionManager.isAliasEntryConfig(propertyConfig) -> {
                return doMatchAlias(definitionElement, propertyElement, propertyConfig, matchOptions)
            }
            propertyConfig.configs.orEmpty().isNotEmpty() -> {
                val blockElement = propertyElement.block
                // 匹配值列表
                if (!doMatchValues(blockElement, propertyConfig.values.orEmpty(), configGroup, matchOptions)) return false
                // 匹配属性列表
                if (!doMatchProperties(definitionElement, blockElement, propertyConfig.properties.orEmpty(), configGroup, matchOptions)) return false
            }
        }
        return true
    }

    private fun doMatchProperties(
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
                val matched = doMatchProperty(definitionElement, propertyElement, propConfig, configGroup, matchOptions)
                if (matched) occurrenceMap.get(propConfig.key)?.let { it.actual++ }
                matched
            }
            matched
        }
        if (!matched) return false

        return occurrenceMap.values.all { it.isValid(relax = true) }
    }

    private fun doMatchValues(
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

    private fun doMatchSingleAlias(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyElement: ParadoxScriptProperty,
        propertyConfig: CwtPropertyConfig,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        val singleAliasName = propertyConfig.valueExpression.value ?: return false
        val singleAlias = configGroup.singleAliases[singleAliasName] ?: return false
        return doMatchProperty(definitionElement, propertyElement, singleAlias.config, configGroup, matchOptions)
    }

    private fun doMatchAlias(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyElement: ParadoxScriptProperty,
        propertyConfig: CwtPropertyConfig,
        matchOptions: Int
    ): Boolean {
        // aliasName和aliasSubName需要匹配
        val aliasName = propertyConfig.keyExpression.value ?: return false
        val key = propertyElement.name
        val quoted = propertyElement.propertyKey.text.isLeftQuoted()
        val configGroup = propertyConfig.configGroup
        val aliasSubName = ParadoxExpressionManager.getMatchedAliasKey(configGroup, aliasName, key, propertyElement, quoted, matchOptions) ?: return false
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
        val aliases = aliasGroup[aliasSubName] ?: return false
        return aliases.any { alias ->
            doMatchProperty(definitionElement, propertyElement, alias.config, configGroup, matchOptions)
        }
    }

    fun resolveNameFromTypeConfig(element: ParadoxScriptDefinitionElement, typeKey: String, typeConfig: CwtTypeConfig): String {
        return when {
            // use type key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameFromFile -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // use type key (aka property name), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameField == null -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // force empty (aka anonymous)
            typeConfig.nameField == "" -> ""
            // from property value (which should be a string)
            typeConfig.nameField == "-" -> element.castOrNull<ParadoxScriptProperty>()?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
            // from specific property value in definition declaration (while the property name is declared by config property "name_field")
            else -> element.findProperty(typeConfig.nameField)?.propertyValue<ParadoxScriptString>()?.stringValue.orEmpty()
        }
    }

    fun resolveNameFromTypeConfig(node: LighterASTNode, tree: LighterAST, typeKey: String, typeConfig: CwtTypeConfig): String? {
        return when {
            // use type key (aka file name without extension), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameFromFile -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // use type key (aka property name), remove prefix if exists (while the prefix is declared by config property "starts_with")
            typeConfig.nameField == null -> typeKey.removePrefix(typeConfig.startsWith.orEmpty())
            // force empty (aka anonymous)
            typeConfig.nameField == "" -> ""
            // from property value (which should be a string)
            typeConfig.nameField == "-" -> ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(node, tree)
            // from specific property value in definition declaration (while the property name is declared by config property "name_field")
            else -> ParadoxScriptLightTreeUtil.findPropertyFromPropertyNode(node, tree, typeConfig.nameField!!)
                ?.let { ParadoxScriptLightTreeUtil.getStringValueFromPropertyNode(it, tree) }
        }
    }

    fun getLocalizedNames(element: ParadoxScriptDefinitionElement): Set<String> {
        val primaryLocalisations = getPrimaryLocalisations(element)
        return primaryLocalisations.mapNotNull { ParadoxLocalisationManager.getLocalizedText(it) }.toSet()
    }

    fun getPrimaryLocalisationKey(element: ParadoxScriptDefinitionElement): String? {
        return doGetPrimaryLocalisationKeyFromCache(element)
    }

    private fun doGetPrimaryLocalisationKeyFromCache(element: ParadoxScriptDefinitionElement): String? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisationKey) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisationKey(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.LocalisationFileTracker)
        }
    }

    private fun doGetPrimaryLocalisationKey(element: ParadoxScriptDefinitionElement): String? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者CWT规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val key = resolveResult?.name ?: continue
            return key
        }
        return null
    }

    fun getPrimaryLocalisation(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return doGetPrimaryLocalisationFromCache(element)
    }

    private fun doGetPrimaryLocalisationFromCache(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisation(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.LocalisationFileTracker, ParadoxModificationTrackers.LocaleTracker)
        }
    }

    private fun doGetPrimaryLocalisation(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null // 没有或者CWT规则不完善
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisation = resolveResult?.element ?: continue
            return localisation
        }
        return null
    }

    fun getPrimaryLocalisations(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        return doGetPrimaryLocalisationsFromCache(element)
    }

    private fun doGetPrimaryLocalisationsFromCache(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisations) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisations(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.LocalisationFileTracker, ParadoxModificationTrackers.LocaleTracker)
        }
    }

    private fun doGetPrimaryLocalisations(element: ParadoxScriptDefinitionElement): Set<ParadoxLocalisationProperty> {
        val definitionInfo = element.definitionInfo ?: return emptySet()
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return emptySet() // 没有或者CWT规则不完善
        val result = mutableSetOf<ParadoxLocalisationProperty>()
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for (primaryLocalisation in primaryLocalisations) {
            val resolveResult = CwtLocationExpressionManager.resolve(primaryLocalisation.locationExpression, element, definitionInfo) { preferLocale(preferredLocale) }
            val localisations = resolveResult?.elements ?: continue
            result.addAll(localisations)
        }
        return result
    }

    fun getPrimaryImage(element: ParadoxScriptDefinitionElement): PsiFile? {
        return doGetPrimaryImageFromCache(element)
    }

    private fun doGetPrimaryImageFromCache(element: ParadoxScriptDefinitionElement): PsiFile? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryImage) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryImage(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.ScriptFileTracker)
        }
    }

    private fun doGetPrimaryImage(element: ParadoxScriptDefinitionElement): PsiFile? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return null // 没有或者CWT规则不完善
        for (primaryImage in primaryImages) {
            val resolved = CwtLocationExpressionManager.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            val file = resolved?.element?.castOrNull<PsiFile>()
            if (file == null) continue
            element.putUserData(PlsKeys.imageFrameInfo, resolved.frameInfo)
            return file
        }
        return null
    }
}
