package com.windea.plugin.idea.pls.util

import com.intellij.psi.*
import com.windea.plugin.idea.pls.cwt.psi.*
import com.windea.plugin.idea.pls.model.*
import java.util.*

/**
 * Cwt配置文件的数据解析器。
 *
 * 返回值类型：[CwtConfig]
 */
object CwtConfigResolver {
	fun resolve(file: PsiFile): CwtConfig {
		if(file !is CwtFile) throw java.lang.IllegalArgumentException("Invalid file type")
		val rootBlock = file.rootBlock ?: return CwtConfig.empty
		return resolveRootBlock(rootBlock)
	}
	
	fun resolveRootBlock(rootBlock: CwtRootBlock): CwtConfig {
		return when {
			rootBlock.isEmpty -> CwtConfig.empty
			rootBlock.isArray -> CwtConfig(rootBlock.valueList.mapNotNull { resolveValue(it) }, emptyList())
			rootBlock.isObject -> CwtConfig(emptyList(), rootBlock.propertyList.mapNotNull { resolveProperty(it) })
			else -> CwtConfig.empty
		}
	}
	
	private fun resolveProperty(property: CwtProperty): CwtConfigProperty? {
		val key = property.propertyName
		val (documentation, options) = getDocumentationAndOptions(property)
		var value: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		val propertyValue = property.value
		when {
			propertyValue == null -> return null
			propertyValue is CwtString -> value = propertyValue.value
			propertyValue is CwtBlock -> {
				when {
					propertyValue.isEmpty -> {
						values = emptyList()
						properties = emptyList()
					}
					propertyValue.isArray -> {
						values = propertyValue.valueList.map { resolveValue(it) }
						properties = emptyList()
					}
					propertyValue.isObject -> {
						values = emptyList()
						properties = propertyValue.propertyList.mapNotNull { resolveProperty(it) }
					}
				}
			}
		}
		return CwtConfigProperty(key, value, values, properties, options, documentation)
	}
	
	private fun resolveValue(value: CwtValue): CwtConfigValue {
		val (documentation, options) = getDocumentationAndOptions(value)
		var _value: String? = null
		var values: List<CwtConfigValue>? = null
		var properties: List<CwtConfigProperty>? = null
		when {
			value is CwtString -> _value = value.value
			value is CwtBlock -> {
				when {
					value.isEmpty -> {
						values = emptyList()
						properties = emptyList()
					}
					value.isArray -> {
						values = value.valueList.map{ resolveValue(it) }
						properties = emptyList()
					}
					value.isObject -> {
						values = emptyList()
						properties = value.propertyList.mapNotNull { resolveProperty(it) }
					}
				}
			}
		}
		return CwtConfigValue(_value, values, properties, options, documentation)
	}
	
	private fun getDocumentationAndOptions(element: PsiElement): Pair<String, CwtConfigOptions> {
		var current = element
		val documentationComments = LinkedList<CwtDocumentationComment>()
		val optionComments = LinkedList<CwtOptionComment>()
		while(true) {
			current = current.prevSibling
			if(current == null) break
			when {
				current is PsiWhiteSpace || current is PsiComment -> continue
				current is CwtDocumentationComment -> documentationComments.addFirst(current)
				current is CwtOptionComment -> optionComments.addFirst(current)
				else -> break
			}
		}
		val documentation = getDocumentation(documentationComments)
		val options = getOptions(optionComments)
		return documentation to options
	}
	
	private fun getDocumentation(documentationComments: MutableList<CwtDocumentationComment>): String {
		return documentationComments.joinToString("\n") { it.documentationText?.text.orEmpty() }.trim()
	}
	
	private fun getOptions(optionComments: MutableList<CwtOptionComment>): CwtConfigOptions {
		var cardinality: String? = null
		var required: Boolean? = null
		var push_scope: String? = null
		var replace_scope: Map<String, String>? = null
		var severity: String? = null
		var scope: String? = null
		var scopes: List<String>? = null
		for(optionComment in optionComments) {
			for(value in optionComment.valueList) {
				when(value.value) {
					"required" -> required = true
				}
			}
			for(option in optionComment.optionList) {
				val optionName = option.name
				val optionValue = option.value ?: continue
				when(optionName) {
					"cardinality" -> {
						if(optionValue is CwtString) cardinality = optionValue.value
					}
					"push_scope" -> {
						if(optionValue is CwtString) push_scope = optionValue.value
					}
					"replace_scope" -> {
						if(optionValue is CwtBlock) {
							val map = mutableMapOf<String, String>()
							for(o in optionValue.optionList) {
								val n = o.optionName
								val v = o.value
								if(v is CwtString) map.put(n, v.value)
							}
							replace_scope = map
						}
					}
					"severity" -> {
						if(optionValue is CwtString) severity = optionValue.value
					}
					"scope" -> {
						when {
							optionValue is CwtString -> scope = optionValue.value
							optionValue is CwtBlock -> scopes = optionValue.valueList.map { it.value }
						}
					}
				}
			}
		}
		return CwtConfigOptions(cardinality, required, push_scope, replace_scope, severity, scope, scopes)
	}
}