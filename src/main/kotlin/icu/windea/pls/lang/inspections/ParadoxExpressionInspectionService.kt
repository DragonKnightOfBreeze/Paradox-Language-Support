package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtRowType
import icu.windea.pls.config.config.containingDirectConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.forEachIndexedFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.core.match.similarity.SimilarityMatchOptions
import icu.windea.pls.core.match.similarity.SimilarityMatchService
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.truncate
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextService
import icu.windea.pls.lang.fixes.GenerateLocalisationsFix
import icu.windea.pls.lang.fixes.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.fixes.ReplaceWithExpressionFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionFix
import icu.windea.pls.lang.fixes.ReplaceWithSimilarExpressionInListFix
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.tagType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDataExpression
import icu.windea.pls.script.psi.propertyKey

object ParadoxExpressionInspectionService {
    // region Common Methods

    fun getDefaultLocationForContainer(element: ParadoxScriptMember): PsiElement? {
        return when (element) {
            is ParadoxScriptFile -> element
            is ParadoxScriptProperty -> element.propertyKey
            is ParadoxScriptValue -> {
                element.propertyKey?.let { return it } // `k` for `k = v`
                if(element is PsiBoundElement) return element.leftBound // `{` for `{...}`
                element
            }
            else -> element
        }
    }

    fun getSimilarityBasedFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
        val literals = CwtConfigManager.findLiterals(configs)
        if (literals.isEmpty()) return emptyList()

        val input = element.value
        if (input.isEmpty()) return emptyList()
        val ignoreCase = when (element) {
            is ParadoxScriptStringExpressionElement -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
        val options = if (ignoreCase) SimilarityMatchOptions.IGNORE_CASE else SimilarityMatchOptions.DEFAULT

        // 查询输入项的最佳匹配，但排除完全匹配的相似项
        val matches = SimilarityMatchService.findBestMatches(input, literals, options).filter { it.score < 1.0 }
        if (matches.isEmpty()) return emptyList()

        // 为最匹配的项提供单独的快速修复（直接替换）
        // 如果匹配项不唯一，再为所有匹配项提供一个快速修复（弹出列表） - 如果分别提供快速修复，这些快速修复最终会按名字正序排序（这不符合预期）
        val fixes = mutableListOf<LocalQuickFix>()
        val first = matches.first()
        fixes += ReplaceWithSimilarExpressionFix(element, first)
        val remain = matches.drop(1)
        if (remain.isNotEmpty()) {
            fixes += ReplaceWithSimilarExpressionInListFix(element, matches)
        }

        return fixes
    }

    fun getLocalisationReferenceFixes(element: ParadoxExpressionElement, configs: List<CwtMemberConfig<*>>): List<LocalQuickFix> {
        if (configs.isEmpty()) return emptyList()
        if (element !is ParadoxScriptStringExpressionElement) return emptyList()
        val context = configs.firstNotNullOfOrNull {
            ParadoxLocalisationCodeInsightContextService.fromReference(element, it, fromInspection = true)
        }
        if (context == null) return emptyList()
        return listOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }

    // endregion

    // region MissingExpressionInspection

    fun checkForMissingExpression(file: ParadoxScriptFile, context: ParadoxExpressionInspectionContext) {
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return
        if (configContext.skipMissingExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(file, ParadoxMatchOptions(forDeclarationRoot = true))
        checkForMissingExpression(file, configs, context)
    }

    fun checkForMissingExpression(element: ParadoxScriptBlock, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // skip if containing property key is parameterized
        val propertyKey = element.propertyKey
        if (propertyKey != null && propertyKey.text.isParameterized()) return

        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return
        if (configContext.skipMissingExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        checkForMissingExpression(element, configs, context)
    }

    private fun checkForMissingExpression(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext) {
        if (skipForMissingExpression(element, configs)) return
        val occurrences = ParadoxConfigManager.getChildOccurrences(element, configs)
        if (occurrences.isEmpty()) return
        val overriddenProvider = ParadoxConfigManager.getOverriddenProvider(configs)
        occurrences.forEach { (configExpression, occurrence) ->
            if (overriddenProvider != null && overriddenProvider.skipMissingExpressionCheck(configs, configExpression)) return@forEach
            val r = checkMinOccurrence(element, occurrence, configExpression, context)
            if (!r) return
        }
    }

    private fun skipForMissingExpression(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
        // 子句不为空且可以精确匹配多个子句规则时，不适用此检查
        return when {
            configs.isEmpty() -> true
            configs.size == 1 -> false
            element is ParadoxScriptFile && element.members().none() -> false
            element is ParadoxScriptBlock && element.members().none() -> false
            else -> true
        }
    }

    private fun checkMinOccurrence(element: ParadoxScriptMember, occurrence: ParadoxMatchOccurrence, configExpression: CwtDataExpression, context: ParadoxExpressionInspectionContext): Boolean {
        val holder = context.holder
        val location = getDefaultLocationForContainer(element) ?: return true
        val (actual, min, _, lenientMin) = occurrence
        if (min != null && actual < min) {
            val expressionType = ChronicleBundle.expressionType(configExpression)
            val isConst = configExpression.type == CwtDataTypes.Constant
            val shortDescription = when {
                isConst -> ChronicleInspectionBundle.message("inspection.missingExpression.desc.1", expressionType, configExpression)
                else -> ChronicleInspectionBundle.message("inspection.missingExpression.desc.2", expressionType, configExpression)
            }
            val description = when {
                context.showExpect -> {
                    val minDefine = occurrence.minDefine
                    val details = when {
                        minDefine == null -> ChronicleInspectionBundle.message("inspection.missingExpression.details.1", min, actual)
                        else -> ChronicleInspectionBundle.message("inspection.missingExpression.details.2", min, actual, minDefine)
                    }
                    ChronicleBundle.inspectionDescription(shortDescription, details)
                }
                else -> shortDescription
            }
            val highlightType = InspectionService.getWeakerHighlightType(context.tool, lenientMin)
            val fileLevel = element is PsiFile
            if (!fileLevel && context.firstOnly && holder.hasResults()) return false
            if (fileLevel && context.firstOnlyOnFile && holder.hasResults()) return false
            holder.registerProblem(location, description, highlightType)
        }
        return true
    }

    // endregion

    // region TooManyExpressionInspection

    fun checkForTooManyExpression(file: ParadoxScriptFile, context: ParadoxExpressionInspectionContext) {
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return
        if (configContext.skipTooManyExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(file, ParadoxMatchOptions(forDeclarationRoot = true))
        checkForTooManyExpression(file, configs, context)
    }

    fun checkForTooManyExpression(element: ParadoxScriptBlock, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // skip if containing property key is parameterized
        val propertyKey = element.propertyKey
        if (propertyKey != null && propertyKey.text.isParameterized()) return

        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return
        if (configContext.skipTooManyExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        checkForTooManyExpression(element, configs, context)
    }

    private fun checkForTooManyExpression(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext) {
        if (skipForTooManyExpression(element, configs)) return
        val occurrences = ParadoxConfigManager.getChildOccurrences(element, configs)
        if (occurrences.isEmpty()) return
        val overriddenProvider = ParadoxConfigManager.getOverriddenProvider(configs)
        occurrences.forEach { (configExpression, occurrence) ->
            if (overriddenProvider != null && overriddenProvider.skipTooManyExpressionCheck(configs, configExpression)) return@forEach
            val r = checkMaxOccurrence(element, occurrence, configExpression, context)
            if (!r) return
        }
    }

    private fun skipForTooManyExpression(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
        // 子句不为空且可以精确匹配多个子句规则时，不适用此检查
        return when {
            configs.isEmpty() -> true
            configs.size == 1 -> false
            element is ParadoxScriptMemberContainer && element.members().none() -> false
            else -> true
        }
    }

    private fun checkMaxOccurrence(element: ParadoxScriptMember, occurrence: ParadoxMatchOccurrence, configExpression: CwtDataExpression, context: ParadoxExpressionInspectionContext): Boolean {
        val holder = context.holder
        val location = getDefaultLocationForContainer(element) ?: return true
        val (actual, _, max, _, lenientMax) = occurrence
        if (max != null && actual > max) {
            val expressionType = ChronicleBundle.expressionType(configExpression)
            val isConst = configExpression.type == CwtDataTypes.Constant
            val shortDescription = when {
                isConst -> ChronicleInspectionBundle.message("inspection.tooManyExpression.desc.1", expressionType, configExpression)
                else -> ChronicleInspectionBundle.message("inspection.tooManyExpression.desc.2", expressionType, configExpression)
            }
            val description = when {
                context.showExpect -> {
                    val maxDefine = occurrence.maxDefine
                    val details = when {
                        maxDefine == null -> ChronicleInspectionBundle.message("inspection.tooManyExpression.details.1", max, actual)
                        else -> ChronicleInspectionBundle.message("inspection.tooManyExpression.details.2", max, actual, maxDefine)
                    }
                    ChronicleBundle.inspectionDescription(shortDescription, details)
                }
                else -> shortDescription
            }
            val highlightType = InspectionService.getWeakerHighlightType(context.tool, lenientMax)
            val fileLevel = element is PsiFile
            if (!fileLevel && context.firstOnly && holder.hasResults()) return false
            if (fileLevel && context.firstOnlyOnFile && holder.hasResults()) return false
            holder.registerProblem(location, description, highlightType)
        }
        return true
    }

    // endregion

    // region UnresolvedExpressionInspection

    fun checkForUnresolvedExpression(element: ParadoxScriptExpressionElement, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // NOTE 3.0.2 do not skip by default (try to match with parameters if possible)
        //// skip if it is parameterized
        // if (element is ParadoxParameterAwareElement && element.text.isParameterized()) return

        // NOTE 3.0.2 not very necessary, but in case
        // skip if it is a special tag (Do not consider whether matched configs exist)
        if (element is ParadoxScriptString && element.tagType != null) return

        // 如果不存在规则上下文，则直接跳过
        // 如果存在规则上下文，但指定要跳过检查，则直接跳过
        // 如果存在匹配的规则，则直接跳过
        // 如果当前节点未通过检查，而父节点也未通过检查，也需要跳过，避免冗余的报错

        // skip if config context not exists
        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return
        // skip if config context should be skipped (mainly based on member path and member role)
        if (configContext.skipUnresolvedExpressionCheck()) return

        // skip if there are any matched configs (use fallback if is property key)
        val fallback = element is ParadoxScriptPropertyKey
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = fallback))
        if (configs.isNotEmpty()) return

        var parentConfigContext: CwtConfigContext? = null
        run {
            val parent = if (element is ParadoxScriptPropertyKey) element.parent?.parent else element.parent
            if (parent == null) return@run
            parentConfigContext = ParadoxConfigManager.getConfigContext(parent) ?: return@run
            if (parentConfigContext.skipUnresolvedExpressionCheck()) return@run
            val configs = ParadoxConfigManager.getConfigs(parent)
            if (configs.isNotEmpty()) return@run
            return // skip if the parent node also fails the check
        }

        val expectedConfigs = ParadoxConfigManager.getExpectedConfigs(element, configContext, parentConfigContext)
        if (skipForUnresolvedExpression(element, expectedConfigs, context)) return

        // 开始检查
        ParadoxInspectionService.applyUnresolvedExpressionCheckers(element, expectedConfigs, context)
    }

    fun checkForUnresolvedExpression(element: ParadoxCsvExpressionElement, context: ParadoxExpressionInspectionContext) {
        val rowConfig = context.rowConfig
        if (rowConfig == null) return
        if (element !is ParadoxCsvColumn) return
        if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header columns

        // 如果不存在对应的列规则，则直接跳过
        // 如果存在对应的列规则且匹配，则直接跳过
        // 按需忽略最后一行

        // skip if the column config can be matched
        val columnConfig = ParadoxConfigManager.getColumnConfig(element, context.rowConfig) ?: return // skip (checked by `IncorrectColumnSizeInspection`)
        if (ParadoxConfigManager.isMatchedColumnConfig(element, columnConfig)) return

        val expectedConfigs = ParadoxConfigManager.getExpectedConfigs(columnConfig)
        if (skipForUnresolvedExpression(element, expectedConfigs, context)) return

        // 开始检查
        ParadoxInspectionService.applyUnresolvedExpressionCheckers(element, expectedConfigs, context)
    }

    private fun skipForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): Boolean {
        if (expectedConfigs.isEmpty()) return false
        val isPathReference = ProcessorScope.allFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { it.type in CwtDataTypeSets.PathReference }
        if (isPathReference) return true // will be checked by `UnresolvedPathReferenceInspection` instead
        if (context.ignoredByConfigs && ParadoxConfigManager.checkExtendedConfig(element, expectedConfigs)) return true
        return false
    }

    fun getDefaultLocationForUnresolvedExpression(element: ParadoxExpressionElement): PsiElement {
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isEmptyColumn(element)) {
            return ParadoxCsvPsiService.getLocationForEmptyColumn(element) // in case
        }
        return element
    }

    fun getDefaultDescriptionForUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext): String {
        val expressionType = ChronicleBundle.expressionType(element)
        val text = element.presentableText
        val description = when {
            !context.showExpect -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.0", expressionType, text)
            expectedConfigs.isEmpty() -> ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.1", expressionType, text)
            else -> {
                val expectedConfigExpressions = expectedConfigs.mapFast { it.configExpression.expressionString }.toSet()
                val expectText = expectedConfigExpressions.truncate(context.truncateExpect).joinToString()
                ChronicleInspectionBundle.message("inspection.unresolvedExpression.desc.2", expressionType, text, expectText)
            }
        }
        return description
    }

    // endregion

    // region IncorrectExpressionInspection

    fun checkForIncorrectExpression(element: ParadoxScriptExpressionElement, context: ParadoxExpressionInspectionContext) {
        if (element is ParadoxScriptBlock) return // skip
        if (element is ParadoxScriptBoolean) return // skip

        // skip if is not a data expression
        if (!element.isDataExpression()) return

        // 得到完全匹配的规则
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return

        // 开始检查
        ParadoxInspectionService.applyIncorrectExpressionCheckers(element, config, context)

        // TODO 1.3.26+ 应当也适用于各种复杂表达式中的数据源
    }

    fun checkForIncorrectExpression(element: ParadoxCsvExpressionElement, context: ParadoxExpressionInspectionContext) {
        val rowConfig = context.rowConfig
        if (rowConfig == null) return
        if (element !is ParadoxCsvColumn) return
        if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header columns
        if (ParadoxCsvPsiService.isEmptyColumn(element)) return // skip empty columns

        // 得到完全匹配的规则
        val columnConfig = ParadoxConfigManager.getColumnConfig(element, rowConfig) ?: return // skip (checked by `IncorrectColumnSizeInspection`)
        if (!ParadoxConfigManager.isMatchedColumnConfig(element, columnConfig)) return // skip (checked by `UnresolvedExpressionInspection`)
        val config = columnConfig.valueConfig ?: return

        // 开始检查
        ParadoxInspectionService.applyIncorrectExpressionCheckers(element, config, context)
    }

    // endregion

    // region ConflictingExpressionInspection

    // NOTE 3.0.2 由于匹配逻辑和检查逻辑存在一些细节上的缺陷，改为默认禁用，避免误报和误导
    // TODO 3.0.2+ 考虑进一步完善相关的匹配逻辑和检查逻辑

    fun checkForConflictingExpression(element: ParadoxScriptBlock, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // skip if containing property key is parameterized
        val propertyKey = element.propertyKey
        if (propertyKey != null && propertyKey.text.isParameterized()) return

        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        if (skipForConflictingExpression(element, configs)) return
        reportForConflictingExpression(element, context)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun skipForConflictingExpression(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
        // 子句可以精确匹配多个子句规则时，适用此检查
        if (configs.isEmpty()) return true
        // 这里需要先按实际对应的规则位置去重
        if (configs.distinctBy { it.pointer }.size == 1) return true
        // 如果是重载后提供的规则，跳过此检查
        if (isOverriddenConfigsForConflictingExpression(configs)) return true
        // 如果存在规则，规则的子句中的所有 key 和 value 都可以分别被另一个规则的子句中的所有 key 和 value 包含，则仅使用这些规则
        val configsToCheck = filterConfigsForConflictingExpression(configs)
        if (configsToCheck.size == 1) return true
        return false
    }

    private fun isOverriddenConfigsForConflictingExpression(configs: List<CwtMemberConfig<*>>): Boolean {
        return configs.anyFast { it.containingDirectConfig.castOrNull<CwtPropertyConfig>()?.overriddenProvider != null }
    }

    private fun filterConfigsForConflictingExpression(configs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        val configsToCheck = configs.filterFast { config ->
            val childConfigs = config.configs
            childConfigs != null && configs.anyFast { config0 ->
                val childConfigs0 = config0.configs
                config0 != config && childConfigs0 != null && childConfigs0.containsAll(childConfigs)
            }
        }
        return configsToCheck.ifEmpty { configs }
    }

    private fun reportForConflictingExpression(element: ParadoxScriptMember, context: ParadoxExpressionInspectionContext) {
        // TODO 3.0.2
        val holder = context.holder
        val location = getDefaultLocationForContainer(element) ?: return
        val text = ""
        val isKey = location is ParadoxScriptPropertyKey
        val description = when {
            isKey -> ChronicleInspectionBundle.message("inspection.conflictingExpression.desc.1", text)
            else -> ChronicleInspectionBundle.message("inspection.conflictingExpression.desc.2", text)
        }
        holder.registerProblem(location, description)
    }

    // endregion

    // region UnresolvedPathReferenceInspection

    fun checkForUnresolvedPathReference(element: ParadoxScriptStringExpressionElement, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // skip if is parameterized
        if (element.text.isParameterized()) return

        // 得到匹配的第一个规则
        val valueConfig = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        val value = element.value
        if (skipForUnresolvedPathReference(element, value, valueConfig, context)) return
        val configExpression = valueConfig.configExpression
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            val virtualFile = value.toVirtualFile()
            if (virtualFile != null) return
            reportForUnresolvedPathReference(element, value, configExpression, context)
            return
        }
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression.type)
        if (pathReferenceExpressionSupport != null) {
            val pathReference = value.normalizePath()
            run {
                val fileNames = pathReferenceExpressionSupport.resolveFileName(configExpression, pathReference)
                if (fileNames.isNullOrEmpty()) return@run
                if (fileNames.any { fileName -> fileName.matchesPatterns(context.ignoredFileNames, ignoreCase = true) }) return // 忽略
            }
            val selector = ParadoxFilePathSearch.selector(context.holder.project, context.holder.file) // use file as context
            if (ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null) return
            reportForUnresolvedPathReference(element, value, configExpression, context)
        }
    }

    private fun skipForUnresolvedPathReference(element: ParadoxScriptStringExpressionElement, value: String, memberConfig: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        if (context.ignoredByConfigs && ParadoxConfigManager.checkExtendedConfig(value, element, memberConfig)) return true
        return false
    }

    private fun reportForUnresolvedPathReference(element: ParadoxScriptStringExpressionElement, value: String, configExpression: CwtDataExpression, context: ParadoxExpressionInspectionContext) {
        val holder = context.holder
        val shortDescription = when (configExpression.type) {
            CwtDataTypes.Icon -> ChronicleInspectionBundle.message("inspection.unresolvedPathReference.desc.icon", value)
            CwtDataTypes.FilePath -> ChronicleInspectionBundle.message("inspection.unresolvedPathReference.desc.filePath", value)
            CwtDataTypes.FileName -> ChronicleInspectionBundle.message("inspection.unresolvedPathReference.desc.fileName", value)
            CwtDataTypes.AbsoluteFilePath -> ChronicleInspectionBundle.message("inspection.unresolvedPathReference.desc.abs", value)
            else -> ChronicleInspectionBundle.message("inspection.unresolvedPathReference.desc", value)
        }
        val description = when {
            context.showExpect -> {
                val details = ChronicleInspectionBundle.message("inspection.unresolvedPathReference.details", configExpression)
                ChronicleBundle.inspectionDescription(shortDescription, details)
            }
            else -> shortDescription
        }
        holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }

    // endregion

    // region IncorrectPathReferenceInspection

    fun checkForIncorrectPathReference(element: ParadoxScriptStringExpressionElement, context: ParadoxExpressionInspectionContext) {
        // skip if is not a data expression
        if (!element.isDataExpression()) return
        // skip if is parameterized
        if (element.text.isParameterized()) return

        // 得到完全匹配的规则
        val config = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(fallback = false)).firstOrNull() ?: return
        val configExpression = config.configExpression
        val dataType = configExpression.type
        if (dataType !in CwtDataTypeSets.PathReference) return
        if (dataType == CwtDataTypes.Icon) return // no file extension in expression
        val expectedFileExtensions = config.optionMetadata.fileExtensions.orEmpty()
        if (expectedFileExtensions.isEmpty()) return
        val value = element.value
        val fileExtension = value.substringAfterLast('.', "")
        if (expectedFileExtensions.any { fileExtension.equals(it, true) }) return
        reportForIncorrectPathReference(element, value, expectedFileExtensions, context)
    }

    private fun reportForIncorrectPathReference(location: PsiElement, value: String, expectFileExtensions: Set<String>, context: ParadoxExpressionInspectionContext) {
        val holder = context.holder
        val expectText = expectFileExtensions.joinToString()
        val description = when {
            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectPathReference.desc.1", value, expectText)
            else -> ChronicleInspectionBundle.message("inspection.incorrectPathReference.desc.0", value)
        }
        holder.registerProblem(location, description)
    }

    // endregion

    // region IncorrectColumnNameInspection

    fun checkForIncorrectColumnName(element: ParadoxCsvHeader, context: ParadoxExpressionInspectionContext) {
        val holder = context.holder
        val rowConfig = context.rowConfig
        if (rowConfig == null) return
        when (rowConfig.type) {
            CwtRowType.Key -> {
                val allColumnNames = rowConfig.columns.map { it.key }
                if (allColumnNames.isEmpty()) return // skip (checked by `IncorrectColumnSizeInspection`)
                val existingColumnNames = ParadoxCsvPsiService.getColumnNames(element)
                val expectColumnNames = mutableSetOf<String>().apply { addAll(allColumnNames) }.apply { removeAll(existingColumnNames) }
                val expectText = expectColumnNames.truncate(context.truncateExpect).joinToString()
                element.columnList.forEachIndexedFast f@{ columnIndex, columnElement ->
                    if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) return@f // ignored
                    if (columnIndex >= rowConfig.columns.size) {
                        val description = when {
                            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.4", rowConfig.name)
                            else -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                        return // skip (no future checks)
                    }
                    if (columnElement.name in allColumnNames) return@f // continue (matched)
                    if (expectColumnNames.isNotEmpty()) {
                        val description = when {
                            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.1", rowConfig.name, expectText)
                            else -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.0")
                        }
                        val expectColumnNamePreferred = rowConfig.columns[columnIndex].key
                        if (expectColumnNamePreferred in expectColumnNames) {
                            val fix = ReplaceWithExpressionFix(expectColumnNamePreferred)
                            holder.registerProblem(columnElement, description, fix)
                        } else {
                            holder.registerProblem(columnElement, description)
                        }
                    } else {
                        val description = when {
                            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.3", rowConfig.name, expectText)
                            else -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                    }
                }
            }
            CwtRowType.Index -> {
                element.columnList.forEachIndexedFast f@{ columnIndex, columnElement ->
                    if (rowConfig.skipLastColumn && columnIndex == rowConfig.columns.size) return@f // ignored
                    if (columnIndex >= rowConfig.columns.size) {
                        val description = when {
                            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.4", rowConfig.name)
                            else -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.0")
                        }
                        holder.registerProblem(columnElement, description)
                        return // skip (no future checks)
                    }
                    val expectColumnName = rowConfig.columns[columnIndex].key
                    if (expectColumnName == columnElement.name) return@f // continue (matched)
                    val description = when {
                        context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.2", rowConfig.name, expectColumnName)
                        else -> ChronicleInspectionBundle.message("inspection.incorrectColumnName.desc.0")
                    }
                    val fix = ReplaceWithExpressionFix(expectColumnName)
                    holder.registerProblem(columnElement, description, fix)
                }
            }
        }
    }

    // endregion

    // region IncorrectColumnSizeInspection

    fun checkForIncorrectColumnSize(element: ParadoxCsvColumnContainer, context: ParadoxExpressionInspectionContext) {
        val holder = context.holder
        val rowConfig = context.rowConfig
        if (rowConfig == null) return
        if (rowConfig.skipLastRow && ParadoxCsvPsiService.isLastRow(element)) return // ignored
        val columnSize = ParadoxCsvPsiService.getColumnSize(element)
        val expectColumnSize = rowConfig.columns.size
        if (columnSize == expectColumnSize) return
        if (rowConfig.skipLastColumn && columnSize == expectColumnSize + 1) return // ignored
        val location = element.lastChild ?: return // latest non-empty column or separator
        val description = when {
            context.showExpect -> ChronicleInspectionBundle.message("inspection.incorrectColumnSize.desc.1", rowConfig.name, expectColumnSize, columnSize)
            else -> ChronicleInspectionBundle.message("inspection.incorrectColumnSize.desc.0")
        }
        holder.registerProblem(location, description)
    }

    // endregion
}
