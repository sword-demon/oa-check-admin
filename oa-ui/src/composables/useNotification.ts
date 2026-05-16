import { ref } from 'vue'
import { getUnreadCount } from '@/api/notification'

const unreadCount = ref(0)
let intervalId: ReturnType<typeof setInterval> | null = null

export function useNotification() {
  async function fetchUnreadCount() {
    try {
      unreadCount.value = await getUnreadCount()
    } catch {
      // ignore errors during polling
    }
  }

  function startPolling(intervalMs = 30000) {
    if (intervalId) return
    fetchUnreadCount()
    intervalId = setInterval(fetchUnreadCount, intervalMs)
  }

  function stopPolling() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  return { unreadCount, fetchUnreadCount, startPolling, stopPolling }
}
