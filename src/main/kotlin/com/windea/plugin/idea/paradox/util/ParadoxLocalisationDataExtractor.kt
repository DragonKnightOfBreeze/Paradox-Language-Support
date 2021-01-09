package com.windea.plugin.idea.paradox.util

import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

/**
 * Paradox本地化文件的数据提取器。
 *
 * 返回值类型：`Map<String,String>`
 */
object ParadoxLocalisationDataExtractor {
	fun extract(file: PsiFile):Map<String,String>{
		if(file !is ParadoxLocalisationFile) throw IllegalArgumentException("Invalid file type")

		val result = mutableMapOf<String,String>()
		file.forEachChild {
			when(it){
				//如果是property，则仅提取key和value放入
				is ParadoxLocalisationProperty -> {
					if(it.isValid) {
						val name = it.name
						val value = it.value
						if(name.isNotEmpty() && value != null) result[name] = value
					}
				}
			}
		}
		return result
	}
}