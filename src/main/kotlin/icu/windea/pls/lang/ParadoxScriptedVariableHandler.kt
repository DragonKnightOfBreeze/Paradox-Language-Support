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
@Suppress("unused", "UNUSED_PARAMETER")
object ParadoxScriptedVariableHandler {
    //stub methods
    
    fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val file = selectFile(parentStub.psi) ?: return null
        val name = psi.name
        val gameType = selectGameType(file)
        return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
    }
    
    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val file = selectFile(parentStub.psi) ?: return null
        val name = node.firstChild(tree, SCRIPTED_VARIABLE_NAME)
            ?.firstChild(tree, SCRIPTED_VARIABLE_NAME_ID)
            ?.internNode(tree)?.toString() ?: return null
        val gameType = selectGameType(file)
        return ParadoxScriptScriptedVariableStubImpl(parentStub, name, gameType)
    }
    
    fun shouldCreateStub(node: ASTNode): Boolean {
        //仅当是全局的scripted_variable时才创建stub
        //val parentType = node.treeParent.elementType
        //if(parentType != ROOT_BLOCK) return false
        val file = selectFile(node.psi) ?: return false
        val entryPath = file.fileInfo?.entryPath?.path ?: return false
        return "common/scripted_variables".matchesPath(entryPath, acceptSelf = false)
    }
    
    fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        //仅当是全局的scripted_variable时才创建stub
        //val parentType = tree.getParent(node)?.tokenType
        //if(parentType != ROOT_BLOCK) return false
        val file = selectFile(parentStub.psi) ?: return false
        val entryPath = file.fileInfo?.entryPath?.path ?: return false
        return "common/scripted_variables".matchesPath(entryPath, acceptSelf = false)
    }
}