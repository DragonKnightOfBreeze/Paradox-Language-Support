package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.impl.*

object ParadoxScriptValueStubElementType : IStubElementType<ParadoxScriptValueStub, ParadoxScriptString>(
	"STRING",
	ParadoxScriptLanguage
) {
	override fun getExternalId(): String {
		return "paradoxScript.value"
	}
	
	override fun createPsi(stub: ParadoxScriptValueStub): ParadoxScriptString {
		return SmartParadoxScriptString(stub, this)
	}
	
	override fun createStub(psi: ParadoxScriptString, parentStub: StubElement<out PsiElement>): ParadoxScriptValueStub {
		val config = psi.getValueConfig()
		return when {
			config == null -> throw InternalError() //不期望的结果
			config.valueExpression.type == CwtDataTypes.ValueSet -> {
				val name = psi.value
				val valueSetName = config.valueExpression.value.orEmpty()
				ParadoxValueInValueSetStubImpl(parentStub, name, valueSetName)
			}
			else -> throw InternalError() //不期望的结果
		}
	}
	
	override fun shouldCreateStub(node: ASTNode?): Boolean {
		val psi = node?.psi?.castOrNull<ParadoxScriptString>() ?: return false
		val config = psi.getValueConfig()
		return when {
			config == null -> false
			config.valueExpression.type == CwtDataTypes.ValueSet -> {
				psi.isSimpleScriptExpression() //不带参数，不为复杂表达式
			}
			else -> false
		}
	}
	
	override fun indexStub(stub: ParadoxScriptValueStub, sink: IndexSink) {
		when{
			stub is ParadoxValueInValueSetStub -> {
				stub.valueSetName.takeIfNotEmpty()?.let { valueSetName -> sink.occurrence(ParadoxValueInValueSetIndex.key, valueSetName) }
			}
			else -> throw InternalError() //不期望的结果
		}
	}
	
	override fun serialize(stub: ParadoxScriptValueStub, dataStream: StubOutputStream) {
		when {
			stub is ParadoxValueInValueSetStub -> {
				dataStream.writeByte(stub.flag.toInt())
				dataStream.writeName(stub.name)
				dataStream.writeName(stub.valueSetName)
			}
			else -> throw InternalError() //不期望的结果
		}
	}
	
	override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): ParadoxScriptValueStub {
		val flag = dataStream.readByte()
		when(flag){
			ParadoxValueInValueSetStub.FLAG -> {
				val name = dataStream.readNameString().orEmpty()
				val valueSetName = dataStream.readNameString().orEmpty()
				return ParadoxValueInValueSetStubImpl(parentStub, name, valueSetName)
			}
			else -> throw InternalError() //不期望的结果
		}
	}
}