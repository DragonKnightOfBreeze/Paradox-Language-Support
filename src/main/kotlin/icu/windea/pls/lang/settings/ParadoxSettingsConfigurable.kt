package icu.windea.pls.lang.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.editor.folding.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
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
                    comboBox(ParadoxGameType.entries)
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            val oldDefaultGameType = defaultGameType
                            val newDefaultGameType = settings.defaultGameType
                            if (oldDefaultGameType != newDefaultGameType) {
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
                    val defaultGameDirectories = settings.defaultGameDirectories
                    ParadoxGameType.entries.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                    val defaultList = defaultGameDirectories.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxGameDirectoriesDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("settings.general.defaultGameDirectories.link"), action)
                        .onApply {
                            val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                            val newDefaultGameDirectories = list.toMutableMap()
                            if (oldDefaultGameDirectories != newDefaultGameDirectories) {
                                settings.defaultGameDirectories = newDefaultGameDirectories
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC)
                                    .onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
                            }
                        }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
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
                            if (oldPreferredLocale != newPreferredLocale) {
                                preferredLocale = newPreferredLocale
                                refreshForOpenedFiles()
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
                            if (oldIgnoredFileNameSet != newIgnoredFileNameSet) {
                                ignoredFileNameSet = newIgnoredFileNameSet
                                val fileNames = mutableSetOf<String>()
                                fileNames += oldIgnoredFileNameSet
                                fileNames += newIgnoredFileNameSet
                                //设置中的被忽略文件名被更改时，需要重新解析相关文件（IDE之后会自动请求重新索引）
                                val files = PlsManager.findFilesByFileNames(fileNames)
                                PlsManager.reparseAndRefreshFiles(files)
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
                            if (oldLocalConfigDirectory != newLocalConfigDirectory) {
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
                //showGeneratedModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.showGeneratedModifiers"))
                        .bindSelected(settings.documentation::showGeneratedModifiers)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.showGeneratedModifiers.tooltip") }
                }
            }
            //folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                //commentEnabled
                lateinit var commentEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.commentEnabled"))
                        .bindSelected(foldingSettings::commentEnabled)
                        .applyToComponent { commentEnabledCb = this }
                }
                //comment
                row {
                    checkBox(PlsBundle.message("settings.folding.comment"))
                        .bindSelected(foldingSettings::comment)
                        .enabledIf(commentEnabledCb.selected)
                }
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
                //localisationReferencesFullyEnabled
                lateinit var localisationReferencesFullyEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationReferencesFullyEnabled"))
                        .bindSelected(foldingSettings::localisationReferencesFullyEnabled)
                        .applyToComponent { localisationReferencesFullyEnabledCb = this }
                }
                //localisationReferencesFully
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationReferencesFully"))
                        .bindSelected(foldingSettings::localisationReferencesFully)
                        .enabledIf(localisationReferencesFullyEnabledCb.selected)
                }
                //localisationIconsFullyEnabled
                lateinit var localisationIconsFullyEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFullyEnabled"))
                        .bindSelected(foldingSettings::localisationIconsFullyEnabled)
                        .applyToComponent { localisationIconsFullyEnabledCb = this }
                }
                //localisationIconsFully
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(foldingSettings::localisationIconsFully)
                        .enabledIf(localisationIconsFullyEnabledCb.selected)
                }
                //localisationCommandsEnabled
                lateinit var localisationCommandsEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationCommandsEnabled"))
                        .bindSelected(foldingSettings::localisationCommandsEnabled)
                        .applyToComponent { localisationCommandsEnabledCb = this }
                }
                //localisationCommands
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(foldingSettings::localisationCommands)
                        .enabledIf(localisationCommandsEnabledCb.selected)
                }
                //localisationConceptsEnabled
                lateinit var localisationConceptsEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConceptsEnabled"))
                        .bindSelected(foldingSettings::localisationConceptsEnabled)
                        .applyToComponent { localisationConceptsEnabledCb = this }
                }
                //localisationConcepts
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConcepts"))
                        .bindSelected(foldingSettings::localisationConcepts)
                        .enabledIf(localisationConceptsEnabledCb.selected)
                }
                //localisationConceptTextsEnabled
                lateinit var localisationConceptTextsEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTextsEnabled"))
                        .bindSelected(foldingSettings::localisationConceptTextsEnabled)
                        .applyToComponent { localisationConceptTextsEnabledCb = this }
                }
                //localisationConceptTexts
                row {
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(foldingSettings::localisationConceptTexts)
                        .enabledIf(localisationConceptTextsEnabledCb.selected)
                }
                //scriptedVariableReferencesEnabled
                lateinit var scriptedVariableReferencesEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferencesEnabled"))
                        .bindSelected(foldingSettings::scriptedVariableReferencesEnabled)
                        .applyToComponent { scriptedVariableReferencesEnabledCb = this }
                }
                //scriptedVariableReferences
                row {
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(foldingSettings::scriptedVariableReferences)
                        .enabledIf(scriptedVariableReferencesEnabledCb.selected)
                }
                //variableOperationExpressionsEnabled
                lateinit var variableOperationExpressionsEnabledCb: JBCheckBox
                row {
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressionsEnabled"))
                        .bindSelected(foldingSettings::variableOperationExpressionsEnabled)
                        .applyToComponent { variableOperationExpressionsEnabledCb = this }
                }
                //variableOperationExpressions
                row {
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                        .bindSelected(foldingSettings::variableOperationExpressions)
                        .enabledIf(variableOperationExpressionsEnabledCb.selected)
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
                //completeByExtendedConfigs
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByExtendedConfigs"))
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
                buttonsGroup(PlsBundle.message("settings.generation.localisationStrategy")) {
                    row {
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.0"), LocalisationGenerationStrategy.EmptyText)
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.1"), LocalisationGenerationStrategy.SpecificText).applyToComponent { rb = this }
                        textField().bindText(settings.generation::localisationStrategyText.toNonNullableProperty("")).enabledIf(rb.selected)
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        radioButton(PlsBundle.message("settings.generation.localisationStrategy.2"), LocalisationGenerationStrategy.FromLocale).applyToComponent { rb = this }
                        localeComboBox(addAuto = true).bindItem(settings.generation::localisationStrategyLocale.toNullableProperty()).enabledIf(rb.selected)
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

                    val definitionTypeBindingsInCallHierarchy = settings.hierarchy.definitionTypeBindingsInCallHierarchy
                    val defaultList = definitionTypeBindingsInCallHierarchy.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = ParadoxDefinitionTypeBindingsInCallHierarchyDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
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
                    lateinit var configContextForParametersCb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(settings.inference::configContextForParameters)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForParameters.tooltip") }
                        .onApply { refreshForParameterInference() }
                        .applyToComponent { configContextForParametersCb = this }

                    //configContextForParametersFast
                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForParametersFast)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextFast.tooltip") }
                        .onApply { refreshForParameterInference() }
                        .enabledIf(configContextForParametersCb.selected)
                }
                //configContextForInlineScripts
                row {
                    lateinit var configContextForInlineScriptsCb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(settings.inference::configContextForInlineScripts)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForInlineScripts.tooltip") }
                        .onApply { refreshForInlineScriptInference() }
                        .applyToComponent { configContextForInlineScriptsCb = this }

                    //configContextForInlineScriptsFast
                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForInlineScriptsFast)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextFast.tooltip") }
                        .onApply { refreshForInlineScriptInference() }
                        .enabledIf(configContextForInlineScriptsCb.selected)
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(settings.inference::scopeContext)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContext.tooltip") }
                        .onApply { refreshForScopeContextInference() }
                }
                //scopeContextForEvents
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                        .bindSelected(settings.inference::scopeContextForEvents)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContextForEvents.tooltip") }
                        .onApply { refreshForScopeContextInference() }
                }
                //scopeContextForOnActions
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                        .bindSelected(settings.inference::scopeContextForOnActions)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.scopeContextForOnActions.tooltip") }
                        .onApply { refreshForScopeContextInference() }
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
                //highlightLocalisationColorId
                row {
                    checkBox(PlsBundle.message("settings.others.highlightLocalisationColorId"))
                        .bindSelected(settings.others::highlightLocalisationColorId)
                        .onApply { refreshForOpenedFiles() }
                }
                //renderLocalisationColorfulText
                row {
                    checkBox(PlsBundle.message("settings.others.renderLocalisationColorfulText"))
                        .bindSelected(settings.others::renderLocalisationColorfulText)
                        .onApply { refreshForOpenedFiles() }
                }
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
            onApply {

            }
        }
    }

    //NOTE 如果应用更改时涉及多个相关字段，下面这些回调可能同一回调会被多次调用，不过目前看来问题不大

    private fun refreshForOpenedFiles() {
        val openedFiles = PlsManager.findOpenedFiles()
        PlsManager.reparseAndRefreshFiles(openedFiles, reparse = false)
    }

    private fun refreshForParameterInference() {
        ParadoxModificationTrackers.ParameterConfigInferenceTracker.incModificationCount()
    }

    private fun refreshForInlineScriptInference() {
        ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptConfigInferenceTracker.incModificationCount()
        val openedFiles = PlsManager.findOpenedFiles { file, _ -> ParadoxInlineScriptManager.getInlineScriptExpression(file) != null }
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }

    private fun refreshForScopeContextInference() {
        ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
        refreshForOpenedFiles()
    }
}

