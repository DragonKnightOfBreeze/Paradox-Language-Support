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
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.lang.settings.PlsSettingsStrategies.*
import icu.windea.pls.lang.ui.localeComboBox
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.ParadoxGameType
import java.awt.event.ActionEvent

class PlsSettingsConfigurable : BoundConfigurable(ChronicleBundle.message("settings")), SearchableConfigurable {
    private val callbackLock = CallbackLock()

    override fun getId() = "pls"

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            // general
            group(ChronicleBundle.message("settings.general")) { configureGroupForGeneral() }
            // documentation
            collapsibleGroup(ChronicleBundle.message("settings.documentation")) { configureGroupForDocumentation() }
            // completion
            collapsibleGroup(ChronicleBundle.message("settings.completion")) { configureGroupForCompletion() }
            // folding
            collapsibleGroup(ChronicleBundle.message("settings.folding")) { configureGroupForFolding() }
            // generation
            collapsibleGroup(ChronicleBundle.message("settings.generation")) { configureGroupForGeneration() }
            // hierarchy
            collapsibleGroup(ChronicleBundle.message("settings.hierarchy")) { configureGroupForHierarchy() }
            // diff
            collapsibleGroup(ChronicleBundle.message("settings.diff")) { configureGroupForDiff() }
            // navigation
            collapsibleGroup(ChronicleBundle.message("settings.navigation")) { configureGroupForNavigation() }
            // inference
            collapsibleGroup(ChronicleBundle.message("settings.inference")) { configureGroupForInference() }
            // others
            collapsibleGroup(ChronicleBundle.message("settings.others")) { configureGroupForOthers() }
        }
    }

    private fun Panel.configureGroupForGeneral() {
        val groupName = "general"
        val settings = PlsSettings.getInstance().state
        val gameTypes = ParadoxGameType.getAllSpecific()
        val locales = ParadoxLocaleManager.getGlobalLocales(includeAuto = true)

        // defaultGameType
        row {
            label(ChronicleBundle.message("settings.general.defaultGameType")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.general.defaultGameType.comment"))
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
            label(ChronicleBundle.message("settings.general.defaultGameDirectories")).widthGroup("general")
                .comment(ChronicleBundle.message("settings.general.defaultGameDirectories.comment"))
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
                    PlsSettingsManager.onDefaultGameDirectoriesChanged(callbackLock, oldDefaultGameDirectories, newDefaultGameDirectories)
                }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // preferredLocale
        row {
            label(ChronicleBundle.message("settings.general.preferredLocale")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.general.preferredLocale.comment"))
            var preferredLocale = settings.preferredLocale
            localeComboBox(locales)
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
            label(ChronicleBundle.message("settings.general.ignoredFileNames")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.general.ignoredFileNames.comment", MAX_LINE_LENGTH_WORD_WRAP))
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
                    // 设置中的被忽略文件名被更改时，需要重新解析相关文件
                    PlsSettingsManager.refreshForFilesByFileNames(callbackLock, fileNames)
                }
        }
    }

    private fun Panel.configureGroupForDocumentation() {
        val settings = PlsSettings.getInstance().state.documentation

        // renderLineComment
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderLineComment"))
                .bindSelected(settings::renderLineComment)
            contextHelp(ChronicleBundle.message("settings.documentation.renderLineComment.tip"))
        }
        // renderRelatedLocalisationsForScriptedVariables
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables"))
                .bindSelected(settings::renderRelatedLocalisationsForScriptedVariables)
            contextHelp(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForScriptedVariables.tip"))
        }
        // renderRelatedLocalisationsForDefinitions
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions"))
                .bindSelected(settings::renderRelatedLocalisationsForDefinitions)
            contextHelp(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForDefinitions.tip"))
        }
        // renderRelatedImagesForDefinitions
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderRelatedImagesForDefinitions"))
                .bindSelected(settings::renderRelatedImagesForDefinitions)
            contextHelp(ChronicleBundle.message("settings.documentation.renderRelatedImagesForDefinitions.tip"))
        }
        // renderNameDescForModifiers
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderNameDescForModifiers"))
                .bindSelected(settings::renderNameDescForModifiers)
            contextHelp(ChronicleBundle.message("settings.documentation.renderNameDescForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderIconForModifiers"))
                .bindSelected(settings::renderIconForModifiers)
            contextHelp(ChronicleBundle.message("settings.documentation.renderIconForModifiers.tip"))
        }
        // renderLocalisationForLocalisations
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderLocalisationForLocalisations"))
                .bindSelected(settings::renderLocalisationForLocalisations)
            contextHelp(ChronicleBundle.message("settings.documentation.renderLocalisationForLocalisations.tip"))
        }
        // renderRelatedLocalisationsForComplexEnumValues
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues"))
                .bindSelected(settings::renderRelatedLocalisationsForComplexEnumValues)
            contextHelp(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForComplexEnumValues.tip"))
        }
        // renderRelatedLocalisationsForDynamicValues
        row {
            checkBox(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues"))
                .bindSelected(settings::renderRelatedLocalisationsForDynamicValues)
            contextHelp(ChronicleBundle.message("settings.documentation.renderRelatedLocalisationsForDynamicValues.tip"))
        }
        // showScopes
        row {
            checkBox(ChronicleBundle.message("settings.documentation.showScopes"))
                .bindSelected(settings::showScopes)
            contextHelp(ChronicleBundle.message("settings.documentation.showScopes.tip"))
        }
        // showScopeContext
        row {
            checkBox(ChronicleBundle.message("settings.documentation.showScopeContext"))
                .bindSelected(settings::showScopeContext)
            contextHelp(ChronicleBundle.message("settings.documentation.showScopeContext.tip"))
        }
        // showParameters
        row {
            checkBox(ChronicleBundle.message("settings.documentation.showParameters"))
                .bindSelected(settings::showParameters)
            contextHelp(ChronicleBundle.message("settings.documentation.showParameters.tip"))
        }
        // showGeneratedModifiers
        row {
            checkBox(ChronicleBundle.message("settings.documentation.showGeneratedModifiers"))
                .bindSelected(settings::showGeneratedModifiers)
            contextHelp(ChronicleBundle.message("settings.documentation.showGeneratedModifiers.tip"))
        }
        // showOverrideStrategies
        row {
            checkBox(ChronicleBundle.message("settings.documentation.showOverrideStrategy"))
                .bindSelected(settings::showOverrideStrategy)
            contextHelp(ChronicleBundle.message("settings.documentation.showOverrideStrategy.tip"))
        }
    }

    private fun Panel.configureGroupForCompletion() {
        val settings = PlsSettings.getInstance().state.completion

        // completeScriptedVariableNames
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeScriptedVariableNames"))
                .bindSelected(settings::completeScriptedVariableNames)
        }
        // completeDefinitionNames
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeDefinitionNames"))
                .bindSelected(settings::completeDefinitionNames)
        }
        // completeLocalisationNames
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeLocalisationNames"))
                .bindSelected(settings::completeLocalisationNames)
        }
        // completeDefineNames
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeDefineNames"))
                .bindSelected(settings::completeDefineNames)
        }
        // completeVariableNames
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeVariableNames"))
                .bindSelected(settings::completeVariableNames)
        }
        // completeInlineScriptUsage
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeInlineScriptUsage"))
                .bindSelected(settings::completeInlineScriptUsages)
        }
        // completeDefinitionInjectionExpressions
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeDefinitionInjectionExpressions"))
                .bindSelected(settings::completeDefinitionInjectionExpressions)
        }
        // completeWithValue
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeWithValue"))
                .bindSelected(settings::completeWithValue)
            contextHelp(ChronicleBundle.message("settings.completion.completeWithValue.tip"))
        }
        // completeWithClauseTemplate
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeWithClauseTemplate"))
                .bindSelected(settings::completeWithClauseTemplate)
            contextHelp(ChronicleBundle.message("settings.completion.completeWithClauseTemplate.tip"))

            link(ChronicleBundle.message("link.configure")) {
                val dialog = ClauseTemplateSettingsDialog()
                dialog.show()
            }
        }
        // completeOnlyScopeIsMatched
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                .bindSelected(settings::completeOnlyScopeIsMatched)
            contextHelp(ChronicleBundle.message("settings.completion.completeOnlyScopeIsMatched.tip"))
        }
        // completeByLocalizedName
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeByLocalizedName"))
                .bindSelected(settings::completeByLocalizedName)
            contextHelp(ChronicleBundle.message("settings.completion.completeByLocalizedName.tip"))
        }
        // completeByExtendedConfigs
        row {
            checkBox(ChronicleBundle.message("settings.completion.completeByExtendedConfigs"))
                .bindSelected(settings::completeByExtendedConfigs)
            contextHelp(ChronicleBundle.message("settings.completion.completeByExtendedConfigs.tip"))
        }
    }

    private fun Panel.configureGroupForFolding() {
        val settings = PlsSettings.getInstance().state.folding

        // comments & commentsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.comments"))
                .bindSelected(settings::comments)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::commentsByDefault)
                .enabledIf(cb.selected)
        }
        // conditionalBlocks & conditionalBlocksByDefault
        row {
            checkBox(ChronicleBundle.message("settings.folding.conditionalBlocks"))
                .bindSelected(settings::conditionalBlocks)
                .enabled(false)
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::conditionalBlocksByDefault)
        }
        // inlineMaths & inlineMathsByDefault
        row {
            checkBox(ChronicleBundle.message("settings.folding.inlineMaths"))
                .bindSelected(settings::inlineMaths)
                .enabled(false)
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::inlineMathsByDefault)
        }
        // localisationTexts & localisationTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationTexts"))
                .bindSelected(settings::localisationTexts)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationTextsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationParametersFully & localisationParametersFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationParametersFully"))
                .bindSelected(settings::localisationParametersFully)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationParametersFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationIconsFully & localisationIconsFullyByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationIconsFully"))
                .bindSelected(settings::localisationIconsFully)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationIconsFullyByDefault)
                .enabledIf(cb.selected)
        }
        // localisationCommands & localisationCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationCommands"))
                .bindSelected(settings::localisationCommands)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptCommands & localisationConceptCommandsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationConceptCommands"))
                .bindSelected(settings::localisationConceptCommands)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptCommandsByDefault)
                .enabledIf(cb.selected)
        }
        // localisationConceptTexts & localisationConceptTextsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.localisationConceptTexts"))
                .bindSelected(settings::localisationConceptTexts)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::localisationConceptTextsByDefault)
                .enabledIf(cb.selected)
        }
        // scriptedVariableReferences & scriptedVariableReferencesByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.scriptedVariableReferences"))
                .bindSelected(settings::scriptedVariableReferences)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::scriptedVariableReferencesByDefault)
                .enabledIf(cb.selected)
        }
        // variableOperationExpressions & variableOperationExpressionsByDefault
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.folding.variableOperationExpressions"))
                .bindSelected(settings::variableOperationExpressions)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.folding.byDefault"))
                .bindSelected(settings::variableOperationExpressionsByDefault)
                .enabledIf(cb.selected)
        }
    }

    private fun Panel.configureGroupForGeneration() {
        val settings = PlsSettings.getInstance().state.generation
        val locales = ParadoxLocaleManager.getGlobalLocales(includeAuto = true)

        // localisationStrategy
        row {
            val property = AtomicProperty(settings.localisationStrategy)
            label(ChronicleBundle.message("settings.generation.localisationStrategy"))
            comboBox(LocalisationGeneration.entries, textListCellRenderer { it?.text })
                .bindItem(settings::localisationStrategy.toNullableProperty())
                .bindItem(property)
            textField().bindText(settings::localisationStrategyText.toNonNullableProperty(""))
                .visibleIf(property.transform { it == LocalisationGeneration.SpecificText })
            localeComboBox(locales).bindItem(settings::localisationStrategyLocale.toNullableProperty())
                .visibleIf(property.transform { it == LocalisationGeneration.FromLocale })
        }
        // blankLineBetweenLocalisationGroups
        row {
            checkBox(ChronicleBundle.message("settings.generation.blankLineBetweenLocalisationGroups"))
                .bindSelected(settings::blankLineBetweenLocalisationGroups)
        }
        // moveIntoLocalisationGroups
        row {
            checkBox(ChronicleBundle.message("settings.generation.moveIntoLocalisationGroups"))
                .bindSelected(settings::moveIntoLocalisationGroups)
        }
    }

    private fun Panel.configureGroupForHierarchy() {
        val settings = PlsSettings.getInstance().state.hierarchy

        // showLocalizedName
        row {
            checkBox(ChronicleBundle.message("settings.hierarchy.showLocalizedName"))
                .bindSelected(settings::showLocalizedName)
        }
        // showLocationInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.hierarchy.showLocationInfo"))
                .bindSelected(settings::showLocationInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.hierarchy.showLocationInfoByPath"))
                .bindSelected(settings::showLocationInfoByPath)
                .enabledIf(cb.selected)
            checkBox(ChronicleBundle.message("settings.hierarchy.showLocationInfoByRootInfo"))
                .bindSelected(settings::showLocationInfoByRootInfo)
                .enabledIf(cb.selected)
        }

        // showScriptedVariablesInCallHierarchy
        row {
            checkBox(ChronicleBundle.message("settings.hierarchy.showScriptedVariablesInCallHierarchy"))
                .bindSelected(settings::showScriptedVariablesInCallHierarchy)
        }
        // showDefinitionsInCallHierarchy
        row {
            checkBox(ChronicleBundle.message("settings.hierarchy.showDefinitionsInCallHierarchy"))
                .bindSelected(settings::showDefinitionsInCallHierarchy)

            val definitionTypeBindingsInCallHierarchy = settings.definitionTypeBindingsInCallHierarchy
            val defaultList = definitionTypeBindingsInCallHierarchy.toMutableEntryList()
            var list = defaultList.mapTo(mutableListOf()) { it.copy() }
            val action = { _: ActionEvent ->
                val dialog = DefinitionTypeBindingsInCallHierarchyDialog(list)
                if (dialog.showAndGet()) list = dialog.resultList
            }
            link(ChronicleBundle.message("settings.hierarchy.definitionTypeBindings.link"), action)
                .onApply { settings.definitionTypeBindingsInCallHierarchy = list.toMutableMap() }
                .onReset { list = defaultList }
                .onIsModified { list != defaultList }
        }
        // showLocalisationsInCallHierarchy
        row {
            checkBox(ChronicleBundle.message("settings.hierarchy.showLocalisationsInCallHierarchy"))
                .bindSelected(settings::showLocalisationsInCallHierarchy)
        }

        // showEventInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.hierarchy.showEventInfo"))
                .bindSelected(settings::showEventInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.hierarchy.showEventInfoByType"))
                .bindSelected(settings::showEventInfoByType)
                .enabledIf(cb.selected)
            checkBox(ChronicleBundle.message("settings.hierarchy.showEventInfoByAttributes"))
                .bindSelected(settings::showEventInfoByAttributes)
                .enabledIf(cb.selected)
        }
        // showTechInfo
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.hierarchy.showTechInfo"))
                .bindSelected(settings::showTechInfo)
                .applyToComponent { cb = this }
            checkBox(ChronicleBundle.message("settings.hierarchy.showTechInfoByTier"))
                .bindSelected(settings::showTechInfoByTier)
                .enabledIf(cb.selected)
            checkBox(ChronicleBundle.message("settings.hierarchy.showTechInfoByArea"))
                .bindSelected(settings::showTechInfoByArea)
                .enabledIf(cb.selected)
            checkBox(ChronicleBundle.message("settings.hierarchy.showTechInfoByCategories"))
                .bindSelected(settings::showTechInfoByCategories)
                .enabledIf(cb.selected)
            checkBox(ChronicleBundle.message("settings.hierarchy.showTechInfoByAttributes"))
                .bindSelected(settings::showTechInfoByAttributes)
                .enabledIf(cb.selected)
        }

        // eventTreeGrouping
        row {
            label(ChronicleBundle.message("settings.hierarchy.eventTreeGrouping"))
            comboBox(EventTreeGrouping.entries, textListCellRenderer { it?.text })
                .bindItem(settings::eventTreeGrouping.toNullableProperty())
        }
        // techTreeGrouping
        row {
            label(ChronicleBundle.message("settings.hierarchy.techTreeGrouping"))
            comboBox(TechTreeGrouping.entries, textListCellRenderer { it?.text })
                .bindItem(settings::techTreeGrouping.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForDiff() {
        val settings = PlsSettings.getInstance().state.diff

        // defaultDiffGroup
        row {
            label(ChronicleBundle.message("settings.diff.defaultDiffGroup"))
            comboBox(DiffGroup.entries, textListCellRenderer { it?.text })
                .bindItem(settings::defaultDiffGroup.toNullableProperty())
        }
    }

    private fun Panel.configureGroupForNavigation() {
        val settings = PlsSettings.getInstance().state.navigation

        // seForTargets
        row {
            label(ChronicleBundle.message("settings.navigation.seForTargets"))
            contextHelp(ChronicleBundle.message("settings.navigation.seForTargets.tip"))
        }
        indent {
            row {
                checkBox(ChronicleBundle.message("settings.navigation.seForScriptedVariables"))
                    .bindSelected(settings::seForScriptedVariables)
                checkBox(ChronicleBundle.message("settings.navigation.seForDefinitions"))
                    .bindSelected(settings::seForDefinitions)
                checkBox(ChronicleBundle.message("settings.navigation.seForLocalisations"))
                    .bindSelected(settings::seForLocalisations)
                checkBox(ChronicleBundle.message("settings.navigation.seForSyncedLocalisations"))
                    .bindSelected(settings::seForSyncedLocalisations)
            }
        }
        indent {
            row {
                checkBox(ChronicleBundle.message("settings.navigation.seForTargetByText"))
                    .bindSelected(settings::seForTargetsByText)
                contextHelp(ChronicleBundle.message("settings.navigation.seForTargetByText.tip"))
            }
        }

        // seForConfigs
        row {
            label(ChronicleBundle.message("settings.navigation.seForConfigs"))
            contextHelp(ChronicleBundle.message("settings.navigation.seForConfigs.tip"))
        }
        indent {
            row {
                checkBox(ChronicleBundle.message("settings.navigation.seForTypeConfigs"))
                    .bindSelected(settings::seForTypeConfigs)
                checkBox(ChronicleBundle.message("settings.navigation.seForComplexEnumConfigs"))
                    .bindSelected(settings::seForComplexEnumConfigs)
                checkBox(ChronicleBundle.message("settings.navigation.seForTriggerConfigs"))
                    .bindSelected(settings::seForTriggerConfigs)
                checkBox(ChronicleBundle.message("settings.navigation.seForEffectConfigs"))
                    .bindSelected(settings::seForEffectConfigs)
            }
        }
    }

    private fun Panel.configureGroupForInference() {
        val settings = PlsSettings.getInstance().state.inference

        // injectionForParameterValue
        row {
            checkBox(ChronicleBundle.message("settings.inference.injectionForParameterValue"))
                .bindSelected(settings::injectionForParameterValue)
                .onApply { PlsSettingsManager.refreshFiles(callbackLock) }
            contextHelp(ChronicleBundle.message("settings.inference.injectionForParameterValue.tip"))
        }
        // injectionForLocalisationText
        row {
            checkBox(ChronicleBundle.message("settings.inference.injectionForLocalisationText"))
                .bindSelected(settings::injectionForLocalisationText)
                .onApply { PlsSettingsManager.refreshFiles(callbackLock) }
            contextHelp(ChronicleBundle.message("settings.inference.injectionForLocalisationText.tip"))
        }
        // configContextForParameters
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.inference.configContextForParameters"))
                .bindSelected(settings::configContextForParameters)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(ChronicleBundle.message("settings.inference.configContextForParameters.tip"))

            // configContextForParametersFast
            checkBox(ChronicleBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForParametersFast)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fast.tip"))

            // configContextForParametersFromUsages
            checkBox(ChronicleBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForParametersFromUsages)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fromUsages.tip"))

            // configContextForParametersFromConfig
            checkBox(ChronicleBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForParametersFromConfig)
                .onApply { PlsSettingsManager.refreshForParameterInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fromConfig.tip"))
        }
        // configContextForInlineScripts
        row {
            lateinit var cb: JBCheckBox
            checkBox(ChronicleBundle.message("settings.inference.configContextForInlineScripts"))
                .bindSelected(settings::configContextForInlineScripts)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .applyToComponent { cb = this }
            contextHelp(ChronicleBundle.message("settings.inference.configContextForInlineScripts.tip"))

            // configContextForInlineScriptsFast
            checkBox(ChronicleBundle.message("settings.inference.fast"))
                .bindSelected(settings::configContextForInlineScriptsFast)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fast.tip"))

            // configContextForInlineScriptsFromUsages
            checkBox(ChronicleBundle.message("settings.inference.fromUsages"))
                .bindSelected(settings::configContextForInlineScriptsFromUsages)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fromUsages.tip"))

            // configContextForInlineScriptsFromConfig
            checkBox(ChronicleBundle.message("settings.inference.fromConfig"))
                .bindSelected(settings::configContextForInlineScriptsFromConfig)
                .onApply { PlsSettingsManager.refreshForInlineScriptInference(callbackLock) }
                .enabledIf(cb.selected)
            contextHelp(ChronicleBundle.message("settings.inference.fromConfig.tip"))
        }
        // scopeContext
        row {
            checkBox(ChronicleBundle.message("settings.inference.scopeContext"))
                .bindSelected(settings::scopeContext)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBundle.message("settings.inference.scopeContext.tip"))
        }
        // scopeContextForEvents
        row {
            checkBox(ChronicleBundle.message("settings.inference.scopeContextForEvents"))
                .bindSelected(settings::scopeContextForEvents)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBundle.message("settings.inference.scopeContextForEvents.tip"))
        }
        // scopeContextForOnActions
        row {
            checkBox(ChronicleBundle.message("settings.inference.scopeContextForOnActions"))
                .bindSelected(settings::scopeContextForOnActions)
                .onApply { PlsSettingsManager.refreshForScopeContextInference(callbackLock) }
            contextHelp(ChronicleBundle.message("settings.inference.scopeContextForOnActions.tip"))
        }
    }

    private fun Panel.configureGroupForOthers() {
        val settings = PlsSettings.getInstance().state.others

        // showEditorContextToolbar
        row {
            checkBox(ChronicleBundle.message("settings.others.showEditorContextToolbar"))
                .bindSelected(settings::showEditorContextToolbar)
        }
        // showLaunchGameActionInEditorContextToolbar
        row {
            checkBox(ChronicleBundle.message("settings.others.showLaunchGameActionInEditorContextToolbar"))
                .bindSelected(settings::showLaunchGameActionInEditorContextToolbar)
        }
        // showLocalisationFloatingToolbar
        row {
            checkBox(ChronicleBundle.message("settings.others.showLocalisationFloatingToolbar"))
                .bindSelected(settings::showLocalisationFloatingToolbar)
        }
        // highlightLocalisationColorId
        row {
            checkBox(ChronicleBundle.message("settings.others.highlightLocalisationColorId"))
                .bindSelected(settings::highlightLocalisationColorId)
                .onApply { PlsSettingsManager.refreshFiles(callbackLock) }
        }
        // renderLocalisationColorfulText
        row {
            checkBox(ChronicleBundle.message("settings.others.renderLocalisationColorfulText"))
                .bindSelected(settings::renderLocalisationColorfulText)
                .onApply { PlsSettingsManager.refreshFiles(callbackLock) }
        }
    }
}
