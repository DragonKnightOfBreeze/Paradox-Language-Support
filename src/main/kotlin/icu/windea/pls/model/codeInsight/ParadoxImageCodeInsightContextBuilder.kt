package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.inspections.script.common.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightContext.*
import icu.windea.pls.script.psi.*

object ParadoxImageCodeInsightContextBuilder {
    fun fromFile(
        file: PsiFile,
        fromInspection: Boolean = false,
    ): ParadoxImageCodeInsightContext? {
        if (file !is ParadoxScriptFile) return null
        val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
        val children = mutableListOf<ParadoxImageCodeInsightContext>()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxScriptDefinitionElement -> fromDefinition(element, fromInspection = fromInspection)?.let { children.add(it) }
                    is ParadoxScriptStringExpressionElement -> fromExpression(element, fromInspection = fromInspection)?.let { children.add(it) }
                }
                if (!ParadoxPsiManager.inMemberContext(element)) return //optimize
                super.visitElement(element)
            }
        })
        return ParadoxImageCodeInsightContext(Type.File, file.name, codeInsightInfos, children)
    }

    fun fromDefinition(
        definition: ParadoxScriptDefinitionElement,
        fromInspection: Boolean = false,
    ): ParadoxImageCodeInsightContext? {
        val inspection = if (fromInspection) getMissingImageInspection(definition) else null

        if (!(inspection == null || inspection.checkForDefinitions)) return null
        val definitionInfo = definition.definitionInfo ?: return null
        val project = definitionInfo.project
        val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()

        for (info in definitionInfo.images) {
            ProgressManager.checkCanceled()
            val expression = info.locationExpression
            val resolved = CwtLocationExpressionManager.resolve(expression, definition, definitionInfo)
            val type = when {
                info.required -> ParadoxImageCodeInsightInfo.Type.Required
                info.primary -> ParadoxImageCodeInsightInfo.Type.Primary
                else -> ParadoxImageCodeInsightInfo.Type.Optional
            }
            val name = resolved?.nameOrFilePath
            val gfxName = CwtLocationExpressionManager.resolvePlaceholder(expression, definitionInfo.name)?.takeIf { it.startsWith("GFX_") }
            val check = when {
                info.required -> true
                (inspection == null || inspection.checkPrimaryForDefinitions) && (info.primary || info.primaryByInference) -> true
                (inspection == null || inspection.checkOptionalForDefinitions) && !info.required -> true
                else -> false
            }
            val missing = resolved?.element == null && resolved?.message == null
            val dynamic = resolved?.message != null
            val codeInsightInfo = ParadoxImageCodeInsightInfo(type, name, gfxName, info, check, missing, dynamic)
            codeInsightInfos += codeInsightInfo
        }

        for (info in definitionInfo.modifiers) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            run {
                val type = ParadoxImageCodeInsightInfo.Type.GeneratedModifierIcon
                val check = inspection == null || inspection.checkGeneratedModifierIconsForDefinitions
                val paths = ParadoxModifierManager.getModifierIconPaths(modifierName, definition)
                val pathToUse = paths.firstOrNull() ?: return@run
                val missing = paths.all { path -> isMissing(path, project, definition) }
                val codeInsightInfo = ParadoxImageCodeInsightInfo(type, pathToUse, null, null, check, missing, false)
                codeInsightInfos += codeInsightInfo
            }
        }

        return ParadoxImageCodeInsightContext(Type.Definition, definitionInfo.name, codeInsightInfos, fromInspection = fromInspection)
    }

    fun fromExpression(
        element: ParadoxScriptStringExpressionElement,
        fromInspection: Boolean = false,
    ): ParadoxImageCodeInsightContext? {
        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return null
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        fromModifier(element, config, fromInspection = fromInspection)?.let { return it }
        return null
    }

    fun fromModifier(
        element: ParadoxScriptStringExpressionElement,
        config: CwtMemberConfig<*>,
        fromInspection: Boolean = false,
    ): ParadoxImageCodeInsightContext? {
        val inspection = if (fromInspection) getMissingImageInspection(element) else null

        if (!(inspection == null || inspection.checkForModifiers)) return null
        if (config.expression.type != CwtDataTypes.Modifier) return null
        val modifierName = element.value
        val project = config.configGroup.project
        val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()

        run {
            val type = ParadoxImageCodeInsightInfo.Type.ModifierIcon
            val check = inspection == null || inspection.checkModifierIcons
            val paths = ParadoxModifierManager.getModifierIconPaths(modifierName, element)
            val pathToUse = paths.firstOrNull() ?: return@run
            val missing = paths.all { path -> isMissing(path, project, element) }
            val codeInsightInfo = ParadoxImageCodeInsightInfo(type, pathToUse, null, null, check, missing, false)
            codeInsightInfos += codeInsightInfo
        }

        return ParadoxImageCodeInsightContext(Type.Modifier, modifierName, codeInsightInfos, fromInspection = fromInspection)
    }

    private fun isMissing(iconPath: String, project: Project, context: PsiElement): Boolean {
        val iconSelector = selector(project, context).file()
        val missing = ParadoxFilePathSearch.searchIcon(iconPath, iconSelector).findFirst() == null
        return missing
    }

    private fun getMissingImageInspection(context: PsiElement): MissingImageInspection? {
        return getInspectionToolState("ParadoxScriptMissingImage", context, context.project)?.enabledTool?.castOrNull()
    }
}
