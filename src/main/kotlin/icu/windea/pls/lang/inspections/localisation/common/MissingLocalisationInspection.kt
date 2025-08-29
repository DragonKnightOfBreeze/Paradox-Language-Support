package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.core.bindTextWhenChanged
import icu.windea.pls.core.util.properties.fromCommandDelimitedString
import icu.windea.pls.lang.quickfix.GenerateLocalisationsFix
import icu.windea.pls.lang.quickfix.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.ui.locale.ParadoxLocaleCheckBoxDialog
import icu.windea.pls.lang.ui.locale.ParadoxPreferredLocaleDialog
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightInfo
import javax.swing.JComponent

/**
 * 缺失的本地化的检查
 */
class MissingLocalisationInspection : LocalInspectionTool() {
    @JvmField
    var checkForPreferredLocale = true
    @JvmField
    var checkForSpecificLocales = true
    @JvmField
    var locales = ""

    var localeSet: Set<String> by ::locales.fromCommandDelimitedString()

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
        val locales = mutableSetOf<CwtLocaleConfig>()
        if (checkForPreferredLocale) locales.add(ParadoxLocaleManager.getPreferredLocaleConfig())
        if (checkForSpecificLocales) localeSet.mapNotNullTo(locales) { allLocaleMap.get(it) }
        if (locales.isEmpty()) return PsiElementVisitor.EMPTY_VISITOR
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationProperty) visitLocalisation(element)
            }

            private fun visitLocalisation(element: ParadoxLocalisationProperty) {
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromLocalisation(element, locales, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(context, element, holder)
            }

            private fun registerProblems(context: ParadoxLocalisationCodeInsightContext, element: PsiElement, holder: ProblemsHolder) {
                val location = when {
                    element is ParadoxLocalisationFile -> element
                    element is ParadoxLocalisationProperty -> element.propertyKey
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
                val name = codeInsightInfo.name ?: return null
                val from = PlsBundle.message("inspection.localisation.missingLocalisation.from", name)
                val localeId = codeInsightInfo.locale.id
                return PlsBundle.message("inspection.localisation.missingLocalisation.desc", from, localeId)
            }

            private fun getFixes(element: PsiElement, context: ParadoxLocalisationCodeInsightContext): List<LocalQuickFix> {
                return buildList {
                    this += GenerateLocalisationsFix(element, context)
                    this += GenerateLocalisationsInFileFix(element)
                }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //checkForPreferredLocale
            row {
                checkBox(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForPreferredLocale"))
                    .bindSelected(::checkForPreferredLocale)
                    .actionListener { _, component -> checkForPreferredLocale = component.isSelected }
                cell(ActionLink(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForPreferredLocale.configure")) {
                    //ShowSettingsUtil.getInstance().showSettingsDialog(null, ParadoxSettingsConfigurable::class.java)
                    val dialog = ParadoxPreferredLocaleDialog()
                    dialog.showAndGet()
                })
            }
            //checkForSpecificLocales
            row {
                checkBox(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForSpecificLocales"))
                    .bindSelected(::checkForSpecificLocales)
                    .actionListener { _, component -> checkForSpecificLocales = component.isSelected }
                val cb = textField().bindText(::locales).bindTextWhenChanged(::locales).visible(false).component
                cell(ActionLink(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForSpecificLocales.configure")) {
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
        }
    }
}
