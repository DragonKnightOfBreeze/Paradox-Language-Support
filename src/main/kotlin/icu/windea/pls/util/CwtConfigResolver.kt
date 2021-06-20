package icu.windea.pls.util

import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.psi.*
import java.util.*

/**
 * Cwt配置文件的解析器。
 *
 * 返回值类型：[CwtFileConfig]
 */
object CwtConfigResolver {
	fun resolve(file: PsiFile): CwtFileConfig {
		if(file !is CwtFile) throw IllegalArgumentException("Invalid file type (expect: 'CwtFile')")
		val rootBlock = file.block ?: return EmptyCwtConfig
		return when {
			rootBlock.isEmpty -> EmptyCwtConfig
			rootBlock.isArray -> {
				val values = rootBlock.valueList.mapNotNull { resolveValue(it) }
				CwtFileConfig(emptyPointer(), values, emptyList())
			}
			rootBlock.isObject -> {
				val properties = rootBlock.propertyList.mapNotNull { resolveProperty(it) }
				CwtFileConfig(emptyPointer(), emptyList(), properties)
			}
			else -> EmptyCwtConfig
		}
	}
	
	fun resolveProperty(property: CwtProperty): CwtPropertyConfig? {
		val pointer = property.createSmartPointer()
		val key = property.propertyName
		val propValue = property.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtValueConfig>? = null
		var properties: List<CwtPropertyConfig>? = null
		val documentation: String?
		val options: List<CwtOptionConfig>?
		val optionValues: List<CwtOptionValueConfig>?
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
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		documentation = getDocumentation(documentationElements)
		options = getOptions(optionElements)
		optionValues = getOptionValues(optionValueElements)
		return CwtPropertyConfig(
			pointer, key, property.propertyValue, booleanValue, intValue, floatValue, stringValue, values, properties,
			documentation, options, optionValues, separatorType
		)
	}
	
	fun resolveValue(value: CwtValue): CwtValueConfig {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtValueConfig>? = null
		var properties: List<CwtPropertyConfig>? = null
		val documentation: String?
		val options: List<CwtOptionConfig>?
		val optionValues: List<CwtOptionValueConfig>?
		
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
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		documentation = getDocumentation(documentationElements)
		options = getOptions(optionElements)
		optionValues = getOptionValues(optionValueElements)
		
		return CwtValueConfig(
			value.createSmartPointer(), value.value, booleanValue, intValue, floatValue, stringValue,
			values, properties, documentation, options, optionValues
		)
	}
	
	fun resolveOption(option: CwtOption): CwtOptionConfig? {
		val key = option.optionName
		val optionValue = option.value ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtOptionValueConfig>? = null
		var options: List<CwtOptionConfig>? = null
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
		return CwtOptionConfig(
			emptyPointer(),key, optionValue.value, 
			booleanValue, intValue, floatValue, stringValue, values, options, separatorType
		)
	}
	
	fun resolveOptionValue(option: CwtValue): CwtOptionValueConfig {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var values: List<CwtOptionValueConfig>? = null
		var options: List<CwtOptionConfig>? = null
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
		return CwtOptionValueConfig(
			emptyPointer(),option.value,
			booleanValue, intValue, floatValue, stringValue, values, options
		)
	}
	
	private fun getDocumentation(documentationElements: List<CwtDocumentationText>): String? {
		if(documentationElements.isEmpty()) return null
		return documentationElements.joinToString("\n") { it.text.orEmpty() }.trim()
	}
	
	private fun getOptions(optionElements: List<CwtOption>): List<CwtOptionConfig>? {
		if(optionElements.isEmpty()) return null
		val options = mutableListOf<CwtOptionConfig>()
		for(optionElement in optionElements) {
			val resolved = resolveOption(optionElement) ?: continue
			options.add(resolved)
		}
		return options
	}
	
	private fun getOptionValues(optionValueElements: List<CwtValue>): List<CwtOptionValueConfig>? {
		if(optionValueElements.isEmpty()) return null
		val optionValues = mutableListOf<CwtOptionValueConfig>()
		for(optionValueElement in optionValueElements) {
			val resolved = resolveOptionValue(optionValueElement)
			optionValues.add(resolved)
		}
		return optionValues
	}
}