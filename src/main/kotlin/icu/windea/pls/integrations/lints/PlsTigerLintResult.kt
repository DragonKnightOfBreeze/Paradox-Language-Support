package icu.windea.pls.integrations.lints

import com.fasterxml.jackson.module.kotlin.*
import icu.windea.pls.core.data.*

/**
 * 参见：[JSON output format · amtep/tiger Wiki](https://github.com/amtep/tiger/wiki/JSON-output-format)
 */
class PlsTigerLintResult(
    val items: List<Item> = emptyList()
) : PlsLintResult {
    data class Item(
        /**
         * Severity is the potential impact of a problem.
         * The severity levels may still change, but right now they are these, in order of increasing severity:
         *
         * * "tips" for things that aren't wrong but could be improved.
         * * "untidy" for things that won't affect the player but will cause maintenance headaches for the modders.
         * * "warning" for things that the player will notice but that won't affect gameplay. Missing localizations for example.
         * * "error" for bugs that are likely to affect gameplay.
         * * "fatal" for things that can cause crashes.
         */
        val severity: Severity,
        /**
         * Confidence is how sure the validator is of this problem.
         * The confidence levels may still change, but right now they are these, in order of increasing confidence:
         *
         * * "weak" for likely false positives.
         * * "reasonable" for most messages.
         * * "strong" for confidence that the problem is real.
         */
        val confidence: Confidence,
        /**
         * This is the error key, used for in the config file for filtering reports.
         * The list of error keys is long and will change as more validations are added.
         * It's good to display the error key to the user, so that they can adjust the config file if needed.
         */
        val key: String,
        /**
         * The main error message accompanying this report.
         */
        val message: String?,
        /**
         * Additional information to explain the message. Is often null.
         */
        val info: String?,
        /**
         * An array of location dictionaries, each describing one code location relevant to the error.
         * There will always be at least one location.
         */
        val locations: List<Location>,
    )

    data class Location(
        /**
         * The path to the file, relative to its root location (usually the game directory or the mod directory).
         */
        val path: String,
        /**
         * The origin of the file, usually a short tag that says `"MOD"` or identifies the base game (`"CK3"` or `"Vic3"`).
         * It can be other things, such as supplementary game directories (`"Jomini"` and `"Clausewitz"`)
         * or the short names of other mods the user has configured in the config file.
         */
        val from: String,
        /**
         * The full path to the file, suitable for opening it.
         */
        val fullPath: String,
        /**
         * The line number within the file, starting at 1.
         * Will be `null` if the report is for the whole file.
         */
        val linenr: Int,
        /**
         * The column position within the line, starting at 1, and counting UTF-8 code points.
         * Will be `null` if the report is for the whole file.
         */
        val column: Int,
        /**
         * The length of the item being pointed at, in UTF-8 code points.
         * Can be used for highlighting the whole item. Can be `null` if the length is not known.
         */
        val length: Int,
        /**
         * A short description of the role of this location in the error report.
         * Can be `null`, and is often `null` for the first location in a report.
         */
        val tag: String?,
        /**
         * The contents of the line from the file. Can be `null` if the report is for the whole file,
         * or if there was some error in fetching the line from the file.
         */
        val line: String,
    )

    enum class Severity {
        Tips, Untidy, Warning, Error, Fatal
    }

    enum class Confidence {
        Weak, Reasonable, Strong
    }

    companion object {
        @JvmStatic
        fun parse(json: String): PlsTigerLintResult {
            val items = jsonMapper.readValue<List<Item>>(json)
            return PlsTigerLintResult(items)
        }
    }
}
