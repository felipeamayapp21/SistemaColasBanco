import { ref } from 'vue'

const wsUrl = `ws://${window.location.hostname}:8080/ws/queue`
let ws = null
let reconnectTimer = null
const listeners = new Set()
const isConnected = ref(false)

function connect() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  try {
    ws = new WebSocket(wsUrl)
    ws.onopen = () => {
      isConnected.value = true
    }
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        listeners.forEach(fn => fn(data))
      } catch (e) { /* ignore parse errors */ }
    }
    ws.onclose = () => {
      isConnected.value = false
      reconnectTimer = setTimeout(connect, 3000)
    }
    ws.onerror = () => {
      ws?.close()
    }
  } catch (e) {
    reconnectTimer = setTimeout(connect, 3000)
  }
}

function disconnect() {
  clearTimeout(reconnectTimer)
  listeners.clear()
  ws?.close()
  ws = null
  isConnected.value = false
}

function subscribe(fn) {
  listeners.add(fn)
  return () => listeners.delete(fn)
}

export default { connect, disconnect, subscribe, isConnected }
