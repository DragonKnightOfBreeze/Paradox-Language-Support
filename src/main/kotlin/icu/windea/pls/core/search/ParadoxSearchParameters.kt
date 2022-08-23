package icu.windea.pls.core.search

import com.intellij.openapi.project.*
import com.intellij.psi.search.*
import icu.windea.pls.core.selector.*

interface ParadoxSearchParameters<T> {
	val project: Project
	val scope: SearchScope
	val selector: ChainedParadoxSelector<T>
}