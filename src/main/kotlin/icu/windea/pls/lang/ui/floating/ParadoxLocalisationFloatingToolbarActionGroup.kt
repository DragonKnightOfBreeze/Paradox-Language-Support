package icu.windea.pls.lang.ui.floating

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DefaultActionGroup
import icu.windea.pls.lang.ui.actions.styling.CreateCommandAction
import icu.windea.pls.lang.ui.actions.styling.CreateIconAction
import icu.windea.pls.lang.ui.actions.styling.CreateParameterAction
import icu.windea.pls.lang.ui.actions.styling.CreateRichTextAction
import icu.windea.pls.lang.ui.actions.styling.CreateTextIconAction
import icu.windea.pls.lang.ui.actions.styling.SetColorAction
import icu.windea.pls.lang.ui.actions.styling.SetColorGroup

// com.intellij.openapi.actionSystem.impl.FloatingToolbar.FloatingToolbar
// com.intellij.openapi.fileEditor.impl.text.TextEditorCustomizer
// com.intellij.ui.codeFloatingToolbar.CodeFloatingToolbar
// com.intellij.ui.codeFloatingToolbar.FloatingCodeToolbarEditorCustomizer
// org.intellij.plugins.markdown.ui.floating.MarkdownFloatingToolbar
// org.intellij.plugins.markdown.ui.floating.AddFloatingToolbarTextEditorCustomizer

/**
 * 本地化文件的悬浮工具栏的动作分组。
 *
 * 当用户鼠标选中本地化文本（的其中一部分）时可用。
 *
 * 提供动作：
 * - 快速插入引用 - 不会检查插入后语法是否合法
 * - 快速插入图标 - 不会检查插入后语法是否合法
 * - 快速插入命令 - 不会检查插入后语法是否合法
 * - 快速插入文本图标 - 不会检查插入后语法是否合法
 * - 更改文本颜色 - 将会列出所有可选的颜色代码
 *
 * @see ParadoxLocalisationFloatingToolbarCustomizableGroupProvider
 * @see CreateRichTextAction
 * @see CreateParameterAction
 * @see CreateIconAction
 * @see CreateCommandAction
 * @see CreateTextIconAction
 * @see SetColorGroup
 * @see SetColorAction
 */
class ParadoxLocalisationFloatingToolbarActionGroup : DefaultActionGroup() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
