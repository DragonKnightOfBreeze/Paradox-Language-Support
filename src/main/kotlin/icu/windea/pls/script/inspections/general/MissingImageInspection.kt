package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关图片，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关图片，默认为false。
 * @property checkForModifiers 是否检查修正（的图标）。默认为false。
 */
class MissingImageInspection : LocalInspectionTool() {
    @JvmField var checkForDefinitions = true
    @JvmField var checkPrimaryForDefinitions = false
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkForModifiers = false
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val file = holder.file
        
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when(element) {
                    is ParadoxScriptDefinitionElement -> {
                        if(!checkForDefinitions) return
                        val definitionInfo = element.definitionInfo ?: return
                        visitDefinition(element, definitionInfo)
                    }
                    is ParadoxScriptStringExpressionElement -> {
                        if(!checkForModifiers) return
                        visitStringExpressionElement(element)
                    }
                }
            }
            
            private fun visitDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                ProgressManager.checkCanceled()
                val imageInfos = definitionInfo.images
                if(imageInfos.isEmpty()) return
                val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
                val nameToDistinct = mutableSetOf<String>()
                val infoMap = mutableMapOf<String, Info>()
                //进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
                var hasPrimary = false
                for(info in imageInfos) {
                    ProgressManager.checkCanceled()
                    val expression = info.locationExpression
                    if(nameToDistinct.contains(info.key)) continue
                    if(info.primary && hasPrimary) continue
                    //多个位置表达式无法解析时，使用第一个
                    if(info.required || if(info.primary) checkPrimaryForDefinitions else checkOptionalForDefinitions) {
                        val resolved = expression.resolve(definition, definitionInfo, project)
                        if(resolved != null) {
                            if(resolved.message != null) { //dynamic, inlined, etc.
                                infoMap.remove(info.key)
                                nameToDistinct.add(info.key)
                                if(info.primary) hasPrimary = true
                            } else if(resolved.file == null) {
                                infoMap.putIfAbsent(info.key, Info(info, resolved.filePath))
                            } else {
                                infoMap.remove(info.key)
                                nameToDistinct.add(info.key)
                                if(info.primary) hasPrimary = true
                            }
                        } else if(expression.propertyName != null || (expression.placeholder != null && definitionInfo.name.isNotEmpty())) {
                            //无法直接获取到图片路径的场合 - 从属性值获取，或者从占位符文本获取且定义非匿名
                            infoMap.putIfAbsent(info.key, Info(info, null))
                        }
                    }
                }
                if(infoMap.isNotEmpty()) {
                    //显示为WEAK_WARNING
                    //缺失多个时，每个算作一个问题
                    for((info, key) in infoMap.values) {
                        val message = getMessage(info, key, definitionInfo.name)
                        holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING)
                    }
                }
            }
            
            private fun getMessage(info: ParadoxDefinitionRelatedImageInfo, key: String?, definitionName: String): String {
                val expression = info.locationExpression
                val p1 = when {
                    info.required -> PlsBundle.message("inspection.script.general.missingImage.description.p1.1")
                    info.primary -> PlsBundle.message("inspection.script.general.missingImage.description.p1.2")
                    else -> PlsBundle.message("inspection.script.general.missingImage.description.p1.3")
                }
                val p2 = when {
                    key != null -> PlsBundle.message("inspection.script.general.missingImage.description.p2.1", key)
                    expression.placeholder.let { it != null && it.startsWith("GFX_") } -> PlsBundle.message("inspection.script.general.missingImage.description.p2.2", expression.resolvePlaceholder(definitionName)!!)
                    expression.propertyName != null -> PlsBundle.message("inspection.script.general.missingImage.description.p2.3", expression.propertyName)
                    else -> PlsBundle.message("inspection.script.general.missingImage.description.p2.4", expression.expressionString)
                }
                return PlsBundle.message("inspection.script.general.missingImage.description", p1, p2)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if(!checkForModifiers) return
                val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                if(config.expression.type != CwtDataType.Modifier) return
                val name = element.value
                val iconPath = ParadoxModifierHandler.getModifierIconPath(name)
                val iconFile = run {
                    val iconSelector = fileSelector(project, file) //use file as context
                    ParadoxFilePathSearch.search(iconPath, null, iconSelector).find()
                }
                if(iconFile == null) {
                    val message = PlsBundle.message("inspection.script.general.missingImage.description.1", name)
                    holder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING)
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions)
                        .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
            }
        }
    }
    
    data class Info(
        val info: ParadoxDefinitionRelatedImageInfo,
        val key: String?
    )
}
