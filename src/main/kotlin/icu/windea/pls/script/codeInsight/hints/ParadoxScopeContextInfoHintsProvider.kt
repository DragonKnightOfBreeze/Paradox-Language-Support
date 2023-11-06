package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeInsight.hints.ParadoxScopeContextInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义或定义成员的作用域上下文信息的内嵌提示（`this = ? root = ? from = ?`）。
 */
@Suppress("UnstableApiUsage")
class ParadoxScopeContextInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	data class Settings(
		var showOnlyIfScopeIsChanged: Boolean = true
	)
	
	private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxScopeContextInfoHintsSettingsKey")
	
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
						.actionListener { _, component -> settings.showOnlyIfScopeIsChanged = component.isSelected }
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
		val offset = leftCurlyBrace.endOffset
		val isAtLineEnd = editor.document.isAtLineEnd(offset, true)
		if(!isAtLineEnd) return true //仅当作为子句开始的左花括号位于行尾时，才显示此内嵌提示
		if(!ParadoxScopeHandler.isScopeContextSupported(element, indirect = true)) return true
		val scopeContext = ParadoxScopeHandler.getScopeContext(element)
		if(scopeContext != null) {
			//don't need show if scope is not changed
			if(settings.showOnlyIfScopeIsChanged && !ParadoxScopeHandler.isScopeContextChanged(element, scopeContext)) return true
			
			val gameType = selectGameType(file) ?: return true
			val configGroup = getConfigGroup(file.project, gameType)
			val presentation = doCollect(scopeContext, configGroup)
			val finalPresentation = presentation.toFinalPresentation(this, file.project)
			sink.addInlineElement(offset, true, finalPresentation, true)
		}
		return true
	}
	
	private fun PresentationFactory.doCollect(scopeInfo: ParadoxScopeContext, configGroup: CwtConfigGroup): InlayPresentation {
		val presentations = mutableListOf<InlayPresentation>()
		var appendSeparator = false
		scopeInfo.map.forEach { (key, value) ->
			if(appendSeparator) {
				presentations.add(smallText(" "))
			} else {
				appendSeparator = true
			}
			presentations.add(systemLinkPresentation(key, configGroup))
			presentations.add(smallText(" = "))
			presentations.add(scopeLinkPresentation(value, configGroup))
		}
		return SequencePresentation(presentations)
	}
	
	private fun PresentationFactory.systemLinkPresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
		return psiSingleReference(smallText(scope)) { configGroup.systemLinks[scope]?.pointer?.element }
	}
	
	private fun PresentationFactory.scopeLinkPresentation(scope: ParadoxScope, configGroup: CwtConfigGroup): InlayPresentation {
		return when {
			ParadoxScopeHandler.isFakeScopeId(scope.id) -> smallText(scope.id)
			scope is ParadoxScope.InferredScope -> seq(psiSingleReference(smallText(scope.id)) { getScopeElement(configGroup, scope) }, smallText("!"))
			else -> psiSingleReference(smallText(scope.id)) { getScopeElement(configGroup, scope) }
		}
	}
	
	private fun getScopeElement(configGroup: CwtConfigGroup, scope: ParadoxScope): CwtProperty? {
		return configGroup.scopeAliasMap[scope.id]?.pointer?.element
	}
}