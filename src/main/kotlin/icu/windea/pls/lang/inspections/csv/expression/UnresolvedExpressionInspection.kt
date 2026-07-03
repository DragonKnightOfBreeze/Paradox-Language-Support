package icu.windea.pls.lang.inspections.csv.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvVisitor
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
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是可接受的 CSV 文件
        return ParadoxPsiFileMatchService.isCsvFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        if (file !is ParadoxCsvFile) return PsiElementVisitor.EMPTY_VISITOR
        val header = file.header
        if (header == null) return PsiElementVisitor.EMPTY_VISITOR
        val rowConfig = ParadoxCsvManager.getRowConfig(file)
        if (rowConfig == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : ParadoxCsvVisitor() {
            override fun visitColumn(element: ParadoxCsvColumn) {
                ProgressManager.checkCanceled()
                if (ParadoxCsvPsiService.isHeaderColumn(element)) return // skip header columns
                val location = getLocation(element) ?: return

                // - 如果不存在对应的列规则，则直接跳过
                // - 如果存在对应的列规则且匹配，则直接跳过
                // - 按需忽略最后一行

                val columnConfig = ParadoxCsvManager.getColumnConfig(element, rowConfig) ?: return
                if (ParadoxCsvManager.isMatchedColumnConfig(element, columnConfig)) return

                val expectedConfig = columnConfig.valueConfig
                if (isSkipped(element, expectedConfig)) return
                val expectedConfigs = expectedConfig.to.singletonListOrEmpty()
                val description = getDescription(element, expectedConfigs) ?: getDefaultDescription(element, rowConfig, expectedConfig)
                val highlightType = getHighlightType(element, expectedConfigs) ?: ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                val fixes = getFixes(element, expectedConfigs)
                holder.registerProblem(location, description, highlightType, *fixes)
            }

            private fun isSkipped(element: ParadoxCsvColumn, expectedConfig: CwtValueConfig?): Boolean {
                if (expectedConfig == null) return false
                return when {
                    isIgnoredByConfigs(element, expectedConfig) -> true
                    else -> false
                }
            }

            private fun isIgnoredByConfigs(element: ParadoxCsvColumn, expectedConfig: CwtValueConfig): Boolean {
                return ignoredByConfigs && ParadoxConfigManager.checkExtendedConfig(element, expectedConfig)
            }
        }
    }

    private fun getLocation(element: ParadoxCsvColumn): PsiElement? {
        return when {
            // special handle for empty columns
            ParadoxCsvPsiService.isEmptyColumn(element) -> {
                val isFirst = ParadoxCsvPsiService.getColumnIndex(element) == 0
                element.siblings(forward = isFirst).find { it.elementType == ParadoxCsvElementTypes.SEPARATOR }
            }
            else -> element
        }
    }

    private fun getDefaultDescription(element: ParadoxCsvExpressionElement, rowConfig: CwtRowConfig, expectedConfig: CwtValueConfig?): String {
        val expect = when {
            expectedConfig == null -> ""
            showExpectInfo -> expectedConfig.configExpression.expressionString
            else -> null
        }
        val message = when {
            expect == null -> ChronicleBundle.message("inspection.csv.unresolvedExpression.desc.1", element.expression, rowConfig.name)
            expect.isNotEmpty() -> ChronicleBundle.message("inspection.csv.unresolvedExpression.desc.2", element.expression, rowConfig.name, expect)
            else -> ChronicleBundle.message("inspection.csv.unresolvedExpression.desc.3", element.expression, rowConfig.name)
        }
        return message
    }

    private fun getDescription(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): String? {
        return ParadoxInspectionService.getDescriptionForUnresolvedExpression(element, expectedConfigs)
    }

    private fun getHighlightType(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): ProblemHighlightType? {
        return ParadoxInspectionService.getHighlightTypeForUnresolvedExpression(element, expectedConfigs)
    }

    private fun getFixes(element: ParadoxCsvExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Array<LocalQuickFix> {
        return ParadoxInspectionService.getFixesForUnresolvedExpression(element, expectedConfigs)
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
