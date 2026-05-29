<template>
  <component
    :is="to ? 'router-link' : 'button'"
    :to="to"
    :type="to ? undefined : 'button'"
    class="metro-tile"
    :class="[color, { 'tile-wide': wide }]"
    @click="!to && $emit('click')"
  >
    <i v-if="icon" :class="['bi', icon]"></i>
    <span v-if="label" class="metro-tile-label">{{ label }}</span>
    <span v-if="value !== undefined && value !== null" class="metro-tile-value">{{ value }}</span>
    <slot />
  </component>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '' },
  label: { type: String, default: '' },
  value: { type: [String, Number], default: undefined },
  color: { type: String, default: 'tile-blue' },
  wide: { type: Boolean, default: false },
  to: { type: String, default: '' }
})
defineEmits(['click'])
</script>
