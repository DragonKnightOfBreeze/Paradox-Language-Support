package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.generation.ClassMember
import com.intellij.codeInsight.generation.MemberChooserObject
import com.intellij.codeInsight.generation.MemberChooserObjectBase
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightInfo
import javax.swing.Icon

sealed class ParadoxLocalisationGenerationElement(text: String, icon: Icon? = null) : MemberChooserObjectBase(text, icon) {
    data class Item(
        val name: String,
        val info: ParadoxLocalisationCodeInsightInfo,
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Localisation), ClassMember {
        override fun getParentNodeDelegate(): MemberChooserObject? {
            return when (context.type) {
                Type.Definition -> Definition(context.name, context)
                Type.Modifier -> Modifier(context.name, context)
                Type.LocalisationReference -> LocalisationReferences(context)
                Type.SyncedLocalisationReference -> SyncedLocalisationReferences(context)
                Type.Localisation -> Localisations(context)
                else -> null
            }
        }

        override fun equals(other: Any?) = this === other || (other is Item && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "item: $name"
    }

    data class Definition(
        val name: String,
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Definition) {
        override fun equals(other: Any?) = this === other || (other is Definition && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "definition group: $name"
    }

    data class Modifier(
        val name: String,
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(name, PlsIcons.Nodes.Modifier) {
        override fun equals(other: Any?) = this === other || (other is Modifier && name == other.name)

        override fun hashCode() = name.hashCode()

        override fun toString() = "modifier group: $name"
    }

    data class LocalisationReferences(
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.localisationReferences")) {
        override fun equals(other: Any?) = this === other || (other is LocalisationReferences)

        override fun hashCode() = 0

        override fun toString() = "<localisation references>"
    }

    data class SyncedLocalisationReferences(
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.syncedLocalisationReferences")) {
        override fun equals(other: Any?) = this === other || (other is SyncedLocalisationReferences)

        override fun hashCode() = 0

        override fun toString() = "<synced localisation references>"
    }

    data class Localisations(
        val context: ParadoxLocalisationCodeInsightContext
    ) : ParadoxLocalisationGenerationElement(PlsBundle.message("generation.localisation.localisations")) {
        override fun equals(other: Any?) = this === other || (other is Localisations)

        override fun hashCode() = 0

        override fun toString() = "<localisations>"
    }
}
