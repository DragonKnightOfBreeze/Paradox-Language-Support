package icu.windea.pls.lang

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

/**
 * 用于处理定义。
 *
 * @see ParadoxScriptDefinitionElement
 * @see ParadoxDefinitionInfo
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ParadoxDefinitionHandler {
    val definitionNamePrefixKey = Key.create<String>("paradox.definition.prefix")
    
    fun getInfo(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        ProgressManager.checkCanceled()
        val notUseCache = element.getUserData(PlsKeys.isIncompleteKey) == true
        if(notUseCache) {
            val file = element.containingFile
            return resolveInfo(element, file)
        }
        return getInfoFromCache(element)
    }
    
    private fun getInfoFromCache(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionInfoKey) {
            val file = element.containingFile
            val value = resolveInfo(element, file)
            //invalidated on file modification
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun resolveInfo(element: ParadoxScriptDefinitionElement, file: PsiFile = element.containingFile): ParadoxDefinitionInfo? {
        val rootKey = element.name.let { if(element is ParadoxScriptFile) it.substringBeforeLast('.') else it } //如果是文件名，不要包含扩展名
        if(element is ParadoxScriptProperty && rootKey.isParameterAwareExpression()) return null //排除可能带参数的情况
        
        ProgressManager.checkCanceled()
        val project = file.project
        
        //首先尝试直接基于stub进行解析
        val stub = runCatching { element.getStub() }.getOrNull()
        if(stub != null) {
            return resolveInfoByStub(element, stub, project)
        }
        
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.entryPath //这里使用entryPath
        val elementPath = ParadoxElementPathHandler.getFromFile(element, PlsConstants.maxDefinitionDepth) ?: return null
        val gameType = fileInfo.rootInfo.gameType //这里还是基于fileInfo获取gameType
        val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
        for(typeConfig in configGroup.types.values) {
            ProgressManager.checkCanceled()
            if(matchesType(element, typeConfig, path, elementPath, rootKey, configGroup)) {
                //需要懒加载
                return ParadoxDefinitionInfo(null, rootKey, typeConfig, elementPath, gameType, configGroup, element)
            }
        }
        return null
    }
    
    private fun resolveInfoByStub(element: ParadoxScriptDefinitionElement, stub: ParadoxScriptDefinitionElementStub<out ParadoxScriptDefinitionElement>, project: Project): ParadoxDefinitionInfo? {
        val gameType = stub.gameType ?: return null
        val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
        val name = stub.name.takeIfNotEmpty() ?: return null
        val type = stub.type.takeIfNotEmpty() ?: return null
        val typeConfig = configGroup.types[type] ?: return null
        //val subtypes = stub.subtypes
        //val subtypeConfigs = subtypes?.mapNotNull { typeConfig.subtypes[it] }
        val rootKey = stub.rootKey
        val elementPath = stub.elementPath
        return ParadoxDefinitionInfo(name, rootKey, typeConfig, elementPath, gameType, configGroup, element)
            .apply { sourceType = ParadoxDefinitionInfo.SourceType.Stub }
    }
    
    fun matchesType(
        element: ParadoxScriptDefinitionElement,
        typeConfig: CwtTypeConfig,
        path: ParadoxPath,
        elementPath: ParadoxElementPath,
        rootKey: String,
        configGroup: CwtConfigGroup
    ): Boolean {
        //判断element是否需要是scriptFile还是scriptProperty
        val nameFromFileConfig = typeConfig.nameFromFile
        if(nameFromFileConfig) {
            if(element !is ParadoxScriptFile) return false
        } else {
            if(element !is ParadoxScriptProperty) return false
        }
        
        //判断path是否匹配
        val pathConfig = typeConfig.path ?: return false
        val pathStrictConfig = typeConfig.pathStrict
        if(pathStrictConfig) {
            if(pathConfig != path.parent) return false
        } else {
            if(!pathConfig.matchesPath(path.parent)) return false
        }
        //判断path_name是否匹配
        val pathFileConfig = typeConfig.pathFile //String?
        if(pathFileConfig != null) {
            if(pathFileConfig != path.fileName) return false
        }
        //判断path_extension是否匹配（默认为".txt"，CWT文件中可能未填写，此时直接留空）
        val pathExtensionConfig = typeConfig.pathExtension //String?
        if(pathExtensionConfig != null) {
            if(pathExtensionConfig != "." + path.fileExtension) return false
        }
        
        //如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
        //skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
        //skip_root_key可以重复（其中之一匹配即可）
        val skipRootKeyConfig = typeConfig.skipRootKey
        if(skipRootKeyConfig.isNullOrEmpty()) {
            if(elementPath.length > 1) return false
        } else {
            val result = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
            if(!result) return false
        }
        //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = typeConfig.startsWith
        if(!startsWithConfig.isNullOrEmpty()) {
            val result = rootKey.startsWith(startsWithConfig, false)
            if(!result) return false
        }
        //如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = typeConfig.typeKeyRegex
        if(typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(rootKey)
            if(!result) return false
        }
        //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = typeConfig.typeKeyFilter
        if(!typeKeyFilterConfig.isNullOrEmpty()) {
            val result = typeKeyFilterConfig.contains(rootKey)
            if(!result) return false
        }
        //如果name_field存在，则要求type_key必须是指定的所有type_key之一，或者没有任何指定的type_key
        val nameFieldConfig = typeConfig.nameField
        if(nameFieldConfig != null) {
            val result = (typeConfig.typeKeyFilter == null && typeConfig.subtypes.values.all { it.typeKeyFilter == null })
                || typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
                || typeConfig.subtypes.values.any { it.typeKeyFilter?.set?.contains(rootKey) == true }
            if(!result) return false
        }
        
        //判断element的propertyValue是否需要是block
        val declarationConfig = configGroup.declarations[typeConfig.name]?.propertyConfig
        //当进行代码补全时需要特殊处理
        val isBlock = when {
            element.getUserData(PlsKeys.isIncompleteKey) == true -> null
            else -> element.castOrNull<ParadoxScriptProperty>()?.propertyValue?.let { it is ParadoxScriptBlock }
        }
        if(declarationConfig != null && isBlock != null) {
            val isBlockConfig = declarationConfig.valueExpression.type == CwtDataType.Block
            if(isBlockConfig != isBlock) return false
        }
        
        return true
    }
    
    fun matchesType(
        node: LighterASTNode,
        tree: LighterAST,
        typeConfig: CwtTypeConfig,
        path: ParadoxPath,
        elementPath: ParadoxElementPath,
        rootKey: String,
        configGroup: CwtConfigGroup
    ): Boolean {
        //判断element是否需要是scriptFile还是scriptProperty
        val nameFromFileConfig = typeConfig.nameFromFile
        if(nameFromFileConfig) {
            if(node.tokenType != ParadoxScriptStubElementTypes.FILE) return false
        } else {
            if(node.tokenType != PROPERTY) return false
        }
        
        //判断path是否匹配
        val pathConfig = typeConfig.path ?: return false
        val pathStrictConfig = typeConfig.pathStrict
        if(pathStrictConfig) {
            if(pathConfig != path.parent) return false
        } else {
            if(!pathConfig.matchesPath(path.parent)) return false
        }
        //判断path_name是否匹配
        val pathFileConfig = typeConfig.pathFile //String?
        if(pathFileConfig != null) {
            if(pathFileConfig != path.fileName) return false
        }
        //判断path_extension是否匹配（默认为".txt"，CWT文件中可能未填写，此时直接留空）
        val pathExtensionConfig = typeConfig.pathExtension //String?
        if(pathExtensionConfig != null) {
            if(pathExtensionConfig != "." + path.fileExtension) return false
        }
        
        //如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
        //skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
        //skip_root_key可以重复（其中之一匹配即可）
        val skipRootKeyConfig = typeConfig.skipRootKey
        if(skipRootKeyConfig.isNullOrEmpty()) {
            if(elementPath.length > 1) return false
        } else {
            val result = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
            if(!result) return false
        }
        //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = typeConfig.startsWith
        if(!startsWithConfig.isNullOrEmpty()) {
            val result = rootKey.startsWith(startsWithConfig, false)
            if(!result) return false
        }
        //如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = typeConfig.typeKeyRegex
        if(typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(rootKey)
            if(!result) return false
        }
        //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = typeConfig.typeKeyFilter
        if(!typeKeyFilterConfig.isNullOrEmpty()) {
            val result = typeKeyFilterConfig.contains(rootKey)
            if(!result) return false
        }
        //如果name_field存在，则要求type_key必须是指定的所有type_key之一，或者没有任何指定的type_key
        val nameFieldConfig = typeConfig.nameField
        if(nameFieldConfig != null) {
            val result = (typeConfig.typeKeyFilter == null && typeConfig.subtypes.values.all { it.typeKeyFilter == null })
                || typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
                || typeConfig.subtypes.values.any { it.typeKeyFilter?.set?.contains(rootKey) == true }
            if(!result) return false
        }
        
        //判断element的propertyValue是否需要是block
        val declarationConfig = configGroup.declarations[typeConfig.name]?.propertyConfig
        //当进行代码补全时需要特殊处理
        val isBlock = node.firstChild(tree, ParadoxScriptTokenSets.VALUES)?.let { it.tokenType == BLOCK }
        if(declarationConfig != null && isBlock != null) {
            val isBlockConfig = declarationConfig.valueExpression.type == CwtDataType.Block
            if(isBlockConfig != isBlock) return false
        }
        
        return true
    }
    
    fun matchesTypeWithKnownType(
        typeConfig: CwtTypeConfig,
        rootKey: String
    ): Boolean {
        //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = typeConfig.startsWith
        if(!startsWithConfig.isNullOrEmpty()) {
            val result = rootKey.startsWith(startsWithConfig, false)
            if(!result) return false
        }
        //如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = typeConfig.typeKeyRegex
        if(typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(rootKey)
            if(!result) return false
        }
        //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = typeConfig.typeKeyFilter
        if(!typeKeyFilterConfig.isNullOrEmpty()) {
            val filterResult = typeKeyFilterConfig.contains(rootKey)
            if(!filterResult) return false
        }
        //如果name_field存在，则要求type_key必须是指定的所有type_key之一
        val nameFieldConfig = typeConfig.nameField
        if(nameFieldConfig != null) {
            val result = typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
                || typeConfig.subtypes.values.any { subtypeConfig -> subtypeConfig.typeKeyFilter?.set?.contains(rootKey) == true }
            if(!result) return false
        }
        return true
    }
    
    fun matchesTypeWithUnknownDeclaration(
        typeConfig: CwtTypeConfig,
        path: ParadoxPath,
        elementPath: ParadoxElementPath?,
        rootKey: String?
    ): Boolean {
        //判断element是否需要是scriptFile还是scriptProperty
        val nameFromFileConfig = typeConfig.nameFromFile
        if(nameFromFileConfig) return false
        
        //判断path是否匹配
        val pathConfig = typeConfig.path ?: return false
        val pathStrictConfig = typeConfig.pathStrict
        if(pathStrictConfig) {
            if(pathConfig != path.parent) return false
        } else {
            if(!pathConfig.matchesPath(path.parent)) return false
        }
        //判断path_name是否匹配
        val pathFileConfig = typeConfig.pathFile //String?
        if(pathFileConfig != null) {
            if(pathFileConfig != path.fileName) return false
        }
        //判断path_extension是否匹配
        val pathExtensionConfig = typeConfig.pathExtension //String?
        if(pathExtensionConfig != null) {
            if(pathExtensionConfig != "." + path.fileExtension) return false
        }
        
        if(elementPath != null) {
            //如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
            //skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
            //skip_root_key可以重复（其中之一匹配即可）
            val skipRootKeyConfig = typeConfig.skipRootKey
            if(skipRootKeyConfig.isNullOrEmpty()) {
                if(elementPath.length > 1) return false
            } else {
                val skipResult = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
                if(!skipResult) return false
            }
        }
        
        if(rootKey != null) {
            //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
            val startsWithConfig = typeConfig.startsWith
            if(!startsWithConfig.isNullOrEmpty()) {
                val result = rootKey.startsWith(startsWithConfig, false)
                if(!result) return false
            }
            //如果type_key_regex存在，则要求type_key匹配
            val typeKeyRegexConfig = typeConfig.typeKeyRegex
            if(typeKeyRegexConfig != null) {
                val result = typeKeyRegexConfig.matches(rootKey)
                if(!result) return false
            }
            //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
            val typeKeyFilterConfig = typeConfig.typeKeyFilter
            if(!typeKeyFilterConfig.isNullOrEmpty()) {
                val filterResult = typeKeyFilterConfig.contains(rootKey)
                if(!filterResult) return false
            }
            //如果name_field存在，则要求type_key必须是指定的所有type_key之一
            val nameFieldConfig = typeConfig.nameField
            if(nameFieldConfig != null) {
                val result = typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
                    || typeConfig.subtypes.values.any { subtypeConfig -> subtypeConfig.typeKeyFilter?.set?.contains(rootKey) == true }
                if(!result) return false
            }
        }
        
        return true
    }
    
    fun matchesSubtype(
        element: ParadoxScriptDefinitionElement,
        subtypeConfig: CwtSubtypeConfig,
        rootKey: String,
        configGroup: CwtConfigGroup,
        subtypes: MutableList<CwtSubtypeConfig>,
        matchType: Int = CwtConfigMatchType.DEFAULT
    ): Boolean {
        //如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
        val onlyIfNotConfig = subtypeConfig.onlyIfNot
        if(!onlyIfNotConfig.isNullOrEmpty()) {
            val matchesAny = subtypes.any { it.name in onlyIfNotConfig }
            if(matchesAny) return false
        }
        //如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
        val startsWithConfig = subtypeConfig.startsWith
        if(!startsWithConfig.isNullOrEmpty()) {
            val result = rootKey.startsWith(startsWithConfig, false)
            if(!result) return false
        }
        //如果type_key_regex存在，则要求type_key匹配
        val typeKeyRegexConfig = subtypeConfig.typeKeyRegex
        if(typeKeyRegexConfig != null) {
            val result = typeKeyRegexConfig.matches(rootKey)
            if(!result) return false
        }
        //如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
        val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
        if(!typeKeyFilterConfig.isNullOrEmpty()) {
            val filterResult = typeKeyFilterConfig.contains(rootKey)
            if(!filterResult) return false
        }
        //根据config对property进行内容匹配
        val elementConfig = subtypeConfig.config
        return doMatchDefinition(element, elementConfig, configGroup, matchType)
    }
    
    private fun doMatchDefinition(definitionElement: ParadoxScriptDefinitionElement, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
        val childValueConfigs = propertyConfig.values.orEmpty()
        val blockElement = definitionElement.block
        if(childValueConfigs.isNotEmpty()) {
            //匹配值列表
            if(!doMatchValues(blockElement, childValueConfigs, configGroup, matchType)) return false //继续匹配
        }
        val childPropertyConfigs = propertyConfig.properties.orEmpty()
        if(childPropertyConfigs.isNotEmpty()) {
            //匹配属性列表
            if(!doMatchProperties(blockElement, childPropertyConfigs, configGroup, matchType)) return false //继续匹配
        }
        return true
    }
    
    private fun doMatchProperty(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        val propValue = propertyElement.propertyValue
        if(propValue == null) {
            //对于propertyValue同样这样判断（可能脚本没有写完）
            return propertyConfig.cardinality?.min == 0
        } else {
            when {
                //匹配布尔值
                propertyConfig.booleanValue != null -> {
                    if(propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
                }
                //匹配值
                propertyConfig.stringValue != null -> {
                    val expression = ParadoxDataExpression.resolve(propValue, matchType)
                    return ParadoxConfigHandler.matchesScriptExpression(propValue, expression, propertyConfig.valueExpression, propertyConfig, configGroup, matchType)
                }
                //匹配single_alias
                ParadoxConfigHandler.isSingleAlias(propertyConfig) -> {
                    return doMatchSingleAlias(propertyElement, propertyConfig, configGroup, matchType)
                }
                //匹配alias
                ParadoxConfigHandler.isAlias(propertyConfig) -> {
                    return doMatchAlias(propertyElement, propertyConfig, configGroup, matchType)
                }
                propertyConfig.configs.orEmpty().isNotEmpty() -> {
                    val blockElement = propertyElement.block
                    //匹配值列表
                    if(!doMatchValues(blockElement, propertyConfig.values.orEmpty(), configGroup, matchType)) return false
                    //匹配属性列表
                    if(!doMatchProperties(blockElement, propertyConfig.properties.orEmpty(), configGroup, matchType)) return false
                }
            }
        }
        return true
    }
    
    private fun doMatchProperties(blockElement: ParadoxScriptBlockElement?, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        if(propertyConfigs.isEmpty()) return true
        if(blockElement == null) return false
        
        //要求其中所有的value的值在最终都会小于等于指定值
        val minMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.cardinality?.min ?: 1 }) //默认为1
        
        //注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
        val matched = blockElement.processProperty { propertyElement ->
            val keyElement = propertyElement.propertyKey
            val expression = ParadoxDataExpression.resolve(keyElement, matchType)
            val propConfigs = propertyConfigs.filter {
                ParadoxConfigHandler.matchesScriptExpression(keyElement, expression, it.keyExpression, it, configGroup, matchType)
            }
            //如果没有匹配的规则则忽略
            if(propConfigs.isNotEmpty()) {
                val matched = propConfigs.any { propConfig ->
                    val matched = doMatchProperty(propertyElement, propConfig, configGroup, matchType)
                    if(matched) minMap.compute(propConfig.key) { _, v -> if(v == null) 1 else v - 1 }
                    matched
                }
                matched
            } else {
                true
            }
        }
        if(!matched) return false
        
        return minMap.values.any { it <= 0 }
    }
    
    private fun doMatchValues(blockElement: ParadoxScriptBlockElement?, valueConfigs: List<CwtValueConfig>, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        if(valueConfigs.isEmpty()) return true
        if(blockElement == null) return false
        //要求其中所有的value的值在最终都会小于等于指定值
        val minMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.cardinality?.min ?: 1 }) //默认为1
        
        val matched = blockElement.processValue { valueElement ->
            //如果没有匹配的规则则忽略
            val expression = ParadoxDataExpression.resolve(valueElement, matchType)
            
            val matched = valueConfigs.any { valueConfig ->
                val matched = ParadoxConfigHandler.matchesScriptExpression(valueElement, expression, valueConfig.valueExpression, valueConfig, configGroup, matchType)
                if(matched) minMap.compute(valueConfig.value) { _, v -> if(v == null) 1 else v - 1 }
                matched
            }
            matched
        }
        if(!matched) return false
        
        return minMap.values.any { it <= 0 }
    }
    
    private fun doMatchSingleAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        val singleAliasName = propertyConfig.valueExpression.value ?: return false
        val singleAlias = configGroup.singleAliases[singleAliasName] ?: return false
        return doMatchProperty(propertyElement, singleAlias.config, configGroup, matchType)
    }
    
    private fun doMatchAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        //aliasName和aliasSubName需要匹配
        val aliasName = propertyConfig.keyExpression.value ?: return false
        val key = propertyElement.name
        val quoted = propertyElement.propertyKey.text.isLeftQuoted()
        val aliasSubName = ParadoxConfigHandler.getAliasSubName(propertyElement, key, quoted, aliasName, configGroup, matchType) ?: return false
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
        val aliases = aliasGroup[aliasSubName] ?: return false
        return aliases.any { alias ->
            doMatchProperty(propertyElement, alias.config, configGroup, matchType)
        }
    }
    
    fun getName(element: ParadoxScriptDefinitionElement): String? {
        return runCatching { element.getStub() }.getOrNull()?.name ?: element.definitionInfo?.name
    }
    
    fun getType(element: ParadoxScriptDefinitionElement): String? {
        return runCatching { element.getStub() }.getOrNull()?.type ?: element.definitionInfo?.type
    }
    
    fun getSubtypes(element: ParadoxScriptDefinitionElement): List<String>? {
        //定义的subtype可能需要通过访问索引获取，不能在索引时就获取
        return element.definitionInfo?.subtypes
    }
    
    fun getDefinitionNamePrefixOption(definitionInfo: ParadoxDefinitionInfo): String {
        //return config.getOrPutUserData(definitionNamePrefixKey) {
        //	val option = config.options?.find { it.key == "prefix" }
        //	return option?.stringValue.orEmpty()
        //}
        definitionInfo.subtypeConfigs.forEach { subtypeConfig ->
            val config = subtypeConfig.config
            val prefix = config.getOrPutUserData(definitionNamePrefixKey) {
                val option = config.options?.find { it.key == "prefix" }
                return option?.stringValue.orEmpty()
            }
            if(prefix.isNotEmpty()) return prefix
        }
        val prefix = definitionInfo.typeConfig.config.getOrPutUserData(definitionNamePrefixKey) {
            val option = definitionInfo.typeConfig.config.options?.find { it.key == "prefix" }
            return option?.stringValue.orEmpty()
        }
        return prefix
    }
    
    //stub methods
    fun createStubForFile(file: PsiFile, tree: LighterAST): StubElement<*>? {
        //这里使用scriptProperty.definitionInfo.name而非scriptProperty.name
        val psiFile = file as? ParadoxScriptFile ?: return null
        val definitionInfo = psiFile.definitionInfo ?: return null
        val name = definitionInfo.name
        val type = definitionInfo.type
        //val subtypes = definitionInfo.subtypes
        val gameType = definitionInfo.gameType
        //return ParadoxScriptFileStubImpl(psiFile, name, type, subtypes, gameType)
        return ParadoxScriptFileStubImpl(psiFile, name, type, gameType)
    }
    
    fun createStub(psi: ParadoxScriptProperty, parentStub: StubElement<*>): ParadoxScriptPropertyStub? {
        //这里使用scriptProperty.definitionInfo.name而非scriptProperty.name
        val definitionInfo = psi.definitionInfo ?: return null
        val name = definitionInfo.name
        val type = definitionInfo.type
        //val subtypes = definitionInfo.subtypes
        val rootKey = definitionInfo.rootKey
        val elementPath = definitionInfo.elementPath
        val gameType = definitionInfo.gameType
        //return ParadoxScriptPropertyStubImpl(parentStub, name, type, subtypes, rootKey, elementPath, gameType)
        return ParadoxScriptPropertyStubImpl(parentStub, name, type, rootKey, elementPath, gameType)
    }
    
    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptPropertyStub? {
        val rootKey = getNameFromNode(node, tree) ?: return null
        if(rootKey.isParameterAwareExpression()) return null //排除可能带参数的情况
        val psi = parentStub.psi
        val psiFile = psi.containingFile
        val project = psiFile.project
        val file = selectFile(psi) ?: return null
        val fileInfo = file.fileInfo ?: return null
        val gameType = selectGameType(file) ?: return null
        val path = fileInfo.entryPath //这里使用entryPath
        val elementPath = ParadoxElementPathHandler.getFromFile(node, tree, file, PlsConstants.maxDefinitionDepth) ?: return null
        val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
        for(typeConfig in configGroup.types.values) {
            ProgressManager.checkCanceled()
            if(matchesType(node, tree, typeConfig, path, elementPath, rootKey, configGroup)) {
                //NOTE 这里不处理内联的情况
                val name = when {
                    typeConfig.nameFromFile -> rootKey
                    typeConfig.nameField == "" -> {
                        getValueFromNode(node, tree).orAnonymous()
                    }
                    typeConfig.nameField != null -> {
                        node.firstChild(tree, BLOCK)
                            ?.firstChild(tree) { it.tokenType == PROPERTY && getNameFromNode(it, tree)?.equals(typeConfig.nameField, true) == true }
                            ?.let { getValueFromNode(it, tree) }
                            .orAnonymous()
                    }
                    else -> rootKey
                }
                val type = typeConfig.name
                return ParadoxScriptPropertyStubImpl(parentStub, name, type, rootKey, elementPath, gameType)
            }
        }
        return null
    }
    
    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, PROPERTY_KEY)?.firstChild(tree, ParadoxScriptTokenSets.PROPERTY_KEY_TOKENS)?.internNode(tree)?.toString()?.unquote()
    }
    
    private fun getValueFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, STRING)?.firstChild(tree, ParadoxScriptTokenSets.STRING_TOKENS)?.internNode(tree)?.toString()?.unquote()
    }
    
    fun shouldCreateStub(node: ASTNode): Boolean {
        return true //just true
    }
    
    fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return true //just true
    }
}
