import { useEffect, useRef, useState, useCallback } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import { getErrorMessage } from '../utils/errors'
import { timeAgo } from '../utils/time'

function Avatar({ url, name }) {
  if (url) {
    return <img src={url} alt="" className="h-10 w-10 shrink-0 rounded-full object-cover" />
  }
  const initial = name ? name.charAt(0).toUpperCase() : '?'
  return (
    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-soft text-sm font-semibold text-brand">
      {initial}
    </div>
  )
}

export default function Chat() {
  const { interestId } = useParams()
  const navigate = useNavigate()

  const [conversations, setConversations] = useState([])
  const [loadingConvs, setLoadingConvs] = useState(true)
  const [convsError, setConvsError] = useState('')

  const [messages, setMessages] = useState([])
  const [loadingMsgs, setLoadingMsgs] = useState(false)
  const [msgsError, setMsgsError] = useState('')

  const [inputText, setInputText] = useState('')
  const [sending, setSending] = useState(false)
  const [sendError, setSendError] = useState('')

  const messagesEndRef = useRef(null)

  const scrollToBottom = (smooth = true) => {
    messagesEndRef.current?.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto' })
  }

  // Fetch all conversations
  const loadConversations = useCallback(async (isPolling = false) => {
    if (!isPolling) setConvsError('')
    try {
      const res = await api.get('/api/conversations')
      setConversations(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      if (!isPolling) setConvsError(getErrorMessage(err))
    } finally {
      if (!isPolling) setLoadingConvs(false)
    }
  }, [])

  // Fetch messages for active conversation
  const loadMessages = useCallback(async (id, isPolling = false) => {
    if (!id) return
    if (!isPolling) {
      setLoadingMsgs(true)
      setMsgsError('')
    }
    try {
      const res = await api.get(`/api/conversations/${id}/messages`)
      setMessages(Array.isArray(res.data) ? res.data : [])
      if (!isPolling) {
        setTimeout(() => scrollToBottom(false), 50)
      }
    } catch (err) {
      if (!isPolling) setMsgsError(getErrorMessage(err))
    } finally {
      if (!isPolling) setLoadingMsgs(false)
    }
  }, [])

  // Initial load of conversations
  useEffect(() => {
    loadConversations()
  }, [loadConversations])

  // Load messages whenever interestId changes
  useEffect(() => {
    if (interestId) {
      loadMessages(interestId)
    } else {
      setMessages([])
    }
  }, [interestId, loadMessages])

  // Poll conversations & active messages every 10 seconds
  useEffect(() => {
    const timer = setInterval(() => {
      loadConversations(true)
      if (interestId) {
        loadMessages(interestId, true)
      }
    }, 10000)
    return () => clearInterval(timer)
  }, [interestId, loadConversations, loadMessages])

  // Scroll on new messages
  useEffect(() => {
    scrollToBottom(true)
  }, [messages.length])

  const handleSend = async (e) => {
    if (e) e.preventDefault()
    const trimmed = inputText.trim()
    if (!trimmed || sending || !interestId) return
    if (trimmed.length > 1000) {
      setSendError('Message cannot exceed 1000 characters.')
      return
    }

    setSending(true)
    setSendError('')
    try {
      const res = await api.post(`/api/conversations/${interestId}/messages`, {
        body: trimmed,
      })
      setInputText('')
      setMessages((prev) => [...prev, res.data])
      setTimeout(() => scrollToBottom(true), 50)
      // Refresh conversations list to update last message preview
      loadConversations(true)
    } catch (err) {
      setSendError(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  const activeConv = conversations.find((c) => String(c.interestId) === String(interestId))

  return (
    <div className="card flex h-[calc(100vh-10rem)] min-h-[500px] overflow-hidden">
      {/* Left panel: Conversation List */}
      <div
        className={`flex w-full flex-col border-r border-line bg-white md:w-80 lg:w-96 ${
          interestId ? 'hidden md:flex' : 'flex'
        }`}
      >
        <div className="border-b border-line px-4 py-3">
          <h1 className="text-xl font-bold text-ink">Messages</h1>
        </div>

        <div className="flex-1 overflow-y-auto divide-y divide-line">
          {loadingConvs && (
            <p className="p-6 text-center text-xs text-muted">Loading conversations...</p>
          )}

          {convsError && (
            <p className="m-3 rounded bg-red-50 p-2 text-xs text-red-600">{convsError}</p>
          )}

          {!loadingConvs && !convsError && conversations.length === 0 && (
            <div className="p-8 text-center text-xs text-muted">
              No active conversations yet. Once an interest is accepted, you can message here.
            </div>
          )}

          {!loadingConvs &&
            conversations.map((c) => {
              const isSelected = String(c.interestId) === String(interestId)
              return (
                <div
                  key={c.interestId}
                  onClick={() => navigate(`/app/chat/${c.interestId}`)}
                  className={`flex cursor-pointer items-center gap-3 px-4 py-3 transition hover:bg-surface ${
                    isSelected ? 'bg-brand-soft/50 font-medium' : ''
                  }`}
                >
                  <Avatar url={c.otherPhotoUrl} name={c.otherFullName} />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <span className="truncate text-sm font-semibold text-ink">
                        {c.otherFullName || 'Member'}
                      </span>
                      {c.lastMessageTime && (
                        <span className="shrink-0 text-[11px] text-muted">
                          {timeAgo(c.lastMessageTime)}
                        </span>
                      )}
                    </div>
                    <p className="truncate text-xs text-muted mt-0.5">
                      {c.lastMessage || 'No messages yet'}
                    </p>
                  </div>
                </div>
              )
            })}
        </div>
      </div>

      {/* Right panel: Active Chat */}
      <div
        className={`flex flex-1 flex-col bg-surface ${
          !interestId ? 'hidden md:flex' : 'flex'
        }`}
      >
        {interestId ? (
          <>
            {/* Chat header */}
            <div className="flex items-center justify-between border-b border-line bg-white px-4 py-3">
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => navigate('/app/chat')}
                  className="rounded p-1 text-muted hover:text-ink md:hidden"
                  aria-label="Back to conversations"
                >
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polyline points="15 18 9 12 15 6" />
                  </svg>
                </button>

                <Avatar
                  url={activeConv?.otherPhotoUrl}
                  name={activeConv?.otherFullName}
                />

                <div>
                  <h2 className="text-sm font-semibold text-ink">
                    {activeConv?.otherFullName || 'Conversation'}
                  </h2>
                  {activeConv?.otherProfileId && (
                    <Link
                      to={`/app/profiles/${activeConv.otherProfileId}`}
                      className="text-xs text-brand hover:underline"
                    >
                      View profile
                    </Link>
                  )}
                </div>
              </div>
            </div>

            {/* Messages list */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {loadingMsgs && (
                <p className="text-center text-xs text-muted py-6">Loading messages...</p>
              )}

              {msgsError && (
                <p className="rounded bg-red-50 p-2 text-xs text-red-600">{msgsError}</p>
              )}

              {!loadingMsgs && !msgsError && messages.length === 0 && (
                <div className="py-12 text-center text-xs text-muted">
                  No messages yet. Send a friendly message to start the conversation!
                </div>
              )}

              {!loadingMsgs &&
                messages.map((m) => (
                  <div
                    key={m.id}
                    className={`flex flex-col ${m.mine ? 'items-end' : 'items-start'}`}
                  >
                    <div
                      className={`max-w-[80%] rounded-2xl px-4 py-2 text-sm leading-relaxed ${
                        m.mine
                          ? 'rounded-tr-xs bg-brand text-white shadow-xs'
                          : 'rounded-tl-xs card bg-white text-ink shadow-xs'
                      }`}
                    >
                      <p className="break-words whitespace-pre-wrap">{m.body}</p>
                    </div>

                    {m.flagged && (
                      <div className="mt-1 flex max-w-[80%] items-center gap-1.5 rounded border border-amber-200 bg-amber-50 px-2.5 py-1 text-xs text-amber-800">
                        <span>⚠️</span>
                        <span>This message was flagged for safety review.</span>
                      </div>
                    )}

                    <span className="mt-1 px-1 text-[10px] text-muted">
                      {timeAgo(m.sentAt)}
                    </span>
                  </div>
                ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Input form */}
            <div className="border-t border-line bg-white p-3">
              {sendError && (
                <p className="mb-2 rounded bg-red-50 px-2 py-1 text-xs text-red-600">
                  {sendError}
                </p>
              )}
              <form onSubmit={handleSend} className="space-y-2">
                <div className="relative">
                  <textarea
                    rows={2}
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault()
                        handleSend()
                      }
                    }}
                    placeholder="Type a message (Press Enter to send)..."
                    maxLength={1000}
                    className="input resize-none"
                    disabled={sending}
                  />
                </div>
                <div className="flex items-center justify-between">
                  <span
                    className={`text-xs ${
                      inputText.length > 900 ? 'text-amber-600 font-semibold' : 'text-muted'
                    }`}
                  >
                    {inputText.length}/1000
                  </span>
                  <button
                    type="submit"
                    disabled={sending || !inputText.trim() || inputText.length > 1000}
                    className="btn-primary"
                    style={{ width: 'auto', paddingInline: 24, paddingBlock: 8 }}
                  >
                    {sending ? 'Sending...' : 'Send'}
                  </button>
                </div>
              </form>
            </div>
          </>
        ) : (
          <div className="flex h-full flex-col items-center justify-center p-8 text-center text-muted">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              className="text-muted/50 mb-3"
            >
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <h3 className="text-base font-semibold text-ink">Select a conversation</h3>
            <p className="mt-1 text-xs">
              Choose an accepted connection from the list on the left to start chatting.
            </p>
          </div>
        )}
      </div>
    </div>
  )
}

