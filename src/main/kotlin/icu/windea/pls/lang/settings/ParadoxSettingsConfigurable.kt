package icu.windea.pls.lang.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.ParadoxGameType.*
import java.awt.event.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"
    
    @Suppress("DialogTitleCapitalization")
    override fun createPanel(): DialogPanel {
        val settings = getSettings()
        val foldingSettings = ParadoxFoldingSettings.getInstance()
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameType.tooltip") }
                    var defaultGameType = settings.defaultGameType
                    comboBox(entries)
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            val oldDefaultGameType = defaultGameType
                            val newDefaultGameType = settings.defaultGameType
                            if(oldDefaultGameType != newDefaultGameType) {
                                defaultGameType = newDefaultGameType
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC)
                                    .onChange(oldDefaultGameType, newDefaultGameType)
                            }
                        }
                }
                //defaultGameDirectories
                row {
                    label(PlsBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameDirectories.tooltip") }
                    var defaultGameDirectories = settings.defaultGameDirectories
                    entries.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                    var list = defaultGameDirectories.toMutableEntryList().mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxGameDirectoriesDialog(list)
                        if(dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("settings.general.defaultGameDirectories.link"), action)
                        .onApply {
                            settings.defaultGameDirectories = list.toMutableMap()
                            val oldDefaultGameDirectories = defaultGameDirectories.toMap()
                            val newDefaultGameDirectories = settings.defaultGameDirectories
                            if(oldDefaultGameDirectories != newDefaultGameDirectories) {
                                defaultGameDirectories = newDefaultGameDirectories
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC)
                                    .onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
                            }
                        }
                        .onReset { list = defaultGameDirectories.toMutableEntryList() }
                        .onIsModified { list != defaultGameDirectories.toMutableEntryList() }
                }
                //preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.preferredLocale.tooltip") }
                    var preferredLocale = settings.preferredLocale
                    localeComboBox(addAuto = true)
                        .bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            val oldPreferredLocale = preferredLocale
                            val newPreferredLocale = settings.preferredLocale
                            if(oldPreferredLocale != newPreferredLocale) {
                                preferredLocale = newPreferredLocale
                                val openedFiles = ParadoxCoreManager.findOpenedFiles()
                                ParadoxCoreManager.reparseFiles(openedFiles, reparse = false)
                            }
                        }
                }
                //ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.ignoredFileNames.tooltip") }
                    var ignoredFileNameSet = settings.ignoredFileNameSet
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText(settings::ignoredFileNames.toNonNullableProperty(""))
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment"))
                        .align(Align.FILL)
                        .resizableColumn()
                        .onApply {
                            val oldIgnoredFileNameSet = ignoredFileNameSet.toSet()
                            val newIgnoredFileNameSet = settings.ignoredFileNameSet
                            if(oldIgnoredFileNameSet != newIgnoredFileNameSet) {
                                ignoredFileNameSet = newIgnoredFileNameSet
                                val fileNames = mutableSetOf<String>()
                                fileNames += oldIgnoredFileNameSet
                                fileNames += newIgnoredFileNameSet
                                //设置中的被忽略文件名被更改时，需要重新解析相关文件（IDE之后会自动请求重新索引）
                                val files = ParadoxCoreManager.findFilesByFileNames(fileNames)
                                ParadoxCoreManager.reparseFiles(files)
                            }
                        }
                }
                //localConfigDirectory
                row {
                    label(PlsBundle.message("settings.general.localConfigDirectory")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.localConfigDirectory.tooltip") }
                    val descriptor = ParadoxDirectoryDescriptor()
                        .withTitle(PlsBundle.message("settings.general.localConfigDirectory.title"))
                        .asBrowseFolderDescriptor()
                    var localConfigDirectory = settings.localConfigDirectory
                    textFieldWithBrowseButton(null, null, descriptor) { it.path }
                        .bindText(settings::localConfigDirectory.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                        .align(Align.FILL)
                        .onApply {
                            val oldLocalConfigDirectory = localConfigDirectory.orEmpty()
                            val newLocalConfigDirectory = settings.localConfigDirectory.orEmpty()
                            if(oldLocalConfigDirectory != newLocalConfigDirectory) {
                                localConfigDirectory = newLocalConfigDirectory
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxLocalConfigDirectoryListener.TOPIC)
                                    .onChange(oldLocalConfigDirectory, newLocalConfigDirectory)
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
            //folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                //parameterConditionBlocks
                row {
                    checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"))
                        .bindSelected(foldingSettings::parameterConditionBlocks)
                }
                //inlineMathBlocks
                row {
                    checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"))
                        .bindSelected(foldingSettings::inlineMathBlocks)
                }
                //localisationReferencesFully
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationReferencesFully"))
                        .bindSelected(foldingSettings::localisationReferencesFully)
                }
                //localisationIconsFully
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(foldingSettings::localisationIconsFully)
                }
                //localisationCommands
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(foldingSettings::localisationCommands)
                }
                //localisationConcepts
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConcepts"))
                        .bindSelected(foldingSettings::localisationConcepts)
                }
                //localisationConceptTexts
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(foldingSettings::localisationConceptTexts)
                }
                //scriptedVariableReferences
                row {
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(foldingSettings::parameterConditionBlocks)
                }
                //variableOperationExpressions
                row {
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                        .bindSelected(foldingSettings::variableOperationExpressions)
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
                //completeInlineScriptInvocations
                row {
                    checkBox(PlsBundle.message("settings.completion.completeInlineScriptInvocations"))
                        .bindSelected(settings.completion::completeInlineScriptInvocations)
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
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                        .bindSelected(settings.completion::completeWithClauseTemplate)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeWithClauseTemplate.tooltip") }
                    
                    link(PlsBundle.message("settings.completion.clauseTemplate.link")) {
                        val dialog = ParadoxClauseTemplateSettingsDialog()
                        dialog.show()
                    }
                }
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
                //completeByExtendedCwtConfigs
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByExtendedCwtConfigs"))
                        .bindSelected(settings.completion::completeByExtendedConfigs)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeByExtendedCwtConfigs.tooltip") }
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
                buttonsGroup(PlsBundle.message("settings.generation.localisationStrategy"), indent = false) {
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
                        localeComboBox(addAuto = true)
                            .bindItem(settings.generation::localisationStrategyLocale.toNullableProperty())
                            .enabledIf(rbCell.selected)
                    }
                }.bind(settings.generation::localisationStrategy)
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
                    checkBox(PlsBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                        .bindSelected(settings.hierarchy::showDefinitionsInCallHierarchy)
                    
                    val defaultList = settings.hierarchy.definitionTypeBindingsInCallHierarchy.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxDefinitionTypeBindingsInCallHierarchyDialog(list)
                        if(dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("settings.hierarchy.definitionTypeBindings.link"), action)
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
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                //configContextForParameters
                row {
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(settings.inference::configContextForParameters)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForParameters.tooltip") }
                        .onApply {
                            ParadoxModificationTrackers.ParameterConfigInferenceTracker.incModificationCount()
                        }
                }
                //configContextForInlineScripts
                row {
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(settings.inference::configContextForInlineScripts)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForInlineScripts.tooltip") }
                        .onApply {
                            ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
                            ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
                            ParadoxModificationTrackers.InlineScriptConfigInferenceTracker.incModificationCount()
                            val openedFiles = ParadoxCoreManager.findOpenedFiles { file, _ -> ParadoxInlineScriptManager.getInlineScriptExpression(file) != null }
                            ParadoxCoreManager.reparseFiles(openedFiles)
                        }
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(settings.inference::scopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContext.tooltip") }
                        .onApply {
                            ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
                            val openedFiles = ParadoxCoreManager.findOpenedFiles()
                            ParadoxCoreManager.reparseFiles(openedFiles, reparse = false)
                        }
                }
                //scopeContextForEvents
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                        .bindSelected(settings.inference::scopeContextForEvents)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContextForEvents.tooltip") }
                        .onApply {
                            ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
                            val openedFiles = ParadoxCoreManager.findOpenedFiles()
                            ParadoxCoreManager.reparseFiles(openedFiles, reparse = false)
                        }
                }
                //scopeContextForOnActions
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                        .bindSelected(settings.inference::scopeContextForOnActions)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContextForOnActions.tooltip") }
                        .onApply {
                            ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
                            val openedFiles = ParadoxCoreManager.findOpenedFiles()
                            ParadoxCoreManager.reparseFiles(openedFiles, reparse = false)
                        }
                }
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
                //defaultDiffGroup
                buttonsGroup(PlsBundle.message("settings.others.defaultDiffGroup"), indent = false) {
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
}

