import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client.js'
import CarIllustration from './CarIllustration.jsx'

const priceFmt = (n) => Number(n).toLocaleString('en-IN', {
  style: 'currency', currency: 'INR', maximumFractionDigits: 0,
})

const GREETING = {
  role: 'assistant',
  content: "Hi! I'm the AI·KATA assistant. Ask me about available cars, prices, categories, or your past orders.",
  vehicles: [],
}

export default function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([GREETING])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const bottomRef = useRef(null)

  useEffect(() => {
    if (open) bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, open])

  const handleSend = async (e) => {
    e.preventDefault()
    const text = input.trim()
    if (!text || loading) return

    setError('')
    setInput('')
    const history = messages
      .filter((m) => m.role === 'user' || m.role === 'assistant')
      .map((m) => ({ role: m.role, content: m.content }))
    setMessages((prev) => [...prev, { role: 'user', content: text }])
    setLoading(true)

    try {
      const { data } = await api.post('/chat', { message: text, history })
      setMessages((prev) => [...prev, { role: 'assistant', content: data.reply, vehicles: data.vehicles || [] }])
    } catch (err) {
      setError(err.response?.data?.message || 'The assistant is unavailable right now.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-widget">
      {open && (
        <div className="chat-panel">
          <div className="chat-panel-header">
            <span>🚗 AI·KATA Assistant</span>
            <button className="chat-close" onClick={() => setOpen(false)} aria-label="Close chat">✕</button>
          </div>

          <div className="chat-messages">
            {messages.map((m, i) => (
              <div key={i} className={`chat-bubble chat-bubble-${m.role}`}>
                <p>{m.content}</p>
                {m.vehicles?.length > 0 && (
                  <div className="chat-vehicle-list">
                    {m.vehicles.map((v) => (
                      <Link to={`/vehicles/${v.id}`} key={v.id} className="chat-vehicle-card">
                        <div className="chat-vehicle-thumb">
                          <CarIllustration category={v.category} />
                        </div>
                        <div>
                          <strong>{v.make} {v.model}</strong>
                          <span className="muted small">
                            {priceFmt(v.price)} · {v.quantity > 0 ? `${v.quantity} in stock` : 'Out of stock'}
                          </span>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {loading && <div className="chat-bubble chat-bubble-assistant chat-typing">Thinking…</div>}
            {error && <div className="alert">{error}</div>}
            <div ref={bottomRef} />
          </div>

          <form className="chat-input-row" onSubmit={handleSend}>
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about cars, prices, or your orders…"
              disabled={loading}
            />
            <button className="btn btn-solid" type="submit" disabled={loading || !input.trim()}>
              Send
            </button>
          </form>
        </div>
      )}

      <button className="chat-toggle" onClick={() => setOpen((o) => !o)} aria-label="Toggle chat assistant">
        {open ? '✕' : '💬'}
      </button>
    </div>
  )
}
