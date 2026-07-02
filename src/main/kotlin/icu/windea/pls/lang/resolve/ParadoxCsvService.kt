package icu.windea.pls.lang.resolve

import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.CwtRowConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.model.expressions.ParadoxExpression

object ParadoxCsvService {
    fun resolveRowConfig(file: ParadoxCsvFile): CwtRowConfig? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val matchContext = CwtRowConfigMatchContext(configGroup, path)
        val rowConfig = ParadoxConfigMatchService.getMatchedRowConfig(matchContext)
        return rowConfig
    }

    fun getColumnConfig(element: ParadoxCsvColumn, rowConfig: CwtRowConfig): CwtPropertyConfig? {
        val rowElement = element.parent?.castOrNull<ParadoxCsvColumnContainer>() ?: return null
        if (rowConfig.skipLastRow && ParadoxCsvPsiService.isLastRow(rowElement)) return null // #314
        // if (rowConfig.skipLastColumn && ParadoxCsvPsiService.isLastColumn(element)) return null // #314 (not here, not such logic)
        val columnNames = ParadoxCsvPsiService.getColumnNames(rowElement).orNull() ?: return null
        val columnIndex = ParadoxCsvPsiService.getColumnIndex(element)
        return ParadoxConfigMatchService.getColumnConfig(rowConfig, columnNames, columnIndex)
    }

    fun isMatchedColumnConfig(column: ParadoxCsvColumn, columnConfig: CwtPropertyConfig): Boolean {
        if (ParadoxCsvPsiService.isHeaderColumn(column)) return true // header column -> always true

        val configExpression = columnConfig.valueConfig?.configExpression ?: return false
        val configGroup = columnConfig.configGroup
        val expression = ParadoxExpression.resolve(column)
        val context = ParadoxCsvExpressionMatchContext(column, expression, configExpression, configGroup)
        return ParadoxExpressionMatchService.matchCsvExpression(context).get()
    }
}
