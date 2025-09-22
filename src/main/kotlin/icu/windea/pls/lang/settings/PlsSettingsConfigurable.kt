package icu.windea.pls.lang.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.selected
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.listeners.ParadoxDefaultGameDirectoriesListener
import icu.windea.pls.lang.listeners.ParadoxDefaultGameTypeListener
import icu.windea.pls.lang.settings.PlsStrategies.DiffGroup
import icu.windea.pls.lang.settings.PlsStrategies.EventTreeGrouping
import icu.windea.pls.lang.settings.PlsStrategies.LocalisationGeneration
import icu.windea.pls.lang.settings.PlsStrategies.TechTreeGrouping
import icu.windea.pls.lang.ui.locale.localeComboBox
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import java.awt.event.ActionEvent

class PlsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"

    private val groupNameGeneral = "general"
    private val callbackLock = CallbackLock()

    @Suppress("AssignedValueIsNeverRead")
    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsFacade.getSettings()
        val gameTypes = ParadoxGameType.getAll()
            .filter { it != ParadoxGameType.Eu5 } // TODO hidden in plugin settings page until eu5 is released
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup(groupNameGeneral)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameType.tip") }
                    var defaultGameType = settings.defaultGameType
                    comboBox(gameTypes, textListCellRenderer { it?.title })
                        .bindItem(settings::defaultGameType.toNullableProperty())
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
                    gameTypes.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                    val defaultList = defaultGameDirectories.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = DefaultGameDirectoriesDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("configure"), action)
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
                val documentationSettings = settings.documentation

                //renderLineComment
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLineComment"))
                        .bindSelected(documentationSettings::renderLineComment)
                    contextHelp(PlsBundle.message("settings.documentation.renderLineComment.tip"))
                }
                //renderRelatedLocalisationsForScriptedVariables
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForScriptedVariables)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
                }
                //renderRelatedLocalisationsForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
                }
                //renderRelatedImagesForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                        .bindSelected(documentationSettings::renderRelatedImagesForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
                }
                //renderNameDescForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderNameDescForModifiers"))
                        .bindSelected(documentationSettings::renderNameDescForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderIconForModifiers"))
                        .bindSelected(documentationSettings::renderIconForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderIconForModifiers.tip"))
                }
                //renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                        .bindSelected(documentationSettings::renderLocalisationForLocalisations)
                    contextHelp(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
                }
                //renderRelatedLocalisationsForComplexEnumValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForComplexEnumValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
                }
                //renderRelatedLocalisationsForDynamicValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForDynamicValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
                }
                //showScopes
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopes"))
                        .bindSelected(documentationSettings::showScopes)
                    contextHelp(PlsBundle.message("settings.documentation.showScopes.tip"))
                }
                //showScopeContext
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopeContext"))
                        .bindSelected(documentationSettings::showScopeContext)
                    contextHelp(PlsBundle.message("settings.documentation.showScopeContext.tip"))
                }
                //showParameters
                row {
                    checkBox(PlsBundle.message("settings.documentation.showParameters"))
                        .bindSelected(documentationSettings::showParameters)
                    contextHelp(PlsBundle.message("settings.documentation.showParameters.tip"))
                }
                //showGeneratedModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.showGeneratedModifiers"))
                        .bindSelected(documentationSettings::showGeneratedModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.showGeneratedModifiers.tip"))
                }
            }
            //completion
            collapsibleGroup(PlsBundle.message("settings.completion")) {
                val completionSettings = settings.completion

                //completeScriptedVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeScriptedVariableNames"))
                        .bindSelected(completionSettings::completeScriptedVariableNames)
                }
                //completeDefinitionNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeDefinitionNames"))
                        .bindSelected(completionSettings::completeDefinitionNames)
                }
                //completeLocalisationNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeLocalisationNames"))
                        .bindSelected(completionSettings::completeLocalisationNames)
                }
                //completeInlineScriptInvocations
                row {
                    checkBox(PlsBundle.message("settings.completion.completeInlineScriptInvocations"))
                        .bindSelected(completionSettings::completeInlineScriptInvocations)
                }
                //completeVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeVariableNames"))
                        .bindSelected(completionSettings::completeVariableNames)
                }
                //completeWithValue
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithValue"))
                        .bindSelected(completionSettings::completeWithValue)
                    contextHelp(PlsBundle.message("settings.completion.completeWithValue.tip"))
                }
                //completeWithClauseTemplate
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                        .bindSelected(completionSettings::completeWithClauseTemplate)
                    contextHelp(PlsBundle.message("settings.completion.completeWithClauseTemplate.tip"))

                    link(PlsBundle.message("configure")) {
                        val dialog = ClauseTemplateSettingsDialog()
                        dialog.show()
                    }
                }
                //completeOnlyScopeIsMatched
                row {
                    checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                        .bindSelected(completionSettings::completeOnlyScopeIsMatched)
                    contextHelp(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
                }
                //completeByLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByLocalizedName"))
                        .bindSelected(completionSettings::completeByLocalizedName)
                    contextHelp(PlsBundle.message("settings.completion.completeByLocalizedName.tip"))
                }
                //completeByExtendedConfigs
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByExtendedConfigs"))
                        .bindSelected(completionSettings::completeByExtendedConfigs)
                    PlsBundle.message("settings.completion.completeByExtendedCwtConfigs.tip")
                }
            }
            //folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                val foldingSettings = settings.folding

                //comment & commentByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.comment"))
                        .bindSelected(foldingSettings::comment)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::commentByDefault)
                        .enabledIf(cb.selected)
                }
                //parameterConditionBlocks & parameterConditionBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"))
                        .bindSelected(foldingSettings::parameterConditionBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::parameterConditionBlocksByDefault)
                }
                //inlineMathBlocks & inlineMathBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"))
                        .bindSelected(foldingSettings::inlineMathBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::inlineMathBlocksByDefault)
                }
                //localisationReferencesFully & localisationReferencesFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationParametersFully"))
                        .bindSelected(foldingSettings::localisationParametersFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationParametersFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationIconsFully & localisationIconsFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(foldingSettings::localisationIconsFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationIconsFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationCommands & localisationCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(foldingSettings::localisationCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptCommands & localisationConceptCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptCommands"))
                        .bindSelected(foldingSettings::localisationConceptCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationConceptCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptTexts & localisationConceptTextsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(foldingSettings::localisationConceptTexts)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationConceptTextsByDefault)
                        .enabledIf(cb.selected)
                }
                //scriptedVariableReferences & scriptedVariableReferencesByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(foldingSettings::scriptedVariableReferences)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::scriptedVariableReferencesByDefault)
                        .enabledIf(cb.selected)
                }
                //variableOperationExpressions & variableOperationExpressionsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                        .bindSelected(foldingSettings::variableOperationExpressions)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::variableOperationExpressionsByDefault)
                        .enabledIf(cb.selected)
                }
            }
            //generation
            collapsibleGroup(PlsBundle.message("settings.generation")) {
                val generationSettings = settings.generation

                //fileNamePrefix
                row {
                    label(PlsBundle.message("settings.generation.fileNamePrefix"))
                    textField().bindText(generationSettings::fileNamePrefix.toNonNullableProperty(""))
                }.visible(false)
                //localisationStrategy
                buttonsGroup(PlsBundle.message("settings.generation.localisationStrategy")) {
                    row {
                        with(LocalisationGeneration.EmptyText) { radioButton(text, this) }
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        with(LocalisationGeneration.SpecificText) { radioButton(text, this) }.applyToComponent { rb = this }
                        textField().bindText(generationSettings::localisationStrategyText.toNonNullableProperty("")).enabledIf(rb.selected)
                    }
                    row {
                        lateinit var rb: JBRadioButton
                        with(LocalisationGeneration.FromLocale) { radioButton(text, this) }.applyToComponent { rb = this }
                        localeComboBox(withAuto = true).bindItem(generationSettings::localisationStrategyLocale.toNullableProperty()).enabledIf(rb.selected)
                    }
                }.bind(generationSettings::localisationStrategy)
            }
            //hierarchy
            collapsibleGroup(PlsBundle.message("settings.hierarchy")) {
                val hierarchySettings = settings.hierarchy

                //showLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalizedName"))
                        .bindSelected(hierarchySettings::showLocalizedName)
                }
                //showLocationInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfo"))
                        .bindSelected(hierarchySettings::showLocationInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByPath"))
                        .bindSelected(hierarchySettings::showLocationInfoByPath)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByRootInfo"))
                        .bindSelected(hierarchySettings::showLocationInfoByRootInfo)
                        .enabledIf(cb.selected)
                }

                //showScriptedVariablesInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                        .bindSelected(hierarchySettings::showScriptedVariablesInCallHierarchy)
                }
                //showDefinitionsInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                        .bindSelected(hierarchySettings::showDefinitionsInCallHierarchy)

                    val definitionTypeBindingsInCallHierarchy = hierarchySettings.definitionTypeBindingsInCallHierarchy
                    val defaultList = definitionTypeBindingsInCallHierarchy.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = DefinitionTypeBindingsInCallHierarchyDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("settings.hierarchy.definitionTypeBindings.link"), action)
                        .onApply { hierarchySettings.definitionTypeBindingsInCallHierarchy = list.toMutableMap() }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                //showLocalisationsInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                        .bindSelected(hierarchySettings::showLocalisationsInCallHierarchy)
                }

                //showEventInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfo"))
                        .bindSelected(hierarchySettings::showEventInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByType"))
                        .bindSelected(hierarchySettings::showEventInfoByType)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByAttributes"))
                        .bindSelected(hierarchySettings::showEventInfoByAttributes)
                        .enabledIf(cb.selected)
                }
                //showTechInfo
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfo"))
                        .bindSelected(hierarchySettings::showTechInfo)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByTier"))
                        .bindSelected(hierarchySettings::showTechInfoByTier)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByArea"))
                        .bindSelected(hierarchySettings::showTechInfoByArea)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByCategories"))
                        .bindSelected(hierarchySettings::showTechInfoByCategories)
                        .enabledIf(cb.selected)
                    checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByAttributes"))
                        .bindSelected(hierarchySettings::showTechInfoByAttributes)
                        .enabledIf(cb.selected)
                }

                //eventTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.eventTreeGrouping"))
                    comboBox(EventTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(hierarchySettings::eventTreeGrouping.toNullableProperty())
                }
                //techTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.techTreeGrouping"))
                    comboBox(TechTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(hierarchySettings::techTreeGrouping.toNullableProperty())
                }
            }
            //navigation
            collapsibleGroup(PlsBundle.message("settings.navigation")) {
                val navigationSettings = settings.navigation

                //seForSymbols
                row {
                    label(PlsBundle.message("settings.navigation.seForSymbols"))
                    contextHelp(PlsBundle.message("settings.navigation.seForSymbols.tip"))
                }
                indent {
                    row {
                        checkBox(PlsBundle.message("settings.navigation.seForScriptedVariables"))
                            .bindSelected(navigationSettings::seForScriptedVariables)
                        checkBox(PlsBundle.message("settings.navigation.seForDefinitions"))
                            .bindSelected(navigationSettings::seForDefinitions)
                        checkBox(PlsBundle.message("settings.navigation.seForLocalisations"))
                            .bindSelected(navigationSettings::seForLocalisations)
                        checkBox(PlsBundle.message("settings.navigation.seForSyncedLocalisations"))
                            .bindSelected(navigationSettings::seForSyncedLocalisations)
                    }
                }
                indent {
                    row {
                        checkBox(PlsBundle.message("settings.navigation.seForCwtTypeConfigs"))
                            .bindSelected(navigationSettings::seForCwtTypeConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForCwtComplexEnumConfigs"))
                            .bindSelected(navigationSettings::seForCwtComplexEnumConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForCwtTriggerConfigs"))
                            .bindSelected(navigationSettings::seForCwtTriggerConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForCwtEffectConfigs"))
                            .bindSelected(navigationSettings::seForCwtEffectConfigs)
                    }
                }
            }
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                val inferenceSettings = settings.inference

                //configContextForParameters & configContextForParametersFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(inferenceSettings::configContextForParameters)
                        .onApply { refreshForParameterInference() }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForParameters.tip"))

                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(inferenceSettings::configContextForParametersFast)
                        .onApply { refreshForParameterInference() }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.configContextFast.tip"))
                }
                //configContextForInlineScripts & configContextForInlineScriptsFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(inferenceSettings::configContextForInlineScripts)
                        .onApply { refreshForInlineScriptInference() }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForInlineScripts.tip"))

                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(inferenceSettings::configContextForInlineScriptsFast)
                        .onApply { refreshForInlineScriptInference() }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.configContextFast.tip"))
                }
                //scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(inferenceSettings::scopeContext)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContext.tip"))
                }
                //scopeContextForEvents
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                        .bindSelected(inferenceSettings::scopeContextForEvents)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForEvents.tip"))
                }
                //scopeContextForOnActions
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                        .bindSelected(inferenceSettings::scopeContextForOnActions)
                        .onApply { refreshForScopeContextInference() }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForOnActions.tip"))
                }
            }
            //others
            collapsibleGroup(PlsBundle.message("settings.others")) {
                val otherSettings = settings.others

                //showEditorContextToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showEditorContextToolbar"))
                        .bindSelected(otherSettings::showEditorContextToolbar)
                }
                //showLocalisationFloatingToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showLocalisationFloatingToolbar"))
                        .bindSelected(otherSettings::showLocalisationFloatingToolbar)
                }
                //highlightLocalisationColorId
                row {
                    checkBox(PlsBundle.message("settings.others.highlightLocalisationColorId"))
                        .bindSelected(otherSettings::highlightLocalisationColorId)
                        .onApply { refreshForOpenedFiles() }
                }
                //renderLocalisationColorfulText
                row {
                    checkBox(PlsBundle.message("settings.others.renderLocalisationColorfulText"))
                        .bindSelected(otherSettings::renderLocalisationColorfulText)
                        .onApply { refreshForOpenedFiles() }
                }
                //defaultDiffGroup
                row {
                    label(PlsBundle.message("settings.others.defaultDiffGroup"))
                    comboBox(DiffGroup.entries, textListCellRenderer { it?.text })
                        .bindItem(otherSettings::defaultDiffGroup.toNullableProperty())
                }
            }
        }
    }

    private fun onDefaultGameTypeChanged(oldDefaultGameType: ParadoxGameType, newDefaultGameType: ParadoxGameType) {
        if (!callbackLock.check("onDefaultGameTypeChanged")) return

        val messageBus = application.messageBus
        messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(oldDefaultGameType, newDefaultGameType)
    }

    private fun onDefaultGameDirectoriesChanged(oldDefaultGameDirectories: MutableMap<String, String>, newDefaultGameDirectories: MutableMap<String, String>) {
        if (!callbackLock.check("onDefaultGameDirectoriesChanged")) return

        val messageBus = application.messageBus
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
