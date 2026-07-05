package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement

/**
 * 语句。包括成员、封装变量声明、条件化块。
 *
 * @see ParadoxScriptMember
 * @see ParadoxScriptScriptedVariable
 * @see ParadoxScriptConditionalBlock
 */
interface ParadoxScriptStatement : NavigatablePsiElement
