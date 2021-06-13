package icu.windea.pls.util

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.CwtConfig.Companion.EmptyCwtConfig
import icu.windea.pls.cwt.psi.*
import java.util.*

/**
 * Cwt配置文件的数据解析器。
 *
 * 返回值类型：[CwtConfig]
 */
object CwtConfigResolver {
	fun resolve(file: PsiFile): CwtConfig {
		if(file !is CwtFile) throw IllegalArgumentException("Invalid file type (expect: 'CwtFile')")
		val rootBlock = file.rootBlock ?: return EmptyCwtConfig
		return resolveRootBlock(rootBlock)
	}
	
	fun resolveRootBlock(rootBlock: CwtRootBlock): CwtConfig {
		return when {
			rootBlock.isEmpty -> EmptyCwtConfig
			rootBlock.isArray -> CwtConfig(rootBlock.valueList.mapNotNull { resolveValue(it) }, emptyList())
			rootBlock.isObject -> CwtConfig(emptyList(), rootBlock.propertyList.mapNotNull { resolveProperty(it) })
			else -> EmptyCwtConfig
		}
	}
	
	private fun resolveProperty(property: CwtProperty): CwtConfigProperty? {
		val key = property.propertyName
		val propValue = property.value ?: return null
		val resolved = CwtConfigProperty(key, property.propertyValue)
		
		when {
			propValue is CwtBoolean -> resolved.booleanValue = propValue.booleanValue
			propValue is CwtInt -> resolved.intValue = propValue.intValue
			propValue is CwtFloat -> resolved.floatValue = propValue.floatValue
			propValue is CwtString -> resolved.stringValue = propValue.stringValue
			propValue is CwtBlock -> when {
				propValue.isEmpty -> {
					resolved.values = emptyList()
					resolved.properties = emptyList()
				}
				propValue.isArray -> {
					resolved.values = propValue.valueList.map { resolveValue(it) }
					resolved.properties = emptyList()
				}
				propValue.isObject -> {
					resolved.values = emptyList()
					resolved.properties = propValue.propertyList.mapNotNull { resolveProperty(it) }
				}
			}
		}
		
		var current: PsiElement = property
		val documentationElements = LinkedList<CwtDocumentationText>()
		val optionElements = LinkedList<CwtOption>()
		val optionValueElements = LinkedList<CwtValue>()
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is PsiWhiteSpace || current is PsiComment -> continue
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) documentationElements.addFirst(documentationText)
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						optionElements.addFirst(option)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							optionValueElements.addFirst(optionValue)
						}
					}
				}
				else -> break
			}
		}
		resolved.documentation = getDocumentation(documentationElements)
		resolved.options = getOptions(optionElements)
		resolved.optionValues = getOptionValues(optionValueElements)
		
		return resolved
	}
	
	private fun resolveValue(value: CwtValue): CwtConfigValue {
		val resolved = CwtConfigValue(value.value)
		
		when {
			value is CwtBoolean -> resolved.booleanValue = value.booleanValue
			value is CwtInt -> resolved.intValue = value.intValue
			value is CwtFloat -> resolved.floatValue = value.floatValue
			value is CwtString -> resolved.stringValue = value.stringValue
			value is CwtBlock -> when {
				value.isEmpty -> {
					resolved.values = emptyList()
					resolved.properties = emptyList()
				}
				value.isArray -> {
					resolved.values = value.valueList.map { resolveValue(it) }
					resolved.properties = emptyList()
				}
				value.isObject -> {
					resolved.values = emptyList()
					resolved.properties = value.propertyList.mapNotNull { resolveProperty(it) }
				}
			}
		}
		
		var current: PsiElement = value
		val documentationElements = LinkedList<CwtDocumentationText>()
		val optionElements = LinkedList<CwtOption>()
		val optionValueElements = LinkedList<CwtValue>()
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is PsiWhiteSpace || current is PsiComment -> continue
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) documentationElements.addFirst(documentationText)
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						optionElements.addFirst(option)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							optionValueElements.addFirst(optionValue)
						}
					}
				}
				else -> break
			}
		}
		resolved.documentation = getDocumentation(documentationElements)
		resolved.options = getOptions(optionElements)
		resolved.optionValues = getOptionValues(optionValueElements)
		
		return resolved
	}
	
	private fun resolveOption(option: CwtOption): CwtConfigOption? {
		val key = option.optionName
		val separator = CwtConfigSeparator.resolve(option.optionSeparator?.text) ?: return null
		val optionValue = option.value ?: return null
		val resolved = CwtConfigOption(key, separator, optionValue.value)
		when {
			optionValue is CwtBoolean -> resolved.booleanValue = optionValue.booleanValue
			optionValue is CwtInt -> resolved.intValue = optionValue.intValue
			optionValue is CwtFloat -> resolved.floatValue = optionValue.floatValue
			optionValue is CwtString -> resolved.stringValue = optionValue.stringValue
			optionValue is CwtBlock -> when {
				optionValue.isEmpty -> {
					resolved.values = emptyList()
					resolved.options = emptyList()
				}
				optionValue.isArray -> {
					resolved.values = optionValue.valueList.map { resolveOptionValue(it) }
					resolved.options = emptyList()
				}
				optionValue.isObject -> {
					resolved.values = emptyList()
					resolved.options = optionValue.optionList.mapNotNull { resolveOption(it) }
				}
			}
		}
		
		return resolved
	}
	
	private fun resolveOptionValue(option: CwtValue): CwtConfigOptionValue {
		val resolved = CwtConfigOptionValue(option.value)
		when {
			option is CwtBoolean -> {
				resolved.booleanValue = option.booleanValue
			}
			option is CwtInt -> {
				resolved.intValue = option.intValue
			}
			option is CwtFloat -> {
				resolved.floatValue = option.floatValue
			}
			option is CwtString -> {
				resolved.stringValue = option.stringValue
			}
			option is CwtBlock -> {
				when {
					option.isEmpty -> {
						resolved.values = emptyList()
						resolved.options = emptyList()
					}
					option.isArray -> {
						resolved.values = option.valueList.map { resolveOptionValue(it) }
						resolved.options = emptyList()
					}
					option.isObject -> {
						resolved.values = emptyList()
						resolved.options = option.optionList.mapNotNull { resolveOption(it) }
					}
				}
			}
		}
		
		return resolved
	}
	
	private fun getDocumentation(documentationElements: List<CwtDocumentationText>): String? {
		if(documentationElements.isEmpty()) return null
		return documentationElements.joinToString("\n") { it.text.orEmpty() }.trim()
	}
	
	private fun getOptions(optionElements: List<CwtOption>): List<CwtConfigOption>? {
		if(optionElements.isEmpty()) return null
		val options = mutableListOf<CwtConfigOption>()
		for(optionElement in optionElements) {
			val resolved = resolveOption(optionElement) ?: continue
			options.add(resolved)
		}
		return options
	}
	
	private fun getOptionValues(optionValueElements: List<CwtValue>): List<CwtConfigOptionValue>? {
		if(optionValueElements.isEmpty()) return null
		val optionValues = mutableListOf<CwtConfigOptionValue>()
		for(optionValueElement in optionValueElements) {
			val resolved = resolveOptionValue(optionValueElement)
			optionValues.add(resolved)
		}
		return optionValues
	}
}