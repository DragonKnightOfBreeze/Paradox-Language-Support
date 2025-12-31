package icu.windea.pls.ep.configGroup

import com.intellij.openapi.application.readAction
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.config.findPropertyByPath
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.takeWithOperator
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.model.paths.CwtConfigPath
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * 用于初始化规则分组中需要经过计算的那些数据。
 */
class CwtComputedConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override suspend fun process(configGroup: CwtConfigGroup) {
        val currentCoroutineContext = currentCoroutineContext()
        val initializer = configGroup.initializer

        // compute `type2ModifiersMap` and complete `modifiers`
        run {
            currentCoroutineContext.ensureActive()
            for ((name, modifierConfig) in initializer.modifiers) {
                for (snippetExpression in modifierConfig.template.snippetExpressions) {
                    if (snippetExpression.type == CwtDataTypes.Definition) {
                        val typeExpression = snippetExpression.value ?: continue
                        initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { FastMap() }[name] = modifierConfig
                    }
                }
            }
            // merge all properties named 'modifiers'
            for ((name, typeConfig) in initializer.types) {
                val modifiersProps = typeConfig.config.properties?.filter { it.key == "modifiers" }
                if (modifiersProps.isNullOrEmpty()) continue
                modifiersProps.forEach { prop ->
                    for (p in prop.properties.orEmpty()) {
                        val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
                        if (subtypeName != null) {
                            for (pp in p.properties.orEmpty()) {
                                val typeExpression = "$name.$subtypeName"
                                val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(pp, pp.key, typeExpression) ?: continue
                                initializer.modifiers[modifierConfig.name] = modifierConfig
                                initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { FastMap() }[pp.key] = modifierConfig
                            }
                        } else {
                            val typeExpression = name
                            val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(p, p.key, typeExpression) ?: continue
                            initializer.modifiers[modifierConfig.name] = modifierConfig
                            initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { FastMap() }[p.key] = modifierConfig
                        }
                    }
                }
            }
        }

        // compute `generatedModifiers` and `predefinedModifiers`
        run {
            currentCoroutineContext.ensureActive()
            initializer.modifiers.values
                .filter { it.template.expressionString.isNotEmpty() }
                .sortedByDescending { it.template.snippetExpressions.size } // put xxx_<xxx>_xxx before xxx_<xxx>
                .associateByTo(initializer.generatedModifiers) { it.name }
            initializer.modifiers.values
                .filter { it.template.expressionString.isEmpty() }
                .associateByTo(initializer.predefinedModifiers) { it.name }
        }

        // compute `swappedTypes` and add missing declarations with swapped type
        run {
            currentCoroutineContext.ensureActive()
            for (typeConfig in initializer.types.values) {
                if (typeConfig.baseType == null) continue
                val typeName = typeConfig.name
                initializer.swappedTypes[typeName] = typeConfig
                val baseTypeName = typeConfig.baseType!!.substringBefore('.')
                val baseDeclarationConfig = initializer.declarations[baseTypeName] ?: continue
                val rootKeysList = typeConfig.skipRootKey.filter { it.isNotEmpty() }.orNull() ?: continue
                val typeKey = typeConfig.typeKeyFilter?.takeWithOperator()?.singleOrNull() ?: continue
                val configPaths = rootKeysList.map { CwtConfigPath.resolve(it.drop(1) + typeKey) }
                val c0 = baseDeclarationConfig.configForDeclaration
                val c = configPaths.firstNotNullOfOrNull { c0.findPropertyByPath(it, ignoreCase = true) } ?: continue
                // read action is required here (for logging)
                val declarationConfig = readAction { CwtDeclarationConfig.resolve(c, name = typeName) } ?: continue
                initializer.declarations[typeName] = declarationConfig
            }
        }

        // add missing localisation links from links
        run {
            currentCoroutineContext.ensureActive()
            val localisationLinksStatic = initializer.localisationLinks.values.filter { it.dataSources.isEmpty() }
            if (localisationLinksStatic.isNotEmpty()) return@run
            val linksStatic = initializer.links.values.filter { it.dataSources.isEmpty() }
            for (linkConfig in linksStatic) {
                initializer.localisationLinks[linkConfig.name] = CwtLinkConfig.resolveForLocalisation(linkConfig)
            }
        }

        // bind `categoryConfigMap` for modifier configs
        run {
            currentCoroutineContext.ensureActive()
            for (modifier in initializer.modifiers.values) {
                for (category in modifier.categories) {
                    val categoryConfig = initializer.modifierCategories[category] ?: continue
                    modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
                }
            }
        }

        // compute `aliasKeysGroupConst` and `aliasKeysGroupNoConst`
        run {
            currentCoroutineContext.ensureActive()
            for ((k, v) in initializer.aliasGroups) {
                val keysConst = caseInsensitiveStringKeyMap<String>()
                val keysNoConst = FastSet<String>()
                for (key in v.keys) {
                    if (CwtDataExpression.resolve(key, true).type == CwtDataTypes.Constant) {
                        keysConst[key] = key
                    } else {
                        keysNoConst += key
                    }
                }
                if (keysConst.isNotEmpty()) {
                    initializer.aliasKeysGroupConst[k] = keysConst
                }
                if (keysNoConst.isNotEmpty()) {
                    val sorted = keysNoConst.sortedByPriority({ CwtDataExpression.resolve(it, true) }, { initializer })
                    val fastSet = FastSet<String>()
                    fastSet.addAll(sorted)
                    initializer.aliasKeysGroupNoConst[k] = fastSet
                }
            }
        }

        // compute `relatedLocalisationPatterns`
        run {
            currentCoroutineContext.ensureActive()
            with(initializer.relatedLocalisationPatterns) {
                val r = mutableSetOf<String>()
                initializer.types.values.forEach { c ->
                    c.localisation?.locationConfigs?.forEach { (_, lc) -> r += lc.value }
                }
                r.forEach { s ->
                    val i = s.indexOf('$')
                    if (i == -1) return@forEach
                    this += tupleOf(s.substring(0, i), s.substring(i + 1))
                }
                this.sortedWith(compareBy({ it.first }, { it.second }))
            }
        }

        // compute `linksModel`
        run {
            currentCoroutineContext.ensureActive()
            with(initializer.linksModel) {
                val staticLinks = initializer.links.values.filter { it.isStatic }
                staticLinks.forEach { c ->
                    if (c.forScope()) forScopeStatic += c
                    if (c.forValue()) forValueStatic += c
                }
                val dynamicLinksSorted = initializer.links.values.filter { !it.isStatic }.sortedByPriority({ it.configExpression }, { initializer })
                dynamicLinksSorted.forEach { c ->
                    if (c.forScope()) {
                        if (c.fromArgument && c.prefix != null) forScopeFromArgumentSorted += c
                        if (c.fromData && c.prefix != null) forScopeFromDataSorted += c
                        if (c.fromData && c.prefix == null) forScopeFromDataNoPrefixSorted += c
                    }
                    if (c.forValue()) {
                        if (c.name == "variable") variable += c
                        if (c.fromArgument && c.prefix != null) forValueFromArgumentSorted += c
                        if (c.fromData && c.prefix != null) forValueFromDataSorted += c
                        if (c.fromData && c.prefix == null) forValueFromDataNoPrefixSorted += c
                    }
                }
            }
        }

        // compute `localisationLinksModel`
        run {
            currentCoroutineContext.ensureActive()
            with(initializer.localisationLinksModel) {
                val staticLinks = initializer.localisationLinks.values.filter { it.isStatic }
                staticLinks.forEach { c ->
                    if (c.forScope()) forScopeStatic += c
                    if (c.forValue()) forValueStatic += c
                }
                val dynamicLinksSorted = initializer.localisationLinks.values.filter { !it.isStatic }.sortedByPriority({ it.configExpression }, { initializer })
                dynamicLinksSorted.forEach { c ->
                    if (c.forScope()) {
                        if (c.fromArgument && c.prefix != null) forScopeFromArgumentSorted += c
                        if (c.fromData && c.prefix != null) forScopeFromDataSorted += c
                        if (c.fromData && c.prefix == null) forScopeFromDataNoPrefixSorted += c
                    }
                    if (c.forValue()) {
                        if (c.fromArgument && c.prefix != null) forValueFromArgumentSorted += c
                        if (c.fromData && c.prefix != null) forValueFromDataSorted += c
                        if (c.fromData && c.prefix == null) forValueFromDataNoPrefixSorted += c
                    }
                }
            }
        }

        // compute `definitionTypesModel`
        run {
            currentCoroutineContext.ensureActive()
            with(initializer.definitionTypesModel) {
                with(supportParameters) {
                    for (parameterConfig in initializer.parameterConfigs) {
                        val propertyConfig = parameterConfig.parentConfig as? CwtPropertyConfig ?: continue
                        val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                        val contextExpression = if (aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression
                        else CwtDataExpression.resolve(aliasSubName, true)
                        if (contextExpression.type == CwtDataTypes.Definition) {
                            contextExpression.value?.let { this += it }
                        }
                    }
                }

                // 按文件路径计算，更准确地说，按规则的文件路径模式是否有交集来计算
                // based on file paths, in detail, based on file path patterns (has any same file path patterns)
                with(mayWithTypeKeyPrefix) {
                    val types = initializer.types.values.filter { c -> c.typeKeyPrefix != null }
                    val filePathPatterns = types.flatMapTo(mutableSetOf()) { c -> c.filePathPatterns }
                    val types1 = initializer.types.values.filter { c ->
                        val filePathPatterns1 = c.filePathPatterns
                        filePathPatterns1.isNotEmpty() && filePathPatterns1.any { it in filePathPatterns }
                    }
                    types1.forEach { c -> this += c.name }
                }
            }
        }
    }

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        // 2.0.7 nothing now (since it's not very necessary)
    }
}
