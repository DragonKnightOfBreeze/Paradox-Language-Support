package icu.windea.pls.localisation.psi

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

object ParadoxLocalisationFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxLocalisationLanguage){
	private const val externalId = "paradoxLocalisation.file"
	private const val stubVersion = 3 //0.7.4
	
	override fun getExternalId() = externalId
	
	override fun getStubVersion() = stubVersion
	
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
			return when {
				node.elementType == LOCALE -> true
				node.elementType == PROPERTY_LIST -> false
				node.elementType == PROPERTY -> false
				parent.elementType == PROPERTY -> true
				else -> true
			}
		}
	}
}
