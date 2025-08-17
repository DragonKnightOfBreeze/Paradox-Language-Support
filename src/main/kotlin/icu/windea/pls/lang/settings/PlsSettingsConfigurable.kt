package icu.windea.pls.lang.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.PlsStrategies.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import java.awt.event.*

class PlsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"

    private val groupNameGeneral = "general"
    private val callbackLock = CallbackLock()

    @Suppress("AssignedValueIsNeverRead")
    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsFacade.getSettings()
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup(groupNameGeneral)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameType.tip") }
                    var defaultGameType = settings.defaultGameType
                    comboBox(ParadoxGameType.entries).bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            val oldDefaultGameType = defaultGameType
                            val newDefaultGameType = settings.defaultGameType
                            if (oldDefaultGameType == newDefaultGameType) return@onApply
                            defaultGameType = newDefaultGameType
                            onDefaultGameTypeChanged(oldDefaultGameType, newDefaultGameType)
                        }
                }
                //defaultGameDirectories
                row {
                    label(PlsBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameDirectories.tip") }
                    val defaultGameDirectories = settings.defaultGameDirectories
                    ParadoxGameType.entries.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                    val defaultList = defaultGameDirectories.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = DefaultGameDirectoriesDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("settings.general.defaultGameDirectories.link"), action)
                        .onApply {
                            val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                            val newDefaultGameDirectories = list.toMutableMap()
                            if (oldDefaultGameDirectories == newDefaultGameDirectories) return@onApply
                            settings.defaultGameDirectories = newDefaultGameDirectories
                            onDefaultGameDirectoriesChanged(oldDefaultGameDirectories, newDefaultGameDirectories)
                        }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                //preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup(groupNameGeneral)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.preferredLocale.tip") }
                    var preferredLocale = settings.preferredLocale
                    localeComboBox(withAuto = true).bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            val oldPreferredLocale = preferredLocale
                            val newPreferredLocale = settings.preferredLocale
                            if (oldPreferredLocale == newPreferredLocale) return@onApply
                            preferredLocale = newPreferredLocale
                            refreshForOpenedFiles()
                        }
                }
                //ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup(groupNameGeneral)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.ignoredFileNames.tip") }
                    var ignoredFileNameSet = settings.ignoredFileNameSet
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText(settings::ignoredFileNames.toNonNullableProperty(""))
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment", MAX_LINE_LENGTH_WORD_WRAP))
                        .align(Align.FILL)
                        .resizableColumn()
                        .onApply {
                            val oldIgnoredFileNameSet = ignoredFileNameSet.toSet()
                            val newIgnoredFileNameSet = settings.ignoredFileNameSet
                            if (oldIgnoredFileNameSet == newIgnoredFileNameSet) return@onApply
                            ignoredFileNameSet = newIgnoredFileNameSet
                            val fileNames = mutableSetOf<String>()
                            fileNames += oldIgnoredFileNameSet
                            fileNames += newIgnoredFileNameSet
                            //设置中的被忽略文件名被更改时，需要重新解析相关文件（IDE之后会自动请求重新索引）
                            refreshForFilesByFileNames(fileNames)
                        }
                }
            }
            //documentation
            collapsibleGroup(PlsBundle.message("settings.documentation")) {
                //renderLineComment
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLineComment"))
                        .bindSelected(settings.documentation::renderLineComment)
                    contextHelp(PlsBundle.message("settings.documentation.renderLineComment.tip"))
                }
                //renderRelatedLocalisationsForScriptedVariables
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForScriptedVariables)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
                }
                //renderRelatedLocalisationsForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
                }
                //renderRelatedImagesForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                        .bindSelected(settings.documentation::renderRelatedImagesForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
                }
                //renderNameDescForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderNameDescForModifiers"))
                        .bindSelected(settings.documentation::renderNameDescForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderIconForModifiers"))
                        .bindSelected(settings.documentation::renderIconForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderIconForModifiers.tip"))
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                        .bindSelected(settings.documentation::renderLocalisationForLocalisations)
                    contextHelp(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
                }
                //renderRelatedLocalisationsForComplexEnumValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForComplexEnumValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
                }
                //renderRelatedLocalisationsForDynamicValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForDynamicValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
                }
                //showScopes
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopes"))
                        .bindSelected(settings.documentation::showScopes)
                    contextHelp(PlsBundle.message("settings.documentation.showScopes.tip"))
                }
                //showScopeContext
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopeContext"))
                        .bindSelected(settings.documentation::showScopeContext)
                    contextHelp(PlsBundle.message("settings.documentation.showScopeContext.tip"))
                }
                //showParameters
                row {
                    checkBox(PlsBundle.message("settings.documentation.showParameters"))
                        .bindSelected(settings.documentation::showParameters)
                    contextHelp(PlsBundle.message("settings.documentation.showParameters.tip"))
                }
                //showGeneratedModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.showGeneratedModifiers"))
                        .bindSelected(settings.documentation::showGeneratedModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.showGeneratedModifiers.tip"))
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
                    contextHelp(PlsBundle.message("settings.completion.completeWithValue.tip"))
                }
                //completeWithClauseTemplate
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                        .bindSelected(settings.completion::completeWithClauseTemplate)
                    contextHelp(PlsBundle.message("settings.completion.completeWithClauseTemplate.tip"))

                    link(PlsBundle.message("settings.completion.clauseTemplate.link")) {
                        val dialog = ClauseTemplateSettingsDialog()
                        dialog.show()
                    }
                }
                //completeOnlyScopeIsMatched
                row {
                    checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                        .bindSelected(settings.completion::completeOnlyScopeIsMatched)
                    contextHelp(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
                }
                //completeByLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByLocalizedName"))
                        .bindSelected(settings.completion::completeByLocalizedName)
                    contextHelp(PlsBundle.message("settings.completion.completeByLocalizedName.tip"))
                }
                //completeByExtendedConfigs
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByExtendedConfigs"))
                        .bindSelected(settings.completion::completeByExtendedConfigs)
                    PlsBundle.message("settings.completion.completeByExtendedCwtConfigs.tip")
                }
            }
            //folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                //comment & commentByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.comment"))
                        .bindSelected(PlsFacade.getSettings().folding::comment)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::commentByDefault)
                        .enabledIf(cb.selected)
                }
                //parameterConditionBlocks & parameterConditionBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"))
                        .bindSelected(PlsFacade.getSettings().folding::parameterConditionBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::parameterConditionBlocksByDefault)
                }
                //inlineMathBlocks & inlineMathBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"))
                        .bindSelected(PlsFacade.getSettings().folding::inlineMathBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::inlineMathBlocksByDefault)
                }
                //localisationReferencesFully & localisationReferencesFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationParametersFully"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationParametersFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationParametersFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationIconsFully & localisationIconsFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationIconsFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationIconsFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationCommands & localisationCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptCommands & localisationConceptCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptCommands"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationConceptCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationConceptCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptTexts & localisationConceptTextsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationConceptTexts)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::localisationConceptTextsByDefault)
                        .enabledIf(cb.selected)
                }
                //scriptedVariableReferences & scriptedVariableReferencesByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(PlsFacade.getSettings().folding::scriptedVariableReferences)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::scriptedVariableReferencesByDefault)
                        .enabledIf(cb.selected)
                }
                //variableOperationExpressions & variableOperationExpressionsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                        .bindSelected(PlsFacade.getSettings().folding::variableOperationExpressions)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(PlsFacade.getSettings().folding::variableOperationExpressionsByDefault)
                        .enabledIf(cb.selected)
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
                        with(LocalisationGeneration.EmptyText) { radioButton(text, this) }
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        with(LocalisationGeneration.SpecificText) { radioButton(text, this) }.applyToComponent { rb = this }
                        textField().bindText(settings.generation::localisationStrategyText.toNonNullableProperty("")).enabledIf(rb.selected)
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        with(LocalisationGeneration.FromLocale) { radioButton(text, this) }.applyToComponent { rb = this }
                        localeComboBox(withAuto = true).bindItem(settings.generation::localisationStrategyLocale.toNullableProperty()).enabledIf(rb.selected)
                    }
                }.bind(settings.generation::localisationStrategy)
            }
            //hierarchy
            collapsibleGroup(PlsBundle.message("settings.hierarchy")) {
                //showLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalizedName"))
                        .bindSelected(settings.hierarchy::showLocalizedName)
                }
                //showLocationInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfo"))
                        .bindSelected(settings.hierarchy::showLocationInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByPath"))
                        .bindSelected(settings.hierarchy::showLocationInfoByPath)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByRootInfo"))
                        .bindSelected(settings.hierarchy::showLocationInfoByRootInfo)
                        .enabledIf(cb.selected)
                }

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
                        val dialog = DefinitionTypeBindingsInCallHierarchyDialog(list)
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

                //showEventInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfo"))
                        .bindSelected(settings.hierarchy::showEventInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByType"))
                        .bindSelected(settings.hierarchy::showEventInfoByType)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByAttributes"))
                        .bindSelected(settings.hierarchy::showEventInfoByAttributes)
                        .enabledIf(cb.selected)
                }
                //showTechInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfo"))
                        .bindSelected(settings.hierarchy::showTechInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByTier"))
                        .bindSelected(settings.hierarchy::showTechInfoByTier)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByArea"))
                        .bindSelected(settings.hierarchy::showTechInfoByArea)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByCategories"))
                        .bindSelected(settings.hierarchy::showTechInfoByCategories)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByAttributes"))
                        .bindSelected(settings.hierarchy::showTechInfoByAttributes)
                        .enabledIf(cb.selected)
                }

                //eventTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.eventTreeGrouping"))
                    comboBox(EventTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(settings.hierarchy::eventTreeGrouping.toNullableProperty())
                }
                //techTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.techTreeGrouping"))
                    comboBox(TechTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(settings.hierarchy::techTreeGrouping.toNullableProperty())
                }
            }
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                //configContextForParameters & configContextForParametersFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(settings.inference::configContextForParameters)
                        .onApply { refreshForParameterInference() }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForParameters.tip"))

                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForParametersFast)
                        .onApply { refreshForParameterInference() }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.configContextFast.tip"))
                }
                //configContextForInlineScripts & configContextForInlineScriptsFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(settings.inference::configContextForInlineScripts)
                        .onApply { refreshForInlineScriptInference() }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForInlineScripts.tip"))

                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForInlineScriptsFast)
                        .onApply { refreshForInlineScriptInference() }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.configContextFast.tip"))
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(settings.inference::scopeContext)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContext.tip"))
                }
                //scopeContextForEvents
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                        .bindSelected(settings.inference::scopeContextForEvents)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForEvents.tip"))
                }
                //scopeContextForOnActions
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                        .bindSelected(settings.inference::scopeContextForOnActions)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForOnActions.tip"))
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
                row {
                    label(PlsBundle.message("settings.others.defaultDiffGroup"))
                    comboBox(DiffGroup.entries, textListCellRenderer { it?.text })
                        .bindItem(settings.others::defaultDiffGroup.toNullableProperty())
                }
            }
        }
    }

    private fun onDefaultGameTypeChanged(oldDefaultGameType: ParadoxGameType, newDefaultGameType: ParadoxGameType) {
        if (!callbackLock.check("onDefaultGameTypeChanged")) return

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(oldDefaultGameType, newDefaultGameType)
    }

    private fun onDefaultGameDirectoriesChanged(oldDefaultGameDirectories: MutableMap<String, String>, newDefaultGameDirectories: MutableMap<String, String>) {
        if (!callbackLock.check("onDefaultGameDirectoriesChanged")) return

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC).onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
    }

    //NOTE 如果应用更改时涉及多个相关字段，下面这些回调可能同一回调会被多次调用，不过目前看来问题不大

    private fun refreshForFilesByFileNames(fileNames: MutableSet<String>) {
        if (!callbackLock.check("refreshForFilesByFileNames")) return

        val files = PlsCoreManager.findFilesByFileNames(fileNames)
        PlsCoreManager.reparseFiles(files)
    }

    private fun refreshForOpenedFiles() {
        if (!callbackLock.check("refreshOnlyForOpenedFiles")) return

        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.refreshFiles(openedFiles)
    }

    private fun refreshForParameterInference() {
        if (!callbackLock.check("refreshForParameterInference")) return

        ParadoxModificationTrackers.ParameterConfigInferenceTracker.incModificationCount()
        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.reparseFiles(openedFiles)
    }

    private fun refreshForInlineScriptInference() {
        if (!callbackLock.check("refreshForInlineScriptInference")) return

        ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptConfigInferenceTracker.incModificationCount()
        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
        PlsCoreManager.reparseFiles(openedFiles)
    }

    private fun refreshForScopeContextInference() {
        if (!callbackLock.check("refreshForScopeContextInference")) return

        ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
        val openedFiles = PlsCoreManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsCoreManager.reparseFiles(openedFiles)
    }
}
