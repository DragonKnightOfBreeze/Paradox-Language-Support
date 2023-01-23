package icu.windea.pls.cwt.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.psi.*

/**
 * 用于补充当前声明。
 */
class CwtSmartEnterProcessor : SmartEnterProcessorWithFixers()