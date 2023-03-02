package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的本地化的检查。
 * @property locales 要检查的语言区域。默认检查英文。
 * @property checkPrimaryLocale 是否同样检查主要的语言区域。默认为true。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关本地化，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关本地化，默认为false。
 * @property checkForModifiers 是否检查修饰符。默认为false。
 */
class MissingLocalisationInspection : LocalInspectionTool() {
    @OptionTag(converter = CommaDelimitedStringListConverter::class)
    @JvmField var locales = setOf("l_english")
    @JvmField var checkForDefinitions = true
    @JvmField var checkPrimaryLocale = true
    @JvmField var checkPrimaryForDefinitions = false
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkForModifiers = false
    
    private val localeList by lazy {
        val allLocales = getCwtConfig().core.localisationLocales
        locales.mapNotNullTo(mutableListOf()) { allLocales.get(it) }
    }
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return Visitor(this, holder)
    }
    
    private class Visitor(
        private val inspection: MissingLocalisationInspection,
        private val holder: ProblemsHolder
    ) : ParadoxScriptVisitor() {
        override fun visitFile(file: PsiFile) {
            if(inspection.locales.isEmpty()) return
            if(!inspection.checkForDefinitions) return
            val scriptFile = file.castOrNull<ParadoxScriptFile>() ?: return
            val definitionInfo = scriptFile.definitionInfo ?: return
            visitDefinition(scriptFile, definitionInfo)
        }
        
        override fun visitProperty(property: ParadoxScriptProperty) {
            ProgressManager.checkCanceled()
            if(inspection.locales.isEmpty()) return
            if(!inspection.checkForDefinitions) return
            val definitionInfo = property.definitionInfo ?: return
            visitDefinition(property, definitionInfo)
        }
        
        private fun visitDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
            ProgressManager.checkCanceled()
            val project = definitionInfo.project
            val localisationInfos = definitionInfo.localisation
            if(localisationInfos.isEmpty()) return
            val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
            val nameToDistinct = mutableSetOf<String>()
            val infoMap = mutableMapOf<String, Tuple3<ParadoxDefinitionRelatedLocalisationInfo, String?, CwtLocalisationLocaleConfig>>()
            //进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
            val hasPrimaryLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
            runReadAction {
                for(info in localisationInfos) {
                    ProgressManager.checkCanceled()
                    if(info.required || if(info.primary) inspection.checkPrimaryForDefinitions else inspection.checkOptionalForDefinitions) {
                        for(locale in inspection.localeList) {
                            if(nameToDistinct.contains(info.name + "@" + locale)) continue
                            if(info.primary && hasPrimaryLocales.contains(locale)) continue
                            //多个位置表达式无法解析时，使用第一个
                            val selector = localisationSelector(project, definition).locale(locale)
                            val resolved = info.locationExpression.resolve(definition, definitionInfo, project, selector)
                            if(resolved != null) {
                                if(resolved.message != null) continue //skip if it's dynamic or inlined
                                if(resolved.localisation == null) {
                                    infoMap.putIfAbsent(info.name + "@" + locale, tupleOf(info, resolved.key, locale))
                                } else {
                                    infoMap.remove(info.name + "@" + locale)
                                    nameToDistinct.add(info.name + "@" + locale)
                                    if(info.primary) hasPrimaryLocales.add(locale)
                                }
                            } else if(info.locationExpression.propertyName != null) {
                                //从定义的属性推断，例如，#name
                                infoMap.putIfAbsent(info.name + "@" + locale, tupleOf(info, null, locale))
                            }
                        }
                    }
                }
            }
            if(infoMap.isNotEmpty()) {
                //显示为WEAK_WARNING，且缺失多个时，每个算作一个问题
                for((info, key, locale) in infoMap.values) {
                    val message = getMessage(info, key, locale)
                    holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING)
                }
            }
        }
        
        private fun getMessage(info: ParadoxDefinitionRelatedLocalisationInfo, key: String?, locale: CwtLocalisationLocaleConfig): String {
            val expression = info.locationExpression
            val propertyName = expression.propertyName
            return when {
                info.required -> when {
                    key != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.1.1", key, locale)
                    propertyName != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.1.2", propertyName, locale)
                    else -> PlsBundle.message("inspection.script.general.missingLocalisation.description.1.3", expression, locale)
                }
                info.primary -> when {
                    key != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.2.1", key, locale)
                    propertyName != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.2.2", propertyName, locale)
                    else -> PlsBundle.message("inspection.script.general.missingLocalisation.description.2.3", expression, locale)
                }
                else -> when {
                    key != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.3.1", key, locale)
                    propertyName != null -> PlsBundle.message("inspection.script.general.missingLocalisation.description.3.2", propertyName, locale)
                    else -> PlsBundle.message("inspection.script.general.missingLocalisation.description.3.3", expression, locale)
                }
            }
        }
        
        override fun visitPropertyKey(element: ParadoxScriptPropertyKey) {
            visitStringExpressionElement(element)
        }
        
        override fun visitString(element: ParadoxScriptString) {
            visitStringExpressionElement(element)
        }
        
        private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
            ProgressManager.checkCanceled()
            if(inspection.locales.isEmpty()) return
            if(!inspection.checkForModifiers) return
            val config = ParadoxCwtConfigHandler.getConfigs(element).firstOrNull() ?: return
            val configGroup = config.info.configGroup
            if(config.expression.type != CwtDataType.Modifier) return
            val project = configGroup.project
            val name = element.value
            val keys = ParadoxModifierHandler.getModifierNameKeys(name)
            val missingLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
            for(locale in inspection.localeList) {
                val selector = localisationSelector(project, element).locale(locale)
                val localisation = keys.firstNotNullOfOrNull {
                    ParadoxLocalisationSearch.search(it, selector).findFirst()
                }
                if(localisation == null) missingLocales.add(locale)
            }
            if(missingLocales.isNotEmpty()) {
                for(locale in missingLocales) {
                    val message = PlsBundle.message("inspection.script.general.missingLocalisation.description.4", name, locale)
                    holder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING)
                }
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                cell(createLocaleTableModel(localeList))
                    .align(Align.FILL)
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.forPreferredLocale"))
                    .bindSelected(::checkPrimaryLocale)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.missingLocalisation.option.forPrimaryLocale.tooltip") }
                    .actionListener { _, component -> checkPrimaryLocale = component.isSelected }
            }
            lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions)
                        .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
            }
        }
    }
    
    //private class GenerateMissingLocalisationFix(
    //	private val keys: Set<String>,
    //	element: ParadoxScriptDefinitionElement
    //): LocalQuickFixAndIntentionActionOnPsiElement(element), HighPriorityAction{
    //	override fun getText() = PlsBundle.message("inspection.script.definition.inspection.missingRelatedLocalisation.quickfix.1")
    //	
    //	override fun getFamilyName() = text
    //	
    //	override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    //		//TODO
    //	}
    //	
    //	override fun availableInBatchMode() = false
    //}
}
