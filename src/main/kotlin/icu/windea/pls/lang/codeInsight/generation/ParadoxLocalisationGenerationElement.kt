package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.generation.ClassMember
import com.intellij.codeInsight.generation.MemberChooserObject
import com.intellij.codeInsight.generation.MemberChooserObjectBase
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightInfo
import javax.swing.Icon

sealed class ParadoxLocalisationGenerationElement(text: String, icon: Icon? = null) : MemberChooserObjectBase(text, icon) {
    data class Item(
        val name: String,
        val info: ParadoxLocalisationCodeInsightInfo,
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Localisation), ClassMember {
        override fun getParentNodeDelegate(): MemberChooserObject? {
            return when (context.type) {
                Type.Definition -> Definition(context.name)
                Type.Modifier -> Modifier(context.name)
                Type.LocalisationReference -> LocalisationReferences()
                Type.SyncedLocalisationReference -> SyncedLocalisationReferences()
                Type.Localisation -> Localisations()
                else -> null
            }
        }

        override fun equals(other: Any?) = this === other || (other is Item && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "item: $name"
    }

    class Definition(val name: String) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Definition) {
        override fun equals(other: Any?) = this === other || (other is Definition && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "definition group: $name"
    }

    class Modifier(val name: String) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Modifier) {
        override fun equals(other: Any?) = this === other || (other is Modifier && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "modifier group: $name"
    }

    class LocalisationReferences : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.localisationReferences")) {
        override fun equals(other: Any?) = this === other || (other is LocalisationReferences)

        override fun hashCode() = 0

        override fun toString() = "<localisation references>"
    }

    class SyncedLocalisationReferences : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.syncedLocalisationReferences")) {
        override fun equals(other: Any?) = this === other || (other is SyncedLocalisationReferences)

        override fun hashCode() = 0

        override fun toString() = "<synced localisation references>"
    }

    class Localisations : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.localisations")) {
        override fun equals(other: Any?) = this === other || (other is Localisations)

        override fun hashCode() = 0

        override fun toString() = "<localisations>"
    }
}
