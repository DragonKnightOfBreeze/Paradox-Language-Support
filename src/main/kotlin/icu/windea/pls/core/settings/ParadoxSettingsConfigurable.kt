package icu.windea.pls.core.settings

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings"), "settings.language.pls"), SearchableConfigurable {
	override fun getId() = "settings.language.pls"
	
	//通用设置
	//  （下拉框）默认游戏类型
	//  （整数输入框）最大补全大小
	//  （复选框）偏好重载的引用
	//脚本语言设置
	//  （可伸缩输入框）被忽略的文件名
	//  文档
	//    （复选框）渲染行注释
	//    （复选框）渲染相关的本地化
	//    （复选框）渲染相关的图片
	//本地化语言设置
	//  （下拉框）主要语言区域
	//  （整数输入框）截断长度
	//  文档
	//    （复选框）渲染行注释
	//    （复选框）渲染本地化
	
	override fun createPanel(): DialogPanel {
		val settings = getSettings()
		return panel {
			group(PlsBundle.message("settings.generic")) {
				row {
					label(PlsBundle.message("settings.generic.defaultGameType")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.defaultGameType.tooltip")
					}
					val values = ParadoxGameType.valueList
					comboBox(values)
						.bindItem({
							settings.defaultGameType
						}, {
							if(it != null) {
								settings.defaultGameType = it
							}
						})
						.onApply {
							//不存在模组根目录的游戏类型标记文件，设置中的默认游戏类型被更改时，也要重新解析相关文件
							runWriteAction {
								for(rootInfo in ParadoxRootInfo.values) {
									if(rootInfo.gameTypeFromMarkerFile == null) {
										reparseFilesInRoot(rootInfo.rootFile)
									}
								}
							}
						}
				}
				row {
					label(PlsBundle.message("settings.generic.maxCompleteSize")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.maxCompleteSize.tooltip")
					}
					intTextField(0..1000).bindIntText(settings::maxCompleteSize)
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
							settings.scriptIgnoredFileNames.orEmpty()
						}, {
							settings.scriptIgnoredFileNames = it
							settings.finalScriptIgnoredFileNames = it.toCommaDelimitedStringSet(ignoreCase = true)
						})
						.onApply {
							runWriteAction {
								reparseScriptFiles()
							}
						}
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
						if(value == "auto") {
							text = PlsBundle.message("settings.localisation.primaryLocale.auto")
						} else {
							text = InternalConfigHandler.getLocale(value)!!.description
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
			group(PlsBundle.message("settings.generation")) {
				row {
					label(PlsBundle.message("settings.generation.fileNamePrefix")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generation.fileNamePrefix.tooltip")
					}
					textField().bindText(settings::generationFileNamePrefix)
				}
			}
		}
	}
	
	@Suppress("UnstableApiUsage")
	private fun refreshInlayHints() {
		//不存在模组根目录的游戏类型标记文件，设置中的默认游戏类型被更改时，也要重新解析相关文件
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
