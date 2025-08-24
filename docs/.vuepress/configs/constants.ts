import { computed } from "vue";
import { useRouteLocale } from "vuepress/client";

export const coreGameType = 'core' as const;

export const gameTypes = [
  'stellaris',
  'ck2',
  'ck3',
  'eu4',
  'hoi4',
  'ir',
  'vic2',
  'vic3',
] as const;

export const routeLocale = computed(() => useRouteLocale().value.replaceAll("/", ""))
