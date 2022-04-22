package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*

interface ParadoxLocalisationStub: StubElement<ParadoxLocalisationProperty> {
	val name:String
	val category: ParadoxLocalisationCategory
	//注意不要添加localeConfig作为属性，因为决定这个的在对应的PsiElement结构之外
}

