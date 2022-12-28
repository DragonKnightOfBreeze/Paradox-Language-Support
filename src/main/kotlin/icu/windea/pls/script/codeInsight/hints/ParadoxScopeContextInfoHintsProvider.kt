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
import icu.windea.pls.config.script.*
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.codeInsight.hints.ParadoxScopeContextInfoHintsProvider.*
import icu.windea.pls.script.psi.*
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
		var showOnlyIfScopeIsChanged: Boolean = true
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
						.bindSelected(settings::showOnlyIfScopeIsChanged)
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		if(file !is ParadoxScriptFile) return true
		if(element !is ParadoxScriptProperty) return true
		//show only for properties with clause value, and left curly brace should be at end of line
		val block = element.propertyValue as? ParadoxScriptBlock ?: return true
		val leftCurlyBrace = block.findChild(ParadoxScriptElementTypes.LEFT_BRACE) ?: return true
		val offset = leftCurlyBrace.textRange.endOffset
		val isAtLineEnd = editor.document.isAtLineEnd(offset, true)
		if(!isAtLineEnd) return true //仅当作为子句开始的左花括号位于行尾时，才显示此内嵌提示
		if(!ParadoxScopeConfigHandler.isScopeContextSupported(element)) return true
		val scopeContext = ParadoxScopeConfigHandler.getScopeContext(element, file)
		if(scopeContext != null) {
			//don't need show if scope is not changed
			if(settings.showOnlyIfScopeIsChanged && !ParadoxScopeConfigHandler.isScopeContextChanged(element, scopeContext, file)) return true
			
			val gameType = selectGameType(file) ?: return true
			val configGroup = getCwtConfig(file.project).getValue(gameType)
			val presentation = collectScopeContext(scopeContext, configGroup)
			val finalPresentation = presentation.toFinalPresentation(this, file.project)
			sink.addInlineElement(offset, true, finalPresentation, true)
		}
		return true
	}
	
	private fun PresentationFactory.collectScopeContext(scopeInfo: ParadoxScopeContext, configGroup: CwtConfigGroup): InlayPresentation {
		val presentations = mutableListOf<InlayPresentation>()
		var appendSeparator = false
		scopeInfo.map.forEach { (key, value) ->
			if(appendSeparator) {
				presentations.add(smallText(" "))
			} else {
				appendSeparator = true
			}
			presentations.add(systemScopePresentation(key, configGroup))
			presentations.add(smallText(" = "))
			presentations.add(scopeLinkPresentation(value, configGroup))
		}
		return SequencePresentation(presentations)
	}
	
	private fun PresentationFactory.systemScopePresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
		return psiSingleReference(smallText(scope)) { configGroup.systemScopes[scope]?.pointer?.element }
	}
	
	private fun PresentationFactory.scopeLinkPresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
		if(ParadoxScopeConfigHandler.isFakeScopeId(scope)) {
			return smallText(scope)
		} else {
			return psiSingleReference(smallText(scope)) { configGroup.scopeAliasMap[scope]?.pointer?.element }
		}
	}
}