package icu.windea.pls.config.cwt

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import java.lang.invoke.*
import java.util.*

/**
 * Cwt规则的解析器。
 */
object CwtConfigResolver {
	private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
	
	fun resolve(file: CwtFile, info: CwtConfigGroupInfo): CwtFileConfig {
		val rootBlock = file.block
		val properties = SmartList<CwtPropertyConfig>()
		val values = SmartList<CwtValueConfig>()
		val fileConfig = CwtFileConfig(file.createPointer(), info, properties, values, file.name)
		rootBlock?.processChild {
			when {
				it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(properties).end()
				it is CwtValue -> resolveValue(it, file, fileConfig).addTo(values).end()
				else -> end()
			}
		}
		return fileConfig
	}
	
	private fun resolveProperty(property: CwtProperty, file: CwtFile, fileConfig: CwtFileConfig): CwtPropertyConfig? {
		val pointer = property.createPointer(file)
		val key = property.name
		val propertyValue = property.propertyValue
		if(propertyValue == null) {
			logger.error("Incorrect cwt config in ${fileConfig.name}\n${property.text}")
			return null
		}
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var configs: List<CwtDataConfig<*>>? = null
		var documentationLines: LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues: LinkedList<CwtOptionValueConfig>? = null
		val separatorType = property.separatorType
		when {
			propertyValue is CwtBoolean -> booleanValue = propertyValue.booleanValue
			propertyValue is CwtInt -> intValue = propertyValue.intValue
			propertyValue is CwtFloat -> floatValue = propertyValue.floatValue
			propertyValue is CwtString -> stringValue = propertyValue.stringValue
			propertyValue is CwtBlock -> when {
				propertyValue.isEmpty -> {
					configs = emptyList()
				}
				else -> {
					configs = SmartList()
					propertyValue.processChild {
						when {
							it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(configs).end()
							it is CwtValue -> resolveValue(it, file, fileConfig).addTo(configs).end()
							else -> end()
						}
					}
				}
			}
		}
		
		var current: PsiElement = property
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						if(options == null) options = LinkedList()
						val resolvedOption = resolveOption(option, file, fileConfig)
						if(resolvedOption != null) options.addFirst(resolvedOption)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
							optionValues.addFirst(resolvedOptionValue)
						}
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		val documentation = documentationLines?.joinToString("\n")
		
		val config = CwtPropertyConfig(
			pointer, fileConfig.info, key, propertyValue.value,
			booleanValue, intValue, floatValue, stringValue, configs,
			documentation, options, optionValues, separatorType
		)
		fileConfig.info.acceptConfigExpression(config.keyExpression, config.parent)
		fileConfig.info.acceptConfigExpression(config.valueExpression, config.parent)
		configs?.forEach { it.parent = config }
		return config
	}
	
	private fun resolveValue(value: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtValueConfig {
		val pointer = value.createPointer(file)
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var configs: List<CwtDataConfig<*>>? = null
		var documentationLines: LinkedList<String>? = null
		var options: LinkedList<CwtOptionConfig>? = null
		var optionValues: LinkedList<CwtOptionValueConfig>? = null
		
		when {
			value is CwtBoolean -> booleanValue = value.booleanValue
			value is CwtInt -> intValue = value.intValue
			value is CwtFloat -> floatValue = value.floatValue
			value is CwtString -> stringValue = value.stringValue
			value is CwtBlock -> when {
				value.isEmpty -> {
					configs = emptyList()
				}
				else -> {
					configs = SmartList()
					value.processChild {
						when {
							it is CwtProperty -> resolveProperty(it, file, fileConfig)?.addTo(configs).end()
							it is CwtValue -> resolveValue(it, file, fileConfig).addTo(configs).end()
							else -> end()
						}
					}
				}
			}
		}
		
		var current: PsiElement = value
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is CwtOptionComment -> {
					val option = current.option
					if(option != null) {
						if(options == null) options = LinkedList()
						val resolvedOption = resolveOption(option, file, fileConfig)
						if(resolvedOption != null) options.addFirst(resolvedOption)
					} else {
						val optionValue = current.value
						if(optionValue != null) {
							if(optionValues == null) optionValues = LinkedList()
							val resolvedOptionValue = resolveOptionValue(optionValue, file, fileConfig)
							optionValues.addFirst(resolvedOptionValue)
						}
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		val documentation = documentationLines?.joinToString("\n")
		
		val config = CwtValueConfig(
			pointer, fileConfig.info, value.value,
			booleanValue, intValue, floatValue, stringValue, configs, 
			documentation, options, optionValues
		)
		fileConfig.info.acceptConfigExpression(config.valueExpression, config.parent)
		configs?.forEach { it.parent = config }
		return config
	}
	
	private fun resolveOption(option: CwtOption, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionConfig? {
		val key = option.name
		val optionValue = option.optionValue ?: return null
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var options: List<CwtOptionConfig>? = null
		var optionValues: List<CwtOptionValueConfig>? = null
		val separatorType = option.separatorType
		when {
			optionValue is CwtBoolean -> booleanValue = optionValue.booleanValue
			optionValue is CwtInt -> intValue = optionValue.intValue
			optionValue is CwtFloat -> floatValue = optionValue.floatValue
			optionValue is CwtString -> stringValue = optionValue.stringValue
			optionValue is CwtBlock -> when {
				optionValue.isEmpty -> {
					options = emptyList()
					optionValues = emptyList()
				}
				else -> {
					options = SmartList()
					optionValues = SmartList()
					optionValue.processChild {
						when {
							it is CwtOption -> resolveOption(it, file, fileConfig)?.addTo(options).end()
							it is CwtValue -> resolveOptionValue(it, file, fileConfig).addTo(optionValues).end()
							else -> end()
						}
					}
				}
			}
		}
		return CwtOptionConfig(
			emptyPointer(), fileConfig.info, key, optionValue.value,
			booleanValue, intValue, floatValue, stringValue, options, optionValues, separatorType
		)
	}
	
	private fun resolveOptionValue(option: CwtValue, file: CwtFile, fileConfig: CwtFileConfig): CwtOptionValueConfig {
		var booleanValue: Boolean? = null
		var intValue: Int? = null
		var floatValue: Float? = null
		var stringValue: String? = null
		var options: List<CwtOptionConfig>? = null
		var optionValues: List<CwtOptionValueConfig>? = null
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
						options = emptyList()
						optionValues = emptyList()
					}
					else -> {
						options = SmartList()
						optionValues = SmartList()
						option.processChild {
							when {
								it is CwtOption -> resolveOption(it, file, fileConfig)?.addTo(options).end()
								it is CwtValue -> resolveOptionValue(it, file, fileConfig).addTo(optionValues).end()
								else -> end()
							}
						}
					}
				}
			}
		}
		return CwtOptionValueConfig(
			emptyPointer(), fileConfig.info, option.value,
			booleanValue, intValue, floatValue, stringValue, options, optionValues
		)
	}
}
