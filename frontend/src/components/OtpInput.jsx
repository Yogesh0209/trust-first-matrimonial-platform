import { useRef } from 'react'

export default function OtpInput({ value, onChange }) {
  const refs = useRef([])
  const digits = Array.from({ length: 6 }, (_, i) => value[i] || '')

  const handleChange = (i, e) => {
    const d = e.target.value.replace(/\D/g, '').slice(-1)
    const arr = [...digits]
    arr[i] = d
    onChange(arr.join(''))
    if (d && i < 5) refs.current[i + 1].focus()
  }

  const handleKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !digits[i] && i > 0) refs.current[i - 1].focus()
  }

  const handlePaste = (e) => {
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
    if (pasted) {
      e.preventDefault()
      onChange(pasted)
      refs.current[Math.min(pasted.length, 5)].focus()
    }
  }

  return (
    <div className="flex justify-between gap-2" onPaste={handlePaste}>
      {digits.map((d, i) => (
        <input
          key={i}
          ref={(el) => (refs.current[i] = el)}
          value={d}
          inputMode="numeric"
          maxLength={1}
          autoFocus={i === 0}
          onChange={(e) => handleChange(i, e)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          className="h-12 w-11 rounded-xl border border-line bg-white text-center text-lg font-semibold focus:border-brand focus:outline-none"
        />
      ))}
    </div>
  )
}