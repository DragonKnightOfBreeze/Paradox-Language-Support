package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.generation.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的本地化的检查。
 * @property locales 要检查的语言区域。默认检查英文。
 * @property checkPreferredLocale 是否同样检查主要的语言区域。默认为true。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关本地化，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关本地化，默认为false。
 * @property checkForModifiers 是否检查修正。默认为false。
 */
class MissingLocalisationInspection : LocalInspectionTool() {
    @OptionTag(converter = CommaDelimitedStringSetConverter::class)
    @JvmField var locales = mutableSetOf<String>()
    @JvmField var checkForDefinitions = true
    @JvmField var checkPreferredLocale = true
    @JvmField var checkPrimaryForDefinitions = false
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkForModifiers = false
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val file = holder.file
        val allLocaleConfigs = getCwtConfig().core.localisationLocales
        val localeConfigs = locales.mapNotNullTo(mutableSetOf()) { allLocaleConfigs.get(it) }
        if(checkPreferredLocale) {
            localeConfigs.add(preferredParadoxLocale())
        }
        
        return object : PsiElementVisitor() {
            var inFileContext: GenerateLocalisationsInFileContext? = null
            
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
                val localisationInfos = definitionInfo.localisations
                if(localisationInfos.isEmpty()) return
                val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
                val nameToDistinct = mutableSetOf<String>()
                val infoMap = mutableMapOf<String, Info>()
                //进行代码检查时，规则文件中声明了多个不同名字的primaryLocalisation/primaryImage的场合，只要匹配其中一个名字的即可
                val hasPrimaryLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
                for(info in localisationInfos) {
                    ProgressManager.checkCanceled()
                    if(info.required || if(info.primary) checkPrimaryForDefinitions else checkOptionalForDefinitions) {
                        for(locale in localeConfigs) {
                            if(nameToDistinct.contains(info.name + "@" + locale)) continue
                            if(info.primary && hasPrimaryLocales.contains(locale)) continue
                            //多个位置表达式无法解析时，使用第一个
                            val selector = localisationSelector(project, file).locale(locale) //use file as context
                            val resolved = info.locationExpression.resolve(definition, definitionInfo, selector)
                            if(resolved != null) {
                                if(resolved.message != null) continue //skip if it's dynamic or inlined
                                if(resolved.localisation == null) {
                                    infoMap.putIfAbsent(info.name + "@" + locale, Info(info, resolved.key, locale))
                                } else {
                                    infoMap.remove(info.name + "@" + locale)
                                    nameToDistinct.add(info.name + "@" + locale)
                                    if(info.primary) hasPrimaryLocales.add(locale)
                                }
                            } else if(info.locationExpression.propertyName != null) {
                                //从定义的属性推断，例如，#name
                                infoMap.putIfAbsent(info.name + "@" + locale, Info(info, null, locale))
                            }
                        }
                    }
                }
                
                if(infoMap.isNotEmpty()) {
                    //添加快速修复
                    val fixes = getFixes(definition, definitionInfo, infoMap).toTypedArray()
                    
                    //显示为WEAK_WARNING
                    //缺失多个时，每个算作一个问题
                    for((info, key, locale) in infoMap.values) {
                        val message = getMessage(info, key, locale)
                        holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING, *fixes)
                    }
                }
            }
            
            private fun getFixes(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, infoMap: Map<String, Info>): List<LocalQuickFix> {
                return buildList {
                    val context = GenerateLocalisationsContext(definitionInfo.name, infoMap.mapNotNullTo(mutableSetOf()) { it.value.key })
                    add(GenerateLocalisationsFix(context, definition))
                    if(inFileContext == null) {
                        val fileName = definition.containingFile.name
                        inFileContext = GenerateLocalisationsInFileContext(fileName, mutableListOf())
                    }
                    val inFileContext = inFileContext!!
                    inFileContext.contextList.add(context)
                    add(GenerateLocalisationsInFileFix(inFileContext, definition))
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
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                if(localeConfigs.isEmpty()) return
                if(!checkForModifiers) return
                val config = ParadoxConfigResolver.getConfigs(element).firstOrNull() ?: return
                if(config.expression.type != CwtDataType.Modifier) return
                val name = element.value
                val key = ParadoxModifierHandler.getModifierNameKey(name)
                val missingLocales = mutableSetOf<CwtLocalisationLocaleConfig>()
                for(locale in localeConfigs) {
                    val selector = localisationSelector(project, file)
                        .locale(locale)
                        .withConstraint(ParadoxLocalisationConstraint.Modifier) //use file as context
                    val localisation = ParadoxLocalisationSearch.search(key, selector).findFirst()
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
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
            row {
                label(PlsBundle.message("inspection.script.general.missingLocalisation.option.locales"))
            }
            indent {
                row {
                    cell(ParadoxLocaleTableModel.createPanel(locales))
                        .align(Align.FILL)
                }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingLocalisation.option.forPreferredLocale"))
                    .bindSelected(::checkPreferredLocale)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.missingLocalisation.option.forPreferredLocale.tooltip") }
                    .actionListener { _, component -> checkPreferredLocale = component.isSelected }
            }
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
    
    data class Info(
        val info: ParadoxDefinitionRelatedLocalisationInfo,
        val key: String?,
        val locale: CwtLocalisationLocaleConfig
    )
}
