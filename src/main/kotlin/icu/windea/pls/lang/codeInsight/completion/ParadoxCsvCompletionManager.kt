package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.icon
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.util.ParadoxCsvManager

object ParadoxCsvCompletionManager {
    fun addColumnCompletions(columnElement: ParadoxCsvColumn, context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.file !is ParadoxCsvFile) return

        if (columnElement.isHeaderColumn()) {
            completeHeaderColumn(context, result)
            return
        }

        val columnConfig = ParadoxCsvManager.getColumnConfig(columnElement) ?: return
        val config = columnConfig.valueConfig ?: return
        run {
            val context = context.copy(config = config)
            ParadoxCompletionManager.completeCsvExpression(context, result)
        }
    }

    fun completeHeaderColumn(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ProgressManager.checkCanceled()
        val column = context.contextElement.castOrNull<ParadoxCsvColumn>() ?: return
        if (!column.isHeaderColumn()) return
        val file = context.file
        if (file !is ParadoxCsvFile) return
        val rowConfig = ParadoxCsvManager.getRowConfig(file) ?: return
        val header = column.parent?.castOrNull<ParadoxCsvHeader>() ?: return
        val existingHeaderNames = header.children()
            .mapNotNull { it as? ParadoxCsvColumn }
            .filterIsInstance<ParadoxCsvColumn> { it != column }
            .map { it.value }
            .toSet()
        val columnConfigs = rowConfig.columns.filterNot { it.key in existingHeaderNames }.values
        if (columnConfigs.isEmpty()) return
        columnConfigs.forEach f@{ columnConfig ->
            ProgressManager.checkCanceled()
            run {
                val context = context.copy(config = columnConfig)
                val name = columnConfig.key
                val element = columnConfig.pointer.element ?: return@f
                val typeFile = columnConfig.pointer.containingFile
                val lookupElement = LookupElementBuilder.create(element, name)
                    .withIcon(PlsIcons.Nodes.Column)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withPriority(PlsCompletionPriorities.constant)
                result.addElement(lookupElement, context)
            }
        }
    }
}
