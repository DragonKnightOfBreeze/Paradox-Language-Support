import prismCwtPlugin from "../plugins/prism/prism-cwt-plugin.js";
import prismParadoxScriptPlugin from "../plugins/prism/prism-paradox-script-plugin.js";
import prismParadoxLocalisationPlugin from "../plugins/prism/prism-paradox-localisation-plugin.js";
import prismParadoxCsvPlugin from "../plugins/prism/prism-paradox-csv-plugin.js";
import shikiCwt from "../highlighters/shiki/shiki-cwt.js";
import shikiParadoxScript from "../highlighters/shiki/shiki-paradox-script.js";
import shikiParadoxLocalisation from "../highlighters/shiki/shiki-paradox-localisation.js";
import shikiParadoxCsv from "../highlighters/shiki/shiki-paradox-csv.js";
import { MarkdownHighlighterOptions } from "vuepress-theme-hope";
import shikiCwtPlugin from "../plugins/shiki/shiki-cwt-plugin.js";
import shikiParadoxScriptPlugin from "../plugins/shiki/shiki-paradox-script-plugin.js";
import shikiParadoxLocalisationPlugin from "../plugins/shiki/shiki-paradox-localisation-plugin.js";
import shikiParadoxCsvPlugin from "../plugins/shiki/shiki-paradox-csv-plugin.js";

export const usePrism = false;

export function getPrismPlugins() {
  if (!usePrism) return [];
  return [
    prismCwtPlugin(),
    prismParadoxScriptPlugin(),
    prismParadoxLocalisationPlugin(),
    prismParadoxCsvPlugin(),
  ]
}

export function getShikiPlugins() {
  if (usePrism) return [];
  return [
    shikiCwtPlugin(),
    shikiParadoxScriptPlugin(),
    shikiParadoxLocalisationPlugin(),
    shikiParadoxCsvPlugin(),
  ]
}

export function getHighlighterOptions(): MarkdownHighlighterOptions {
  if (usePrism) {
    return {
      type: "prismjs"
    }
  } else {
    return {
      type: "shiki",
      langs: [
        () => shikiCwt(),
        () => shikiParadoxScript(),
        () => shikiParadoxLocalisation(),
        () => shikiParadoxCsv(),
      ]
    }
  }
}
