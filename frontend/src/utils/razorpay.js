let loading = null

// Loads Razorpay's checkout script once and returns window.Razorpay
export function loadRazorpay() {
  if (window.Razorpay) return Promise.resolve(window.Razorpay)
  if (!loading) {
    loading = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://checkout.razorpay.com/v1/checkout.js'
      script.onload = () => resolve(window.Razorpay)
      script.onerror = () => {
        loading = null
        reject(new Error('Could not load the payment window. Check your internet connection.'))
      }
      document.body.appendChild(script)
    })
  }
  return loading
}