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
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer
import icu.windea.pls.config.configGroup.CwtLinksModelBase
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.config.select.selectConfigScope
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.FastList
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.model.paths.CwtConfigPath

/**
 * 用于初始化规则分组中需要经过计算的那些数据。
 */
class CwtComputedConfigGroupProcessor : CwtConfigGroupProcessor {
    override suspend fun process(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer

        checkCanceled()
        computeLocales(initializer)

        checkCanceled()
        addModifiersFromTypes(initializer)

        checkCanceled()
        computeModifiers(initializer)

        checkCanceled()
        computeSwappedTypesAndAddMissingDeclarations(initializer)

        checkCanceled()
        addMissingLocalisationLinksFromLinks(initializer)

        checkCanceled()
        bindCategoryConfigMapForModifierConfigs(initializer)

        checkCanceled()
        computeAliasKeysGroups(initializer)

        checkCanceled()
        computeRelatedLocalisationPatterns(initializer)

        checkCanceled()
        computeLinksModel(initializer, initializer.linksModel, initializer.links.values)

        checkCanceled()
        computeLinksModel(initializer, initializer.localisationLinksModel, initializer.localisationLinks.values)

        checkCanceled()
        computeMacrosModel(initializer)

        checkCanceled()
        computeDefinitionTypeModel(initializer)
    }

    private fun computeLocales(initializer: CwtConfigGroupInitializer) {
        for (localeConfig in initializer.locales.values) {
            initializer.globalLocales += localeConfig
            if (localeConfig.supports) initializer.supportedLocales += localeConfig
        }
    }

    private fun addModifiersFromTypes(initializer: CwtConfigGroupInitializer) {
        // compute `type2ModifiersMap`
        for ((name, modifierConfig) in initializer.modifiers) {
            for (snippetExpression in modifierConfig.template.snippetExpressions) {
                if (snippetExpression.type == CwtDataTypes.Definition) {
                    val typeExpression = snippetExpression.value ?: continue
                    initializer.type2ModifiersMap.computeIfAbsent(typeExpression) { FastMap() }[name] = modifierConfig
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

    private fun computeModifiers(initializer: CwtConfigGroupInitializer) {
        initializer.modifiers.values
            .filter { it.template.expressionString.isEmpty() }
            .associateByTo(initializer.predefinedModifiers) { it.name }
        initializer.modifiers.values
            .filter { it.template.expressionString.isNotEmpty() }
            .sortedByDescending { it.template.snippetExpressions.size } // put xxx_<xxx>_xxx before xxx_<xxx>
            .associateByTo(initializer.generatedModifiers) { it.name }
    }

    private suspend fun computeSwappedTypesAndAddMissingDeclarations(initializer: CwtConfigGroupInitializer) {
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

    private fun addMissingLocalisationLinksFromLinks(initializer: CwtConfigGroupInitializer) {
        val localisationLinksStatic = initializer.localisationLinks.values.filter { it.dataSources.isEmpty() }
        if (localisationLinksStatic.isNotEmpty()) return
        val linksStatic = initializer.links.values.filter { it.dataSources.isEmpty() }
        for (linkConfig in linksStatic) {
            initializer.localisationLinks[linkConfig.name] = CwtLinkConfig.resolveForLocalisation(linkConfig)
        }
    }

    private fun bindCategoryConfigMapForModifierConfigs(initializer: CwtConfigGroupInitializer) {
        for (modifier in initializer.modifiers.values) {
            for (category in modifier.categories) {
                val categoryConfig = initializer.modifierCategories[category] ?: continue
                modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
            }
        }
    }

    private fun computeAliasKeysGroups(initializer: CwtConfigGroupInitializer) {
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

    private fun computeRelatedLocalisationPatterns(initializer: CwtConfigGroupInitializer) {
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

    private fun computeLinksModel(initializer: CwtConfigGroupInitializer, linksModel: CwtLinksModelBase, links: Collection<CwtLinkConfig>) {
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
            val dynamicLinksSorted = links.filter { !it.isStatic }.sortedByPriority({ it.configExpression }, { initializer })
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
                            forScopeFromArgumentSortedByPrefix.getOrPut(c.prefixFromArgument) { FastList() } += c
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
                            forValueFromArgumentSortedByPrefix.getOrPut(c.prefixFromArgument) { FastList() } += c
                        }
                    }
                }
            }
        }
    }

    private fun computeMacrosModel(initializer: CwtConfigGroupInitializer) {
        with(initializer.macrosModel) {
            initializer.macros.forEach { c ->
                when (c) {
                    is CwtMacroConfig.InlineScript -> forInlineScripts += c
                    is CwtMacroConfig.DefinitionInjection -> forDefinitionInjections = c
                }
            }
        }
    }

    private fun computeDefinitionTypeModel(initializer: CwtConfigGroupInitializer) {
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
