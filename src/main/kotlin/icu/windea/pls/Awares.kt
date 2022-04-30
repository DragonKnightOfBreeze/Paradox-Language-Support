package icu.windea.pls

import javax.swing.Icon

interface IdAware {
	val id: String
}

interface DescriptionAware {
	val description: String
}

interface IconAware {
	val icon: Icon
}

interface TextAware {
	val text: String
}