package com.windea.plugin.idea.paradox.script.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptStubElementTypes.Companion.FILE
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
	override fun getExternalId(): String {
		return "paradoxScript.file"
	}
	
	override fun getBuilder(): StubBuilder {
		return Builder()
	}
	
	override fun shouldBuildStubFor(file: VirtualFile?): Boolean {
		return file?.paradoxFileInfo?.rootType != null
	}
	
	class Builder : DefaultStubBuilder() {
		override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
			//仅包括作为scripted_variable的顶级的variable，以及可能作为类型定义的1-2级的property
			val type = node.elementType
			val parentType = parent.elementType
			return when {
				parentType == FILE && type !== ROOT_BLOCK -> true
				//parentType == ROOT_BLOCK && type != PROPERTY && type != VARIABLE -> true
				parentType == ROOT_BLOCK && type != PROPERTY && (type != VARIABLE || (parent.treeParent.psi as PsiFile)
					.parent?.let{ it.name != "scripted_variables" }?:true)  -> true
				parentType == BLOCK && type != PROPERTY -> true
				parent.treeParent?.treeParent?.treeParent?.elementType == ROOT_BLOCK && type != PROPERTY -> true
				type == ROOT_BLOCK || type == BLOCK || type == PROPERTY || type == PROPERTY_VALUE || type == VARIABLE -> false
				else -> true
			}
		}
	}
}
