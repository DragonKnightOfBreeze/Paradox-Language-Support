<template>
  <span>
    <template v-if="locale === 'zh'">
      定义类型表达式。<!--
   -->可以仅包含基础类型（如<code>civic_or_origin</code>），<!--
   -->也可以包含基础类型以及多个匹配的子类型，用点号分割（如<code>civic_or_origin.origin</code>）。
    </template>
    <template v-else>
      Definition type expression.
      Can be a base type (e.g., <code>civic_or_origin</code>),
      or include base type and subtypes separated by dots (e.g., <code>civic_or_origin.origin</code>).
    </template>
  </span>
</template>

<script setup lang="ts">
import { routeLocale } from '../../configs/constants'

const locale = routeLocale
</script>
