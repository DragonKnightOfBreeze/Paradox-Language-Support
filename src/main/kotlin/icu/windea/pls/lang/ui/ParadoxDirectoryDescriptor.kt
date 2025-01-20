package icu.windea.pls.lang.ui

import com.intellij.openapi.fileChooser.*

class ParadoxDirectoryDescriptor(
    chooseMultiple: Boolean = false
) : FileChooserDescriptor(false, true, false, false, false, chooseMultiple)
