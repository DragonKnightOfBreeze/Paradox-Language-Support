package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage){
	override fun getExternalId(): String {
		return "paradoxLocalisation.file"
	}
	
	override fun getStubVersion(): Int {
		return 1 //0.6.7
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
	
	class Builder: DefaultStubBuilder(){
		override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
			//仅包括propertyList和property
			return when{
				node.elementType == PROPERTY_LIST -> false
				node.elementType == PROPERTY -> false
				else -> true
			}
		}
	}
}
