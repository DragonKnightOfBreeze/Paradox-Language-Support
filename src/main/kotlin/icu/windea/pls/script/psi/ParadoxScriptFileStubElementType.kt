package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptStubElementTypes.Companion.FILE
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

class ParadoxScriptFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
	override fun getExternalId(): String {
		return "paradoxScript.file"
	}
	
	override fun getBuilder(): StubBuilder {
		return Builder()
	}
	
	override fun shouldBuildStubFor(file: VirtualFile?): Boolean {
		//仅为合法的paradox文件创建索引
		try {
			return file?.fileInfo?.rootType != null
		} catch(e: Exception) {
			return false
		}
	}
	
	class Builder : DefaultStubBuilder() {
		override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
			//仅包括variable和property
			val type = node.elementType
			val parentType = parent.elementType
			return when {
				parentType == FILE && type != ROOT_BLOCK -> true
				parentType == FILE -> false
				type == PROPERTY -> false
				type == VARIABLE -> false
				parentType == PROPERTY || parentType == PROPERTY_VALUE -> false
				parentType == ROOT_BLOCK || parentType == BLOCK -> true
				else -> true
			}
		}
	}
}
