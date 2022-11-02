package icu.windea.pls.core

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.*
import com.intellij.openapi.ui.*
import com.intellij.refactoring.*
import com.intellij.ui.dsl.builder.*
import javax.swing.text.*
import kotlin.reflect.*

val <T: DialogWrapper> T.dialog get() = this

fun <T : JTextComponent> Cell<T>.bindText(prop: KMutableProperty0<String?>): Cell<T> {
	return bindText({ prop.get().orEmpty() }, { prop.set(it) })
}

fun Row.pathCompletionShortcutComment() {
	val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION))
	comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
}