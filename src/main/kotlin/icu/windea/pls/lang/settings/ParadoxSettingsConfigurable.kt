package icu.windea.pls.lang.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.awt.event.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"
    
    @Suppress("DialogTitleCapitalization")
    override fun createPanel(): DialogPanel {
        val settings = getSettings()
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.defaultGameType.tooltip")
                        }
                    val oldDefaultGameType = settings.defaultGameType
                    comboBox(ParadoxGameType.valueList)
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            if(oldDefaultGameType != settings.defaultGameType) {
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC)
                                    .onChange(oldDefaultGameType, settings.defaultGameType)
                            }
                        }
                }
                //defaultGameDirectories
                row {
                    label(PlsBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.defaultGameDirectories.tooltip")
                        }
                    val oldDefaultGameDirectories = settings.defaultGameDirectories
                    ParadoxGameType.values.forEach { oldDefaultGameDirectories.putIfAbsent(it.id, "") }
                    val defaultList = oldDefaultGameDirectories.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxGameDirectoriesDialog(list)
                        if(dialog.showAndGet()) {
                            list = dialog.resultList
                        }
                    }
                    link(PlsBundle.message("settings.general.configureDefaultGameDirectories"), action)
                        .onApply {
                            settings.defaultGameDirectories = list.toMutableMap()
                            if(oldDefaultGameDirectories != settings.defaultGameDirectories) {
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC)
                                    .onChange(oldDefaultGameDirectories, settings.defaultGameDirectories)
                            }
                        }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                //preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.preferredLocale.tooltip")
                        }
                    val oldPreferredLocale = settings.preferredLocale
                    localeComboBox(settings)
                        .bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            if(oldPreferredLocale != settings.preferredLocale) {
                                ParadoxCoreHandler.refreshInlayHints()
                            }
                        }
                }
                //ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup("general")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.ignoredFileNames.tooltip")
                        }
                    val oldIgnoredFileNameSet = settings.ignoredFileNameSet
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText(settings::ignoredFileNames.toNonNullableProperty(""))
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment"))
                        .align(Align.FILL)
                        .resizableColumn()
                        .onApply {
                            if(oldIgnoredFileNameSet != settings.ignoredFileNameSet) {
                                val fileNames = mutableSetOf<String>()
                                fileNames += oldIgnoredFileNameSet
                                fileNames += settings.ignoredFileNameSet
                                //设置中的被忽略文件名被更改时，需要重新解析相关文件（IDE之后会自动请求重新索引）
                                runWriteAction { ParadoxCoreHandler.reparseFilesByFileNames(fileNames) }
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
                //renderNameDescForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderNameDescForModifiers"))
                        .bindSelected(settings.documentation::renderNameDescForModifiers)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderNameDescForModifiers.tooltip") }
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
                        label(PlsBundle.message("settings.completion.maxMemberCountInOneLine")).applyToComponent {
                            toolTipText = PlsBundle.message("settings.completion.maxMemberCountInOneLine.tooltip")
                        }
                        intTextField(0..10).bindIntText(settings.completion::maxMemberCountInOneLine)
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
                //fileNamePrefix
                row {
                    label(PlsBundle.message("settings.generation.fileNamePrefix"))
                    textField().bindText(settings.generation::fileNamePrefix.toNonNullableProperty(""))
                }.visible(false)
                //localisationStrategy
                buttonsGroup(PlsBundle.message("settings.generation.localisationStrategy")) {
                    row {
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.0"), LocalisationGenerationStrategy.EmptyText)
                    }
                    row {
                        lateinit var rbCell: Cell<JBRadioButton>
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.1"), LocalisationGenerationStrategy.SpecificText)
                            .apply { rbCell = this }
                        textField().bindText(settings.generation::localisationStrategyText.toNonNullableProperty(""))
                            .enabledIf(rbCell.selected)
                    }
                    row {
                        lateinit var rbCell: Cell<JBRadioButton>
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.2"), LocalisationGenerationStrategy.FromLocale)
                            .apply { rbCell = this }
                        localeComboBox(settings)
                            .bindItem(settings.generation::localisationStrategyLocale.toNullableProperty())
                            .enabledIf(rbCell.selected)
                    }
                }.bind(settings.generation::localisationStrategy)
            }
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                //argumentValue
                row {
                    checkBox(PlsBundle.message("settings.inference.argumentValueConfig"))
                        .bindSelected(settings.inference::parameterConfig)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.argumentValueConfig.tooltip") }
                        .onApply {
                            ParadoxModificationTrackerProvider.ParameterConfigInferenceTracker.incModificationCount()
                        }
                }
                //inlineScriptConfig
                row {
                    checkBox(PlsBundle.message("settings.inference.inlineScriptConfig"))
                        .bindSelected(settings.inference::inlineScriptConfig)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.inlineScriptConfig.tooltip") }
                        .onApply {
                            ParadoxModificationTrackerProvider.InlineScriptConfigInferenceTracker.incModificationCount()
                            ParadoxCoreHandler.refreshInlineScriptInlayHints()
                        }
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(settings.inference::scopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContext.tooltip") }
                        .onApply {
                            ParadoxModificationTrackerProvider.DefinitionScopeContextInferenceTracker.incModificationCount()
                        }
                }
                //eventScopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.eventScopeContext"))
                        .bindSelected(settings.inference::eventScopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.eventScopeContext.tooltip") }
                        .onApply {
                            ParadoxModificationTrackerProvider.DefinitionScopeContextInferenceTracker.incModificationCount()
                        }
                }
                //onActionScopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.onActionScopeContext"))
                        .bindSelected(settings.inference::onActionScopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.onActionScopeContext.tooltip") }
                        .onApply {
                            ParadoxModificationTrackerProvider.DefinitionScopeContextInferenceTracker.incModificationCount()
                        }
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
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxDefinitionTypeBindingsInCallHierarchyDialog(list)
                        if(dialog.showAndGet()) {
                            list = dialog.resultList
                        }
                    }
                    link(PlsBundle.message("settings.hierarchy.configureDefinitionTypeBindings"), action)
                        .enabledIf(cbCell.selected)
                        .onApply { settings.hierarchy.definitionTypeBindingsInCallHierarchy = list.toMutableMap() }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                //showLocalisationsInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                        .bindSelected(settings.hierarchy::showLocalisationsInCallHierarchy)
                }
            }
            //diff
            collapsibleGroup(PlsBundle.message("settings.diff")) {
                //defaultDiffGroup
                buttonsGroup(PlsBundle.message("settings.diff.defaultDiffGroup"), indent = false) {
                    row {
                        radioButton(PlsBundle.message("settings.diff.defaultDiffGroup.0"), DiffGroupStrategy.VsCopy)
                    }
                    row {
                        radioButton(PlsBundle.message("settings.diff.defaultDiffGroup.1"), DiffGroupStrategy.First)
                    }
                    row {
                        radioButton(PlsBundle.message("settings.diff.defaultDiffGroup.2"), DiffGroupStrategy.Last)
                    }
                }.bind(settings.diff::defaultDiffGroup)
            }
            //others
            collapsibleGroup(PlsBundle.message("settings.others")) {
                //showEditorContextToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showEditorContextToolbar"))
                        .bindSelected(settings.others::showEditorContextToolbar)
                }
                //showLocalisationFloatingToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showLocalisationFloatingToolbar"))
                        .bindSelected(settings.others::showLocalisationFloatingToolbar)
                }
            }
        }
    }
}

