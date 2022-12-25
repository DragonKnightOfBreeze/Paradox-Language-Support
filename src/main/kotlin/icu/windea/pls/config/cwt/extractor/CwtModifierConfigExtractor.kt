package icu.windea.pls.config.cwt.extractor

/**
 * 用于从`logs/script_documentation/modifiers.log`生成`modifiers.cwt`。
 */
object CwtModifierConfigExtractor {
	data class ModifierConfig(
		val name: String,
		val categories: Set<String>
	)
	
	@JvmStatic
	fun main(args: Array<String>) {
		
	}
	
	fun extract(fromPath: String, toPath: String) {
		
	}
}