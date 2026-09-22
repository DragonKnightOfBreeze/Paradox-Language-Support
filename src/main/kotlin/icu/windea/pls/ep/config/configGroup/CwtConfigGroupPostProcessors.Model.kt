package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.isStatic
import icu.windea.pls.config.config.prefixFromArgument
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.filePathPatterns
import icu.windea.pls.config.model.CwtAliasModelBase
import icu.windea.pls.config.model.CwtConfigGroupDataModelBase
import icu.windea.pls.config.model.CwtLinkModelBase
import icu.windea.pls.config.model.CwtMacroModelBase
import icu.windea.pls.config.model.CwtScopeModelBase
import icu.windea.pls.config.model.CwtTypeModelBase
import icu.windea.pls.config.model.CwtUnionModelBase
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.CaseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.model.scope.ParadoxScope
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntArraySet
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlin.collections.iterator

class CwtModelConfigGroupPostProcessor: CwtConfigGroupPostProcessor {
    // NOTE 3.0.3 models should be also pre-computed by `CwtConfigGroupPostProcessor`, instead of `CwtConfigGroupProcessor`
    // otherwise, for example, `sortedByPriority` will not work correctly for data types that use dynamic priority (e.g., `CwtDataTypes.EnumValue`)

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        val dataModel = configGroup.dataModel
        if (dataModel !is CwtConfigGroupDataModelBase) return

        checkCanceled()
        computeTypeModel(configGroup, dataModel.typeModel)

        checkCanceled()
        computeScopeModel(configGroup, dataModel.scopeModel)

        checkCanceled()
        computeLinkModel(configGroup, dataModel.linkModel, dataModel.links.values)

        checkCanceled()
        computeLinkModel(configGroup, dataModel.localisationLinkModel, dataModel.localisationLinks.values)

        checkCanceled()
        computeUnionModel(configGroup, dataModel.unionModel)

        checkCanceled()
        computeAliasModel(configGroup, dataModel.aliasModel)

        checkCanceled()
        computeMacroModel(configGroup, dataModel.macroModel)
    }

    private fun computeTypeModel(configGroup: CwtConfigGroup, typeModel: CwtTypeModelBase) {
        with(typeModel) {
            configGroup.types.values.forEach { c ->
                if (c.baseType.isNotNullOrEmpty()) {
                    base2Swapped[c.baseType] = c.name
                    swapped2Base[c.name] = c.baseType
                }
            }

            configGroup.attributes.parameterConfigs.forEach { c ->
                val propertyConfig = c.parentConfig as? CwtPropertyConfig ?: return@forEach
                val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                val contextExpression = if (aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression
                else CwtDataExpression.resolve(aliasSubName, CwtDataExpressionRole.Key)
                if (contextExpression.type == CwtDataTypes.Definition) {
                    contextExpression.metadata.value?.let { supportParameters += it }
                }
            }

            // based on file paths, in detail, based on file path patterns (has any same file path patterns)
            val types = configGroup.types.values.filter { c -> c.typeKeyPrefix != null && !c.typePerFile }
            val filePathPatterns = types.flatMapTo(mutableSetOf()) { c -> c.filePathPatterns }
            configGroup.types.values.forEach { c ->
                if (c.filePathPatterns.any { it in filePathPatterns }) {
                    typeKeyPrefixAware += c.name
                }
            }

            // from localisation icons
            localisationIconResolvable += ParadoxLocalisationIconService.getDefinitionTypes(configGroup.gameType)
        }
    }

    private fun computeScopeModel(configGroup: CwtConfigGroup, scopeModel: CwtScopeModelBase) {
        with(scopeModel) {
            // process scope configs
            configGroup.scopes.values.forEach { c ->
                val scopeId = ParadoxScope.getId(c.name)
                val scopeIndex = ParadoxScope.resolve(scopeId).index
                val aliasesResult = IntArrayList()
                val parentsResult = IntArrayList()
                computeScopeModelForAliases(c, aliasesResult)
                computeScopeModelForParents(c, parentsResult)
                if (aliasesResult.isNotEmpty()) {
                    val target = base2Aliases
                    target.getOrPut(scopeIndex) { IntArraySet() }.addAll(aliasesResult)
                    aliasesResult.forEachFast { scopeIndex -> target.getOrPut(scopeIndex) { IntArraySet() }.addAll(aliasesResult) }
                }
                if (parentsResult.isNotEmpty()) {
                    val target = base2ParentScopes
                    target.getOrPut(scopeIndex) { IntArraySet() }.addAll(parentsResult)
                    aliasesResult.forEachFast { scopeIndex -> target.getOrPut(scopeIndex) { IntArraySet() }.addAll(parentsResult) }
                }
            }
            // process parent scopes (add from aliases)
            for (value in base2ParentScopes.values) {
                val aliasesResult = IntArrayList()
                value.forEach { scopeIndex -> base2Aliases[scopeIndex]?.let { aliasesResult.addAll(it) } }
                value.addAll(aliasesResult)
            }
            // process child scopes (add from parent scopes)
            for ((key, value) in base2ParentScopes) {
                value.forEach { scopeIndex -> base2ChildScopes.getOrPut(scopeIndex) { IntArraySet() }.add(key) }
            }
            // process matched scopes and promoted scopes (add from aliases, parent scopes, child scopes)
            for ((key, value) in base2Aliases) {
                base2MatchedScopes.getOrPut(key) { IntArraySet() }.addAll(value)
                base2PromotedScopes.getOrPut(key) { IntArraySet() }.addAll(value)
            }
            for ((key, value) in base2ParentScopes) {
                base2MatchedScopes.getOrPut(key) { IntArraySet() }.addAll(value)
                base2PromotedScopes.getOrPut(key) { IntArraySet() }.addAll(value)
            }
            for ((key, value) in base2ChildScopes) {
                base2MatchedScopes.getOrPut(key) { IntArraySet() }.addAll(value)
            }
        }
    }

    private fun computeScopeModelForAliases(config: CwtScopeConfig, result: IntArrayList) {
        config.aliases.forEach { alias ->
            val aliasIndex = ParadoxScope.resolve(alias).index
            result.add(aliasIndex)
        }
    }

    private fun computeScopeModelForParents(config: CwtScopeConfig, result: IntArrayList) {
        // 3.0.2 collect recursively (cyclic parents are guarded here)
        var parent = config.isSubscopeOf ?: return
        val guardStack = mutableSetOf<String>()
        while (true) {
            if (!guardStack.add(parent)) break
            val parentIndex = ParadoxScope.resolve(parent).index
            result.add(parentIndex)
            val parentConfig = config.configGroup.scopes[parent] ?: break
            parent = parentConfig.isSubscopeOf ?: break
        }
    }

    private fun computeLinkModel(configGroup: CwtConfigGroup, linksModel: CwtLinkModelBase, links: Collection<CwtLinkConfig>) {
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
            val dynamicLinks = links.filter { !it.isStatic }
            val dynamicLinksSorted = dynamicLinks.sortedByPriority({ it.configExpression }, { configGroup }) // 按优先级排序
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

    private fun computeUnionModel(configGroup: CwtConfigGroup, unionModel: CwtUnionModelBase) {
        with(unionModel) {
            for ((unionName, union) in configGroup.unions) {
                val const = CaseInsensitiveStringKeyMap<CwtValueConfig>()
                val nonConst = ObjectArrayList<CwtValueConfig>()

                for (valueConfig in union.valueConfigs) {
                    val configExpression = valueConfig.configExpression
                    if (configExpression.type == CwtDataTypes.Constant) {
                        const[configExpression.expressionString] = valueConfig
                    } else {
                        nonConst += valueConfig
                    }
                }
                if (const.isNotEmpty()) {
                    forConst[unionName] = const
                }
                if (nonConst.isNotEmpty()) {
                    val nonConstKeysSorted = nonConst.sortedByPriority({ it.configExpression }, { configGroup })
                    forNonConstSorted[unionName] = ObjectArrayList(nonConstKeysSorted)
                }
            }
        }
    }

    private fun computeAliasModel(configGroup: CwtConfigGroup, aliasModel: CwtAliasModelBase) {
        with(aliasModel) {
            for ((aliasName, aliases) in configGroup.aliasGroups) {
                val const = CaseInsensitiveStringKeyMap<String>()
                val nonConst = ObjectLinkedOpenHashSet<String>()
                for (key in aliases.keys) {
                    val configExpression = CwtDataExpression.resolve(key, CwtDataExpressionRole.Key)
                    if (configExpression.type == CwtDataTypes.Constant) {
                        const[key] = key
                    } else {
                        nonConst += key
                    }
                }
                if (const.isNotEmpty()) {
                    forConst[aliasName] = const
                }
                if (nonConst.isNotEmpty()) {
                    val nonConstSorted = nonConst.sortedByPriority({ CwtDataExpression.resolve(it, CwtDataExpressionRole.Key) }, { configGroup })
                    forNonConstSorted[aliasName] = ObjectLinkedOpenHashSet(nonConstSorted)
                }
            }
        }
    }

    private fun computeMacroModel(configGroup: CwtConfigGroup, macroModel: CwtMacroModelBase) {
        with(macroModel) {
            configGroup.macros.forEach { c ->
                when (c) {
                    is CwtMacroConfig.InlineScript -> {
                        forInlineScripts += c
                    }
                    is CwtMacroConfig.DefinitionInjection -> {
                        forDefinitionInjections = c
                    }
                }
            }
        }
    }
}
