package icu.windea.pls.lang

import com.intellij.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.model.*
import org.jetbrains.annotations.*
import java.util.function.*

@NonNls
private const val BUNDLE = "messages.PlsDocBundle"

object PlsDocBundle {
    private val INSTANCE = DynamicBundle(PlsDocBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): @Nls String {
        return INSTANCE.getMessage(key, *params)
    }

    @JvmStatic
    fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): Supplier<@Nls String> {
        return INSTANCE.getLazyMessage(key, *params)
    }

    @JvmStatic
    fun eventType(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.event.type.$name")
            ?: INSTANCE.messageOrNull("general.event.type.$name")
            ?: INSTANCE.getMessage("default.event.type", name)
    }

    @JvmStatic
    fun eventAttribute(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.event.attribute.$name")
            ?: INSTANCE.messageOrNull("general.event.attribute.$name")
            ?: INSTANCE.getMessage("general.event.attribute.default", name)
    }

    @JvmStatic
    fun technologyTier(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.tier.$name")
            ?: INSTANCE.messageOrNull("general.technology.tier.$name")
            ?: INSTANCE.getMessage("default.technology.tier", name)
    }

    @JvmStatic
    fun technologyResearchArea(name: String, gameType: ParadoxGameType?, project: Project, context: Any? = null): @Nls String {
        run {
            val selector = selector(project, context).localisation().contextSensitive()
                .withGameType(gameType)
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            val localisation = ParadoxLocalisationSearch.search(name.uppercase(), selector).find() ?: return@run
            val text = ParadoxLocalisationTextRenderer.render(localisation).orNull()
            if (text != null) return text
        }

        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.area.$name")
            ?: INSTANCE.messageOrNull("general.technology.area.$name")
            ?: INSTANCE.getMessage("default.technology.area", name)
    }

    @JvmStatic
    fun technologyCategory(name: String, gameType: ParadoxGameType?, project: Project, context: Any? = null): @Nls String {
        run {
            val selector = selector(project, context).definition().contextSensitive()
                .withGameType(gameType)
            val definition = ParadoxDefinitionSearch.search(name, "technology_category", selector).find() ?: return@run
            val localizedName = ParadoxDefinitionManager.getPrimaryLocalisation(definition)
            if (localizedName != null) {
                val text = ParadoxLocalisationTextHtmlRenderer.render(localizedName).orNull()
                if (text != null) return text
            }
        }

        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.category.$name")
            ?: INSTANCE.messageOrNull("general.technology.category.$name")
            ?: INSTANCE.getMessage("default.technology.category", name)
    }

    @JvmStatic
    fun technologyAttribute(name: String, gameType: ParadoxGameType?): @Nls String {
        return INSTANCE.messageOrNull("${gameType?.id ?: "general"}.technology.attribute.$name")
            ?: INSTANCE.messageOrNull("general.technology.attribute.$name")
            ?: INSTANCE.getMessage("default.technology.attribute", name)
    }
}
