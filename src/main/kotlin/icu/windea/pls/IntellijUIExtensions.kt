package icu.windea.pls

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.keymap.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.refactoring.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Cell
import javax.swing.text.*
import kotlin.reflect.*

@Suppress("UnstableApiUsage") 
fun Row.textFieldWithHistoryBrowseButton(
	@NlsContexts.DialogTitle browseDialogTitle: String,
	project: Project,
	fileChooserDescriptor: FileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
	recentKeys: String,
	fileChosen: ((chosenFile: VirtualFile) -> String)? = null
): Cell<TextFieldWithHistoryWithBrowseButton>{
	val component = textFieldWithHistoryWithBrowseButton(project, browseDialogTitle, fileChooserDescriptor, { RecentsManager.getInstance(project).getRecentEntries(recentKeys).orEmpty() } , fileChosen)
	return cell(component)
}

fun <T : JTextComponent> Cell<T>.bindText(prop: KMutableProperty0<String?>): Cell<T> {
	return bindText({ prop.get().orEmpty() }, { prop.set(it) })
}

fun Row.pathCompletionShortcutComment() {
	val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION))
	comment(RefactoringBundle.message("path.completion.shortcut", shortcutText))
}