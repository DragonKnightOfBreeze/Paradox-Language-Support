package icu.windea.pls.lang

import com.intellij.lang.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.impl.*

/**
 * 用于处理封装变量。
 */
@Suppress("unused")
object ParadoxScriptedVariableHandler {
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
        return node.firstChild(tree, SCRIPTED_VARIABLE_NAME)?.firstChild(tree, SCRIPTED_VARIABLE_NAME_TOKEN)?.internNode(tree)?.toString()
    }
    
    fun shouldCreateStub(node: ASTNode): Boolean {
        //仅当不带参数时才创建stub
        if(node.findChildByType(PARAMETER) != null) return false
        //仅当是全局的scripted_variable时才创建stub
        val parentType = node.treeParent.elementType
        if(parentType != ROOT_BLOCK) return false
        val file = selectFile(node.psi) ?: return false
        val entryPath = file.fileInfo?.pathToEntry?.path ?: return false
        return "common/scripted_variables".matchesPath(entryPath, acceptSelf = false)
    }
    
    fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        //仅当不带参数时才创建stub
        if(node.firstChild(tree, PARAMETER) != null) return false
        //仅当是全局的scripted_variable时才创建stub
        val parentType = tree.getParent(node)?.tokenType
        if(parentType != ROOT_BLOCK) return false
        val file = selectFile(parentStub.psi) ?: return false
        val entryPath = file.fileInfo?.pathToEntry?.path ?: return false
        return "common/scripted_variables".matchesPath(entryPath, acceptSelf = false)
    }
}