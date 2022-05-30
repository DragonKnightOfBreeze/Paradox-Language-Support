package icu.windea.pls.core.settings

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings"), "settings.language.pls"), SearchableConfigurable {
	override fun getId() = "settings.language.pls"
	
	override fun createPanel(): DialogPanel {
		val settings = getSettings()
		return panel {
			group(PlsBundle.message("settings.generic")) {
				row {
					label(PlsBundle.message("settings.generic.defaultGameType")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.defaultGameType.tooltip")
					}
					val values = ParadoxGameType.values.toList()
					comboBox(values).bindItem(settings::defaultGameType.toNullableProperty())
				}
				row {
					label(PlsBundle.message("settings.generic.maxCompleteSize")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.maxCompleteSize.tooltip")
					}
					this.intTextField(0..1000).bindIntText(settings::maxCompleteSize)
				}
				row {
					checkBox(PlsBundle.message("settings.generic.preferOverridden"))
						.bindSelected(settings::preferOverridden)
						.applyToComponent { toolTipText = PlsBundle.message("settings.generic.preferOverridden.tooltip") }
				}
			}
			group(PlsBundle.message("settings.script")) {
				row {
					label(PlsBundle.message("settings.generic.ignoredFileNames")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.ignoredFileNames.tooltip")
					}
					expandableTextField({ it.toCommaDelimitedStringMutableList() }, { it.toCommaDelimitedString() })
						.bindText({
							settings.scriptIgnoredFileNames
						}, {
							settings.scriptIgnoredFileNames = it
							settings.finalScriptIgnoredFileNames = it.toCommaDelimitedStringSet(ignoreCase = true)
							fireFileTypesChanged()
						})
						.horizontalAlign(HorizontalAlign.FILL)
						.resizableColumn()
				}
				buttonsGroup(PlsBundle.message("settings.script.doc")) {
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderLineComment"))
							.bindSelected(settings::scriptRenderLineComment)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderLineComment.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderRelatedLocalisation"))
							.bindSelected(settings::scriptRenderRelatedLocalisation)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderRelatedLocalisation.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderRelatedPictures"))
							.bindSelected(settings::scriptRenderRelatedPictures)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderRelatedPictures.tooltip") }
					}
				}
			}
			group(PlsBundle.message("settings.localisation")) {
				row {
					label(PlsBundle.message("settings.localisation.primaryLocale")).applyToComponent {
						toolTipText = PlsBundle.message("settings.localisation.primaryLocale.tooltip")
					}
					comboBox(settings.locales, listCellRenderer { value, _, _ ->
						if(value == "") {
							text = PlsBundle.message("settings.localisation.primaryLocale.auto")
						} else {
							text = ParadoxLocaleConfig.find(value)!!.description
						}
					})
						.bindItem(settings::localisationPrimaryLocale.toNullableProperty())
						.onApply { refreshInlayHints() }
				}
				row {
					label(PlsBundle.message("settings.localisation.truncateLimit")).applyToComponent {
						toolTipText = PlsBundle.message("settings.localisation.truncateLimit.tooltip")
					}
					intTextField(0..100)
						.bindIntText(settings::localisationTruncateLimit)
						.onApply { refreshInlayHints() }
				}
				buttonsGroup(PlsBundle.message("settings.localisation.doc")) {
					row {
						checkBox(PlsBundle.message("settings.localisation.doc.renderLineComment"))
							.bindSelected(settings::localisationRenderLineComment)
							.applyToComponent { toolTipText = PlsBundle.message("settings.localisation.doc.renderLineComment.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.localisation.doc.renderLocalisation"))
							.bindSelected(settings::localisationRenderLocalisation)
							.applyToComponent { toolTipText = PlsBundle.message("settings.localisation.doc.renderLocalisation.tooltip") }
					}
				}
			}
		}
	}
	
	private fun fireFileTypesChanged() {
		//通知文件类型映射发生变化
		try {
			FileTypeManagerEx.getInstanceEx().fireFileTypesChanged()
		} catch(e: Exception) {
			logger().warn(e.message)
		}
	}
	
	@Suppress("UnstableApiUsage")
	private fun refreshInlayHints() {
		//当某些设置变更后，需要刷新内嵌提示
		//com.intellij.codeInsight.hints.VcsCodeAuthorInlayHintsProviderKt.refreshCodeAuthorInlayHints
		try {
			val openProjects = ProjectManager.getInstance().openProjects
			if(openProjects.isEmpty()) return
			for(project in openProjects) {
				val allEditors = FileEditorManager.getInstance(project).allEditors
				if(allEditors.isEmpty()) continue
				for(fileEditor in allEditors) {
					if(fileEditor is TextEditor) {
						val fileType = fileEditor.file.fileType
						if(fileType == ParadoxScriptFileType) {
							val editor = fileEditor.editor
							InlayHintsPassFactory.clearModificationStamp(editor)
						}
					}
				}
			}
		} catch(e: Exception) {
			logger().warn(e.message)
		}
	}
}
