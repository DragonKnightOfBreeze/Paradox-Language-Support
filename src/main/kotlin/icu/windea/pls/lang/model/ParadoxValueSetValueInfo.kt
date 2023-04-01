package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*

data class ParadoxValueSetValueInfo(
	val name: String,
	val valueSetName: String,
	val readWriteAccess: ReadWriteAccessDetector.Access,
	val gameType: ParadoxGameType
	//TODO 保存作用域信息
)
