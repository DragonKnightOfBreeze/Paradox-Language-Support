package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.findChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.codeInsight.hints.text
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.toScopeMap
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 通过内嵌提示显示定义及其成员的作用域上下文信息。
 *
 * 示例：`this = owner root = country from = ?`
 *
 * @see ParadoxScopeContextInfoSettingsProvider
 */
class ParadoxScopeContextInfoHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptProperty) return

        // 属性的值需要是一个块（block），且块的左花括号需要位于行尾（忽略空白和注释）
        val block = element.propertyValue as? ParadoxScriptBlock ?: return
        val leftCurlyBrace = block.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE } ?: return
        val atLineEnd = leftCurlyBrace.siblings(withSelf = false)
            .dropWhile { (it is PsiWhiteSpace && !PlsPsiManager.containsLineBreak(it)) || it is PsiComment }
            .firstOrNull()
        if (atLineEnd !is PsiWhiteSpace || !PlsPsiManager.containsLineBreak(atLineEnd)) return

        val file = element.containingFile ?: return
        val gameType = selectGameType(file) ?: return
        val project = file.project

        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element) ?: return

        val settings = ParadoxDeclarativeHintsSettings.getInstance(project)
        if (settings.showScopeContextOnlyIfIsChanged && !ParadoxScopeManager.isScopeContextChanged(element, scopeContext)) return

        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        sink.addInlinePresentation(leftCurlyBrace.endOffset) {
            val scopeMap = scopeContext.toScopeMap(showPrev = false)
            val m = OnceMarker()
            for ((systemScope, scope) in scopeMap) {
                if (m.mark()) text(" ")
                text(systemScope.optimized(), configGroup.systemScopes[systemScope]?.pointer)
                text(" = ")
                when {
                    ParadoxScopeManager.isUnsureScopeId(scope.id) -> text(scope.id)
                    else -> text(scope.id, configGroup.scopeAliasMap[scope.id]?.pointer)
                }
            }
        }
    }
}
