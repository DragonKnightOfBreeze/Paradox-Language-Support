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
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptFileStubElementType : IStubFileElementType<PsiFileStub<*>>(ParadoxScriptLanguage) {
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
	
	override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
		if(stub is ParadoxScriptFileStub) {
			stub.name?.let { name -> sink.occurrence(ParadoxDefinitionNameIndex.key, name) }
			stub.type?.let { type -> sink.occurrence(ParadoxDefinitionTypeIndex.key, type) }
		}
		super.indexStub(stub, sink)
	}
	
	override fun serialize(stub: PsiFileStub<*>, dataStream: StubOutputStream) {
		if(stub is ParadoxScriptFileStub) {
			dataStream.writeName(stub.name)
			dataStream.writeName(stub.type)
			dataStream.writeName(stub.subtypes?.toCommaDelimitedString())
		}
		super.serialize(stub, dataStream)
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): PsiFileStub<*> {
		val name = dataStream.readNameString()
		val type = dataStream.readNameString()
		val subtypes = dataStream.readNameString()?.toCommaDelimitedStringList()
		return ParadoxScriptFileStubImpl(null, name, type, subtypes)
	}
	
	class Builder : DefaultStubBuilder() {
		override fun createStubForFile(file: PsiFile): StubElement<*> {
			val psiFile = file as? ParadoxScriptFile ?: return super.createStubForFile(file)
			val definitionInfo = psiFile.definitionInfo
			val name = definitionInfo?.name
			val type = definitionInfo?.type
			val subtypes = definitionInfo?.subtypes
			return ParadoxScriptFileStubImpl(psiFile, name, type, subtypes)
		}
		
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
