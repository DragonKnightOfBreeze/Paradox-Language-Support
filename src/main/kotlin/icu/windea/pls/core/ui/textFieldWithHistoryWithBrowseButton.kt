@file:Suppress("unused")

package icu.windea.pls.core.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.properties.ObservableProperty
import com.intellij.openapi.observable.util.bind
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.openapi.ui.validation.transformParameter
import com.intellij.openapi.ui.validation.trimParameter
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.components.textFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.*
import com.intellij.util.containers.map2Array
import javax.swing.JTextField

val TextFieldWithHistoryWithBrowseButton.textField: JTextField get() = childComponent.textEditor

fun Row.textFieldWithHistoryWithBrowseButton(
    fileChooserDescriptor: FileChooserDescriptor,
    project: Project?,
    historyProvider: (() -> List<String>)? = null,
    fileChosen: ((chosenFile: VirtualFile) -> String)? = null
): Cell<TextFieldWithHistoryWithBrowseButton> {
    val component = textFieldWithHistoryWithBrowseButton(project, fileChooserDescriptor, historyProvider, fileChosen)
    return cell(component)
}

fun <T : TextFieldWithHistoryWithBrowseButton> Cell<T>.bindText(property: ObservableMutableProperty<String>): Cell<T> {
    return applyToComponent { bind(property) }
}

fun <C : TextFieldWithHistoryWithBrowseButton> C.bind(property: ObservableProperty<String>): C = apply {
    textField.bind(property)
}

fun <C : TextFieldWithHistoryWithBrowseButton> C.bind(property: ObservableMutableProperty<String>): C = apply {
    textField.bind(property)
}

fun <T : TextFieldWithHistoryWithBrowseButton> Cell<T>.trimmedTextValidation(vararg validations: DialogValidation.WithParameter<() -> String>): Cell<T> =
    textValidation(*validations.map2Array { it.trimParameter() })

fun <T : TextFieldWithHistoryWithBrowseButton> Cell<T>.textValidation(vararg validations: DialogValidation.WithParameter<() -> String>): Cell<T> =
    validation(*validations.map2Array { it.forTextFieldWithHistoryWithBrowseButton() })

fun DialogValidation.WithParameter<() -> String>.forTextFieldWithHistoryWithBrowseButton(): DialogValidation.WithParameter<TextFieldWithHistoryWithBrowseButton> =
    transformParameter<TextFieldWithHistoryWithBrowseButton, () -> String> { ::getText }

