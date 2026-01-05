package icu.windea.pls.lang.inspections.script.definitionInjection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.inspections.PlsInspectionUtil
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.JComponent

/**
 * 检查定义注入的用法是否正确。
 *
 * - 基于定义注入的模式，定义目标的存在性必须正确。
 */
class IncorrectDefinitionInjectionUsageInspection : DefinitionInjectionInspectionBase() {
    private var checkForRelaxModes = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element !is ParadoxScriptProperty) return
                val definitionInjectionInfo = ParadoxDefinitionInjectionManager.getInfo(element) ?: return
                if (definitionInjectionInfo.target.isNullOrEmpty()) {
                    val description = PlsBundle.message("inspection.script.incorrectDefinitionInjectionUsage.desc.1")
                    holder.registerProblem(element.propertyKey, description)
                    return
                }
                if (definitionInjectionInfo.type.isNullOrEmpty()) return // considered "unsupported"
                if (definitionInjectionInfo.typeConfig == null) return // considered "unsupported"
                val target = definitionInjectionInfo.target
                val type = definitionInjectionInfo.type
                val relax = ParadoxDefinitionInjectionManager.isRelaxMode(definitionInjectionInfo)
                val targetExist = ParadoxDefinitionInjectionManager.isTargetExist(definitionInjectionInfo, holder.file)
                if (!targetExist) {
                    if (relax) {
                        if (checkForRelaxModes) {
                            val description = PlsBundle.message("inspection.script.incorrectDefinitionInjectionUsage.desc.3", target, type)
                            val highlightType = PlsInspectionUtil.getWeakerHighlightType() // use weaker highlight type
                            holder.registerProblem(element.propertyKey, description, highlightType)
                        }
                    } else {
                        val description = PlsBundle.message("inspection.script.incorrectDefinitionInjectionUsage.desc.2", target, type)
                        holder.registerProblem(element.propertyKey, description)
                    }
                }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.incorrectDefinitionInjectionUsage.option.checkForRelaxModes"))
                    .bindSelected(::checkForRelaxModes)
                    .actionListener { _, component -> checkForRelaxModes = component.isSelected }
                contextHelp(PlsBundle.message("inspection.script.incorrectDefinitionInjectionUsage.option.checkForRelaxModes.tip"))
            }
        }
    }
}
