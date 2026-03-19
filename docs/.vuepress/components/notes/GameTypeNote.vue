<template>
  <span>
    <template v-if="locale === 'zh'">
      游戏类型ID。<!--
   -->目前有以下可选值：
      <template v-for="(gameType, i) in gameTypes" :key="gameType">
        <code>{{ gameType }}</code><template v-if="i < gameTypes.length - 1">, </template>
      </template><!--
   -->（对于共享的规则分组则为 <code>{{ coreGameType }}</code>）。
    </template>
    <template v-else>
      Game type ID.
      Currently supported values:
      <template v-for="(gameType, i) in gameTypes" :key="gameType">
        <code>{{ gameType }}</code><template v-if="i < gameTypes.length - 1">, </template>
      </template>
      (for shared config groups use <code>{{ coreGameType }}</code>).
    </template>
  </span>
</template>

<script setup lang="ts">
import { coreGameType, gameTypes, routeLocale } from '../../configs/constants'

const locale = routeLocale
</script>
