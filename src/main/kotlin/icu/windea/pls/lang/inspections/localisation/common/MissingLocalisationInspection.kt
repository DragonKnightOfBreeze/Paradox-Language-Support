package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.util.properties.fromCommandDelimitedString
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.quickfix.GenerateLocalisationsFix
import icu.windea.pls.lang.quickfix.GenerateLocalisationsInFileFix
import icu.windea.pls.lang.ui.ParadoxLocaleCheckBoxDialog
import icu.windea.pls.lang.ui.ParadoxPreferredLocaleDialog
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightInfo
import javax.swing.JComponent

/**
 * 缺失的本地化的代码检查。
 */
class MissingLocalisationInspection : LocalInspectionTool() {
    @JvmField
    var checkForPreferredLocale = true
    @JvmField
    var checkForSpecificLocales = true
    @JvmField
    var locales = ""

    @Suppress("ktPropBy")
    var localeSet: Set<String> by ::locales.fromCommandDelimitedString()

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是符合条件的本地化文件
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val allLocaleMap = ParadoxLocaleManager.getLocaleConfigs().associateBy { it.id }
        val locales = mutableSetOf<CwtLocaleConfig>()
        if (checkForPreferredLocale) locales.add(ParadoxLocaleManager.getPreferredLocaleConfig())
        if (checkForSpecificLocales) localeSet.mapNotNullTo(locales) { allLocaleMap.get(it) }
        if (locales.isEmpty()) return PsiElementVisitor.EMPTY_VISITOR
        return object : ParadoxLocalisationVisitor() {
            override fun visitProperty(element: ParadoxLocalisationProperty) {
                ProgressManager.checkCanceled()
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
                val messages = getDescriptions(context)
                if (messages.isEmpty()) return
                val fixes = getFixes(element, context)
                for (description in messages) {
                    holder.registerProblem(location, description, *fixes)
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
        codeInsightInfo.name
            ?.let { return PlsBundle.message("inspection.localisation.missingLocalisation.desc.1", localeId, it) }
        return null
    }

    private fun getFixes(element: PsiElement, context: ParadoxLocalisationCodeInsightContext): Array<LocalQuickFix> {
        return arrayOf(
            GenerateLocalisationsFix(element, context),
            GenerateLocalisationsInFileFix(element),
        )
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // checkForPreferredLocale
            row {
                checkBox(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForPreferredLocale"))
                    .bindSelected(::checkForPreferredLocale.toAtomicProperty())
                cell(ActionLink(PlsBundle.message("link.configure")) {
                    // ShowSettingsUtil.getInstance().showSettingsDialog(null, ParadoxSettingsConfigurable::class.java)
                    val dialog = ParadoxPreferredLocaleDialog()
                    dialog.showAndGet()
                })
            }
            // checkForSpecificLocales
            row {
                checkBox(PlsBundle.message("inspection.localisation.missingLocalisation.option.checkForSpecificLocales"))
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
        }
    }
}
