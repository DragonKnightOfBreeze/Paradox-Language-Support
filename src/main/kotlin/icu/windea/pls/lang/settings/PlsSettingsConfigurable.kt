package icu.windea.pls.lang.settings

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.selected
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.lang.settings.PlsSettingsStrategies.*
import icu.windea.pls.lang.ui.localeComboBox
import icu.windea.pls.model.ParadoxGameType
import java.awt.event.ActionEvent

class PlsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"

    private val groupNameGeneral = "general"
    private val callbackLock = CallbackLock()

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsSettings.getInstance().state
        val gameTypes = ParadoxGameType.getAll()
        return panel {
            // general
            group(PlsBundle.message("settings.general")) {
                // defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup(groupNameGeneral)
                        .comment(PlsBundle.message("settings.general.defaultGameType.comment"))
                    var defaultGameType = settings.defaultGameType
                    comboBox(gameTypes, textListCellRenderer { it?.title })
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            val oldDefaultGameType = defaultGameType
                            val newDefaultGameType = settings.defaultGameType
                            if (oldDefaultGameType == newDefaultGameType) return@onApply
                            defaultGameType = newDefaultGameType
                            PlsSettingsManager.onDefaultGameTypeChanged(callbackLock, oldDefaultGameType, newDefaultGameType)
                        }
                }
                // defaultGameDirectories
                row {
                    label(PlsBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                        .comment(PlsBundle.message("settings.general.defaultGameDirectories.comment"))
                    val defaultGameDirectories = settings.defaultGameDirectories
                    gameTypes.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
                    val defaultList = defaultGameDirectories.toMutableEntryList()
                    var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                    val action = { _: ActionEvent ->
                        val dialog = DefaultGameDirectoriesDialog(list)
                        if (dialog.showAndGet()) list = dialog.resultList
                    }
                    link(PlsBundle.message("link.configure"), action)
                        .onApply {
                            val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                            val newDefaultGameDirectories = list.toMutableMap()
                            if (oldDefaultGameDirectories == newDefaultGameDirectories) return@onApply
                            settings.defaultGameDirectories = newDefaultGameDirectories
                            PlsSettingsManager.onDefaultGameDirectoriesChanged(callbackLock, oldDefaultGameDirectories, newDefaultGameDirectories)
                        }
                        .onReset { list = defaultList }
                        .onIsModified { list != defaultList }
                }
                // preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup(groupNameGeneral)
                        .comment(PlsBundle.message("settings.general.preferredLocale.comment"))
                    var preferredLocale = settings.preferredLocale
                    localeComboBox(withAuto = true)
                        .bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            val oldPreferredLocale = preferredLocale.orEmpty()
                            val newPreferredLocale = settings.preferredLocale.orEmpty()
                            if (oldPreferredLocale == newPreferredLocale) return@onApply
                            preferredLocale = newPreferredLocale
                            PlsSettingsManager.onPreferredLocaleChanged(callbackLock, oldPreferredLocale, newPreferredLocale)
                        }
                }
                // ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup(groupNameGeneral)
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment", MAX_LINE_LENGTH_WORD_WRAP))
                    var ignoredFileNameSet = settings.ignoredFileNameSet
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText(settings::ignoredFileNames.toNonNullableProperty(""))
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
                            // 设置中的被忽略文件名被更改时，需要重新解析相关文件（IDE之后会自动请求重新索引）
                            PlsSettingsManager.refreshForFilesByFileNames(callbackLock, fileNames)
                        }
                }
            }
            // documentation
            collapsibleGroup(PlsBundle.message("settings.documentation")) {
                val documentationSettings = settings.documentation

                // renderLineComment
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLineComment"))
                        .bindSelected(documentationSettings::renderLineComment)
                    contextHelp(PlsBundle.message("settings.documentation.renderLineComment.tip"))
                }
                // renderRelatedLocalisationsForScriptedVariables
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForScriptedVariables)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
                }
                // renderRelatedLocalisationsForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
                }
                // renderRelatedImagesForDefinitions
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                        .bindSelected(documentationSettings::renderRelatedImagesForDefinitions)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
                }
                // renderNameDescForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderNameDescForModifiers"))
                        .bindSelected(documentationSettings::renderNameDescForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
                }
                // renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderIconForModifiers"))
                        .bindSelected(documentationSettings::renderIconForModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.renderIconForModifiers.tip"))
                }
                // renderLocalisationForLocalisations
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                        .bindSelected(documentationSettings::renderLocalisationForLocalisations)
                    contextHelp(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
                }
                // renderRelatedLocalisationsForComplexEnumValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForComplexEnumValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
                }
                // renderRelatedLocalisationsForDynamicValues
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                        .bindSelected(documentationSettings::renderRelatedLocalisationsForDynamicValues)
                    contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
                }
                // showScopes
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopes"))
                        .bindSelected(documentationSettings::showScopes)
                    contextHelp(PlsBundle.message("settings.documentation.showScopes.tip"))
                }
                // showScopeContext
                row {
                    checkBox(PlsBundle.message("settings.documentation.showScopeContext"))
                        .bindSelected(documentationSettings::showScopeContext)
                    contextHelp(PlsBundle.message("settings.documentation.showScopeContext.tip"))
                }
                // showParameters
                row {
                    checkBox(PlsBundle.message("settings.documentation.showParameters"))
                        .bindSelected(documentationSettings::showParameters)
                    contextHelp(PlsBundle.message("settings.documentation.showParameters.tip"))
                }
                // showGeneratedModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.showGeneratedModifiers"))
                        .bindSelected(documentationSettings::showGeneratedModifiers)
                    contextHelp(PlsBundle.message("settings.documentation.showGeneratedModifiers.tip"))
                }
                // showOverrideStrategies
                row {
                    checkBox(PlsBundle.message("settings.documentation.showOverrideStrategy"))
                        .bindSelected(documentationSettings::showOverrideStrategy)
                    contextHelp(PlsBundle.message("settings.documentation.showOverrideStrategy.tip"))
                }
            }
            // completion
            collapsibleGroup(PlsBundle.message("settings.completion")) {
                val completionSettings = settings.completion

                // completeScriptedVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeScriptedVariableNames"))
                        .bindSelected(completionSettings::completeScriptedVariableNames)
                }
                // completeDefinitionNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeDefinitionNames"))
                        .bindSelected(completionSettings::completeDefinitionNames)
                }
                // completeLocalisationNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeLocalisationNames"))
                        .bindSelected(completionSettings::completeLocalisationNames)
                }
                // completeVariableNames
                row {
                    checkBox(PlsBundle.message("settings.completion.completeVariableNames"))
                        .bindSelected(completionSettings::completeVariableNames)
                }
                // completeInlineScriptUsage
                row {
                    checkBox(PlsBundle.message("settings.completion.completeInlineScriptUsage"))
                        .bindSelected(completionSettings::completeInlineScriptUsages)
                }
                // completeDefinitionInjectionExpressions
                row {
                    checkBox(PlsBundle.message("settings.completion.completeDefinitionInjectionExpressions"))
                        .bindSelected(completionSettings::completeDefinitionInjectionExpressions)
                }
                // completeWithValue
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithValue"))
                        .bindSelected(completionSettings::completeWithValue)
                    contextHelp(PlsBundle.message("settings.completion.completeWithValue.tip"))
                }
                // completeWithClauseTemplate
                row {
                    checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                        .bindSelected(completionSettings::completeWithClauseTemplate)
                    contextHelp(PlsBundle.message("settings.completion.completeWithClauseTemplate.tip"))

                    link(PlsBundle.message("link.configure")) {
                        val dialog = ClauseTemplateSettingsDialog()
                        dialog.show()
                    }
                }
                // completeOnlyScopeIsMatched
                row {
                    checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                        .bindSelected(completionSettings::completeOnlyScopeIsMatched)
                    contextHelp(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
                }
                // completeByLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByLocalizedName"))
                        .bindSelected(completionSettings::completeByLocalizedName)
                    contextHelp(PlsBundle.message("settings.completion.completeByLocalizedName.tip"))
                }
                // completeByExtendedConfigs
                row {
                    checkBox(PlsBundle.message("settings.completion.completeByExtendedConfigs"))
                        .bindSelected(completionSettings::completeByExtendedConfigs)
                    contextHelp(PlsBundle.message("settings.completion.completeByExtendedConfigs.tip"))
                }
            }
            // folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                val foldingSettings = settings.folding

                // comments & commentsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.comments"))
                        .bindSelected(foldingSettings::comments)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::commentsByDefault)
                        .enabledIf(cb.selected)
                }
                // parameterConditionBlocks & parameterConditionBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"))
                        .bindSelected(foldingSettings::parameterConditionBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::parameterConditionBlocksByDefault)
                }
                // inlineMathBlocks & inlineMathBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"))
                        .bindSelected(foldingSettings::inlineMathBlocks)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::inlineMathBlocksByDefault)
                }
                // localisationTexts & localisationTextsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationTexts"))
                        .bindSelected(foldingSettings::localisationTexts)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationTextsByDefault)
                        .enabledIf(cb.selected)
                }
                // localisationParametersFully & localisationParametersFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationParametersFully"))
                        .bindSelected(foldingSettings::localisationParametersFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationParametersFullyByDefault)
                        .enabledIf(cb.selected)
                }
                // localisationIconsFully & localisationIconsFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(foldingSettings::localisationIconsFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationIconsFullyByDefault)
                        .enabledIf(cb.selected)
                }
                // localisationCommands & localisationCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(foldingSettings::localisationCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                // localisationConceptCommands & localisationConceptCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptCommands"))
                        .bindSelected(foldingSettings::localisationConceptCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationConceptCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                // localisationConceptTexts & localisationConceptTextsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(foldingSettings::localisationConceptTexts)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::localisationConceptTextsByDefault)
                        .enabledIf(cb.selected)
                }
                // scriptedVariableReferences & scriptedVariableReferencesByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(foldingSettings::scriptedVariableReferences)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(foldingSettings::scriptedVariableReferencesByDefault)
                        .enabledIf(cb.selected)
                }
                // variableOperationExpressions & variableOperationExpressionsByDefault
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
            // generation
            collapsibleGroup(PlsBundle.message("settings.generation")) {
                val generationSettings = settings.generation

                // localisationStrategy
                row {
                    val property = AtomicProperty(generationSettings.localisationStrategy)
                    label(PlsBundle.message("settings.generation.localisationStrategy"))
                    comboBox(LocalisationGeneration.entries, textListCellRenderer { it?.text })
                        .bindItem(generationSettings::localisationStrategy.toNullableProperty())
                        .bindItem(property)
                    textField().bindText(generationSettings::localisationStrategyText.toNonNullableProperty(""))
                        .enabledIf(property.transform { it == LocalisationGeneration.SpecificText })
                    localeComboBox(withAuto = true).bindItem(generationSettings::localisationStrategyLocale.toNullableProperty())
                        .enabledIf(property.transform { it == LocalisationGeneration.FromLocale })
                }
            }
            // hierarchy
            collapsibleGroup(PlsBundle.message("settings.hierarchy")) {
                val hierarchySettings = settings.hierarchy

                // showLocalizedName
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalizedName"))
                        .bindSelected(hierarchySettings::showLocalizedName)
                }
                // showLocationInfo
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

                // showScriptedVariablesInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                        .bindSelected(hierarchySettings::showScriptedVariablesInCallHierarchy)
                }
                // showDefinitionsInCallHierarchy
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
                // showLocalisationsInCallHierarchy
                row {
                    checkBox(PlsBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                        .bindSelected(hierarchySettings::showLocalisationsInCallHierarchy)
                }

                // showEventInfo
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
                // showTechInfo
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

                // eventTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.eventTreeGrouping"))
                    comboBox(EventTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(hierarchySettings::eventTreeGrouping.toNullableProperty())
                }
                // techTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.techTreeGrouping"))
                    comboBox(TechTreeGrouping.entries, textListCellRenderer { it?.text })
                        .bindItem(hierarchySettings::techTreeGrouping.toNullableProperty())
                }
            }
            // diff
            collapsibleGroup(PlsBundle.message("settings.diff")) {
                val diffSettings = settings.diff

                // defaultDiffGroup
                row {
                    label(PlsBundle.message("settings.diff.defaultDiffGroup"))
                    comboBox(DiffGroup.entries, textListCellRenderer { it?.text })
                        .bindItem(diffSettings::defaultDiffGroup.toNullableProperty())
                }
            }
            // navigation
            collapsibleGroup(PlsBundle.message("settings.navigation")) {
                val navigationSettings = settings.navigation

                // seForTargets
                row {
                    label(PlsBundle.message("settings.navigation.seForTargets"))
                    contextHelp(PlsBundle.message("settings.navigation.seForTargets.tip"))
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
                        checkBox(PlsBundle.message("settings.navigation.seForTextBasedTargets"))
                            .bindSelected(navigationSettings::seForTextBasedTargets)
                        contextHelp(PlsBundle.message("settings.navigation.seForTextBasedTargets.tip"))
                    }
                }

                // seForConfigs
                row {
                    label(PlsBundle.message("settings.navigation.seForConfigs"))
                    contextHelp(PlsBundle.message("settings.navigation.seForConfigs.tip"))
                }
                indent {
                    row {
                        checkBox(PlsBundle.message("settings.navigation.seForTypeConfigs"))
                            .bindSelected(navigationSettings::seForTypeConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForComplexEnumConfigs"))
                            .bindSelected(navigationSettings::seForComplexEnumConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForTriggerConfigs"))
                            .bindSelected(navigationSettings::seForTriggerConfigs)
                        checkBox(PlsBundle.message("settings.navigation.seForEffectConfigs"))
                            .bindSelected(navigationSettings::seForEffectConfigs)
                    }
                }
            }
            // inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                val inferenceSettings = settings.inference

                // injectionForParameterValue
                row {
                    checkBox(PlsBundle.message("settings.inference.injectionForParameterValue"))
                        .bindSelected(inferenceSettings::injectionForParameterValue)
                        .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
                    contextHelp(PlsBundle.message("settings.inference.injectionForParameterValue.tip"))
                }
                // injectionForLocalisationText
                row {
                    checkBox(PlsBundle.message("settings.inference.injectionForLocalisationText"))
                        .bindSelected(inferenceSettings::injectionForLocalisationText)
                        .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
                    contextHelp(PlsBundle.message("settings.inference.injectionForLocalisationText.tip"))
                }
                // configContextForParameters
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(inferenceSettings::configContextForParameters)
                        .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForParameters.tip"))

                    // configContextForParametersFast
                    checkBox(PlsBundle.message("settings.inference.fast"))
                        .bindSelected(inferenceSettings::configContextForParametersFast)
                        .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fast.tip"))

                    // configContextForParametersFromUsages
                    checkBox(PlsBundle.message("settings.inference.fromUsages"))
                        .bindSelected(inferenceSettings::configContextForParametersFromUsages)
                        .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fromUsages.tip"))

                    // configContextForParametersFromConfig
                    checkBox(PlsBundle.message("settings.inference.fromConfig"))
                        .bindSelected(inferenceSettings::configContextForParametersFromConfig)
                        .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fromConfig.tip"))
                }
                // configContextForInlineScripts
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(inferenceSettings::configContextForInlineScripts)
                        .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                        .applyToComponent { cb = this }
                    contextHelp(PlsBundle.message("settings.inference.configContextForInlineScripts.tip"))

                    // configContextForInlineScriptsFast
                    checkBox(PlsBundle.message("settings.inference.fast"))
                        .bindSelected(inferenceSettings::configContextForInlineScriptsFast)
                        .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fast.tip"))

                    // configContextForInlineScriptsFromUsages
                    checkBox(PlsBundle.message("settings.inference.fromUsages"))
                        .bindSelected(inferenceSettings::configContextForInlineScriptsFromUsages)
                        .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fromUsages.tip"))

                    // configContextForInlineScriptsFromConfig
                    checkBox(PlsBundle.message("settings.inference.fromConfig"))
                        .bindSelected(inferenceSettings::configContextForInlineScriptsFromConfig)
                        .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                        .enabledIf(cb.selected)
                    contextHelp(PlsBundle.message("settings.inference.fromConfig.tip"))
                }
                // scopeContext
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContext"))
                        .bindSelected(inferenceSettings::scopeContext)
                        .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
                    contextHelp(PlsBundle.message("settings.inference.scopeContext.tip"))
                }
                // scopeContextForEvents
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                        .bindSelected(inferenceSettings::scopeContextForEvents)
                        .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForEvents.tip"))
                }
                // scopeContextForOnActions
                row {
                    checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                        .bindSelected(inferenceSettings::scopeContextForOnActions)
                        .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
                    contextHelp(PlsBundle.message("settings.inference.scopeContextForOnActions.tip"))
                }
            }
            // others
            collapsibleGroup(PlsBundle.message("settings.others")) {
                val otherSettings = settings.others

                // showEditorContextToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showEditorContextToolbar"))
                        .bindSelected(otherSettings::showEditorContextToolbar)
                }
                // showLaunchGameActionInEditorContextToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showLaunchGameActionInEditorContextToolbar"))
                        .bindSelected(otherSettings::showLaunchGameActionInEditorContextToolbar)
                }
                // showLocalisationFloatingToolbar
                row {
                    checkBox(PlsBundle.message("settings.others.showLocalisationFloatingToolbar"))
                        .bindSelected(otherSettings::showLocalisationFloatingToolbar)
                }
                // highlightLocalisationColorId
                row {
                    checkBox(PlsBundle.message("settings.others.highlightLocalisationColorId"))
                        .bindSelected(otherSettings::highlightLocalisationColorId)
                        .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
                }
                // renderLocalisationColorfulText
                row {
                    checkBox(PlsBundle.message("settings.others.renderLocalisationColorfulText"))
                        .bindSelected(otherSettings::renderLocalisationColorfulText)
                        .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
                }
            }
        }
    }
}
