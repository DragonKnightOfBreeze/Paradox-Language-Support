package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.hint.HintManager
import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.platform.ide.progress.withModalProgress
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ParadoxLocalisationGenerationManager {
    val currentContext = ThreadLocal<ParadoxLocalisationCodeInsightContext>()

    fun handleGeneration(file: PsiFile, editor: Editor, context: ParadoxLocalisationCodeInsightContext?, locale: CwtLocaleConfig) {
        if (context == null) {
            HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
            return
        }
        val elements = getElements(context, locale)
        if (elements.isEmpty()) {
            HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
            return
        }
        val project = file.project
        val chooser = showChooser(project, context, elements) ?: return
        val selectedElements = chooser.selectedElements ?: return
        if (selectedElements.isEmpty()) return
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        generateAndOpenFile(file, context, locale, selectedElements)
    }

    private fun showChooser(project: Project, context: ParadoxLocalisationCodeInsightContext, elements: List<ParadoxLocalisationGenerationElement.Item>): ParadoxLocalisationGenerationChooser? {
        try {
            currentContext.set(context)
            val chooser = ParadoxLocalisationGenerationChooser(elements.toTypedArray(), project)
            chooser.title = getChooserTitle(context)
            // by default, select all checked missing localisations
            val missingMembers = elements.filter { it.info.check && it.info.missing }
            chooser.selectElements(missingMembers.toTypedArray())
            chooser.show()
            if (chooser.exitCode != DialogWrapper.OK_EXIT_CODE) return null
            return chooser
        } finally {
            currentContext.remove()
        }
    }

    private fun getChooserTitle(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.chooser.title.0.missing")
                else -> PlsBundle.message("generation.localisation.chooser.title.1.missing")
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.chooser.title.0")
                else -> PlsBundle.message("generation.localisation.chooser.title.1")
            }
        }
    }

    private fun getElements(context: ParadoxLocalisationCodeInsightContext, locale: CwtLocaleConfig): List<ParadoxLocalisationGenerationElement.Item> {
        val elements = mutableListOf<ParadoxLocalisationGenerationElement.Item>()
        processElements(context, locale, elements)
        return elements.distinctBy { it.name } // 去重
    }

    private fun processElements(context: ParadoxLocalisationCodeInsightContext, locale: CwtLocaleConfig, elements: MutableList<ParadoxLocalisationGenerationElement.Item>) {
        for (childContext in context.children) {
            processElements(childContext, locale, elements)
        }
        if (context.infos.isEmpty()) return
        val onlyMissing = context.fromInspection
        for (info in context.infos) {
            if (info.locale != locale) continue
            if (onlyMissing && !info.missing) continue // if from inspection, only missing localisations should be included here
            if (info.name.isNullOrEmpty()) continue
            elements += ParadoxLocalisationGenerationElement.Item(info.name, info, context)
        }
    }

    private fun generateAndOpenFile(file: PsiFile, context: ParadoxLocalisationCodeInsightContext, locale: CwtLocaleConfig, elements: List<ParadoxLocalisationGenerationElement.Item>) {
        val project = file.project
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            val title = getProgressTitle(context)
            val generatedFile = withModalProgress(project, title) {
                val tooltip = getFileTooltip(context)
                ParadoxLocalisationGenerationService.generateFile(file, locale, tooltip, elements) // 生成本地化文件
            }
            withContext(Dispatchers.EDT) {
                OpenFileAction.openFile(generatedFile, project) // 在编辑器中打开临时文件
            }
        }
    }

    private fun getProgressTitle(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.progress.title.0.missing", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.progress.title.1.missing", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.progress.title.2.missing", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.progress.title.3.missing", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.progress.title.4.missing", context.name)
                Type.Localisation -> PlsBundle.message("generation.localisation.progress.title.5.missing", context.name)
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.progress.title.0", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.progress.title.1", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.progress.title.2", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.progress.title.3", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.progress.title.4", context.name)
                Type.Localisation -> PlsBundle.message("generation.localisation.progress.title.5", context.name)
            }
        }
    }

    private fun getFileTooltip(context: ParadoxLocalisationCodeInsightContext): String {
        val onlyMissing = context.fromInspection
        return if (onlyMissing) {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.file.tooltip.0.missing", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.file.tooltip.1.missing", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.file.tooltip.2.missing", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.file.tooltip.3.missing", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.file.tooltip.4.missing", context.name)
                Type.Localisation -> PlsBundle.message("generation.localisation.file.tooltip.5.missing", context.name)
            }
        } else {
            when (context.type) {
                Type.File -> PlsBundle.message("generation.localisation.file.tooltip.0", context.name)
                Type.Definition -> PlsBundle.message("generation.localisation.file.tooltip.1", context.name)
                Type.Modifier -> PlsBundle.message("generation.localisation.file.tooltip.2", context.name)
                Type.LocalisationReference -> PlsBundle.message("generation.localisation.file.tooltip.3", context.name)
                Type.SyncedLocalisationReference -> PlsBundle.message("generation.localisation.file.tooltip.4", context.name)
                Type.Localisation -> PlsBundle.message("generation.localisation.file.tooltip.5", context.name)
            }
        }
    }
}
