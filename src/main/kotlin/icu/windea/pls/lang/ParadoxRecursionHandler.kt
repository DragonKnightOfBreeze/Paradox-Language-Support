package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

object ParadoxRecursionHandler {
    //由于需要处理引用传递的情况，考虑性能问题，目前仅检测第一个递归引用
    //这里需要避免StackOverflowError
    
    fun isRecursiveScriptedVariable(
        element: ParadoxScriptScriptedVariable,
        recursions: MutableCollection<PsiElement>? = null,
    ): Boolean {
        return doIsRecursiveScriptedVariable(element, recursions, mutableSetOf())
    }
    
    private fun doIsRecursiveScriptedVariable(
        element: ParadoxScriptScriptedVariable,
        recursions: MutableCollection<PsiElement>?,
        keys: MutableSet<String>,
    ): Boolean {
        val name = element.name ?: return false
        keys.add(name)
        val entryElement = element.scriptedVariableValue ?: return false
        ProgressManager.checkCanceled()
        var result = false
        entryElement.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.ScriptedVariable.canResolveReference(e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        ProgressManager.checkCanceled()
                        if(!ParadoxResolveConstraint.ScriptedVariable.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptScriptedVariable>() ?: return@f
                        if(resolved.name in keys) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveScriptedVariable(resolved, recursions, keys)
                        }
                        if(result) return
                    }
                }
                super.visitElement(e)
            }
        })
        return result
    }
    
    fun isRecursiveLocalisation(
        element: ParadoxLocalisationProperty,
        recursions: MutableCollection<PsiElement>? = null,
    ): Boolean {
        return doIsRecursiveLocalisation(element, recursions, mutableSetOf())
    }
    
    private fun doIsRecursiveLocalisation(
        element: ParadoxLocalisationProperty,
        recursions: MutableCollection<PsiElement>?,
        keys: MutableSet<String>,
    ): Boolean {
        val name = element.name.orNull() ?: return false
        keys.add(name)
        val entryElement = element.propertyValue ?: return false
        ProgressManager.checkCanceled()
        var result = false
        entryElement.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.Localisation.canResolveReference(e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        ProgressManager.checkCanceled()
                        if(!ParadoxResolveConstraint.Localisation.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxLocalisationProperty>() ?: return@f
                        if(resolved.name in keys) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveLocalisation(resolved, recursions, keys)
                        }
                        if(result) return
                    }
                }
                super.visitElement(e)
            }
        })
        return result
    }
    
    fun isRecursiveDefinition(
        element: ParadoxScriptDefinitionElement,
        recursions: MutableCollection<PsiElement>? = null,
        predicate: ((ParadoxScriptDefinitionElement, PsiElement) -> Boolean)? = null,
    ): Boolean {
        return doIsRecursiveDefinition(element, recursions, mutableSetOf(), predicate)
    }
    
    private fun doIsRecursiveDefinition(
        element: ParadoxScriptDefinitionElement,
        recursions: MutableCollection<PsiElement>?,
        keys: MutableSet<String>,
        predicate: ((ParadoxScriptDefinitionElement, PsiElement) -> Boolean)? = null,
    ): Boolean {
        val definitionInfo = element.definitionInfo ?: return false //skip non-definition
        val name = definitionInfo.name.orNull() ?: return false //skip anonymous definition
        keys.add(name)
        val type = definitionInfo.type
        val entryElement = when {
            element is ParadoxScriptFile -> element.block
            element is ParadoxScriptProperty -> element
            else -> null
        } ?: return false
        ProgressManager.checkCanceled()
        var result = false
        entryElement.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.Definition.canResolveReference(e)) return@run
                    if(predicate != null && !predicate(element, e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        ProgressManager.checkCanceled()
                        if(!ParadoxResolveConstraint.Definition.canResolve(r)) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptDefinitionElement>() ?: return@f
                        val resolvedDefinition = resolved.definitionInfo ?: return@f
                        if(resolvedDefinition.type != type) return@f
                        if(resolved.name in keys) {
                            recursions?.add(e)
                            result = true
                        } else {
                            result = doIsRecursiveDefinition(resolved, recursions, keys)
                        }
                        if(result) return
                    }
                }
                super.visitElement(e)
            }
        })
        return result
    }
}