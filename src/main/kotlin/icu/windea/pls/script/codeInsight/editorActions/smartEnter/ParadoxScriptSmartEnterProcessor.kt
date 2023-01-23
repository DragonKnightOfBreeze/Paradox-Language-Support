package icu.windea.pls.script.codeInsight.editorActions.smartEnter

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

/**
 * 用于补充当前声明。
 */
class ParadoxScriptSmartEnterProcessor : SmartEnterProcessorWithFixers()