package icu.windea.pls.localisation.psi

@Suppress("UNUSED_PARAMETER")
interface ParadoxLocalisationStubElementTypes {
	companion object {
		@JvmField val FILE = ParadoxLocalisationFileStubElementType
		@JvmField val PROPERTY = ParadoxLocalisationPropertyStubElementType
		
		@JvmStatic fun getPropertyType(name:String) = PROPERTY
	}
}
