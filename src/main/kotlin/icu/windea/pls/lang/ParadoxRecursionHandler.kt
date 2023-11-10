package icu.windea.pls.lang

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

object ParadoxRecursionHandler {
    //TODO 1.2.2+ 需要处理引用传递的情况
    
    fun isRecursiveScriptedVariable(
        element: ParadoxScriptScriptedVariable,
        recursions: MutableCollection<PsiElement>? = null
    ): Boolean {
        val name = element.name
        var isRecursive = false
        val entryElement = element.scriptedVariableValue ?: return false
        entryElement.accept(object : PsiElementVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.ScriptedVariable.canResolveReference(e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        if(!ParadoxResolveConstraint.ScriptedVariable.canResolve(r)) return@f
                        if(r.rangeInElement.substring(e.text) != name) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptScriptedVariable>() ?: return@f
                        if(resolved.name != name) return@f
                        isRecursive = true
                        if(recursions != null) {
                            recursions.add(e)
                        } else {
                            return
                        }
                    }
                }
                super.visitElement(e)
            }
        })
        return isRecursive
    }
    
    fun isRecursiveLocalisation(
        element: ParadoxLocalisationProperty,
        recursions: MutableCollection<PsiElement>? = null
    ): Boolean {
        val name = element.name
        var isRecursive = false
        val entryElement = element.propertyValue ?: return false
        entryElement.acceptChildren(object : PsiElementVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.Localisation.canResolveReference(e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        if(!ParadoxResolveConstraint.Localisation.canResolve(r)) return@f
                        if(r.rangeInElement.substring(e.text) != name) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxLocalisationProperty>() ?: return@f
                        if(resolved.name != name) return@f
                        isRecursive = true
                        if(recursions != null) {
                            recursions.add(e)
                        } else {
                            return
                        }
                    }
                }
                if(e.isRichTextContext()) super.visitElement(e)
            }
        })
        return isRecursive
    }
    
    fun isRecursiveDefinition(
        element: ParadoxScriptDefinitionElement,
        recursions: MutableCollection<PsiElement>? = null,
        predicate: ((ParadoxScriptDefinitionElement, PsiElement) -> Boolean)? = null
    ): Boolean {
        //TODO 1.2.2+ 这里需要考虑仅检查*递归调用*的情况
        val definitionInfo = element.definitionInfo ?: return false //skip non-definition
        val name = definitionInfo.name.orNull() ?: return false //skip anonymous definition
        val type = definitionInfo.type
        var isRecursive = false
        val entryElement = when {
            element is ParadoxScriptFile -> element.block
            element is ParadoxScriptProperty -> element
            else -> null
        } ?: return false
        entryElement.accept(object : PsiElementVisitor() {
            override fun visitElement(e: PsiElement) {
                run {
                    if(!ParadoxResolveConstraint.Definition.canResolveReference(e)) return@run
                    if(predicate != null && !predicate(element, e)) return@run
                    e.references.orNull()?.forEachFast f@{ r ->
                        if(!ParadoxResolveConstraint.Definition.canResolve(r)) return@f
                        if(r.rangeInElement.substring(e.text) != name) return@f
                        val resolved = r.resolve()?.castOrNull<ParadoxScriptDefinitionElement>() ?: return@f
                        val resolvedDefinition = resolved.definitionInfo ?: return@f
                        if(resolvedDefinition.name != name || resolvedDefinition.type != type) return@f
                        isRecursive = true
                        isRecursive = true
                        if(recursions != null) {
                            recursions.add(e)
                        } else {
                            return
                        }
                    }
                }
                if(e.isExpressionOrMemberContext()) super.visitElement(e)
            }
        })
        return isRecursive
    }
}