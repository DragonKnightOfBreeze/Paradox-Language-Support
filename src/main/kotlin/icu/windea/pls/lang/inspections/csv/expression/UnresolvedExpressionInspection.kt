package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionService
import icu.windea.pls.lang.inspections.ParadoxInspectionService
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import javax.swing.JComponent

/**
 * （脚本文件中的）无法解析的表达式的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredByConfigs = false
    @JvmField var showExpectInfo = true

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的 CSV 文件
        return ParadoxPsiFileMatchService.isCsvFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        val context = ParadoxExpressionInspectionService.createContext(this, holder)
        val checkers = ParadoxUnresolvedExpressionChecker.EP_NAME.extensionList
        return object : ParadoxCsvVisitor() {
            override fun visitColumn(element: ParadoxCsvColumn) {
                ProgressManager.checkCanceled()
                if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header column

                // - 如果不存在对应的列规则，则直接跳过
                // - 如果存在对应的列规则且匹配，则直接跳过
                // - 按需忽略最后一行

                val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return // skip (checked by `IncorrectColumnSizeInspection`)
                if (ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return

                val expectedConfigs = getExpectedConfigs(columnConfig)
                if (isIgnored(element, expectedConfigs)) return

                ParadoxInspectionService.checkUnresolvedExpression(element, expectedConfigs, context, checkers)
            }

            private fun getExpectedConfigs(columnConfig: CwtPropertyConfig): List<CwtValueConfig> {
                val valueConfig = columnConfig.valueConfig ?: return emptyList()
                return listOf(valueConfig)
            }

            private fun isIgnored(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtValueConfig>): Boolean {
                if (expectedConfigs.isEmpty()) return false
                return isIgnoredByConfigs(element, expectedConfigs)
            }

            private fun isIgnoredByConfigs(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtValueConfig>): Boolean {
                return ignoredByConfigs && expectedConfigs.any { ParadoxConfigManager.checkExtendedConfig(element, it) }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredByConfigs
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs.toAtomicProperty())
            }
            // showExpectInfo
            row {
                checkBox(ChronicleBundle.message("inspection.option.showExpectInfo"))
                    .bindSelected(::showExpectInfo.toAtomicProperty())
            }
        }
    }
}
