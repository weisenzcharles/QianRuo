<template>
  <header class="page-heading">
    <div class="page-heading__content">
      <p v-if="kicker" class="page-kicker">{{ kicker }}</p>
      <h2>{{ title }}</h2>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div v-if="$slots.aside || hasSummary" class="page-heading__aside">
      <slot name="aside">
        <div v-if="hasSummary" class="pending-summary">
          <span>{{ summaryLabel }}</span>
          <strong>{{ summaryValue }}</strong>
          <span v-if="summarySuffix">{{ summarySuffix }}</span>
        </div>
      </slot>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  kicker: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  summaryLabel: {
    type: String,
    default: ''
  },
  summaryValue: {
    type: [String, Number],
    default: null
  },
  summarySuffix: {
    type: String,
    default: ''
  }
})

const hasSummary = computed(
  () =>
    Boolean(props.summaryLabel) ||
    (props.summaryValue !== null && props.summaryValue !== undefined)
)
</script>
