package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

object ParadoxRecursionManager {
    //由于需要处理引用传递的情况，考虑性能问题，目前仅检测第一个递归引用
    //这里需要避免StackOverflowError

    fun isRecursiveScriptedVariable(
        element: ParadoxScriptScriptedVariable,
        recursions: MutableCollection<PsiElement>? = null,
    ): Boolean {
        return doIsRecursiveScriptedVariable(element, recursions, ArrayDeque())
    }

    private fun doIsRecursiveScriptedVariable(
        element: ParadoxScriptScriptedVariable,
        recursions: MutableCollection<PsiElement>?,
        stack: ArrayDeque<String>,
    ): Boolean {
        var result = recursions.isNotNullOrEmpty()
        if (result) return true
        val name = element.name ?: return false
        val entryElement = element.scriptedVariableValue ?: return false
        ProgressManager.checkCanceled()
        stack.addLast(name)
        entryElement.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if (!ParadoxResolveConstraint.ScriptedVariable.canResolveReference(e)) return@run
                    e.references.orNull()?.forEach f@{ r ->
                        ProgressManager.checkCanceled()
                        if (!ParadoxResolveConstraint.ScriptedVariable.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptScriptedVariable>() ?: return@f
                        if (resolved.name in stack) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveScriptedVariable(resolved, recursions, stack)
                        }
                        if (result) return
                    }
                }
                super.visitElement(e)
            }
        })
        stack.removeLast()
        return result
    }

    fun isRecursiveLocalisation(
        element: ParadoxLocalisationProperty,
        recursions: MutableCollection<PsiElement>? = null,
    ): Boolean {
        return doIsRecursiveLocalisation(element, recursions, ArrayDeque())
    }

    private fun doIsRecursiveLocalisation(
        element: ParadoxLocalisationProperty,
        recursions: MutableCollection<PsiElement>?,
        stack: ArrayDeque<String>,
    ): Boolean {
        var result = recursions.isNotNullOrEmpty()
        if (result) return true
        val name = element.name.orNull() ?: return false
        val entryElement = element.propertyValue ?: return false
        ProgressManager.checkCanceled()
        stack.addLast(name)
        entryElement.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if (!ParadoxResolveConstraint.Localisation.canResolveReference(e)) return@run
                    e.references.orNull()?.forEach f@{ r ->
                        ProgressManager.checkCanceled()
                        if (!ParadoxResolveConstraint.Localisation.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxLocalisationProperty>() ?: return@f
                        if (resolved.name in stack) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveLocalisation(resolved, recursions, stack)
                        }
                        if (result) return
                    }
                }
                super.visitElement(e)
            }
        })
        stack.removeLast()
        return result
    }

    fun isRecursiveDefinition(
        element: ParadoxScriptDefinitionElement,
        recursions: MutableCollection<PsiElement>? = null,
        predicate: ((ParadoxScriptDefinitionElement, PsiElement) -> Boolean)? = null,
    ): Boolean {
        return doIsRecursiveDefinition(element, recursions, ArrayDeque(), predicate)
    }

    private fun doIsRecursiveDefinition(
        element: ParadoxScriptDefinitionElement,
        recursions: MutableCollection<PsiElement>?,
        stack: ArrayDeque<String>,
        predicate: ((ParadoxScriptDefinitionElement, PsiElement) -> Boolean)? = null,
    ): Boolean {
        var result = recursions.isNotNullOrEmpty()
        if (result) return true
        val definitionInfo = element.definitionInfo ?: return false //skip non-definition
        val name = definitionInfo.name.orNull() ?: return false //skip anonymous definition
        val type = definitionInfo.type
        val entryElement = when {
            element is ParadoxScriptFile -> element.block
            element is ParadoxScriptProperty -> element
            else -> null
        } ?: return false
        ProgressManager.checkCanceled()
        stack.addLast(name)
        entryElement.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if (!ParadoxResolveConstraint.Definition.canResolveReference(e)) return@run
                    if (predicate != null && !predicate(element, e)) return@run
                    e.references.orNull()?.forEach f@{ r ->
                        ProgressManager.checkCanceled()
                        if (!ParadoxResolveConstraint.Definition.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptDefinitionElement>() ?: return@f
                        val resolvedDefinition = resolved.definitionInfo ?: return@f
                        if (resolvedDefinition.type != type) return@f
                        if (resolved.name in stack) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveDefinition(resolved, recursions, stack)
                        }
                        if (result) return
                    }
                }
                super.visitElement(e)
            }
        })
        stack.removeLast()
        return result
    }
}
