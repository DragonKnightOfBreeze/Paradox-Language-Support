package icu.windea.pls.lang.util

import com.google.common.cache.*
import com.intellij.lang.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

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
        val cachedDefinitionLocalizedNames by createKey<CachedValue<Set<String>>>(Keys)
        val cachedDefinitionPrimaryImage by createKey<CachedValue<PsiFile>>(Keys)
    }

    private val CwtConfigGroup.typeConfigsCache by createKey(CwtConfigGroup.Keys) {
        CacheBuilder.newBuilder().buildCache<ParadoxPath, List<CwtTypeConfig>> { path ->
            types.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }
    }

    //get info & match methods

    fun getInfo(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        //从缓存中获取
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        //invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = doGetInfo(element, file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetInfo(element: ParadoxScriptDefinitionElement, file: PsiFile = element.containingFile): ParadoxDefinitionInfo? {
        val rootKey = element.name.let { if (element is ParadoxScriptFile) it.substringBeforeLast('.') else it } //如果是文件名，不要包含扩展名
        if (element is ParadoxScriptProperty) {
            if (rootKey.isInlineUsage()) return null //排除是内联调用的情况
            if (rootKey.isParameterized()) return null //排除可能带参数的情况
        }
        val project = file.project

        //首先尝试直接基于stub进行解析
        getInfoFromStub(element, project)?.let { return it }

        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType //这里还是基于fileInfo获取gameType
        val elementPath = ParadoxExpressionPathManager.get(element, PlsFacade.getInternalSettings().maxDefinitionDepth) ?: return null
        if (elementPath.path.isParameterized()) return null //忽略表达式路径带参数的情况
        val configGroup = PlsFacade.getConfigGroup(project, gameType) //这里需要指定project
        val rootKeyPrefix = if (element is ParadoxScriptProperty) lazy { ParadoxExpressionPathManager.getKeyPrefixes(element).firstOrNull() } else null
        val typeConfig = getMatchedTypeConfig(element, configGroup, path, elementPath, rootKey, rootKeyPrefix)
        if (typeConfig == null) return null
        return ParadoxDefinitionInfo(element, typeConfig, null, null, rootKey, elementPath, gameType, configGroup)
    }

    fun getMatchedTypeConfig(
        element: ParadoxScriptDefinitionElement,
        configGroup: CwtConfigGroup,
        path: ParadoxPath,
        elementPath: ParadoxExpressionPath,
        rootKey: String,
        rootKeyPrefix: Lazy<String?>?
    ): CwtTypeConfig? {
        //优先从基于文件路经的缓存中获取
        val configs = configGroup.typeConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesType(element, config, path, elementPath, rootKey, rootKeyPrefix) }
    }

    fun getMatchedTypeConfig(
        node: LighterASTNode,
        tree: LighterAST,
        configGroup: CwtConfigGroup,
        path: ParadoxPath,
        elementPath: ParadoxExpressionPath,
        rootKey: String,
        rootKeyPrefix: Lazy<String?>?
    ): CwtTypeConfig? {
        //优先从基于文件路经的缓存中获取
        val configs = configGroup.typeConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesType(node, tree, config, path, elementPath, rootKey, rootKeyPrefix) }
    }

    fun matchesType(
        element: ParadoxScriptDefinitionElement,
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxExpressionPath?,
        rootKey: String?,
        rootKeyPrefix: Lazy<String?>?
    ): Boolean {
        //判断definition是否需要是scriptFile还是scriptProperty
        run {
            if (typeConfig.typePerFile) {
                if (element !is ParadoxScriptFile) return false
            } else {
                if (element !is ParadoxScriptProperty) return false
            }
        }

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, rootKey, rootKeyPrefix)
        if (fastResult != null) return fastResult

        //判断definition的propertyValue是否需要是block
        run {
            val configGroup = typeConfig.configGroup
            val declarationConfig = configGroup.declarations.get(typeConfig.name)?.configForDeclaration ?: return@run
            val propertyValue = element.castOrNull<ParadoxScriptProperty>()?.propertyValue ?: return@run
            //兼容进行代码补全时用户输入未完成的情况
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
        elementPath: ParadoxExpressionPath?,
        rootKey: String?,
        rootKeyPrefix: Lazy<String?>?
    ): Boolean {
        //判断definition是否需要是scriptFile还是scriptProperty
        run {
            val elementType = node.tokenType
            if (typeConfig.typePerFile) {
                if (elementType !is ParadoxScriptFileStubElementType) return false
            } else {
                if (elementType != PROPERTY) return false
            }
        }

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, rootKey, rootKeyPrefix)
        if (fastResult != null) return fastResult

        //判断definition的propertyValue是否需要是block
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
        elementPath: ParadoxExpressionPath?,
        rootKey: String?,
        rootKeyPrefix: Lazy<String?>?
    ): Boolean {
        //判断element是否需要是scriptFile还是scriptProperty
        if (typeConfig.typePerFile) return false

        val fastResult = matchesTypeFast(typeConfig, path, elementPath, rootKey, rootKeyPrefix)
        if (fastResult == false) return fastResult

        return true
    }

    private fun matchesTypeFast(
        typeConfig: CwtTypeConfig,
        path: ParadoxPath?,
        elementPath: ParadoxExpressionPath?,
        rootKey: String?,
        rootKeyPrefix: Lazy<String?>?
    ): Boolean? {
        //判断path是否匹配
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(typeConfig, path)) return false
        }

        if (rootKey != null) {
            //如果选项starts_with存在，则要求type_key匹配这个前缀
            val startsWithConfig = typeConfig.startsWith
            if (!startsWithConfig.isNullOrEmpty()) {
                val result = rootKey.startsWith(startsWithConfig)
                if (!result) return false
            }

            //如果type_key_regex存在，则要求type_key匹配
            val typeKeyRegexConfig = typeConfig.typeKeyRegex
            if (typeKeyRegexConfig != null) {
                val result = typeKeyRegexConfig.matches(rootKey)
                if (!result) return false
            }

            //如果选项type_key_filter存在，则需要通过type_key进行过滤（忽略大小写）
            val typeKeyFilterConfig = typeConfig.typeKeyFilter
            if (typeKeyFilterConfig != null && typeKeyFilterConfig.value.isNotEmpty()) {
                val result = typeKeyFilterConfig.withOperator { it.contains(rootKey) }
                if (!result) return false
            }

            //如果name_field存在，则要求root_key必须是由type_key_filter指定的所有可能的root_key之一，或者没有指定任何root_key
            val nameFieldConfig = typeConfig.nameField
            if (nameFieldConfig != null) {
                val result = typeConfig.possibleRootKeys.isEmpty() || typeConfig.possibleRootKeys.contains(rootKey)
                if (!result) return false
            }
        }

        //如果属性type_key_prefix存在，且有必要校验，则要求其与rootKeyPrefix必须一致（忽略大小写）
        if (rootKeyPrefix != null && typeConfig.name in typeConfig.configGroup.definitionTypesMayWithTypeKeyPrefix) {
            val typeKeyPrefix = typeConfig.typeKeyPrefix
            val result = typeKeyPrefix.equals(rootKeyPrefix.value, ignoreCase = true)
            if (!result) return false
        }

        if (elementPath != null) {
            //如果属性skip_root_key存在，则要判断是否需要跳过rootKey
            //skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
            //skip_root_key可以重复（其中之一匹配即可）
            val skipRootKeyConfig = typeConfig.skipRootKey
            if (skipRootKeyConfig.isNullOrEmpty()) {
                if (elementPath.length > 1) return false
            } else {
                if (elementPath.isEmpty()) return false
                val input = elementPath.subPaths.dropLast(1)
                val result = skipRootKeyConfig.any { Matchers.PathMatcher.matches(input, it, true, true, true) }
                if (!result) return false
            }
        }

        return null //需要进一步匹配
    }

    fun matchesSubtype(
        element: ParadoxScriptDefinitionElement,
        rootKey: String,
        subtypeConfig: CwtSubtypeConfig,
        subtypeConfigs: MutableList<CwtSubtypeConfig>,
        configGroup: CwtConfigGroup,
        matchOptions: Int = ParadoxExpressionMatcher.Options.Default
    ): Boolean {
        val fastResult = matchesSubtypeFast(rootKey, subtypeConfig, subtypeConfigs)
        if (fastResult != null) return fastResult

        //根据config对property进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true
        return doMatchDefinition(element, elementConfig, configGroup, matchOptions)
    }

    private fun matchesSubtypeFast(
        rootKey: String,
        subtypeConfig: CwtSubtypeConfig,
        subtypeConfigs: MutableList<CwtSubtypeConfig>
    ): Boolean? {
        //如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
        val onlyIfNotConfig = subtypeConfig.onlyIfNot
        if (!onlyIfNotConfig.isNullOrEmpty()) {
            val matchesAny = subtypeConfigs.any { it.name in onlyIfNotConfig }
            if (matchesAny) return false
        }

        //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = subtypeConfig.startsWith
        if (!startsWithConfig.isNullOrEmpty()) {
            val result = rootKey.startsWith(startsWithConfig, false)
            if (!result) return false
        }

        //如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = subtypeConfig.typeKeyRegex
        if (typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(rootKey)
            if (!result) return false
        }

        //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
        if (typeKeyFilterConfig != null && typeKeyFilterConfig.value.isNotEmpty()) {
            val filterResult = typeKeyFilterConfig.withOperator { it.contains(rootKey) }
            if (!filterResult) return false
        }

        //根据config对property进行内容匹配
        val elementConfig = subtypeConfig.config
        if (elementConfig.configs.isNullOrEmpty()) return true

        return null //需要进一步匹配
    }

    private fun doMatchDefinition(
        definitionElement: ParadoxScriptDefinitionElement,
        propertyConfig: CwtPropertyConfig,
        configGroup: CwtConfigGroup,
        matchOptions: Int
    ): Boolean {
        //这里不能基于内联后的声明结构，否则可能会导致SOE
        //也不要参数条件表达式中的声明结构判断，
        val childValueConfigs = propertyConfig.values.orEmpty()
        val blockElement = definitionElement.block
        if (childValueConfigs.isNotEmpty()) {
            //匹配值列表
            if (!doMatchValues(blockElement, childValueConfigs, configGroup, matchOptions)) return false //继续匹配
        }
        val childPropertyConfigs = propertyConfig.properties.orEmpty()
        if (childPropertyConfigs.isNotEmpty()) {
            //匹配属性列表
            if (!doMatchProperties(definitionElement, blockElement, childPropertyConfigs, configGroup, matchOptions)) return false //继续匹配
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
        //对于propertyValue同样这样判断（可能脚本没有写完）
        if (propValue == null) return propertyConfig.cardinality?.min == 0

        when {
            //匹配布尔值
            propertyConfig.booleanValue != null -> {
                if (propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
            }
            //匹配值
            propertyConfig.stringValue != null -> {
                val expression = ParadoxScriptExpression.resolve(propValue, matchOptions)
                return ParadoxScriptExpressionMatcher.matches(propValue, expression, propertyConfig.valueExpression, propertyConfig, configGroup, matchOptions).get(matchOptions)
            }
            //匹配single_alias
            ParadoxExpressionManager.isSingleAliasEntryConfig(propertyConfig) -> {
                return doMatchSingleAlias(definitionElement, propertyElement, propertyConfig, configGroup, matchOptions)
            }
            //匹配alias
            ParadoxExpressionManager.isAliasEntryConfig(propertyConfig) -> {
                return doMatchAlias(definitionElement, propertyElement, propertyConfig, matchOptions)
            }
            propertyConfig.configs.orEmpty().isNotEmpty() -> {
                val blockElement = propertyElement.block
                //匹配值列表
                if (!doMatchValues(blockElement, propertyConfig.values.orEmpty(), configGroup, matchOptions)) return false
                //匹配属性列表
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

        //注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
        val matched = blockElement.properties().all p@{ propertyElement ->
            val keyElement = propertyElement.propertyKey
            val expression = ParadoxScriptExpression.resolve(keyElement, matchOptions)
            val propConfigs = propertyConfigs.filter {
                ParadoxScriptExpressionMatcher.matches(keyElement, expression, it.keyExpression, it, configGroup, matchOptions).get(matchOptions)
            }

            //如果没有匹配的规则则忽略
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

        val matched = blockElement.values().process p@{ valueElement ->
            //如果没有匹配的规则则忽略
            val expression = ParadoxScriptExpression.resolve(valueElement, matchOptions)

            val matched = valueConfigs.any { valueConfig ->
                val matched = ParadoxScriptExpressionMatcher.matches(valueElement, expression, valueConfig.valueExpression, valueConfig, configGroup, matchOptions).get(matchOptions)
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

        //aliasName和aliasSubName需要匹配
        val aliasName = propertyConfig.keyExpression.value ?: return false
        val key = propertyElement.name
        val quoted = propertyElement.propertyKey.text.isLeftQuoted()
        val configGroup = propertyConfig.configGroup
        val aliasSubName = ParadoxExpressionManager.getAliasSubName(propertyElement, key, quoted, aliasName, configGroup, matchOptions) ?: return false
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
        val aliases = aliasGroup[aliasSubName] ?: return false
        return aliases.any { alias ->
            doMatchProperty(definitionElement, propertyElement, alias.config, configGroup, matchOptions)
        }
    }

    fun getName(element: ParadoxScriptDefinitionElement): String? {
        runReadAction { element.castOrNull<ParadoxScriptProperty>()?.greenStub }?.let { return it.name }
        return element.definitionInfo?.name
    }

    fun getType(element: ParadoxScriptDefinitionElement): String? {
        runReadAction { element.castOrNull<ParadoxScriptProperty>()?.greenStub }?.let { return it.type }
        return element.definitionInfo?.type
    }

    fun getSubtypes(element: ParadoxScriptDefinitionElement): List<String>? {
        //定义的subtype可能需要通过访问索引获取，不能在索引时就获取
        return element.definitionInfo?.subtypes
    }

    //stub methods

    fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*>? {
        if (file !is ParadoxScriptFile) return null
        val node = tree.root
        val rootKey = file.name.substringBeforeLast('.')
        val project = file.project
        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = selectGameType(vFile) ?: return null
        val path = fileInfo.path
        val elementPath = ParadoxExpressionPath.Empty
        val configGroup = PlsFacade.getConfigGroup(project, gameType) //这里需要指定project
        val typeConfig = getMatchedTypeConfig(node, tree, configGroup, path, elementPath, rootKey, null)
        if (typeConfig == null) return null
        //NOTE 这里不处理需要内联的情况
        val name = getNameWhenCreateDefinitionStub(typeConfig, rootKey, node, tree)
        val type = typeConfig.name
        val subtypes = getSubtypesWhenCreateDefinitionStub(typeConfig, rootKey) //如果无法在索引时获取，之后再懒加载
        return ParadoxScriptFileStubImpl(file, name, type, subtypes, gameType)
    }

    fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub? {
        val rootKey = psi.name
        if (rootKey.isInlineUsage()) return null //排除是内联调用的情况
        if (rootKey.isParameterized()) return null //排除可能带参数的情况
        val definitionInfo = psi.definitionInfo ?: return null
        val name = definitionInfo.name
        val type = definitionInfo.type
        val subtypes = runCatchingCancelable { definitionInfo.subtypes }.getOrNull() //如果无法在索引时获取，之后再懒加载
        val elementPath = definitionInfo.elementPath
        val gameType = definitionInfo.gameType
        return ParadoxScriptPropertyStub.Impl(parentStub, name, type, subtypes, rootKey, elementPath, gameType)
    }

    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub? {
        val rootKey = getNameFromNode(node, tree) ?: return null
        if (rootKey.isInlineUsage()) return null //排除是内联调用的情况
        if (rootKey.isParameterized()) return null //排除可能带参数的情况
        val psi = parentStub.psi
        val file = psi.containingFile
        val project = file.project
        val vFile = selectFile(file) ?: return null
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = selectGameType(vFile) ?: return null
        val path = fileInfo.path
        val configGroup = PlsFacade.getConfigGroup(project, gameType) //这里需要指定project
        val elementPath = ParadoxExpressionPathManager.get(node, tree, vFile, PlsFacade.getInternalSettings().maxDefinitionDepth)
        if (elementPath == null) return null
        val rootKeyPrefix = lazy { ParadoxExpressionPathManager.getKeyPrefixes(node, tree).firstOrNull() }
        val typeConfig = getMatchedTypeConfig(node, tree, configGroup, path, elementPath, rootKey, rootKeyPrefix)
        if (typeConfig == null) return null
        //NOTE 这里不处理需要内联的情况
        val name = getNameWhenCreateDefinitionStub(typeConfig, rootKey, node, tree)
        val type = typeConfig.name
        val subtypes = getSubtypesWhenCreateDefinitionStub(typeConfig, rootKey) //如果无法在索引时获取，之后再懒加载
        return ParadoxScriptPropertyStub.Impl(parentStub, name, type, subtypes, rootKey, elementPath, gameType)
    }

    private fun getNameWhenCreateDefinitionStub(typeConfig: CwtTypeConfig, rootKey: String, node: LighterASTNode, tree: LighterAST): String {
        return when {
            typeConfig.nameFromFile -> rootKey
            typeConfig.nameField == null -> rootKey
            typeConfig.nameField == "" -> ""
            typeConfig.nameField == "-" -> getValueFromNode(node, tree).orEmpty()
            else -> node.firstChild(tree, ParadoxScriptTokenSets.BLOCK_OR_ROOT_BLOCK)
                ?.firstChild(tree) { it.tokenType == PROPERTY && getNameFromNode(it, tree)?.equals(typeConfig.nameField, true) == true }
                ?.let { getValueFromNode(it, tree) }
                .orEmpty()
        }
    }

    private fun getSubtypesWhenCreateDefinitionStub(typeConfig: CwtTypeConfig, rootKey: String): List<String>? {
        val subtypesConfig = typeConfig.subtypes
        val result = mutableListOf<CwtSubtypeConfig>()
        for (subtypeConfig in subtypesConfig.values) {
            if (matchesSubtypeFast(rootKey, subtypeConfig, result) ?: return null) {
                result.add(subtypeConfig)
            }
        }
        return result.map { it.name }
    }

    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, PROPERTY_KEY)
            ?.childrenOfType(tree, PROPERTY_KEY_TOKEN)?.singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }

    private fun getValueFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, STRING)
            ?.childrenOfType(tree, STRING_TOKEN)?.singleOrNull()
            ?.internNode(tree)?.toString()?.unquote()
    }

    fun getInfoFromStub(element: ParadoxScriptDefinitionElement, project: Project): ParadoxDefinitionInfo? {
        val stub = runReadAction { element.castOrNull<ParadoxScriptProperty>()?.greenStub } ?: return null
        if (!(stub.isValidDefinition)) return null
        val name = stub.name
        val type = stub.type
        val gameType = stub.gameType
        val configGroup = PlsFacade.getConfigGroup(project, gameType) //这里需要指定project
        val typeConfig = configGroup.types[type] ?: return null
        val subtypes = stub.subtypes
        val subtypeConfigs = subtypes?.mapNotNull { typeConfig.subtypes[it] }
        val rootKey = stub.rootKey
        val elementPath = stub.elementPath
        return ParadoxDefinitionInfo(element, typeConfig, name, subtypeConfigs, rootKey, elementPath, gameType, configGroup)
    }

    //related localisations & images methods

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
        if (primaryLocalisations.isEmpty()) return null //没有或者CWT规则不完善
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

    private fun doGetPrimaryLocalisationFromCache(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? =
        CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionPrimaryLocalisation) {
            ProgressManager.checkCanceled()
            val value = doGetPrimaryLocalisation(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.LocalisationFileTracker, ParadoxModificationTrackers.LocaleTracker)
        }

    private fun doGetPrimaryLocalisation(element: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryLocalisations = definitionInfo.primaryLocalisations
        if (primaryLocalisations.isEmpty()) return null //没有或者CWT规则不完善
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
        if (primaryLocalisations.isEmpty()) return emptySet() //没有或者CWT规则不完善
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
            value.withDependencyItems(element, ParadoxModificationTrackers.FileTracker)
        }
    }

    private fun doGetPrimaryImage(element: ParadoxScriptDefinitionElement): PsiFile? {
        val definitionInfo = element.definitionInfo ?: return null
        val primaryImages = definitionInfo.primaryImages
        if (primaryImages.isEmpty()) return null //没有或者CWT规则不完善
        for (primaryImage in primaryImages) {
            val resolved = CwtLocationExpressionManager.resolve(primaryImage.locationExpression, element, definitionInfo, toFile = true)
            val file = resolved?.element?.castOrNull<PsiFile>()
            if (file == null) continue
            element.putUserData(PlsKeys.imageFrameInfo, resolved.frameInfo)
            return file
        }
        return null
    }

    fun getLocalizedNames(element: ParadoxScriptDefinitionElement): Set<String> {
        return doGetLocalizedNamesFromCache(element)
    }

    private fun doGetLocalizedNamesFromCache(element: ParadoxScriptDefinitionElement): Set<String> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefinitionLocalizedNames) {
            ProgressManager.checkCanceled()
            val value = doGetLocalizedNames(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.LocalisationFileTracker, ParadoxModificationTrackers.LocaleTracker)
        }
    }

    private fun doGetLocalizedNames(element: ParadoxScriptDefinitionElement): Set<String> {
        //这里返回的是基于偏好语言区域的所有本地化名字（即使最终会被覆盖掉）
        val localizedNames = mutableSetOf<String>()
        val primaryLocalisations = getPrimaryLocalisations(element)
        primaryLocalisations.forEach { localisation ->
            ProgressManager.checkCanceled()
            val r = ParadoxLocalisationTextRenderer().render(localisation).orNull()
            if (r != null) localizedNames.add(r)
        }
        return localizedNames
    }
}
