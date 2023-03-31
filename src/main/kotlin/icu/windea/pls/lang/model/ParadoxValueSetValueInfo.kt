package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*

data class ParadoxValueSetValueInfo(
	val name: String,
	val valueSetName: String,
	val gameType: ParadoxGameType,
	val readWriteAccess: ReadWriteAccessDetector.Access
	//TODO 保存作用域信息
)
