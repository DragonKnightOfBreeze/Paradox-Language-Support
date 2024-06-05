package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

/**
 * 用于处理封装变量。
 */
object ParadoxScriptedVariableHandler {
    val localScriptedVariableKey = createKey<CachedValue<List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>>>("paradox.localScriptedVariables")
    
    fun getLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        return CachedValuesManager.getCachedValue(file, localScriptedVariableKey) {
            val value = doGetLocalScriptedVariables(file)
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        val result = mutableListOf<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptScriptedVariable) {
                    result.add(element.createPointer(file))
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        return result
    }
    
    //stub methods
    
    fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = psi.name ?: return null
        return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
    }
    
    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val psi = parentStub.psi
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = getNameFromNode(node, tree) ?: return null
        return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
    }
    
    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        //这里认为名字是不带参数的
        return node.firstChild(tree, SCRIPTED_VARIABLE_NAME)
            ?.children(tree)
            ?.takeIf { it.size == 2 && it.first().tokenType == AT }
            ?.last()
            ?.internNode(tree)?.toString()
    }
    
    fun shouldCreateStub(node: ASTNode): Boolean {
        //仅当是全局的scripted_variable时才创建stub
        val parentType = node.treeParent.elementType
        if(parentType != ROOT_BLOCK) return false
        val file = selectFile(node.psi) ?: return false
        val pathToEntry = file.fileInfo?.pathToEntry?.path ?: return false
        return "common/scripted_variables".matchesPath(pathToEntry, acceptSelf = false)
    }
    
    fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        //仅当是全局的scripted_variable时才创建stub
        val parentType = tree.getParent(node)?.tokenType
        if(parentType != ROOT_BLOCK) return false
        val file = selectFile(parentStub.psi) ?: return false
        val pathToEntry = file.fileInfo?.pathToEntry?.path ?: return false
        return "common/scripted_variables".matchesPath(pathToEntry, acceptSelf = false)
    }
}