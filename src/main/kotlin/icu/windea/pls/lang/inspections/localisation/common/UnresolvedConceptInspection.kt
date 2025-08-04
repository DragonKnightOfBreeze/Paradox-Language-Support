package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*
import javax.swing.*

/**
 * 无法解析的概念的检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的CWT规则存在，是否需要忽略此代码检查。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedConceptInspection : LocalInspectionTool() {
    @JvmField
    var ignoredByConfigs = false
    @JvmField
    var ignoredInInjectedFiles = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxLocalisationConceptCommand) visitConceptCommand(element)
            }

            private fun visitConceptCommand(element: ParadoxLocalisationConceptCommand) {
                if (isIgnoredByConfigs(element)) return
                val location = element.conceptName ?: return
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val name = element.name
                holder.registerProblem(location, PlsBundle.message("inspection.localisation.unresolvedConcept.desc", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }

            private fun isIgnoredByConfigs(element: ParadoxLocalisationConceptCommand): Boolean {
                if (!ignoredByConfigs) return false
                val name = element.name
                val configs = configGroup.extendedDefinitions.findFromPattern(name, element, configGroup).orEmpty()
                val config = configs.find { ParadoxDefinitionTypeExpression.resolve(it.type).matches(ParadoxDefinitionTypes.GameConcept) }
                if (config != null) return true
                return false
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (ignoredInInjectedFiles && PlsFileManager.isInjectedFile(file.virtualFile)) return false
        val fileInfo = file.fileInfo ?: return false
        return ParadoxFileManager.inLocalisationPath(fileInfo.path)
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //ignoredByConfigs
            row {
                checkBox(PlsBundle.message("inspection.localisation.unresolvedConcept.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs)
                    .actionListener { _, component -> ignoredByConfigs = component.isSelected }
            }
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
        }
    }
}
