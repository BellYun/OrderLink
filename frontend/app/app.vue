<script setup lang="ts">
const { data: backend, error, pending, refresh } = useBackendStatus()

const refreshStatus = () => refresh()

const checkedAt = computed(() => {
  if (!backend.value?.timestamp) return 'Waiting for response'

  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'medium'
  }).format(new Date(backend.value.timestamp))
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <a class="brand" href="/" aria-label="OrderLink home">OrderLink</a>
      <span class="environment">LOCAL</span>
    </header>

    <main>
      <section class="page-heading">
        <p class="eyebrow">COMMERCE OPERATIONS</p>
        <h1>System overview</h1>
        <p>Core services and development environment status.</p>
      </section>

      <section class="status-panel" aria-labelledby="service-status-title">
        <div class="section-heading">
          <div>
            <p class="eyebrow">INFRASTRUCTURE</p>
            <h2 id="service-status-title">Service status</h2>
          </div>
          <button type="button" :disabled="pending" @click="refreshStatus">
            {{ pending ? 'Checking...' : 'Refresh' }}
          </button>
        </div>

        <dl class="status-list">
          <div>
            <dt>Frontend</dt>
            <dd><span class="status-dot is-up" />Nuxt is running</dd>
          </div>
          <div>
            <dt>Backend API</dt>
            <dd v-if="backend"><span class="status-dot is-up" />{{ backend.service }} is {{ backend.status }}</dd>
            <dd v-else-if="error"><span class="status-dot is-down" />Unavailable</dd>
            <dd v-else><span class="status-dot" />Connecting</dd>
          </div>
          <div>
            <dt>Last checked</dt>
            <dd>{{ checkedAt }}</dd>
          </div>
        </dl>

        <p v-if="error" class="error-message">
          Start the Spring Boot server on port 8080, then refresh this status.
        </p>
      </section>
    </main>
  </div>
</template>
