package icu.windea.pls.core.util

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

/**
 * Paradox本地化文件的数据解析器。
 *
 * 返回值类型：`Map<String,String>`
 */
@Suppress("unused")
object ParadoxLocalisationDataResolver {
	/**
	 * 解析本地化文件的数据。跳过不合法的[PsiElement]，忽略重复的[ParadoxLocalisationProperty]。
	 */
	fun resolve(file: PsiFile): Map<String, Map<String, String>> {
		if(file !is ParadoxLocalisationFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxLocalisationFile')")
		val result: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
		file.processChildren file@{ fileItem ->
			if(!fileItem.isValid) return@file true
			when {
				fileItem is ParadoxLocalisationPropertyList -> {
					val locale = fileItem.findOptionalChild<ParadoxLocalisationLocale>() ?: return@file true
					val localeName = locale.name
					val properties = mutableMapOf<String, String>()
					result[localeName] = properties
					fileItem.processChildren list@{ listItem ->
						if(!listItem.isValid) return@list true
						when {
							listItem is ParadoxLocalisationProperty -> {
								resolveProperty(listItem, properties)
								true
							}
							else -> true
						}
					}
				}
				else -> true
			}
		}
		return result
	}
	
	fun resolveProperty(property: ParadoxLocalisationProperty, result: MutableMap<String, String>) {
		val name = property.name
		val value = property.value ?: return
		result[name] = value
	}
}

