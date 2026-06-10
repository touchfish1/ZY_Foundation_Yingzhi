<template>
  <div class="playground-page">
    <div class="playground-layout">
      <div class="playground-sidebar">
        <h2>API Playground</h2>
        <div class="sidebar-section">
          <label>Model</label>
          <select v-model="model">
            <optgroup label="OpenAI">
              <option value="gpt-4o">gpt-4o</option>
              <option value="gpt-4-turbo">gpt-4-turbo</option>
              <option value="gpt-4">gpt-4</option>
              <option value="gpt-3.5-turbo">gpt-3.5-turbo</option>
              <option value="text-embedding-3-small">text-embedding-3-small</option>
            </optgroup>
            <optgroup label="Claude (Anthropic)">
              <option value="claude-3-opus">claude-3-opus</option>
              <option value="claude-3-sonnet">claude-3-sonnet</option>
              <option value="claude-3-haiku">claude-3-haiku</option>
              <option value="claude-2">claude-2</option>
            </optgroup>
            <optgroup label="DeepSeek">
              <option value="deepseek-chat">deepseek-chat</option>
              <option value="deepseek-coder">deepseek-coder</option>
            </optgroup>
            <optgroup label="通义千问 (Qwen)">
              <option value="qwen-max">qwen-max</option>
              <option value="qwen-plus">qwen-plus</option>
              <option value="qwen-turbo">qwen-turbo</option>
              <option value="qwen2.5-72b-instruct">qwen2.5-72b-instruct</option>
            </optgroup>
            <optgroup label="Moonshot (Kimi)">
              <option value="moonshot-v1-8k">moonshot-v1-8k</option>
              <option value="moonshot-v1-32k">moonshot-v1-32k</option>
              <option value="moonshot-v1-128k">moonshot-v1-128k</option>
            </optgroup>
          </select>
        </div>
        <div class="sidebar-section">
          <label>Temperature</label>
          <div class="range-row">
            <input type="range" min="0" max="2" step="0.1" v-model.number="temperature" />
            <span class="range-value">{{ temperature }}</span>
          </div>
        </div>
        <div class="sidebar-section">
          <label class="checkbox-label">
            <input type="checkbox" v-model="streamEnabled" />
            <span>流式输出 (SSE)</span>
          </label>
        </div>
        <div class="sidebar-section">
          <label>System Prompt</label>
          <textarea v-model="systemPrompt" placeholder="Optional system message..." rows="3"></textarea>
        </div>
        <div class="sidebar-section">
          <label>API Key</label>
          <input v-model="apiKey" type="password" placeholder="sk-..." />
        </div>
        <div class="sidebar-section">
          <label>Endpoint</label>
          <input :value="`${apiBase}/v1/chat/completions`" disabled />
        </div>
        <button class="btn-send" :disabled="sending || !apiKey" @click="sendRequest">
          {{ sending ? '发送中...' : '发送请求' }}
        </button>
      </div>

      <div class="playground-main">
        <div class="message-list" ref="messageListRef">
          <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
            <div class="msg-label">{{ msg.role === 'user' ? 'You' : 'Assistant' }}</div>
            <div class="msg-content">{{ msg.content }}<span v-if="sending && i === messages.length - 1 && msg.role === 'assistant'" class="stream-cursor">▊</span></div>
          </div>
          <div v-if="error" class="message error-msg">
            <div class="msg-label">Error</div>
            <div class="msg-content">{{ error }}</div>
          </div>
          <div v-if="messages.length === 0 && !error" class="empty-state">
            输入消息并点击发送开始调试
          </div>
        </div>

        <div class="input-row">
          <textarea v-model="userInput" @keydown.enter.exact.prevent="sendRequest"
            placeholder="输入消息..." rows="2"></textarea>
          <button class="btn-send-small" :disabled="sending || !apiKey || !userInput.trim()"
            @click="sendRequest">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
usePageMeta('API Playground', '在线调试 AI API 接口')

import { ref, nextTick } from 'vue'
const config = useRuntimeConfig()
const auth = useSaasAuth()

const apiBase = config.public.aiBase || config.public.apiBase || ''
const apiKey = ref(auth.user.value?.apiKey || '')
const model = ref('gpt-4o')
const temperature = ref(0.7)
const streamEnabled = ref(true)
const systemPrompt = ref('')
const userInput = ref('')
const messages = ref<{ role: string; content: string }[]>([])
const sending = ref(false)
const streamingContent = ref('')
const error = ref('')
const messageListRef = ref<HTMLElement | null>(null)

async function sendRequest() {
  const input = userInput.value.trim()
  if (!input || sending.value) return
  error.value = ''

  messages.value.push({ role: 'user', content: input })
  userInput.value = ''
  scrollToBottom()

  sending.value = true
  streamingContent.value = ''
  try {
    const body: any = {
      model: model.value,
      messages: [],
      stream: streamEnabled.value
    }
    if (systemPrompt.value.trim()) {
      body.messages.push({ role: 'system', content: systemPrompt.value.trim() })
    }
    for (const msg of messages.value) {
      body.messages.push({ role: msg.role, content: msg.content })
    }
    if (temperature.value !== 0.7) body.temperature = temperature.value

    if (streamEnabled.value) {
      await sendStreamRequest(body)
    } else {
      await sendRegularRequest(body)
    }
  } catch (e: any) {
    error.value = e?.data?.error?.message || e?.message || 'Request failed'
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

async function sendRegularRequest(body: any) {
  const res = await auth.authFetch<any>('/v1/chat/completions', {
    method: 'POST',
    body,
    headers: { 'Authorization': `Bearer ${apiKey.value}` }
  })
  const choice = res?.choices?.[0]
  if (choice?.message?.content) {
    messages.value.push({ role: 'assistant', content: choice.message.content })
  } else {
    error.value = 'Empty response'
  }
}

async function sendStreamRequest(body: any) {
  const url = `${apiBase}/v1/chat/completions`
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${apiKey.value}`
    },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    const err = await response.text()
    throw new Error(err)
  }

  const reader = response.body?.getReader()
  if (!reader) throw new Error('Stream not supported')

  messages.value.push({ role: 'assistant', content: '' })
  const assistantMsg = messages.value[messages.value.length - 1]
  let buffer = ''

  const decoder = new TextDecoder()
  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed || !trimmed.startsWith('data: ')) continue
      const data = trimmed.slice(6).trim()
      if (data === '[DONE]') continue

      try {
        const parsed = JSON.parse(data)
        const content = parsed?.choices?.[0]?.delta?.content || ''
        if (content) {
          assistantMsg.content += content
          streamingContent.value = assistantMsg.content
          scrollToBottom()
        }
      } catch {
        // skip unparseable chunks
      }
    }
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.playground-page {
  min-height: calc(100vh - var(--vp-nav-height));
  padding: 24px;
}
.playground-layout {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - var(--vp-nav-height) - 48px);
}
.playground-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.playground-sidebar h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: var(--vp-c-text);
}
.sidebar-section label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text-2);
  margin-bottom: 6px;
}
.sidebar-section select,
.sidebar-section input,
.sidebar-section textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: var(--vp-c-bg);
  color: var(--vp-c-text);
  font-size: 13px;
  font-family: var(--vp-font-family-base);
  box-sizing: border-box;
}
.sidebar-section textarea {
  resize: vertical;
}
.range-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.range-row input[type="range"] {
  flex: 1;
  padding: 0;
  border: none;
}
.range-value {
  min-width: 28px;
  text-align: center;
  font-size: 13px;
  color: var(--vp-c-text-2);
  font-family: var(--vp-font-family-mono);
}
.btn-send {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: var(--vp-c-brand);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.btn-send:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--vp-c-text-2);
  cursor: pointer;
}
.checkbox-label input[type="checkbox"] {
  width: auto;
}
.stream-cursor {
  animation: blink 1s step-end infinite;
  margin-left: 2px;
  font-size: 14px;
  color: var(--vp-c-brand);
}
@keyframes blink {
  50% { opacity: 0; }
}
.playground-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--vp-c-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--vp-c-bg);
}
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
}
.message.user {
  align-self: flex-end;
  background: var(--vp-c-brand);
  color: white;
}
.message.assistant {
  align-self: flex-start;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text);
  border: 1px solid var(--vp-c-border);
}
.error-msg {
  align-self: center;
  background: var(--vp-c-danger-soft);
  color: var(--vp-c-danger);
  border: 1px solid var(--vp-c-danger);
  max-width: 100%;
}
.msg-label {
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 4px;
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.msg-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.empty-state {
  text-align: center;
  color: var(--vp-c-text-3);
  font-size: 14px;
  padding: 60px 0;
}
.input-row {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid var(--vp-c-border);
  background: var(--vp-c-bg);
}
.input-row textarea {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid var(--vp-c-border);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text);
  font-size: 14px;
  font-family: var(--vp-font-family-base);
  resize: none;
}
.btn-send-small {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background: var(--vp-c-brand);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  align-self: flex-end;
}
.btn-send-small:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
@media (max-width: 768px) {
  .playground-layout { flex-direction: column; }
  .playground-sidebar { width: 100%; }
}
</style>
