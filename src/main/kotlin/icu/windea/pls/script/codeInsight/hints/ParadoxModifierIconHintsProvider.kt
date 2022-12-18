package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxModifierIconHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 修饰符的图标的内嵌提示
 */
@Suppress("UnstableApiUsage")
class ParadoxModifierIconHintsProvider: ParadoxScriptHintsProvider<Settings>() {
	companion object {
		private val settingsKey = SettingsKey<Settings>("ParadoxModifierIconHintsSettingsKey")
	}
	
	data class Settings(
		var iconHeightLimit: Int = 32
	)
	
	override val name: String get() = PlsBundle.message("script.hints.modifierIcon")
	override val description: String get() = PlsBundle.message("script.hints.modifierIcon")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					label(PlsBundle.message("script.hints.settings.iconHeightLimit")).widthGroup("left")
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
					textField()
						.bindIntText(settings::iconHeightLimit)
						.errorOnApply(PlsBundle.message("script.hints.error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
			}
		}
	}
	
	//icu.windea.pls.core.tool.ParadoxLocalisationTextHintsRenderer.renderIconTo
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptStringExpressionElement) {
			//基于stub
			val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull() ?: return true
			val type = config.expression.type
			if(type == CwtDataTypes.Modifier) {
				val name = element.value
				val configGroup = config.info.configGroup
				val project = configGroup.project
				val iconPaths =  ModifierConfigHandler.getModifierIconPaths(name, configGroup)
				val iconFile = iconPaths.firstNotNullOfOrNull {
					val iconSelector = fileSelector().gameType(configGroup.gameType).preferRootFrom(element)
					findFileByFilePath(it, project, selector = iconSelector)
				} ?: return true
				val iconUrl = ParadoxDdsUrlResolver.resolveByFile(iconFile, defaultToUnknown = false)
				if(iconUrl.isNotEmpty()) {
					//忽略异常
					runCatching {
						//找不到图标的话就直接跳过
						val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return true
						//基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
						if(icon.iconHeight <= settings.iconHeightLimit) {
							//点击可以导航到声明处（DDS）
							val presentation = psiSingleReference(smallScaledIcon(icon)){ iconFile.toPsiFile(project) }
							val finalPresentation = presentation.toFinalPresentation(this, file.project, smaller = true)
							val endOffset = element.textRange.endOffset
							sink.addInlineElement(endOffset, true, finalPresentation, false)
						}
					}
				}
			}
		}
		return true
	}
}