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
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.PlsStrategies.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.model.*
import java.awt.event.*

@Suppress("UnstableApiUsage")
class PlsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "pls"

    override fun createPanel(): DialogPanel {
        val settings = getSettings()
        return panel {
            //general
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameType.tooltip") }
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
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.defaultGameDirectories.tooltip") }
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
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup("general")
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.preferredLocale.tooltip") }
                    var preferredLocale = settings.preferredLocale
                    localeComboBox(withAuto = true).bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            val oldPreferredLocale = preferredLocale
                            val newPreferredLocale = settings.preferredLocale
                            if (oldPreferredLocale == newPreferredLocale) return@onApply
                            preferredLocale = newPreferredLocale
                            refreshOnlyForOpenedFiles()
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
                        val dialog = ClauseTemplateSettingsDialog()
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
            //folding
            collapsibleGroup(PlsBundle.message("settings.folding")) {
                //comment & commentByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.comment"))
                        .bindSelected(getSettings().folding::comment)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::commentByDefault)
                        .enabledIf(cb.selected)
                }
                //parameterConditionBlocks & parameterConditionBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"))
                        .selected(true)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::parameterConditionBlocksByDefault)
                }
                //inlineMathBlocks & inlineMathBlocksByDefault
                row {
                    checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"))
                        .selected(true)
                        .enabled(false)
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::inlineMathBlocksByDefault)
                }
                //localisationReferencesFully & localisationReferencesFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationReferencesFully"))
                        .bindSelected(getSettings().folding::localisationReferencesFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::localisationReferencesFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationIconsFully & localisationIconsFullyByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationIconsFully"))
                        .bindSelected(getSettings().folding::localisationIconsFully)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::localisationIconsFullyByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationCommands & localisationCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationCommands"))
                        .bindSelected(getSettings().folding::localisationCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::localisationCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptCommands & localisationConceptCommandsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptCommands"))
                        .bindSelected(getSettings().folding::localisationConceptCommands)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::localisationConceptCommandsByDefault)
                        .enabledIf(cb.selected)
                }
                //localisationConceptTexts & localisationConceptTextsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"))
                        .bindSelected(getSettings().folding::localisationConceptTexts)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::localisationConceptTextsByDefault)
                        .enabledIf(cb.selected)
                }
                //scriptedVariableReferences & scriptedVariableReferencesByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"))
                        .bindSelected(getSettings().folding::scriptedVariableReferences)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::scriptedVariableReferencesByDefault)
                        .enabledIf(cb.selected)
                }
                //variableOperationExpressions & variableOperationExpressionsByDefault
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"))
                        .bindSelected(getSettings().folding::variableOperationExpressions)
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.folding.byDefault"))
                        .bindSelected(getSettings().folding::variableOperationExpressionsByDefault)
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
                    comboBox(EventTreeGrouping.entries).bindItem(settings.hierarchy::eventTreeGrouping.toNullableProperty())
                }
                //techTreeGrouping
                row {
                    label(PlsBundle.message("settings.hierarchy.techTreeGrouping"))
                    comboBox(TechTreeGrouping.entries).bindItem(settings.hierarchy::techTreeGrouping.toNullableProperty())
                }
            }
            //inference
            collapsibleGroup(PlsBundle.message("settings.inference")) {
                //configContextForParameters & configContextForParametersFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForParameters"))
                        .bindSelected(settings.inference::configContextForParameters)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForParameters.tooltip") }
                        .onApply { refreshForParameterInference() }
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForParametersFast)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextFast.tooltip") }
                        .onApply { refreshForParameterInference() }
                        .enabledIf(cb.selected)
                }
                //configContextForInlineScripts & configContextForInlineScriptsFast
                row {
                    lateinit var cb: JBCheckBox
                    checkBox(PlsBundle.message("settings.inference.configContextForInlineScripts"))
                        .bindSelected(settings.inference::configContextForInlineScripts)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextForInlineScripts.tooltip") }
                        .onApply { refreshForInlineScriptInference() }
                        .applyToComponent { cb = this }
                    checkBox(PlsBundle.message("settings.inference.configContextFast"))
                        .bindSelected(settings.inference::configContextForInlineScriptsFast)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.configContextFast.tooltip") }
                        .onApply { refreshForInlineScriptInference() }
                        .enabledIf(cb.selected)
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
                lateinit var cbLocal: JBCheckBox
                //enableBuiltInConfigGroups
                row {
                    checkBox(PlsBundle.message("settings.others.enableBuiltInConfigGroups"))
                        .bindSelected(settings.others::enableBuiltInConfigGroups)
                        .onApply { onConfigDirectoriesChanged() }
                }
                //enableLocalConfigGroups
                row {
                    checkBox(PlsBundle.message("settings.others.enableLocalConfigGroups"))
                        .bindSelected(settings.others::enableLocalConfigGroups)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.others.enableLocalConfigGroups.tooltip") }
                        .onApply { onConfigDirectoriesChanged() }
                        .applyToComponent { cbLocal = this }
                }
                //enableProjectLocalConfigGroups
                row {
                    checkBox(PlsBundle.message("settings.others.enableProjectLocalConfigGroups"))
                        .bindSelected(settings.others::enableProjectLocalConfigGroups)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.others.enableProjectLocalConfigGroups.tooltip") }
                        .onApply { onConfigDirectoriesChanged() }
                }
                //localConfigDirectory
                row {
                    label(PlsBundle.message("settings.others.localConfigDirectory"))
                        .applyToComponent { toolTipText = PlsBundle.message("settings.others.localConfigDirectory.tooltip") }
                    val descriptor = ParadoxDirectoryDescriptor()
                        .withTitle(PlsBundle.message("settings.others.localConfigDirectory.title"))
                        .asBrowseFolderDescriptor()
                    textFieldWithBrowseButton(descriptor, null)
                        .bindText(settings.others::localConfigDirectory.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                        .align(Align.FILL)
                        .onApply { onConfigDirectoriesChanged() }
                }.enabledIf(cbLocal.selected)

                separator()

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
                        .onApply { refreshOnlyForOpenedFiles() }
                }
                //renderLocalisationColorfulText
                row {
                    checkBox(PlsBundle.message("settings.others.renderLocalisationColorfulText"))
                        .bindSelected(settings.others::renderLocalisationColorfulText)
                        .onApply { refreshOnlyForOpenedFiles() }
                }
                //defaultDiffGroup
                row {
                    label(PlsBundle.message("settings.others.defaultDiffGroup"))
                    comboBox(DiffGroup.entries).bindItem(settings.others::defaultDiffGroup.toNullableProperty())
                }
            }
        }
    }

    private fun onDefaultGameTypeChanged(oldDefaultGameType: ParadoxGameType, newDefaultGameType: ParadoxGameType) {
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(oldDefaultGameType, newDefaultGameType)
    }

    private fun onDefaultGameDirectoriesChanged(oldDefaultGameDirectories: MutableMap<String, String>, newDefaultGameDirectories: MutableMap<String, String>) {
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC).onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
    }

    private fun onConfigDirectoriesChanged() {
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxConfigDirectoriesListener.TOPIC).onChange()
    }

    //NOTE 如果应用更改时涉及多个相关字段，下面这些回调可能同一回调会被多次调用，不过目前看来问题不大

    private fun refreshForFilesByFileNames(fileNames: MutableSet<String>) {
        val files = PlsManager.findFilesByFileNames(fileNames)
        PlsManager.reparseAndRefreshFiles(files)
    }

    private fun refreshOnlyForOpenedFiles() {
        val openedFiles = PlsManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsManager.reparseAndRefreshFiles(openedFiles, reparse = false)
    }

    private fun refreshForParameterInference() {
        ParadoxModificationTrackers.ParameterConfigInferenceTracker.incModificationCount()
        val openedFiles = PlsManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }

    private fun refreshForInlineScriptInference() {
        ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptsTracker.incModificationCount()
        ParadoxModificationTrackers.InlineScriptConfigInferenceTracker.incModificationCount()
        val openedFiles = PlsManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }

    private fun refreshForScopeContextInference() {
        ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker.incModificationCount()
        val openedFiles = PlsManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsManager.reparseAndRefreshFiles(openedFiles)
    }
}
