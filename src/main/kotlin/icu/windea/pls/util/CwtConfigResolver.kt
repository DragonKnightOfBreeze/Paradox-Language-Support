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
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		val documentation: String?
		val options: List<CwtConfigOption>?
		val optionValues: List<CwtConfigOptionValue>?
		val separatorType = property.separatorType
		when {
			propValue is CwtBoolean -> booleanValue = propValue.booleanValue
			propValue is CwtInt -> intValue = propValue.intValue
			propValue is CwtFloat -> floatValue = propValue.floatValue
			propValue is CwtString -> stringValue = propValue.stringValue
			propValue is CwtBlock -> when {
				propValue.isEmpty -> {
					values = emptyList()
					properties = emptyList()
				}
				propValue.isArray -> {
					values = propValue.valueList.map { resolveValue(it) }
					properties = emptyList()
				}
				propValue.isObject -> {
					values = emptyList()
					properties = propValue.propertyList.mapNotNull { resolveProperty(it) }
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
		documentation = getDocumentation(documentationElements)
		options = getOptions(optionElements)
		optionValues = getOptionValues(optionValueElements)
		return CwtConfigProperty(
			key, property.propertyValue, booleanValue, intValue, floatValue, stringValue, values, properties,
			documentation, options, optionValues, separatorType
		)
	}
	
	private fun resolveValue(value: CwtValue): CwtConfigValue {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		val documentation: String?
		val options: List<CwtConfigOption>?
		val optionValues: List<CwtConfigOptionValue>?
		
		when {
			value is CwtBoolean -> booleanValue = value.booleanValue
			value is CwtInt -> intValue = value.intValue
			value is CwtFloat -> floatValue = value.floatValue
			value is CwtString -> stringValue = value.stringValue
			value is CwtBlock -> when {
				value.isEmpty -> {
					values = emptyList()
					properties = emptyList()
				}
				value.isArray -> {
					values = value.valueList.map { resolveValue(it) }
					properties = emptyList()
				}
				value.isObject -> {
					values = emptyList()
					properties = value.propertyList.mapNotNull { resolveProperty(it) }
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
		documentation = getDocumentation(documentationElements)
		options = getOptions(optionElements)
		optionValues = getOptionValues(optionValueElements)
		
		return CwtConfigValue(
			value.value, booleanValue, intValue, floatValue, stringValue, values, properties,
			documentation, options, optionValues
		)
	}
	
	private fun resolveOption(option: CwtOption): CwtConfigOption? {
		val key = option.optionName
		val optionValue = option.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtConfigOptionValue>? = null
		var options: List<CwtConfigOption>? = null
		val separatorType = option.separatorType
		when {
			optionValue is CwtBoolean -> booleanValue = optionValue.booleanValue
			optionValue is CwtInt -> intValue = optionValue.intValue
			optionValue is CwtFloat -> floatValue = optionValue.floatValue
			optionValue is CwtString -> stringValue = optionValue.stringValue
			optionValue is CwtBlock -> when {
				optionValue.isEmpty -> {
					values = emptyList()
					options = emptyList()
				}
				optionValue.isArray -> {
					values = optionValue.valueList.map { resolveOptionValue(it) }
					options = emptyList()
				}
				optionValue.isObject -> {
					values = emptyList()
					options = optionValue.optionList.mapNotNull { resolveOption(it) }
				}
			}
		}
		return CwtConfigOption(key, optionValue.value, booleanValue, intValue, floatValue, stringValue, values, options, separatorType)
	}
	
	private fun resolveOptionValue(option: CwtValue): CwtConfigOptionValue {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtConfigOptionValue>? = null
		var options: List<CwtConfigOption>? = null
		when {
			option is CwtBoolean -> {
				booleanValue = option.booleanValue
			}
			option is CwtInt -> {
				intValue = option.intValue
			}
			option is CwtFloat -> {
				floatValue = option.floatValue
			}
			option is CwtString -> {
				stringValue = option.stringValue
			}
			option is CwtBlock -> {
				when {
					option.isEmpty -> {
						values = emptyList()
						options = emptyList()
					}
					option.isArray -> {
						values = option.valueList.map { resolveOptionValue(it) }
						options = emptyList()
					}
					option.isObject -> {
						values = emptyList()
						options = option.optionList.mapNotNull { resolveOption(it) }
					}
				}
			}
		}
		return CwtConfigOptionValue(option.value, booleanValue, intValue, floatValue, stringValue, values, options)
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