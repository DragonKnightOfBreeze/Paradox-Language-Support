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
    private val callbackLock = CallbackLock()

    override fun getId() = "pls"

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            // general
            group(PlsBundle.message("settings.general")) { configureGroupForGeneral() }
            // documentation
            collapsibleGroup(PlsBundle.message("settings.documentation")) { configureGroupForDocumentation() }
            // completion
            collapsibleGroup(PlsBundle.message("settings.completion")) { configureGroupForCompletion() }
            // folding
            collapsibleGroup(PlsBundle.message("settings.folding")) { configureGroupForFolding() }
            // generation
            collapsibleGroup(PlsBundle.message("settings.generation")) { configureGroupForGeneration() }
            // hierarchy
            collapsibleGroup(PlsBundle.message("settings.hierarchy")) { configureGroupForHierarchy() }
            // diff
            collapsibleGroup(PlsBundle.message("settings.diff")) { configureGroupForDiff() }
            // navigation
            collapsibleGroup(PlsBundle.message("settings.navigation")) { configureGroupForNavigation() }
            // inference
            collapsibleGroup(PlsBundle.message("settings.inference")) { configureGroupForInference() }
            // others
            collapsibleGroup(PlsBundle.message("settings.others")) { configureGroupForOthers() }
        }
    }

    private fun Panel.configureGroupForGeneral() {
        val groupName = "general"
        val gameTypes = ParadoxGameType.getAll()

        // defaultGameType
        row {
            label(PlsBundle.message("settings.general.defaultGameType")).widthGroup(groupName)
                .comment(PlsBundle.message("settings.general.defaultGameType.comment"))
            var defaultGameType = PlsSettings.getInstance().state.defaultGameType
            comboBox(gameTypes, textListCellRenderer { it?.title })
                .bindItem(PlsSettings.getInstance().state::defaultGameType.toNullableProperty())
                .onApply {
                    val oldDefaultGameType = defaultGameType
                    val newDefaultGameType = PlsSettings.getInstance().state.defaultGameType
                    if (oldDefaultGameType == newDefaultGameType) return@onApply
                    defaultGameType = newDefaultGameType
                    PlsSettingsManager.onDefaultGameTypeChanged(callbackLock, oldDefaultGameType, newDefaultGameType)
                }
        }
        // defaultGameDirectories
        row {
            label(PlsBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                .comment(PlsBundle.message("settings.general.defaultGameDirectories.comment"))
            val defaultGameDirectories = PlsSettings.getInstance().state.defaultGameDirectories
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
                    PlsSettings.getInstance().state.defaultGameDirectories = newDefaultGameDirectories
                    PlsSettingsManager.onDefaultGameDirectoriesChanged(callbackLock, oldDefaultGameDirectories, newDefaultGameDirectories)
                }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // preferredLocale
        row {
            label(PlsBundle.message("settings.general.preferredLocale")).widthGroup(groupName)
                .comment(PlsBundle.message("settings.general.preferredLocale.comment"))
            var preferredLocale = PlsSettings.getInstance().state.preferredLocale
            localeComboBox(withAuto = true)
                .bindItem(PlsSettings.getInstance().state::preferredLocale.toNullableProperty())
                .onApply {
                    val oldPreferredLocale = preferredLocale.orEmpty()
                    val newPreferredLocale = PlsSettings.getInstance().state.preferredLocale.orEmpty()
                    if (oldPreferredLocale == newPreferredLocale) return@onApply
                    preferredLocale = newPreferredLocale
                    PlsSettingsManager.onPreferredLocaleChanged(callbackLock, oldPreferredLocale, newPreferredLocale)
                }
        }
        // ignoredFileNames
        row {
            label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup(groupName)
                .comment(PlsBundle.message("settings.general.ignoredFileNames.comment", MAX_LINE_LENGTH_WORD_WRAP))
            var ignoredFileNameSet = PlsSettings.getInstance().state.ignoredFileNameSet
            expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                .bindText(PlsSettings.getInstance().state::ignoredFileNames.toNonNullableProperty(""))
                .align(Align.FILL)
                .resizableColumn()
                .onApply {
                    val oldIgnoredFileNameSet = ignoredFileNameSet.toSet()
                    val newIgnoredFileNameSet = PlsSettings.getInstance().state.ignoredFileNameSet
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

    private fun Panel.configureGroupForDocumentation() {
        val settings = PlsSettings.getInstance().state.documentation

        // renderLineComment
        row {
            checkBox(PlsBundle.message("settings.documentation.renderLineComment"))
                .bindSelected(settings::renderLineComment)
            contextHelp(PlsBundle.message("settings.documentation.renderLineComment.tip"))
        }
        // renderRelatedLocalisationsForScriptedVariables
        row {
            checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                .bindSelected(settings::renderRelatedLocalisationsForScriptedVariables)
            contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
        }
        // renderRelatedLocalisationsForDefinitions
        row {
            checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                .bindSelected(settings::renderRelatedLocalisationsForDefinitions)
            contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
        }
        // renderRelatedImagesForDefinitions
        row {
            checkBox(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                .bindSelected(settings::renderRelatedImagesForDefinitions)
            contextHelp(PlsBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
        }
        // renderNameDescForModifiers
        row {
            checkBox(PlsBundle.message("settings.documentation.renderNameDescForModifiers"))
                .bindSelected(settings::renderNameDescForModifiers)
            contextHelp(PlsBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(PlsBundle.message("settings.documentation.renderIconForModifiers"))
                .bindSelected(settings::renderIconForModifiers)
            contextHelp(PlsBundle.message("settings.documentation.renderIconForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                .bindSelected(settings::renderLocalisationForLocalisations)
            contextHelp(PlsBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
        }
        // renderRelatedLocalisationsForComplexEnumValues
        row {
            checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                .bindSelected(settings::renderRelatedLocalisationsForComplexEnumValues)
            contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
        }
        // renderRelatedLocalisationsForDynamicValues
        row {
            checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                .bindSelected(settings::renderRelatedLocalisationsForDynamicValues)
            contextHelp(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
        }
        // showScopes
        row {
            checkBox(PlsBundle.message("settings.documentation.showScopes"))
                .bindSelected(settings::showScopes)
            contextHelp(PlsBundle.message("settings.documentation.showScopes.tip"))
        }
        // showScopeContext
        row {
            checkBox(PlsBundle.message("settings.documentation.showScopeContext"))
                .bindSelected(settings::showScopeContext)
            contextHelp(PlsBundle.message("settings.documentation.showScopeContext.tip"))
        }
        // showParameters
        row {
            checkBox(PlsBundle.message("settings.documentation.showParameters"))
                .bindSelected(settings::showParameters)
            contextHelp(PlsBundle.message("settings.documentation.showParameters.tip"))
        }
        // showGeneratedModifiers
        row {
            checkBox(PlsBundle.message("settings.documentation.showGeneratedModifiers"))
                .bindSelected(settings::showGeneratedModifiers)
            contextHelp(PlsBundle.message("settings.documentation.showGeneratedModifiers.tip"))
        }
        // showOverrideStrategies
        row {
            checkBox(PlsBundle.message("settings.documentation.showOverrideStrategy"))
                .bindSelected(settings::showOverrideStrategy)
            contextHelp(PlsBundle.message("settings.documentation.showOverrideStrategy.tip"))
        }
    }

    private fun Panel.configureGroupForCompletion() {
        val settings = PlsSettings.getInstance().state.completion

        // completeScriptedVariableNames
        row {
            checkBox(PlsBundle.message("settings.completion.completeScriptedVariableNames"))
                .bindSelected(settings::completeScriptedVariableNames)
        }
        // completeDefinitionNames
        row {
            checkBox(PlsBundle.message("settings.completion.completeDefinitionNames"))
                .bindSelected(settings::completeDefinitionNames)
        }
        // completeLocalisationNames
        row {
            checkBox(PlsBundle.message("settings.completion.completeLocalisationNames"))
                .bindSelected(settings::completeLocalisationNames)
        }
        // completeVariableNames
        row {
            checkBox(PlsBundle.message("settings.completion.completeVariableNames"))
                .bindSelected(settings::completeVariableNames)
        }
        // completeInlineScriptUsage
        row {
            checkBox(PlsBundle.message("settings.completion.completeInlineScriptUsage"))
                .bindSelected(settings::completeInlineScriptUsages)
        }
        // completeDefinitionInjectionExpressions
        row {
            checkBox(PlsBundle.message("settings.completion.completeDefinitionInjectionExpressions"))
                .bindSelected(settings::completeDefinitionInjectionExpressions)
        }
        // completeWithValue
        row {
            checkBox(PlsBundle.message("settings.completion.completeWithValue"))
                .bindSelected(settings::completeWithValue)
            contextHelp(PlsBundle.message("settings.completion.completeWithValue.tip"))
        }
        // completeWithClauseTemplate
        row {
            checkBox(PlsBundle.message("settings.completion.completeWithClauseTemplate"))
                .bindSelected(settings::completeWithClauseTemplate)
            contextHelp(PlsBundle.message("settings.completion.completeWithClauseTemplate.tip"))

            link(PlsBundle.message("link.configure")) {
                val dialog = ClauseTemplateSettingsDialog()
                dialog.show()
            }
        }
        // completeOnlyScopeIsMatched
        row {
            checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                .bindSelected(settings::completeOnlyScopeIsMatched)
            contextHelp(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
        }
        // completeByLocalizedName
        row {
            checkBox(PlsBundle.message("settings.completion.completeByLocalizedName"))
                .bindSelected(settings::completeByLocalizedName)
            contextHelp(PlsBundle.message("settings.completion.completeByLocalizedName.tip"))
        }
        // completeByExtendedConfigs
        row {
            checkBox(PlsBundle.message("settings.completion.completeByExtendedConfigs"))
                .bindSelected(settings::completeByExtendedConfigs)
            contextHelp(PlsBundle.message("settings.completion.completeByExtendedConfigs.tip"))
        }
    }

    private fun Panel.configureGroupForFolding() {
        val settings = PlsSettings.getInstance().state.folding

        // comments & commentsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.comments"))
                .bindSelected(settings::comments)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::commentsByDefault)
                .enabledIf(cb.selected)
        }
        // parameterConditions & parameterConditionsByDefault
        row {
            checkBox(PlsBundle.message("settings.folding.parameterConditions"))
                .bindSelected(settings::parameterConditions)
                .enabled(false)
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::parameterConditionsByDefault)
        }
        // inlineMaths & inlineMathsByDefault
        row {
            checkBox(PlsBundle.message("settings.folding.inlineMaths"))
                .bindSelected(settings::inlineMaths)
                .enabled(false)
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::inlineMathsByDefault)
        }
        // localisationTexts & localisationTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationTexts"))
                .bindSelected(settings::localisationTexts)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationTextsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationParametersFully & localisationParametersFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationParametersFully"))
                .bindSelected(settings::localisationParametersFully)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationParametersFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationIconsFully & localisationIconsFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                .bindSelected(settings::localisationIconsFully)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationIconsFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationCommands & localisationCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                .bindSelected(settings::localisationCommands)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptCommands & localisationConceptCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationConceptCommands"))
                .bindSelected(settings::localisationConceptCommands)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptTexts & localisationConceptTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                .bindSelected(settings::localisationConceptTexts)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptTextsByDefault)
                .enabledIf(cb.selected)
        }
        // scriptedVariableReferences & scriptedVariableReferencesByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                .bindSelected(settings::scriptedVariableReferences)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::scriptedVariableReferencesByDefault)
                .enabledIf(cb.selected)
        }
        // variableOperationExpressions & variableOperationExpressionsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                .bindSelected(settings::variableOperationExpressions)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::variableOperationExpressionsByDefault)
                .enabledIf(cb.selected)
        }
    }

    private fun Panel.configureGroupForGeneration() {
        val settings = PlsSettings.getInstance().state.generation

        // moveIntoLocalisationGroups
        row {
            checkBox(PlsBundle.message("settings.generation.moveIntoLocalisationGroups"))
                .bindSelected(settings::moveInfoLocalisationGroups)
        }
        // newLineBetweenLocalisationGroups
        row {
            checkBox(PlsBundle.message("settings.generation.newLineBetweenLocalisationGroups"))
                .bindSelected(settings::newLineBetweenLocalisationGroups)
        }
        // localisationStrategy
        row {
            val property = AtomicProperty(settings.localisationStrategy)
            label(PlsBundle.message("settings.generation.localisationStrategy"))
            comboBox(LocalisationGeneration.entries, textListCellRenderer { it?.text })
                .bindItem(settings::localisationStrategy.toNullableProperty())
                .bindItem(property)
            textField().bindText(settings::localisationStrategyText.toNonNullableProperty(""))
                .visibleIf(property.transform { it == LocalisationGeneration.SpecificText })
            localeComboBox(withAuto = true).bindItem(settings::localisationStrategyLocale.toNullableProperty())
                .visibleIf(property.transform { it == LocalisationGeneration.FromLocale })
        }
    }

    private fun Panel.configureGroupForHierarchy() {
        val settings = PlsSettings.getInstance().state.hierarchy

        // showLocalizedName
        row {
            checkBox(PlsBundle.message("settings.hierarchy.showLocalizedName"))
                .bindSelected(settings::showLocalizedName)
        }
        // showLocationInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.hierarchy.showLocationInfo"))
                .bindSelected(settings::showLocationInfo)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByPath"))
                .bindSelected(settings::showLocationInfoByPath)
                .enabledIf(cb.selected)
            checkBox(PlsBundle.message("settings.hierarchy.showLocationInfoByRootInfo"))
                .bindSelected(settings::showLocationInfoByRootInfo)
                .enabledIf(cb.selected)
        }

        // showScriptedVariablesInCallHierarchy
        row {
            checkBox(PlsBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                .bindSelected(settings::showScriptedVariablesInCallHierarchy)
        }
        // showDefinitionsInCallHierarchy
        row {
            checkBox(PlsBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                .bindSelected(settings::showDefinitionsInCallHierarchy)

            val definitionTypeBindingsInCallHierarchy = settings.definitionTypeBindingsInCallHierarchy
            val defaultList = definitionTypeBindingsInCallHierarchy.toMutableEntryList()
            var list = defaultList.mapTo(mutableListOf()) { it.copy() }
            val action = { _: ActionEvent ->
                val dialog = DefinitionTypeBindingsInCallHierarchyDialog(list)
                if (dialog.showAndGet()) list = dialog.resultList
            }
            link(PlsBundle.message("settings.hierarchy.definitionTypeBindings.link"), action)
                .onApply { settings.definitionTypeBindingsInCallHierarchy = list.toMutableMap() }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // showLocalisationsInCallHierarchy
        row {
            checkBox(PlsBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                .bindSelected(settings::showLocalisationsInCallHierarchy)
        }

        // showEventInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.hierarchy.showEventInfo"))
                .bindSelected(settings::showEventInfo)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByType"))
                .bindSelected(settings::showEventInfoByType)
                .enabledIf(cb.selected)
            checkBox(PlsBundle.message("settings.hierarchy.showEventInfoByAttributes"))
                .bindSelected(settings::showEventInfoByAttributes)
                .enabledIf(cb.selected)
        }
        // showTechInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.hierarchy.showTechInfo"))
                .bindSelected(settings::showTechInfo)
                .applyToComponent { cb = this }
            checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByTier"))
                .bindSelected(settings::showTechInfoByTier)
                .enabledIf(cb.selected)
            checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByArea"))
                .bindSelected(settings::showTechInfoByArea)
                .enabledIf(cb.selected)
            checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByCategories"))
                .bindSelected(settings::showTechInfoByCategories)
                .enabledIf(cb.selected)
            checkBox(PlsBundle.message("settings.hierarchy.showTechInfoByAttributes"))
                .bindSelected(settings::showTechInfoByAttributes)
                .enabledIf(cb.selected)
        }

        // eventTreeGrouping
        row {
            label(PlsBundle.message("settings.hierarchy.eventTreeGrouping"))
            comboBox(EventTreeGrouping.entries, textListCellRenderer { it?.text })
                .bindItem(settings::eventTreeGrouping.toNullableProperty())
        }
        // techTreeGrouping
        row {
            label(PlsBundle.message("settings.hierarchy.techTreeGrouping"))
            comboBox(TechTreeGrouping.entries, textListCellRenderer { it?.text })
                .bindItem(settings::techTreeGrouping.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForDiff() {
        val settings = PlsSettings.getInstance().state.diff

        // defaultDiffGroup
        row {
            label(PlsBundle.message("settings.diff.defaultDiffGroup"))
            comboBox(DiffGroup.entries, textListCellRenderer { it?.text })
                .bindItem(settings::defaultDiffGroup.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForNavigation() {
        val settings = PlsSettings.getInstance().state.navigation

        // seForTargets
        row {
            label(PlsBundle.message("settings.navigation.seForTargets"))
            contextHelp(PlsBundle.message("settings.navigation.seForTargets.tip"))
        }
        indent {
            row {
                checkBox(PlsBundle.message("settings.navigation.seForScriptedVariables"))
                    .bindSelected(settings::seForScriptedVariables)
                checkBox(PlsBundle.message("settings.navigation.seForDefinitions"))
                    .bindSelected(settings::seForDefinitions)
                checkBox(PlsBundle.message("settings.navigation.seForLocalisations"))
                    .bindSelected(settings::seForLocalisations)
                checkBox(PlsBundle.message("settings.navigation.seForSyncedLocalisations"))
                    .bindSelected(settings::seForSyncedLocalisations)
            }
        }
        indent {
            row {
                checkBox(PlsBundle.message("settings.navigation.seForTextBasedTargets"))
                    .bindSelected(settings::seForTextBasedTargets)
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
                    .bindSelected(settings::seForTypeConfigs)
                checkBox(PlsBundle.message("settings.navigation.seForComplexEnumConfigs"))
                    .bindSelected(settings::seForComplexEnumConfigs)
                checkBox(PlsBundle.message("settings.navigation.seForTriggerConfigs"))
                    .bindSelected(settings::seForTriggerConfigs)
                checkBox(PlsBundle.message("settings.navigation.seForEffectConfigs"))
                    .bindSelected(settings::seForEffectConfigs)
            }
        }
    }

    private fun Panel.configureGroupForInference() {
        val settings = PlsSettings.getInstance().state.inference

        // injectionForParameterValue
        row {
            checkBox(PlsBundle.message("settings.inference.injectionForParameterValue"))
                .bindSelected(settings::injectionForParameterValue)
                .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
            contextHelp(PlsBundle.message("settings.inference.injectionForParameterValue.tip"))
        }
        // injectionForLocalisationText
        row {
            checkBox(PlsBundle.message("settings.inference.injectionForLocalisationText"))
                .bindSelected(settings::injectionForLocalisationText)
                .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
            contextHelp(PlsBundle.message("settings.inference.injectionForLocalisationText.tip"))
        }
        // configContextForParameters
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                .bindSelected(settings::configContextForParameters)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(PlsBundle.message("settings.inference.configContextForParameters.tip"))

            // configContextForParametersFast
            checkBox(PlsBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForParametersFast)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fast.tip"))

            // configContextForParametersFromUsages
            checkBox(PlsBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForParametersFromUsages)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fromUsages.tip"))

            // configContextForParametersFromConfig
            checkBox(PlsBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForParametersFromConfig)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fromConfig.tip"))
        }
        // configContextForInlineScripts
        row {
            lateinit var cb: JBCheckBox
            checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                .bindSelected(settings::configContextForInlineScripts)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(PlsBundle.message("settings.inference.configContextForInlineScripts.tip"))

            // configContextForInlineScriptsFast
            checkBox(PlsBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForInlineScriptsFast)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fast.tip"))

            // configContextForInlineScriptsFromUsages
            checkBox(PlsBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForInlineScriptsFromUsages)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fromUsages.tip"))

            // configContextForInlineScriptsFromConfig
            checkBox(PlsBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForInlineScriptsFromConfig)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(PlsBundle.message("settings.inference.fromConfig.tip"))
        }
        // scopeContext
        row {
            checkBox(PlsBundle.message("settings.inference.scopeContext"))
                .bindSelected(settings::scopeContext)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(PlsBundle.message("settings.inference.scopeContext.tip"))
        }
        // scopeContextForEvents
        row {
            checkBox(PlsBundle.message("settings.inference.scopeContextForEvents"))
                .bindSelected(settings::scopeContextForEvents)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(PlsBundle.message("settings.inference.scopeContextForEvents.tip"))
        }
        // scopeContextForOnActions
        row {
            checkBox(PlsBundle.message("settings.inference.scopeContextForOnActions"))
                .bindSelected(settings::scopeContextForOnActions)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(PlsBundle.message("settings.inference.scopeContextForOnActions.tip"))
        }
    }

    private fun Panel.configureGroupForOthers() {
        val settings = PlsSettings.getInstance().state.others

        // showEditorContextToolbar
        row {
            checkBox(PlsBundle.message("settings.others.showEditorContextToolbar"))
                .bindSelected(settings::showEditorContextToolbar)
        }
        // showLaunchGameActionInEditorContextToolbar
        row {
            checkBox(PlsBundle.message("settings.others.showLaunchGameActionInEditorContextToolbar"))
                .bindSelected(settings::showLaunchGameActionInEditorContextToolbar)
        }
        // showLocalisationFloatingToolbar
        row {
            checkBox(PlsBundle.message("settings.others.showLocalisationFloatingToolbar"))
                .bindSelected(settings::showLocalisationFloatingToolbar)
        }
        // highlightLocalisationColorId
        row {
            checkBox(PlsBundle.message("settings.others.highlightLocalisationColorId"))
                .bindSelected(settings::highlightLocalisationColorId)
                .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
        }
        // renderLocalisationColorfulText
        row {
            checkBox(PlsBundle.message("settings.others.renderLocalisationColorfulText"))
                .bindSelected(settings::renderLocalisationColorfulText)
                .onApply { PlsSettingsManager.refreshForOpenedFiles(callbackLock) }
        }
    }
}
