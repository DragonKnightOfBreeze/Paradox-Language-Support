package icu.windea.pls.lang.ui

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*

abstract class DialogWithValidation(project: Project) : DialogWrapper(project) {
    fun validate(validator: () -> ValidationInfo?) {
        val validationInfo = validator()
        if (validationInfo != null) {
            setErrorText(validationInfo.message, validationInfo.component)
            isOKActionEnabled = false
        } else {
            setErrorText(null)
            isOKActionEnabled = true
        }
    }
}

