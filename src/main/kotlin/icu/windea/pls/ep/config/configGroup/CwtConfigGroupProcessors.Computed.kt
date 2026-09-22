package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.select.selectConfigScope
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.model.paths.CwtConfigPath
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap

/**
 * 用于初始化规则分组中需要经过计算的那些数据。
 */
class CwtComputedConfigGroupProcessor : CwtConfigGroupProcessor {
    private val logger = thisLogger()

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
        computeRelatedLocalisationPatterns(configGroup)
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
                    val typeExpression = snippetExpression.metadata.value ?: continue
                    initializer.type2ModifiersMap.getOrPut(typeExpression) { Object2ObjectLinkedOpenHashMap() }[name] = modifierConfig
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
                            initializer.type2ModifiersMap.getOrPut(typeExpression) { Object2ObjectLinkedOpenHashMap() }[pp.key] = modifierConfig
                        }
                    } else {
                        val typeExpression = name
                        val modifierConfig = CwtModifierConfig.resolveFromDefinitionModifier(p, p.key, typeExpression) ?: continue
                        initializer.modifiers[modifierConfig.name] = modifierConfig
                        initializer.type2ModifiersMap.getOrPut(typeExpression) { Object2ObjectLinkedOpenHashMap() }[p.key] = modifierConfig
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
            logger.info("Computed missing declarations for swapped type '$typeName' from base type '$baseTypeName'.")
        }
    }

    private fun addMissingLocalisationLinksFromLinks(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        val localisationLinksStatic = initializer.localisationLinks.values.filter { it.dataSources.isEmpty() }
        if (localisationLinksStatic.isNotEmpty()) return
        val linksStatic = initializer.links.values.filter { it.dataSources.isEmpty() }
        for (linkConfig in linksStatic) {
            val linkName = linkConfig.name
            initializer.localisationLinks[linkName] = CwtLinkConfig.resolveForLocalisation(linkConfig)
            logger.info("Added missing localisation link '$linkName' from normal links.")
        }
    }

    private fun bindCategoryConfigMapForModifierConfigs(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        for (modifier in initializer.modifiers.values) {
            for (category in modifier.categories) {
                val categoryConfig = initializer.modifierCategories[category] ?: continue
                modifier.bindCategoryConfig(categoryConfig.name, categoryConfig)
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
}
