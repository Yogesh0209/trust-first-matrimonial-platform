import Logo from './Logo'

export default function CenterLayout({ children }) {
  return (
    <div className="min-h-screen border-t-4 border-brand">
      <div className="mt-10 flex justify-center">
        <Logo />
      </div>
      <div className="card mx-auto mt-6 w-[calc(100%-2rem)] max-w-sm p-8">{children}</div>
    </div>
  )
}