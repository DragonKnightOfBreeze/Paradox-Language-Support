package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.bindTextWhenChanged
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的本地化的检查。
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
    var checkPrimaryForDefinitions = false
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

    var localeSet: Set<String> by ::locales.fromCommandDelimitedString()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
        val locales = mutableSetOf<CwtLocalisationLocaleConfig>()
        if (checkForPreferredLocale) locales.add(ParadoxLocaleManager.getPreferredLocaleConfig())
        if (checkForSpecificLocales) localeSet.mapNotNullTo(locales) { allLocaleMap.get(it) }
        if (locales.isEmpty()) return PsiElementVisitor.EMPTY_VISITOR
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when (element) {
                    is ParadoxScriptDefinitionElement -> visitDefinition(element)
                    is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                }
            }

            private fun visitDefinition(element: ParadoxScriptDefinitionElement) {
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromDefinition(element, locales, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(context, element, holder)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromExpression(element, locales, forReference = false, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(context, element, holder)
            }

            private fun registerProblems(context: ParadoxLocalisationCodeInsightContext, element: PsiElement, holder: ProblemsHolder) {
                val location = when {
                    element is ParadoxScriptFile -> element
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return
                }
                val messages = getMessages(context)
                if (messages.isEmpty()) return
                val fixes = getFixes(element, context).toTypedArray()
                for (message in messages) {
                    //显示为WEAK_WARNING
                    holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING, *fixes)
                }
            }

            private fun getMessages(context: ParadoxLocalisationCodeInsightContext): List<String> {
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
                return includeMap.values.mapNotNull { getMessage(it) }
            }

            private fun getMessage(codeInsightInfo: ParadoxLocalisationCodeInsightInfo): String? {
                val locationExpression = codeInsightInfo.relatedLocalisationInfo?.locationExpression
                val fromProperty = locationExpression?.takeUnless { it.isPlaceholder }?.location
                val from = fromProperty?.let { PlsBundle.message("inspection.script.missingLocalisation.from.2", it) }
                    ?: codeInsightInfo.name?.let { PlsBundle.message("inspection.script.missingLocalisation.from.1", it) }
                    ?: return null
                val localeId = codeInsightInfo.locale.id
                return PlsBundle.message("inspection.script.missingLocalisation.desc", from, localeId)
            }

            private fun getFixes(element: PsiElement, context: ParadoxLocalisationCodeInsightContext): List<LocalQuickFix> {
                return buildList {
                    this += GenerateLocalisationsFix(element, context)
                    this += GenerateLocalisationsInFileFix(element)
                }
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkGeneratedModifiersForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            //checkForPreferredLocale
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForPreferredLocale"))
                    .bindSelected(::checkForPreferredLocale)
                    .actionListener { _, component -> checkForPreferredLocale = component.isSelected }
                cell(ActionLink(PlsBundle.message("inspection.script.missingLocalisation.option.checkForPreferredLocale.configure")) {
                    //ShowSettingsUtil.getInstance().showSettingsDialog(null, ParadoxSettingsConfigurable::class.java)
                    val dialog = ParadoxPreferredLocaleDialog()
                    dialog.showAndGet()
                })
            }
            //checkForSpecificLocales
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForSpecificLocales"))
                    .bindSelected(::checkForSpecificLocales)
                    .actionListener { _, component -> checkForSpecificLocales = component.isSelected }
                val cb = textField().bindText(::locales).bindTextWhenChanged(::locales).visible(false).component
                cell(ActionLink(PlsBundle.message("inspection.script.missingLocalisation.option.checkForSpecificLocales.configure")) {
                    val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
                    val selectedLocales = localeSet.mapNotNull { allLocaleMap.get(it) }
                    val dialog = ParadoxLocaleCheckBoxDialog(selectedLocales, allLocaleMap.values)
                    if (dialog.showAndGet()) {
                        val newLocaleSet = dialog.localeStatusMap.mapNotNullTo(mutableSetOf()) { (k, v) -> if (v) k.id else null }
                        localeSet = newLocaleSet
                        cb.text = locales //通知UI locales 已经被更改
                    }
                })
            }
            //checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                //checkRequiredForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                //checkPrimaryForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                //checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkOptionalForDefinitions")).apply {
                        bindSelected(::checkOptionalForDefinitions)
                            .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                            .enabledIf(checkForDefinitionsCb.selected)
                    }
                }
                //checkGeneratedModifiersForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifiersForDefinitions"))
                        .bindSelected(::checkGeneratedModifiersForDefinitions)
                        .actionListener { _, component -> checkGeneratedModifiersForDefinitions = component.isSelected }
                        .also { checkGeneratedModifiersForDefinitionsCb = it }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                indent {
                    //checkGeneratedModifierNamesForDefinitions
                    row {
                        checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifierNamesForDefinitions")).apply {
                            bindSelected(::checkGeneratedModifierNamesForDefinitions)
                                .actionListener { _, component -> checkGeneratedModifierNamesForDefinitions = component.isSelected }
                                .enabledIf(checkGeneratedModifiersForDefinitionsCb.selected)
                        }
                    }
                    //checkGeneratedModifierDescriptionsForDefinitions
                    row {
                        checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkGeneratedModifierDescriptionsForDefinitions")).apply {
                            bindSelected(::checkGeneratedModifierDescriptionsForDefinitions)
                                .actionListener { _, component -> checkGeneratedModifierDescriptionsForDefinitions = component.isSelected }
                                .enabledIf(checkGeneratedModifiersForDefinitionsCb.selected)
                        }
                    }
                }
            }
            //checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
                    .also { checkForModifiersCb = it }
            }
            indent {
                //checkModifierNames
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkModifierNames"))
                        .bindSelected(::checkModifierNames)
                        .actionListener { _, component -> checkModifierNames = component.isSelected }
                        .enabledIf(checkForModifiersCb.selected)
                }
                //checkModifierDescriptions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingLocalisation.option.checkModifierDescriptions"))
                        .bindSelected(::checkModifierDescriptions)
                        .actionListener { _, component -> checkModifierDescriptions = component.isSelected }
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
        }
    }
}
