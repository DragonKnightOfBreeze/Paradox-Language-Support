package icu.windea.pls

import com.intellij.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import org.jetbrains.annotations.*
import java.util.function.*

@Suppress("unused")
object PlsDocBundle {
    @NonNls
    private const val BUNDLE = "messages.PlsDocBundle"
    private val INSTANCE = DynamicBundle(PlsDocBundle::class.java, BUNDLE)

    @JvmStatic
    @Nls
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    @Nls
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<String> {
        return INSTANCE.getLazyMessage(key, *params)
    }

    //methods to get specific messages

    @JvmStatic
    @Nls
    fun locale(name: String): String {
        return INSTANCE.messageOrNull("locale.$name")
            ?: name
    }

    @JvmStatic
    @Nls
    fun eventType(name: String, gameType: ParadoxGameType?): String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.event.type.$name")
            ?: INSTANCE.messageOrNull("general.event.type.$name")
            ?: INSTANCE.getMessage("default.event.type", name)
    }

    @JvmStatic
    @Nls
    fun eventAttribute(name: String, gameType: ParadoxGameType?): String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.event.attribute.$name")
            ?: INSTANCE.messageOrNull("general.event.attribute.$name")
            ?: INSTANCE.getMessage("general.event.attribute.default", name)
    }

    @JvmStatic
    @Nls
    fun technologyTier(name: String, gameType: ParadoxGameType?): String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.tier.$name")
            ?: INSTANCE.messageOrNull("general.technology.tier.$name")
            ?: INSTANCE.getMessage("default.technology.tier", name)
    }

    @JvmStatic
    @Nls
    fun technologyArea(name: String, gameType: ParadoxGameType?, project: Project, context: Any? = null): String {
        run {
            val selector = selector(project, context).localisation().contextSensitive()
                .withGameType(gameType)
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            val localisation = ParadoxLocalisationSearch.search(name.uppercase(), selector).find() ?: return@run
            val text = ParadoxLocalisationTextRenderer().render(localisation).orNull()
            if (text != null) return text
        }

        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.area.$name")
            ?: INSTANCE.messageOrNull("general.technology.area.$name")
            ?: INSTANCE.getMessage("default.technology.area", name)
    }

    @JvmStatic
    @Nls
    fun technologyCategory(name: String, gameType: ParadoxGameType?, project: Project, context: Any? = null): String {
        run {
            val selector = selector(project, context).definition().contextSensitive()
                .withGameType(gameType)
            val definition = ParadoxDefinitionSearch.search(name, ParadoxDefinitionTypes.TechnologyCategory, selector).find() ?: return@run
            val localizedName = ParadoxDefinitionManager.getPrimaryLocalisation(definition)
            if (localizedName != null) {
                val text = ParadoxLocalisationTextRenderer().render(localizedName).orNull()
                if (text != null) return text
            }
        }

        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.category.$name")
            ?: INSTANCE.messageOrNull("general.technology.category.$name")
            ?: INSTANCE.getMessage("default.technology.category", name)
    }

    @JvmStatic
    @Nls
    fun technologyAttribute(name: String, gameType: ParadoxGameType?): String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.attribute.$name")
            ?: INSTANCE.messageOrNull("general.technology.attribute.$name")
            ?: INSTANCE.getMessage("default.technology.attribute", name)
    }
}
