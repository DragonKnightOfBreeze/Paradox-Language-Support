package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.internal.statistic.utils.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.codeInsight.hints.ParadoxScopeContextInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import org.jetbrains.kotlin.idea.gradleTooling.*
import javax.swing.*

/**
 * 定义或定义成员的作用域上下文信息的内嵌提示（`this = ? root = ? from = ?`）。
 */
@Suppress("UnstableApiUsage")
class ParadoxScopeContextInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	companion object {
		private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxScopeContextInfoHintsSettingsKey")
	}
	
	data class Settings(
		var showOnlyIfChanged: Boolean = true
	)
	
	override val name: String get() = PlsBundle.message("script.hints.scopeContext")
	override val description: String get() = PlsBundle.message("script.hints.scopeContext.description")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					checkBox(PlsBundle.message("script.hints.scopeContext.settings.showOnlyIfChanged"))
						.bindSelected(settings::showOnlyIfChanged)
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(file !is ParadoxScriptFile) return true
		if(element !is ParadoxScriptProperty) return true
		val block = element.propertyValue as? ParadoxScriptBlock ?: return true
		val leftCurlyBrace = block.findChild(ParadoxScriptElementTypes.LEFT_BRACE) ?: return true
		val offset = leftCurlyBrace.textRange.endOffset
		val isAtLineEnd = editor.document.isAtLineEnd(offset, true)
		if(!isAtLineEnd) return true //仅当作为子句开始的左花括号位于行尾时，才显示此内嵌提示
		val scopeContext = ScopeConfigHandler.getScopeContext(element, file)
		if(scopeContext != null) {
			//don't need show if scope is not changed
			if(settings.showOnlyIfChanged && !ScopeConfigHandler.isScopeContextChanged(element, scopeContext, file)) return true
			//do not show on definition level it scope context is not from type config
			if(!ScopeConfigHandler.hasScopeContext(element, scopeContext)) return true
			
			val gameType = selectGameType(file) ?: return true
			val configGroup = getCwtConfig(file.project).getValue(gameType)
			val presentation = collectScopeContext(scopeContext, configGroup)
			val finalPresentation = presentation.toFinalPresentation(this, file.project)
			sink.addInlineElement(offset, true, finalPresentation, true)
		}
		return true
	}
	
	private fun PresentationFactory.collectScopeContext(scopeInfo: ParadoxScopeConfig, configGroup: CwtConfigGroup): InlayPresentation {
		val presentations = mutableListOf<InlayPresentation>()
		scopeInfo.map.forEach { (key, value) -> 
			presentations.add(smallText("$key = "))
			presentations.add(scopeLinkPresentation(value, configGroup))
		}
		return SequencePresentation(presentations)
	}
	
	private fun PresentationFactory.scopeLinkPresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
		val scopeId = ScopeConfigHandler.getScopeId(scope)
		if(scopeId == ScopeConfigHandler.unknownScopeId || scopeId == ScopeConfigHandler.anyScopeId) {
			return smallText(scopeId)
		}
		return psiSingleReference(smallText(scopeId)) {
			val scopeLink = configGroup.scopeAliasMap[scopeId]
			scopeLink?.pointer?.element
		}
	}
}