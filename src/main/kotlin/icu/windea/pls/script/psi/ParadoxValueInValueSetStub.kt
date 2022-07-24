package icu.windea.pls.script.psi

interface ParadoxValueInValueSetStub : ParadoxScriptValueStub {
	val name: String
	val valueSetName: String
	
	override val flag: Byte get() = FLAG
	
	companion object{
		const val FLAG: Byte = 1
	}
}