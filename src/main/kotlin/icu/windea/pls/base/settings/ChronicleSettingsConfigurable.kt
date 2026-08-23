package icu.windea.pls.base.settings

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.selected
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.base.ChronicleBaseBundle
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.toDelimitedMutableList
import icu.windea.pls.core.toDelimitedString
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.lang.ui.localeComboBox
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.policies.ParadoxDiffGroupingPolicy
import icu.windea.pls.model.policies.ParadoxHierarchyGroupingPolicy
import icu.windea.pls.model.policies.ParadoxLocalisationGenerationStrategy
import java.awt.event.ActionEvent

class ChronicleSettingsConfigurable : BoundConfigurable(ChronicleBaseBundle.message("settings")), SearchableConfigurable {
    private val callbackLock = CallbackLock()

    override fun getId() = "chronicle"

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            // general
            group(ChronicleBaseBundle.message("settings.general")) { configureGroupForGeneral() }
            // documentation
            collapsibleGroup(ChronicleBaseBundle.message("settings.documentation")) { configureGroupForDocumentation() }
            // completion
            collapsibleGroup(ChronicleBaseBundle.message("settings.completion")) { configureGroupForCompletion() }
            // folding
            collapsibleGroup(ChronicleBaseBundle.message("settings.folding")) { configureGroupForFolding() }
            // generation
            collapsibleGroup(ChronicleBaseBundle.message("settings.generation")) { configureGroupForGeneration() }
            // hierarchy
            collapsibleGroup(ChronicleBaseBundle.message("settings.hierarchy")) { configureGroupForHierarchy() }
            // diff
            collapsibleGroup(ChronicleBaseBundle.message("settings.diff")) { configureGroupForDiff() }
            // navigation
            collapsibleGroup(ChronicleBaseBundle.message("settings.navigation")) { configureGroupForNavigation() }
            // inference
            collapsibleGroup(ChronicleBaseBundle.message("settings.inference")) { configureGroupForInference() }
            // others
            collapsibleGroup(ChronicleBaseBundle.message("settings.others")) { configureGroupForOthers() }
        }
    }

    private fun Panel.configureGroupForGeneral() {
        val groupName = "general"
        val settings = ChronicleSettings.getInstance().state
        val gameTypes = ParadoxGameType.getAllSpecific()
        val locales = ParadoxLocaleManager.getGlobalLocales(includeAuto = true)

        // defaultGameType
        row {
            label(ChronicleBaseBundle.message("settings.general.defaultGameType")).widthGroup(groupName)
                .comment(ChronicleBaseBundle.message("settings.general.defaultGameType.comment"))
            var defaultGameType = settings.defaultGameType
            comboBox(gameTypes, textListCellRenderer { it?.title })
                .bindItem(settings::defaultGameType.toNullableProperty())
                .onApply {
                    val oldDefaultGameType = defaultGameType
                    val newDefaultGameType = settings.defaultGameType
                    if (oldDefaultGameType == newDefaultGameType) return@onApply
                    defaultGameType = newDefaultGameType
                    ChronicleSettingsManager.onDefaultGameTypeChanged(callbackLock, oldDefaultGameType, newDefaultGameType)
                }
        }
        // defaultGameDirectories
        row {
            label(ChronicleBaseBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                .comment(ChronicleBaseBundle.message("settings.general.defaultGameDirectories.comment"))
            val defaultGameDirectories = settings.defaultGameDirectories
            gameTypes.forEach { defaultGameDirectories.putIfAbsent(it.id, "") }
            val defaultList = defaultGameDirectories.toMutableEntryList()
            var list = defaultList.mapTo(mutableListOf()) { it.copy() }
            val action = { _: ActionEvent ->
                val dialog = DefaultGameDirectoriesDialog(list)
                if (dialog.showAndGet()) list = dialog.resultList
            }
            link(ChronicleBundle.message("link.configure"), action)
                .onApply {
                    val oldDefaultGameDirectories = defaultGameDirectories.toMutableMap()
                    val newDefaultGameDirectories = list.toMutableMap()
                    if (oldDefaultGameDirectories == newDefaultGameDirectories) return@onApply
                    settings.defaultGameDirectories = newDefaultGameDirectories
                    ChronicleSettingsManager.onDefaultGameDirectoriesChanged(callbackLock, oldDefaultGameDirectories, newDefaultGameDirectories)
                }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // preferredLocale
        row {
            label(ChronicleBaseBundle.message("settings.general.preferredLocale")).widthGroup(groupName)
                .comment(ChronicleBaseBundle.message("settings.general.preferredLocale.comment"))
            var preferredLocale = settings.preferredLocale
            localeComboBox(locales)
                .bindItem(settings::preferredLocale.toNullableProperty())
                .onApply {
                    val oldPreferredLocale = preferredLocale.orEmpty()
                    val newPreferredLocale = settings.preferredLocale.orEmpty()
                    if (oldPreferredLocale == newPreferredLocale) return@onApply
                    preferredLocale = newPreferredLocale
                    ChronicleSettingsManager.onPreferredLocaleChanged(callbackLock, oldPreferredLocale, newPreferredLocale)
                }
        }
        // ignoredFileNames
        row {
            label(ChronicleBaseBundle.message("settings.general.ignoredFileNames")).widthGroup(groupName)
                .comment(ChronicleBaseBundle.message("settings.general.ignoredFileNames.comment", MAX_LINE_LENGTH_WORD_WRAP))
            var ignoredFileNameSet = settings.ignoredFileNameSet
            expandableTextField({ it.toDelimitedMutableList() }, { it.toDelimitedString() })
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
                    // 设置中的被忽略文件名被更改时，需要重新解析相关文件
                    ChronicleSettingsManager.refreshForFilesByFileNames(callbackLock, fileNames, caseSensitive = false)
                }
        }
    }

    private fun Panel.configureGroupForDocumentation() {
        val settings = ChronicleSettings.getInstance().state.documentation

        // renderLineComment
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderLineComment"))
                .bindSelected(settings::renderLineComment)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderLineComment.tip"))
        }
        // renderRelatedLocalisationsForScriptedVariables
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                .bindSelected(settings::renderRelatedLocalisationsForScriptedVariables)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
        }
        // renderRelatedLocalisationsForDefinitions
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                .bindSelected(settings::renderRelatedLocalisationsForDefinitions)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
        }
        // renderRelatedImagesForDefinitions
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                .bindSelected(settings::renderRelatedImagesForDefinitions)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
        }
        // renderNameDescForModifiers
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderNameDescForModifiers"))
                .bindSelected(settings::renderNameDescForModifiers)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderIconForModifiers"))
                .bindSelected(settings::renderIconForModifiers)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderIconForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                .bindSelected(settings::renderLocalisationForLocalisations)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
        }
        // renderRelatedLocalisationsForComplexEnumValues
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                .bindSelected(settings::renderRelatedLocalisationsForComplexEnumValues)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
        }
        // renderRelatedLocalisationsForDynamicValues
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                .bindSelected(settings::renderRelatedLocalisationsForDynamicValues)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
        }
        // showScopes
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.showScopes"))
                .bindSelected(settings::showScopes)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.showScopes.tip"))
        }
        // showScopeContext
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.showScopeContext"))
                .bindSelected(settings::showScopeContext)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.showScopeContext.tip"))
        }
        // showParameters
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.showParameters"))
                .bindSelected(settings::showParameters)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.showParameters.tip"))
        }
        // showGeneratedModifiers
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.showGeneratedModifiers"))
                .bindSelected(settings::showGeneratedModifiers)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.showGeneratedModifiers.tip"))
        }
        // showOverrideStrategies
        row {
            checkBox(ChronicleBaseBundle.message("settings.documentation.showOverrideStrategy"))
                .bindSelected(settings::showOverrideStrategy)
            contextHelp(ChronicleBaseBundle.message("settings.documentation.showOverrideStrategy.tip"))
        }
    }

    private fun Panel.configureGroupForCompletion() {
        val settings = ChronicleSettings.getInstance().state.completion

        // completeScriptedVariableNames
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeScriptedVariableNames"))
                .bindSelected(settings::completeScriptedVariableNames)
        }
        // completeDefinitionNames
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeDefinitionNames"))
                .bindSelected(settings::completeDefinitionNames)
        }
        // completeLocalisationNames
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeLocalisationNames"))
                .bindSelected(settings::completeLocalisationNames)
        }
        // completeDefineNames
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeDefineNames"))
                .bindSelected(settings::completeDefineNames)
        }
        // completeVariableNames
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeVariableNames"))
                .bindSelected(settings::completeVariableNames)
        }
        // completeInlineScriptUsage
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeInlineScriptUsage"))
                .bindSelected(settings::completeInlineScriptUsages)
        }
        // completeDefinitionInjectionExpressions
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeDefinitionInjectionExpressions"))
                .bindSelected(settings::completeDefinitionInjectionExpressions)
        }
        // completeWithValue
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeWithValue"))
                .bindSelected(settings::completeWithValue)
            contextHelp(ChronicleBaseBundle.message("settings.completion.completeWithValue.tip"))
        }
        // completeWithClauseTemplate
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeWithClauseTemplate"))
                .bindSelected(settings::completeWithClauseTemplate)
            contextHelp(ChronicleBaseBundle.message("settings.completion.completeWithClauseTemplate.tip"))

            link(ChronicleBundle.message("link.configure")) {
                val dialog = ClauseTemplateSettingsDialog()
                dialog.show()
            }
        }
        // completeOnlyScopeIsMatched
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                .bindSelected(settings::completeOnlyScopeIsMatched)
            contextHelp(ChronicleBaseBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
        }
        // completeByPresentableName
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeByPresentableName"))
                .bindSelected(settings::completeByPresentableName)
            contextHelp(ChronicleBaseBundle.message("settings.completion.completeByPresentableName.tip"))
        }
        // completeByExtendedConfigs
        row {
            checkBox(ChronicleBaseBundle.message("settings.completion.completeByExtendedConfigs"))
                .bindSelected(settings::completeByExtendedConfigs)
            contextHelp(ChronicleBaseBundle.message("settings.completion.completeByExtendedConfigs.tip"))
        }
    }

    private fun Panel.configureGroupForFolding() {
        val settings = ChronicleSettings.getInstance().state.folding

        // comments & commentsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.comments"))
                .bindSelected(settings::comments)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::commentsByDefault)
                .enabledIf(cb.selected)
        }
        // conditionalBlocks & conditionalBlocksByDefault
        row {
            checkBox(ChronicleBaseBundle.message("settings.folding.conditionalBlocks"))
                .bindSelected(settings::conditionalBlocks)
                .enabled(false)
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::conditionalBlocksByDefault)
        }
        // inlineMaths & inlineMathsByDefault
        row {
            checkBox(ChronicleBaseBundle.message("settings.folding.inlineMaths"))
                .bindSelected(settings::inlineMaths)
                .enabled(false)
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::inlineMathsByDefault)
        }
        // localisationTexts & localisationTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationTexts"))
                .bindSelected(settings::localisationTexts)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationTextsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationParametersFully & localisationParametersFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationParametersFully"))
                .bindSelected(settings::localisationParametersFully)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationParametersFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationIconsFully & localisationIconsFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationIconsFully"))
                .bindSelected(settings::localisationIconsFully)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationIconsFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationCommands & localisationCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationCommands"))
                .bindSelected(settings::localisationCommands)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptCommands & localisationConceptCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationConceptCommands"))
                .bindSelected(settings::localisationConceptCommands)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptTexts & localisationConceptTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.localisationConceptTexts"))
                .bindSelected(settings::localisationConceptTexts)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptTextsByDefault)
                .enabledIf(cb.selected)
        }
        // scriptedVariableReferences & scriptedVariableReferencesByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.scriptedVariableReferences"))
                .bindSelected(settings::scriptedVariableReferences)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::scriptedVariableReferencesByDefault)
                .enabledIf(cb.selected)
        }
        // variableOperationExpressions & variableOperationExpressionsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.folding.variableOperationExpressions"))
                .bindSelected(settings::variableOperationExpressions)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::variableOperationExpressionsByDefault)
                .enabledIf(cb.selected)
        }
    }

    private fun Panel.configureGroupForGeneration() {
        val settings = ChronicleSettings.getInstance().state.generation
        val locales = ParadoxLocaleManager.getGlobalLocales(includeAuto = true)

        // localisationStrategy
        row {
            val property = AtomicProperty(settings.localisationStrategy)
            label(ChronicleBaseBundle.message("settings.generation.localisationStrategy"))
            comboBox(ParadoxLocalisationGenerationStrategy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::localisationStrategy.toNullableProperty())
                .bindItem(property)
            textField().bindText(settings::localisationStrategyText.toNonNullableProperty(""))
                .visibleIf(property.transform { it == ParadoxLocalisationGenerationStrategy.SpecificText })
            localeComboBox(locales).bindItem(settings::localisationStrategyLocale.toNullableProperty())
                .visibleIf(property.transform { it == ParadoxLocalisationGenerationStrategy.FromLocale })
        }
        // blankLineBetweenLocalisationGroups
        row {
            checkBox(ChronicleBaseBundle.message("settings.generation.blankLineBetweenLocalisationGroups"))
                .bindSelected(settings::blankLineBetweenLocalisationGroups)
        }
        // moveIntoLocalisationGroups
        row {
            checkBox(ChronicleBaseBundle.message("settings.generation.moveIntoLocalisationGroups"))
                .bindSelected(settings::moveIntoLocalisationGroups)
        }
    }

    private fun Panel.configureGroupForHierarchy() {
        val settings = ChronicleSettings.getInstance().state.hierarchy

        // showPresentableName
        row {
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showPresentableName"))
                .bindSelected(settings::showPresentableName)
        }
        // showLocationInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showLocationInfo"))
                .bindSelected(settings::showLocationInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showLocationInfoByPath"))
                .bindSelected(settings::showLocationInfoByPath)
                .enabledIf(cb.selected)
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showLocationInfoByRootInfo"))
                .bindSelected(settings::showLocationInfoByRootInfo)
                .enabledIf(cb.selected)
        }

        // showScriptedVariablesInCallHierarchy
        row {
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                .bindSelected(settings::showScriptedVariablesInCallHierarchy)
        }
        // showDefinitionsInCallHierarchy
        row {
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                .bindSelected(settings::showDefinitionsInCallHierarchy)

            val definitionTypeBindingsInCallHierarchy = settings.definitionTypeBindingsInCallHierarchy
            val defaultList = definitionTypeBindingsInCallHierarchy.toMutableEntryList()
            var list = defaultList.mapTo(mutableListOf()) { it.copy() }
            val action = { _: ActionEvent ->
                val dialog = DefinitionTypeBindingsInCallHierarchyDialog(list)
                if (dialog.showAndGet()) list = dialog.resultList
            }
            link(ChronicleBaseBundle.message("settings.hierarchy.definitionTypeBindings.link"), action)
                .onApply { settings.definitionTypeBindingsInCallHierarchy = list.toMutableMap() }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // showLocalisationsInCallHierarchy
        row {
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                .bindSelected(settings::showLocalisationsInCallHierarchy)
        }

        // showEventInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showEventInfo"))
                .bindSelected(settings::showEventInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showEventInfoByType"))
                .bindSelected(settings::showEventInfoByType)
                .enabledIf(cb.selected)
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showEventInfoByAttributes"))
                .bindSelected(settings::showEventInfoByAttributes)
                .enabledIf(cb.selected)
        }
        // showTechInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showTechInfo"))
                .bindSelected(settings::showTechInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showTechInfoByTier"))
                .bindSelected(settings::showTechInfoByTier)
                .enabledIf(cb.selected)
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showTechInfoByArea"))
                .bindSelected(settings::showTechInfoByArea)
                .enabledIf(cb.selected)
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showTechInfoByCategories"))
                .bindSelected(settings::showTechInfoByCategories)
                .enabledIf(cb.selected)
            checkBox(ChronicleBaseBundle.message("settings.hierarchy.showTechInfoByAttributes"))
                .bindSelected(settings::showTechInfoByAttributes)
                .enabledIf(cb.selected)
        }

        // eventTreeGrouping
        row {
            label(ChronicleBaseBundle.message("settings.hierarchy.eventTreeGrouping"))
            comboBox(ParadoxHierarchyGroupingPolicy.EventTree.entries, textListCellRenderer { it?.text })
                .bindItem(settings::eventTreeGrouping.toNullableProperty())
        }
        // techTreeGrouping
        row {
            label(ChronicleBaseBundle.message("settings.hierarchy.techTreeGrouping"))
            comboBox(ParadoxHierarchyGroupingPolicy.TechTree.entries, textListCellRenderer { it?.text })
                .bindItem(settings::techTreeGrouping.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForDiff() {
        val settings = ChronicleSettings.getInstance().state.diff

        // defaultDiffGroup
        row {
            label(ChronicleBaseBundle.message("settings.diff.defaultDiffGroup"))
            comboBox(ParadoxDiffGroupingPolicy.entries, textListCellRenderer { it?.text })
                .bindItem(settings::defaultDiffGroup.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForNavigation() {
        val settings = ChronicleSettings.getInstance().state.navigation

        // seForTargets
        row {
            label(ChronicleBaseBundle.message("settings.navigation.seForTargets"))
            contextHelp(ChronicleBaseBundle.message("settings.navigation.seForTargets.tip"))
        }
        indent {
            row {
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForScriptedVariables"))
                    .bindSelected(settings::seForScriptedVariables)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForDefinitions"))
                    .bindSelected(settings::seForDefinitions)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForLocalisations"))
                    .bindSelected(settings::seForLocalisations)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForSyncedLocalisations"))
                    .bindSelected(settings::seForSyncedLocalisations)
            }
        }
        indent {
            row {
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForTargetByText"))
                    .bindSelected(settings::seForTargetsByText)
                contextHelp(ChronicleBaseBundle.message("settings.navigation.seForTargetByText.tip"))
            }
        }

        // seForConfigs
        row {
            label(ChronicleBaseBundle.message("settings.navigation.seForConfigs"))
            contextHelp(ChronicleBaseBundle.message("settings.navigation.seForConfigs.tip"))
        }
        indent {
            row {
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForTypeConfigs"))
                    .bindSelected(settings::seForTypeConfigs)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForComplexEnumConfigs"))
                    .bindSelected(settings::seForComplexEnumConfigs)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForTriggerConfigs"))
                    .bindSelected(settings::seForTriggerConfigs)
                checkBox(ChronicleBaseBundle.message("settings.navigation.seForEffectConfigs"))
                    .bindSelected(settings::seForEffectConfigs)
            }
        }
    }

    private fun Panel.configureGroupForInference() {
        val settings = ChronicleSettings.getInstance().state.inference

        // injectionForParameterValue
        row {
            checkBox(ChronicleBaseBundle.message("settings.inference.injectionForParameterValue"))
                .bindSelected(settings::injectionForParameterValue)
                .onApply { ChronicleSettingsManager.refreshFiles(callbackLock) }
            contextHelp(ChronicleBaseBundle.message("settings.inference.injectionForParameterValue.tip"))
        }
        // injectionForLocalisationText
        row {
            checkBox(ChronicleBaseBundle.message("settings.inference.injectionForLocalisationText"))
                .bindSelected(settings::injectionForLocalisationText)
                .onApply { ChronicleSettingsManager.refreshFiles(callbackLock) }
            contextHelp(ChronicleBaseBundle.message("settings.inference.injectionForLocalisationText.tip"))
        }
        // configContextForParameters
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.inference.configContextForParameters"))
                .bindSelected(settings::configContextForParameters)
                .onApply { ChronicleSettingsManager.refreshForParameterInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(ChronicleBaseBundle.message("settings.inference.configContextForParameters.tip"))

            // configContextForParametersFast
            checkBox(ChronicleBaseBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForParametersFast)
                .onApply { ChronicleSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fast.tip"))

            // configContextForParametersFromUsages
            checkBox(ChronicleBaseBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForParametersFromUsages)
                .onApply { ChronicleSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fromUsages.tip"))

            // configContextForParametersFromConfig
            checkBox(ChronicleBaseBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForParametersFromConfig)
                .onApply { ChronicleSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fromConfig.tip"))
        }
        // configContextForInlineScripts
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBaseBundle.message("settings.inference.configContextForInlineScripts"))
                .bindSelected(settings::configContextForInlineScripts)
                .onApply { ChronicleSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(ChronicleBaseBundle.message("settings.inference.configContextForInlineScripts.tip"))

            // configContextForInlineScriptsFast
            checkBox(ChronicleBaseBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForInlineScriptsFast)
                .onApply { ChronicleSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fast.tip"))

            // configContextForInlineScriptsFromUsages
            checkBox(ChronicleBaseBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForInlineScriptsFromUsages)
                .onApply { ChronicleSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fromUsages.tip"))

            // configContextForInlineScriptsFromConfig
            checkBox(ChronicleBaseBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForInlineScriptsFromConfig)
                .onApply { ChronicleSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBaseBundle.message("settings.inference.fromConfig.tip"))
        }
        // scopeContext
        row {
            checkBox(ChronicleBaseBundle.message("settings.inference.scopeContext"))
                .bindSelected(settings::scopeContext)
                .onApply { ChronicleSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBaseBundle.message("settings.inference.scopeContext.tip"))
        }
        // scopeContextForEvents
        row {
            checkBox(ChronicleBaseBundle.message("settings.inference.scopeContextForEvents"))
                .bindSelected(settings::scopeContextForEvents)
                .onApply { ChronicleSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBaseBundle.message("settings.inference.scopeContextForEvents.tip"))
        }
        // scopeContextForOnActions
        row {
            checkBox(ChronicleBaseBundle.message("settings.inference.scopeContextForOnActions"))
                .bindSelected(settings::scopeContextForOnActions)
                .onApply { ChronicleSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBaseBundle.message("settings.inference.scopeContextForOnActions.tip"))
        }
    }

    private fun Panel.configureGroupForOthers() {
        val settings = ChronicleSettings.getInstance().state.others

        // showEditorContextToolbar
        row {
            checkBox(ChronicleBaseBundle.message("settings.others.showEditorContextToolbar"))
                .bindSelected(settings::showEditorContextToolbar)
        }
        // showLaunchGameActionInEditorContextToolbar
        row {
            checkBox(ChronicleBaseBundle.message("settings.others.showLaunchGameActionInEditorContextToolbar"))
                .bindSelected(settings::showLaunchGameActionInEditorContextToolbar)
        }
        // showLocalisationFloatingToolbar
        row {
            checkBox(ChronicleBaseBundle.message("settings.others.showLocalisationFloatingToolbar"))
                .bindSelected(settings::showLocalisationFloatingToolbar)
        }
        // highlightLocalisationColorId
        row {
            checkBox(ChronicleBaseBundle.message("settings.others.highlightLocalisationColorId"))
                .bindSelected(settings::highlightLocalisationColorId)
                .onApply { ChronicleSettingsManager.refreshFiles(callbackLock) }
        }
        // renderLocalisationColorfulText
        row {
            checkBox(ChronicleBaseBundle.message("settings.others.renderLocalisationColorfulText"))
                .bindSelected(settings::renderLocalisationColorfulText)
                .onApply { ChronicleSettingsManager.refreshFiles(callbackLock) }
        }
    }

    object Factory {
        fun Panel.configureForLocalisationGeneration(configGroup: CwtConfigGroup? = null) {
            val settings = ChronicleSettings.getInstance().state.generation
            val locales = if(configGroup != null) ParadoxLocaleManager.getSupportedLocales(configGroup, includeAuto = true)
            else ParadoxLocaleManager.getGlobalLocales(includeAuto = true)

            // localisationStrategy
            row {
                val property = AtomicProperty(settings.localisationStrategy)
                label(ChronicleBaseBundle.message("settings.generation.localisationStrategy"))
                comboBox(ParadoxLocalisationGenerationStrategy.entries, textListCellRenderer { it?.text })
                    .bindItem(settings::localisationStrategy.toNullableProperty())
                    .bindItem(property)
                textField().bindText(settings::localisationStrategyText.toNonNullableProperty(""))
                    .visibleIf(property.transform { it == ParadoxLocalisationGenerationStrategy.SpecificText })
                localeComboBox(locales).bindItem(settings::localisationStrategyLocale.toNullableProperty())
                    .visibleIf(property.transform { it == ParadoxLocalisationGenerationStrategy.FromLocale })
            }
            // blankLineBetweenLocalisationGroups
            row {
                checkBox(ChronicleBaseBundle.message("settings.generation.blankLineBetweenLocalisationGroups"))
                    .bindSelected(settings::blankLineBetweenLocalisationGroups)
            }
            // moveIntoLocalisationGroups
            row {
                checkBox(ChronicleBaseBundle.message("settings.generation.moveIntoLocalisationGroups"))
                    .bindSelected(settings::moveIntoLocalisationGroups)
            }
        }
    }
}
