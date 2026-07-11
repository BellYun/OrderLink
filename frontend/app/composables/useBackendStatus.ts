interface BackendStatus {
  status: string
  service: string
  timestamp: string
}

export function useBackendStatus() {
  const config = useRuntimeConfig()

  return useFetch<BackendStatus>('/v1/system/status', {
    baseURL: config.public.apiBase,
    key: 'backend-system-status',
    server: false
  })
}

