package com.windea.plugin.idea.pls.cwt.psi

import com.intellij.psi.tree.*
import com.windea.plugin.idea.pls.cwt.CwtLanguage

class CwtElementType(
	debugName: String
): IElementType(debugName, CwtLanguage)