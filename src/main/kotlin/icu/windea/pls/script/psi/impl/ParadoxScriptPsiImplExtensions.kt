package icu.windea.pls.script.psi.impl

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*

class SmartParadoxScriptProperty : ParadoxScriptPropertyImpl {
	constructor(node: ASTNode) : super(node)
	
	constructor(stub: ParadoxScriptPropertyStub, type: IStubElementType<*, *>) : super(stub, type)
	
	@Volatile private var _name: String? = null
	@Volatile private var _value: String? = null
	@Volatile private var _pathName: String? = null
	@Volatile private var _originalPathName: String? = null
	@Volatile private var _parameterNames: Set<String>? = null
	
	override fun getName(): String {
		return _name ?: super.getName().also { _name = it }
	}
	
	override fun getValue(): String? {
		return _value ?: super.getValue().also { _value = it }
	}
	
	override val pathName: String?
		get() = _pathName ?: super.pathName.also { _pathName = it }
	
	override val originalPathName: String
		get() = _originalPathName ?: super.originalPathName.also { _originalPathName = it }
	
	internal fun getParameterNames(): Set<String>? {
		return _parameterNames ?: doGetParameterNames().also { _parameterNames = it }
	}
	
	private fun doGetParameterNames(): Set<String>? {
		if(!CwtConfigHandler.supportsParameters(this)) return null
		val result = sortedSetOf<String>() //按名字进行排序
		acceptChildren(object: PsiRecursiveElementVisitor(){
			override fun visitElement(element: PsiElement) {
				if(element is IParadoxScriptParameter){
					result.add(element.name)
					return
				}
				super.visitElement(element)
			}
		})
		return result
	}
	
	override fun subtreeChanged() {
		_name = null
		_value = null
		_pathName = null
		_originalPathName = null
		_parameterNames = null
		super.subtreeChanged()
	}
}

/**
 * 得到特定定义声明（scripted_effect/scripted_trigger）中所有被引用的参数的名字。
 */
val ParadoxDefinitionProperty.parameterNames: Set<String>? get() = if(this is SmartParadoxScriptProperty) this.getParameterNames() else null 