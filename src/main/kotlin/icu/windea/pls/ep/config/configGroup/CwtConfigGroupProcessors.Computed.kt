package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.isStatic
import icu.windea.pls.config.config.prefixFromArgument
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtLinksModelBase
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.config.select.selectConfigScope
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.model.paths.CwtConfigPath
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

/**
 * 用于初始化规则分组中需要经过计算的那些数据。
 */
class CwtComputedConfigGroupProcessor : CwtConfigGroupProcessor {
    override suspend fun process(configGroup: CwtConfigGroup) {
        checkCanceled()
        computeLocales(configGroup)

        checkCanceled()
        addModifiersFromTypes(configGroup)

        checkCanceled()
        computeModifiers(configGroup)

        checkCanceled()
        computeSwappedTypesAndAddMissingDeclarations(configGroup)

        checkCanceled()
        addMissingLocalisationLinksFromLinks(configGroup)

        checkCanceled()
        bindCategoryConfigMapForModifierConfigs(configGroup)

        checkCanceled()
        computeAliasKeysGroups(configGroup)

        checkCanceled()
        computeRelatedLocalisationPatterns(configGroup)

        checkCanceled()
        computeLinksModel(configGroup, configGroup.initializer.linksModel, configGroup.initializer.links.values)

        checkCanceled()
        computeLinksModel(configGroup, configGroup.initializer.localisationLinksModel, configGroup.initializer.localisationLinks.values)

        checkCanceled()
        computeMacrosModel(configGroup)

        checkCanceled()
        computeTypesModel(configGroup)
    }

    private fun computeLocales(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        for (localeConfig in initializer.locales.values) {
            initializer.globalLocales += localeConfig
            if (localeConfig.supports) initializer.supportedLocales += localeConfig
        }
    }

    private fun addModifiersFromTypes(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        // compute `type2ModifiersMap`
        for ((name, modifierConfig) in initializer.modifiers) {
            for (snippetExpression in modifierConfig.template.snippetExpressions) {
                if (snippetExpression.type == CwtDataTypes.Definition) {
                    val typeExpression = snippetExpression.value ?: continue
                    initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { Object2ObjectLinkedOpenHashMap() }[name] = modifierConfig
                }
            }
        }
        // merge all properties named `modifiers`
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
                            initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { Object2ObjectLinkedOpenHashMap() }[pp.key] = modifierConfig
                        }
                    } else {
                        val typeExpression = name
                        val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(p, p.key, typeExpression) ?: continue
                        initializer.modifiers[modifierConfig.name] = modifierConfig
                        initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { Object2ObjectLinkedOpenHashMap() }[p.key] = modifierConfig
                    }
                }
            }
        }
    }

    private fun computeModifiers(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        initializer.modifiers.values
            .filter { it.template.expressionString.isEmpty() }
            .associateByTo(initializer.predefinedModifiers) { it.name }
        initializer.modifiers.values
            .filter { it.template.expressionString.isNotEmpty() }
            .sortedByDescending { it.template.snippetExpressions.size } // put xxx_<xxx>_xxx before xxx_<xxx>
            .associateByTo(initializer.generatedModifiers) { it.name }
    }

    private suspend fun computeSwappedTypesAndAddMissingDeclarations(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        for (typeConfig in initializer.types.values) {
            if (typeConfig.baseType.isNullOrEmpty()) continue
            val typeName = typeConfig.name
            initializer.swappedTypes[typeName] = typeConfig
            val baseTypeName = typeConfig.baseType!!.substringBefore('.')
            val baseDeclarationConfig = initializer.declarations[baseTypeName] ?: continue
            val rootKeysList = typeConfig.skipRootKey.filter { it.isNotEmpty() }.orNull() ?: continue
            val typeKey = typeConfig.typeKeyFilter?.takeWithOperator()?.singleOrNull() ?: continue
            // NOTE 2.2.0 it's necessary to convert `any` to `*` in root keys before query by path
            val rawPaths = rootKeysList.map { rootKeys -> rootKeys.drop(1).map { if (it == "any") "*" else it } + typeKey }
            val paths = rawPaths.map { CwtConfigPath.resolve(it).path }
            val rootConfig = baseDeclarationConfig.rootConfig
            val config = selectConfigScope { rootConfig.queryBy(paths).asProperty().one() } ?: continue
            // read action is required here (for logging)
            val declarationConfig = readAction { CwtDeclarationConfig.resolve(config, name = typeName) } ?: continue
            initializer.declarations[typeName] = declarationConfig
        }
    }

    private fun addMissingLocalisationLinksFromLinks(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        val localisationLinksStatic = initializer.localisationLinks.values.filter { it.dataSources.isEmpty() }
        if (localisationLinksStatic.isNotEmpty()) return
        val linksStatic = initializer.links.values.filter { it.dataSources.isEmpty() }
        for (linkConfig in linksStatic) {
            initializer.localisationLinks[linkConfig.name] = CwtLinkConfig.resolveForLocalisation(linkConfig)
        }
    }

    private fun bindCategoryConfigMapForModifierConfigs(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        for (modifier in initializer.modifiers.values) {
            for (category in modifier.categories) {
                val categoryConfig = initializer.modifierCategories[category] ?: continue
                modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
            }
        }
    }

    private fun computeAliasKeysGroups(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        for ((k, v) in initializer.aliasGroups) {
            val keysConst = caseInsensitiveStringKeyMap<String>()
            val keysNoConst = ObjectLinkedOpenHashSet<String>()
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
                val sorted = keysNoConst.sortedByPriority({ CwtDataExpression.resolve(it, true) }, { configGroup })
                val fastSet = ObjectLinkedOpenHashSet<String>()
                fastSet.addAll(sorted)
                initializer.aliasKeysGroupNoConst[k] = fastSet
            }
        }
    }

    private fun computeRelatedLocalisationPatterns(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        with(initializer.relatedLocalisationPatterns) {
            val r = mutableSetOf<String>()
            initializer.types.values.forEach { c ->
                c.localisation?.locationConfigGroup?.values?.forEach { lcs ->
                    lcs.forEach { lc ->
                        r += lc.value
                    }
                }
            }
            r.forEach { s ->
                val i = s.indexOf('$')
                if (i == -1) return@forEach
                this += tupleOf(s.substring(0, i), s.substring(i + 1))
            }
            this.sortedWith(compareBy({ it.first }, { it.second }))
        }
    }

    private fun computeLinksModel(configGroup: CwtConfigGroup, linksModel: CwtLinksModelBase, links: Collection<CwtLinkConfig>) {
        with(linksModel) {
            val staticLinks = links.filter { it.isStatic }
            staticLinks.forEach { c ->
                if (c.type.forScope()) {
                    forScopeStatic += c
                }
                if (c.type.forValue()) {
                    forValueStatic += c
                }
            }
            val dynamicLinksSorted = links.filter { !it.isStatic }.sortedByPriority({ it.configExpression }, { configGroup })
            dynamicLinksSorted.forEach { c ->
                if (c.type.forScope()) {
                    if (c.prefix == null) {
                        if (c.fromData) {
                            forScopeNoPrefixSorted += c
                        }
                    } else {
                        if (c.fromData) {
                            forScopeFromDataSorted += c
                        }
                        if (c.fromArgument) {
                            forScopeFromArgumentSorted += c
                            forScopeFromArgumentSortedByPrefix.getOrPut(c.prefixFromArgument) { ObjectArrayList() } += c
                        }
                    }
                }
                if (c.type.forValue()) {
                    if (c.prefix == null) {
                        if (c.fromData) {
                            forValueNoPrefixSorted += c
                        }
                    } else {
                        if (c.fromData) {
                            forValueFromDataSorted += c
                        }
                        if (c.fromArgument) {
                            forValueFromArgumentSorted += c
                            forValueFromArgumentSortedByPrefix.getOrPut(c.prefixFromArgument) { ObjectArrayList() } += c
                        }
                    }
                }
            }
        }
    }

    private fun computeMacrosModel(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        with(initializer.macrosModel) {
            initializer.macros.forEach { c ->
                when (c) {
                    is CwtMacroConfig.InlineScript -> forInlineScripts += c
                    is CwtMacroConfig.DefinitionInjection -> forDefinitionInjections = c
                }
            }
        }
    }

    private fun computeTypesModel(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        with(initializer.typesModel) {
            initializer.types.values.forEach { c ->
                if (c.baseType.isNotNullOrEmpty()) {
                    base2Swapped[c.baseType] = c.name
                    swapped2Base[c.name] = c.baseType
                }
            }

            initializer.parameterConfigs.forEach { c ->
                val propertyConfig = c.parentConfig as? CwtPropertyConfig ?: return@forEach
                val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                val contextExpression = if (aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression
                else CwtDataExpression.resolve(aliasSubName, true)
                if (contextExpression.type == CwtDataTypes.Definition) {
                    contextExpression.value?.let { supportParameters += it }
                }
            }

            // based on file paths, in detail, based on file path patterns (has any same file path patterns)
            val types = initializer.types.values.filter { c -> c.typeKeyPrefix != null && !c.typePerFile }
            val filePathPatterns = types.flatMapTo(mutableSetOf()) { c -> c.filePathPatterns }
            initializer.types.values.forEach { c ->
                if (c.filePathPatterns.any { it in filePathPatterns }) {
                    typeKeyPrefixAware += c.name
                }
            }
        }
    }
}
