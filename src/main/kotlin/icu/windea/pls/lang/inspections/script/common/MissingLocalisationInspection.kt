package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.util.properties.fromCommandDelimitedString
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.GenerateLocalisationsFix
import icu.windea.pls.lang.quickfix.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.ui.ParadoxLocaleCheckBoxDialog
import icu.windea.pls.lang.ui.ParadoxPreferredLocaleDialog
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import javax.swing.JComponent

/**
 * 缺失的本地化的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class MissingLocalisationInspection : LocalInspectionTool() {
    @JvmField
    var checkForPreferredLocale = true
    @JvmField
    var checkForSpecificLocales = true
    @JvmField
    var locales = ""
    @JvmField
    var checkForDefinitions = true
    @JvmField
    var checkPrimaryForDefinitions = true
    @JvmField
    var checkOptionalForDefinitions = false
    @JvmField
    var checkGeneratedModifiersForDefinitions = false
    @JvmField
    var checkGeneratedModifierNamesForDefinitions = true
    @JvmField
    var checkGeneratedModifierDescriptionsForDefinitions = false
    @JvmField
    var checkForModifiers = false
    @JvmField
    var checkModifierNames = true
    @JvmField
    var checkModifierDescriptions = false
    @JvmField
    var ignoredInInjectedFiles = false

    @Suppress("ktPropBy")
    var localeSet: Set<String> by ::locales.fromCommandDelimitedString()

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的脚本文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
        val locales = mutableSetOf<CwtLocaleConfig>()
        if (checkForPreferredLocale) locales.add(ParadoxLocaleManager.getPreferredLocaleConfig())
        if (checkForSpecificLocales) localeSet.mapNotNullTo(locales) { allLocaleMap.get(it) }
        if (locales.isEmpty()) return PsiElementVisitor.EMPTY_VISITOR
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxDefinitionElement -> visitDefinitionElement(element)
                    is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                }
            }

            private fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromDefinition(element, locales, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(holder, element, context)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromExpression(element, locales, forReference = false, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(holder, element, context)
            }

            private fun registerProblems(holder: ProblemsHolder, element: PsiElement, context: ParadoxLocalisationCodeInsightContext) {
                val location = when {
                    element is ParadoxScriptFile -> element
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return
                }
                val descriptions = getDescriptions(context)
                if (descriptions.isEmpty()) return
                val fixes = getFixes(element, context)
                for (descrption in descriptions) {
                    holder.registerProblem(location, descrption, *fixes)
                }
            }
        }
    }

    private fun getDescriptions(context: ParadoxLocalisationCodeInsightContext): List<String> {
        val includeMap = mutableMapOf<String, ParadoxLocalisationCodeInsightInfo>()
        val excludeKeys = mutableSetOf<String>()
        for (codeInsightInfo in context.infos) {
            if (!codeInsightInfo.check) continue
            val key = codeInsightInfo.key ?: continue
            if (excludeKeys.contains(key)) continue
            if (codeInsightInfo.missing) {
                includeMap.putIfAbsent(key, codeInsightInfo)
            } else {
                includeMap.remove(key)
                excludeKeys.add(key)
            }
        }
        return includeMap.values.mapNotNull { getDescription(it) }
    }

    private fun getDescription(codeInsightInfo: ParadoxLocalisationCodeInsightInfo): String? {
        val localeId = codeInsightInfo.locale.id
        val locationExpression = codeInsightInfo.relatedLocalisationInfo?.locationExpression
        locationExpression?.takeUnless { it.isPlaceholder }?.location
            ?.let { return PlsBundle.message("inspection.script.missingLocalisation.desc.2", localeId, it) }
        codeInsightInfo.name
            ?.let { return PlsBundle.message("inspection.script.missingLocalisation.desc.1", localeId, it) }
        return null
    }

    private fun getFixes(element: PsiElement, context: ParadoxLocalisationCodeInsightContext): Array<LocalQuickFix> {
        return arrayOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }

    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkGeneratedModifiersForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            // checkForPreferredLocale
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForPreferredLocale"))
                    .bindSelected(::checkForPreferredLocale.toAtomicProperty())
                cell(ActionLink(PlsBundle.message("link.configure")) {
                    // ShowSettingsUtil.getInstance().showSettingsDialog(null, ParadoxSettingsConfigurable::class.java)
                    val dialog = ParadoxPreferredLocaleDialog()
                    dialog.showAndGet()
                })
            }
            // checkForSpecificLocales
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForSpecificLocales"))
                    .bindSelected(::checkForSpecificLocales.toAtomicProperty())
                val cb = textField().bindText(::locales.toAtomicProperty()).visible(false).component
                cell(ActionLink(PlsBundle.message("link.configure")) {
                    val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
                    val selectedLocales = localeSet.mapNotNull { allLocaleMap.get(it) }
                    val dialog = ParadoxLocaleCheckBoxDialog(allLocaleMap.values, selectedLocales)
                    if (dialog.showAndGet()) {
                        val newLocaleSet = dialog.localeStatusMap.mapNotNullTo(mutableSetOf()) { (k, v) -> if (v) k.id else null }
                        localeSet = newLocaleSet
                        cb.text = locales // 通知UI locales 已经被更改
                    }
                })
            }
            // checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions.toAtomicProperty())
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                // checkRequiredForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                // checkPrimaryForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkOptionalForDefinitions")).apply {
                        bindSelected(::checkOptionalForDefinitions.toAtomicProperty())
                            .enabledIf(checkForDefinitionsCb.selected)
                    }
                }
                // checkGeneratedModifiersForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifiersForDefinitions"))
                        .bindSelected(::checkGeneratedModifiersForDefinitions.toAtomicProperty())
                        .also { checkGeneratedModifiersForDefinitionsCb = it }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                indent {
                    // checkGeneratedModifierNamesForDefinitions
                    row {
                        checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifierNamesForDefinitions")).apply {
                            bindSelected(::checkGeneratedModifierNamesForDefinitions.toAtomicProperty())
                                .enabledIf(checkGeneratedModifiersForDefinitionsCb.selected)
                        }
                    }
                    // checkGeneratedModifierDescriptionsForDefinitions
                    row {
                        checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifierDescriptionsForDefinitions")).apply {
                            bindSelected(::checkGeneratedModifierDescriptionsForDefinitions.toAtomicProperty())
                                .enabledIf(checkGeneratedModifiersForDefinitionsCb.selected)
                        }
                    }
                }
            }
            // checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers.toAtomicProperty())
                    .also { checkForModifiersCb = it }
            }
            indent {
                // checkModifierNames
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkModifierNames"))
                        .bindSelected(::checkModifierNames.toAtomicProperty())
                        .enabledIf(checkForModifiersCb.selected)
                }
                // checkModifierDescriptions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkModifierDescriptions"))
                        .bindSelected(::checkModifierDescriptions.toAtomicProperty())
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
