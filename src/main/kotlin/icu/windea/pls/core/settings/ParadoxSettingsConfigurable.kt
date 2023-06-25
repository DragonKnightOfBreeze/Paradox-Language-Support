package icu.windea.pls.core.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"
    
    @Suppress("DialogTitleCapitalization")
    override fun createPanel(): DialogPanel {
        val settings = getSettings()
        val oldDefaultGameType = settings.defaultGameType
        val oldPreferredLocale = settings.preferredLocale
        val oldIgnoredFileNameSet = settings.ignoredFileNameSet
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.defaultGameType.tooltip")
                        }
                    val values = ParadoxGameType.valueList
                    comboBox(values)
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            if(oldDefaultGameType != settings.defaultGameType) {
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(settings.defaultGameType)
                            }
                        }
                }
                //preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.preferredLocale.tooltip")
                        }
                    localeComboBox(settings)
                        .bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            if(oldPreferredLocale != settings.preferredLocale) {
                                doRefreshInlayHints()
                            }
                        }
                }
                //ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.ignoredFileNames.tooltip")
                        }
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText(settings::ignoredFileNames.toNonNullableProperty(""))
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment"))
                        .align(Align.FILL)
                        .resizableColumn()
                        .onApply {
                            if(oldIgnoredFileNameSet != settings.ignoredFileNameSet) {
                                doReparseFilesByFileNames(settings.ignoredFileNameSet, oldIgnoredFileNameSet)
                            }
                        }
                }
            }
            //documentation
            collapsibleGroup(PlsBundle.message("settings.documentation")) {
                //renderLineComment
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLineComment"))
                        .bindSelected(settings.documentation::renderLineComment)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderLineComment.tooltip") }
                }
                //renderRelatedLocalisationsForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForDefinitions)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tooltip") }
                }
                //renderRelatedImagesForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                        .bindSelected(settings.documentation::renderRelatedImagesForDefinitions)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tooltip") }
                }
                //renderRelatedLocalisationsForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForModifiers"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForModifiers)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderRelatedLocalisationsForModifiers.tooltip") }
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderIconForModifiers"))
                        .bindSelected(settings.documentation::renderIconForModifiers)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderIconForModifiers.tooltip") }
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                        .bindSelected(settings.documentation::renderLocalisationForLocalisations)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderLocalisationForLocalisations.tooltip") }
                }
                //showScopes
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopes"))
                        .bindSelected(settings.documentation::showScopes)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.showScopes.tooltip") }
                }
                //showScopeContext
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopeContext"))
                        .bindSelected(settings.documentation::showScopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.showScopeContext.tooltip") }
                }
                //showParameters
                row {
                    checkBox(PlsBundle.message("settings.documentation.showParameters"))
                        .bindSelected(settings.documentation::showParameters)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.showParameters.tooltip") }
                }
            }
            //completion
            collapsibleGroup(PlsBundle.message("settings.completion")) {
                //completeScriptedVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeScriptedVariableNames"))
                        .bindSelected(settings.completion::completeScriptedVariableNames)
                }
                //completeDefinitionNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeDefinitionNames"))
                        .bindSelected(settings.completion::completeDefinitionNames)
                }
                //completeLocalisationNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeLocalisationNames"))
                        .bindSelected(settings.completion::completeLocalisationNames)
                }
                //completeVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeVariableNames"))
                        .bindSelected(settings.completion::completeVariableNames)
                }
                //completeWithValue
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithValue"))
                        .bindSelected(settings.completion::completeWithValue)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeWithValue.tooltip") }
                }
                //completeWithClauseTemplate
                lateinit var completeWithClauseTemplateCb: Cell<JBCheckBox>
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                        .bindSelected(settings.completion::completeWithClauseTemplate)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeWithClauseTemplate.tooltip") }
                        .also { completeWithClauseTemplateCb = it }
                }
                indent {
                    //maxExpressionCountInOneLine
                    row {
                        label(PlsBundle.message("settings.completion.maxExpressionCountInOneLine")).applyToComponent {
                            toolTipText = PlsBundle.message("settings.completion.maxExpressionCountInOneLine.tooltip")
                        }
                        intTextField(0..10).bindIntText(settings.completion::maxExpressionCountInOneLine)
                    }
                }.enabledIf(completeWithClauseTemplateCb.selected)
                //completeOnlyScopeIsMatched
                row {
                    checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                        .bindSelected(settings.completion::completeOnlyScopeIsMatched)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tooltip") }
                }
                //completeByLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByLocalizedName"))
                        .bindSelected(settings.completion::completeByLocalizedName)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeByLocalizedName.tooltip") }
                }
            }
            //generation
            collapsibleGroup(PlsBundle.message("settings.generation")) {
                @Suppress("DialogTitleCapitalization")
                buttonsGroup(PlsBundle.message("settings.generation.localisationTextGenerationStrategy")) {
                    row {
                        radioButton(PlsBundle.message("settings.generation.localisationTextGenerationStrategy.0"), LocalisationTextGenerationStrategy.EmptyText)
                    }
                    row {
                        lateinit var rbCell: Cell<JBRadioButton>
                        radioButton(PlsBundle.message("settings.generation.localisationTextGenerationStrategy.1"), LocalisationTextGenerationStrategy.SpecificText)
                            .apply { rbCell = this }
                        textField().bindText(settings.generation::localisationText.toNonNullableProperty(""))
                            .enabledIf(rbCell.selected)
                    }
                    row {
                        lateinit var rbCell: Cell<JBRadioButton>
                        radioButton(PlsBundle.message("settings.generation.localisationTextGenerationStrategy.2"), LocalisationTextGenerationStrategy.FromLocale)
                            .apply { rbCell = this }
                        localeComboBox(settings)
                            .bindItem(settings.generation::localisationTextLocale.toNullableProperty())
                            .enabledIf(rbCell.selected)
                    }
                }.bind(settings.generation::localisationTextGenerationStrategy)
                //fileNamePrefix
                row {
                    label(PlsBundle.message("settings.generation.fileNamePrefix"))
                    textField().bindText(settings.generation::fileNamePrefix.toNonNullableProperty(""))
                }.visible(false)
            }
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                //argumentValue
                row {
                    checkBox(PlsBundle.message("settings.inference.argumentValueConfig"))
                        .bindSelected(settings.inference::argumentValueConfig)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.argumentValueConfig.tooltip") }
                        //如果这个配置的值发生变化，IDE应该会自动刷新，不需要执行额外的回调（直到PSI变更前引用仍然可以被解析，但这并无大碍）
                }
                //inlineScriptConfig
                row {
                    checkBox(PlsBundle.message("settings.inference.inlineScriptConfig"))
                        .bindSelected(settings.inference::inlineScriptConfig)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.inlineScriptConfig.tooltip") }
                        .onApply { doRefreshInlineScripts() }
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(settings.inference::scopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContext.tooltip") }
                        .onApply { ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker.incModificationCount() }
                }
                //eventScopeContextFromOnAction
                row {
                    checkBox(PlsBundle.message("settings.inference.eventScopeContextFromOnAction"))
                        .bindSelected(settings.inference::eventScopeContextFromOnAction)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.eventScopeContextFromOnAction.tooltip") }
                        .onApply { ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker.incModificationCount() }
                }
                //eventScopeContextFromEffect
                row {
                    checkBox(PlsBundle.message("settings.inference.eventScopeContextFromEffect"))
                        .bindSelected(settings.inference::eventScopeContextFromEffect)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.eventScopeContextFromEffect.tooltip") }
                        .onApply { ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker.incModificationCount() }
                }
                //onActionScopeContextFromEffect
                row {
                    checkBox(PlsBundle.message("settings.inference.onActionScopeContextFromEffect"))
                        .bindSelected(settings.inference::onActionScopeContextFromEffect)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.onActionScopeContextFromEffect.tooltip") }
                        .onApply { ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker.incModificationCount() }
                }
            }
            //hierarchy
            collapsibleGroup(PlsBundle.message("settings.hierarchy")) {
                //showScriptedVariablesInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                        .bindSelected(settings.hierarchy::showScriptedVariablesInCallHierarchy)
                }
                //showDefinitionsInCallHierarchy
                row {
                    lateinit var cbCell: Cell<JBCheckBox>
                    checkBox(PlsBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                        .bindSelected(settings.hierarchy::showDefinitionsInCallHierarchy)
                        .apply { cbCell = this }
                    
                    val defaultList = settings.hierarchy.definitionTypeBindingsInCallHierarchy.toMutableEntryList()
                    var list = defaultList.toMutableList()
                    link(PlsBundle.message("settings.hierarchy.configureDefinitionTypeBindings")) {
                        val dialog = ParadoxConfigureDefinitionTypeBindingsInCallHierarchyDialog(list)
                        if(dialog.showAndGet()) {
                            list = dialog.resultList
                        }
                    }.enabledIf(cbCell.selected)
                        .onApply { list.let { settings.hierarchy.definitionTypeBindingsInCallHierarchy = it.toMutableMap() } }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                //showLocalisationsInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                        .bindSelected(settings.hierarchy::showLocalisationsInCallHierarchy)
                }
            }
            //others
            collapsibleGroup(PlsBundle.message("settings.others")) {
                //showEditorFloatingToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showEditorFloatingToolbar"))
                        .bindSelected(settings.others::showEditorFloatingToolbar)
                }
                @Suppress("DialogTitleCapitalization")
                //defaultDiffGroup
                buttonsGroup(PlsBundle.message("settings.others.defaultDiffGroup")) {
                    row {
                        radioButton(PlsBundle.message("settings.others.defaultDiffGroup.0"), DiffGroupStrategy.VsCopy)
                    }
                    row {
                        radioButton(PlsBundle.message("settings.others.defaultDiffGroup.1"), DiffGroupStrategy.First)
                    }
                    row {
                        radioButton(PlsBundle.message("settings.others.defaultDiffGroup.2"), DiffGroupStrategy.Last)
                    }
                }.bind(settings.others::defaultDiffGroup)
            }
        }
    }
    
    private fun Row.localeComboBox(settings: ParadoxSettingsState) =
        this.comboBox(settings.localeList, listCellRenderer { value ->
            if(value == "auto") {
                text = PlsBundle.message("locale.auto")
            } else {
                text = getCwtConfig().core.localisationLocales.getValue(value).description
            }
        })
    
    private fun doReparseFilesByFileNames(ignoredFileNameSet: Set<String>, oldIgnoredFileNameSet: Set<String>) {
        //设置中的被忽略文件名被更改时，需要重新解析相关文件
        val fileNames = mutableSetOf<String>()
        fileNames += oldIgnoredFileNameSet
        fileNames += ignoredFileNameSet
        runWriteAction { ParadoxCoreHandler.reparseFilesByFileNames(fileNames) }
    }
    
    private fun doRefreshInlayHints() {
        ParadoxCoreHandler.refreshInlayHints { file, _ ->
            val fileType = file.fileType
            fileType == ParadoxScriptFileType || fileType == ParadoxLocalisationFileType
        }
    }
    
    private fun doRefreshInlineScripts() {
        //要求重新解析内联脚本文件
        ProjectManager.getInstance().openProjects.forEach { project ->
            ParadoxPsiModificationTracker.getInstance(project).ScriptFileTracker.incModificationCount()
            ParadoxPsiModificationTracker.getInstance(project).InlineScriptsTracker.incModificationCount()
        }
        //刷新内联脚本文件的内嵌提示
        ParadoxCoreHandler.refreshInlayHints { file, _ ->
            ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
        }
    }
}
