package icu.windea.pls.core.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings"), "settings.language.pls"), SearchableConfigurable {
    override fun getId() = "settings.language.pls"
    
    override fun createPanel(): DialogPanel {
        val settings = getSettings()
        val oldDefaultGameType = settings.defaultGameType
        val oldPreferredLocale = settings.preferredLocale
        val oldIgnoredFileNameSet = settings.ignoredFileNameSet
        return panel {
            //generic
            group(PlsBundle.message("settings.general")) {
                //defaultGameType
                row {
                    label(PlsBundle.message("settings.general.defaultGameType")).widthGroup("generic")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.defaultGameType.tooltip")
                        }
                    val values = ParadoxGameType.valueList
                    comboBox(values)
                        .bindItem(settings::defaultGameType.toNullableProperty())
                        .onApply {
                            if(oldDefaultGameType != settings.defaultGameType) {
                                val messageBus = ApplicationManager.getApplication().messageBus
                                messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(settings.defaultGameType)
                            }
                        }
                }
                //preferredLocale
                row {
                    label(PlsBundle.message("settings.general.preferredLocale")).widthGroup("generic")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.preferredLocale.tooltip")
                        }
                    comboBox(settings.locales,
                        listCellRenderer { value, _, _ ->
                            if(value == "auto") {
                                text = PlsBundle.message("settings.general.preferredLocale.auto")
                            } else {
                                text = getCwtConfig().core.localisationLocales.getValue(value).description
                            }
                        })
                        .bindItem(settings::preferredLocale.toNullableProperty())
                        .onApply {
                            if(oldPreferredLocale != settings.preferredLocale) {
                                doRefreshInlayHints()
                            }
                        }
                }
                //ignoredFileNames
                row {
                    label(PlsBundle.message("settings.general.ignoredFileNames")).widthGroup("generic")
                        .applyToComponent {
                            toolTipText = PlsBundle.message("settings.general.ignoredFileNames.tooltip")
                        }
                    expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                        .bindText({ settings.ignoredFileNames.orEmpty() }, { settings.ignoredFileNames = it })
                        .comment(PlsBundle.message("settings.general.ignoredFileNames.comment"))
                        .align(Align.FILL)
                        .resizableColumn()
                        .onApply {
                            if(oldIgnoredFileNameSet != settings.ignoredFileNameSet) {
                                doReparseFilesByFileNames(settings.ignoredFileNameSet, oldIgnoredFileNameSet)
                            }
                        }
                }
                //preferOverridden
                row {
                    checkBox(PlsBundle.message("settings.general.preferOverridden"))
                        .bindSelected(settings::preferOverridden)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.general.preferOverridden.tooltip") }
                }
            }
            //documentation
            group(PlsBundle.message("settings.documentation")) {
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
                //renderRelatedLocalisationsForModifiers
                row {
                    checkBox(PlsBundle.message("settings.documentation.renderRelatedLocalisationsForModifiers"))
                        .bindSelected(settings.documentation::renderRelatedLocalisationsForModifiers)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.documentation.renderRelatedLocalisationsForModifiers.tooltip") }
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
            group(PlsBundle.message("settings.completion")) {
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
                        label(PlsBundle.message("settings.completion.maxExpressionCountInOneLine")).applyToComponent {
                            toolTipText = PlsBundle.message("settings.completion.maxExpressionCountInOneLine.tooltip")
                        }
                        intTextField(0..10).bindIntText(settings.completion::maxExpressionCountInOneLine)
                    }
                }.enabledIf(completeWithClauseTemplateCb.selected)
                //preferCompleteWithClauseTemplate
                row {
                    checkBox(PlsBundle.message("settings.completion.preferCompleteWithClauseTemplate"))
                        .bindSelected(settings.completion::preferCompleteWithClauseTemplate)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.preferCompleteWithClauseTemplate.tooltip") }
                }.enabledIf(completeWithClauseTemplateCb.selected)
                //completeOnlyScopeIsMatched
                row {
                    checkBox(PlsBundle.message("settings.completion.completeOnlyScopeIsMatched"))
                        .bindSelected(settings.completion::completeOnlyScopeIsMatched)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.completion.completeOnlyScopeIsMatched.tooltip") }
                }
            }
            //inference
            group(PlsBundle.message("settings.inference")) {
                //inlineScriptLocation
                row {
                    checkBox(PlsBundle.message("settings.inference.inlineScriptLocation"))
                        .bindSelected(settings.inference::inlineScriptLocation)
                        .applyToComponent { toolTipText = PlsBundle.message("settings.inference.inlineScriptLocation.tooltip") }
                        .onApply { doRefreshInlineScripts() }
                }
            }
            //generation
            group(PlsBundle.message("settings.generation")) {
                //fileNamePrefix
                row {
                    label(PlsBundle.message("settings.generation.fileNamePrefix")).applyToComponent {
                        toolTipText = PlsBundle.message("settings.generation.fileNamePrefix.tooltip")
                    }
                    textField().bindText(settings.generation::fileNamePrefix)
                }
            }.visible(false) //TODO
        }
    }
    
    private fun doReparseFilesByFileNames(ignoredFileNameSet: Set<String>, oldIgnoredFileNameSet: Set<String>) {
        //设置中的被忽略文件名被更改时，需要重新解析相关文件
        val fileNames = mutableSetOf<String>()
        fileNames += oldIgnoredFileNameSet
        fileNames += ignoredFileNameSet
        ParadoxCoreHandler.reparseFilesByFileNames(fileNames)
    }
    
    private fun doRefreshInlayHints() {
        ParadoxCoreHandler.refreshInlayHints { file, _ ->
            val fileType = file.fileType
            fileType == ParadoxScriptFileType || fileType == ParadoxLocalisationFileType
        }
    }
    
    private fun doRefreshInlineScripts() {
        //重新解析inline script文件
        ParadoxModificationTrackerProvider.getInstance().InlineScript.incModificationCount()
        //刷新inline script文件的内嵌提示
        ParadoxCoreHandler.refreshInlayHints { file, _ ->
            ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
        }
    }
}
