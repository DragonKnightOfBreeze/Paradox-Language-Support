import { defineClientConfig } from 'vuepress/client'
import GameTypeNote from './components/notes/GameTypeNote.vue'
import DefinitionTypeNote from './components/notes/DefinitionTypeNote.vue'

export default defineClientConfig({
  enhance({ app }) {
    app.component('GameTypeNote', GameTypeNote)
    app.component('DefinitionTypeNote', DefinitionTypeNote)
  },
})
