package icu.windea.pls.script.psi

interface ParadoxValueSetValueStub : ParadoxScriptValueStub {
	val name: String
	val valueSetName: String
	//TODO 保存作用域信息
	
	override val flag: Byte get() = FLAG
	
	companion object{
		const val FLAG: Byte = 1
	}
}