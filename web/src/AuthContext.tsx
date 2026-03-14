import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react'

type AuthUser = {
  username: string
  fullName: string
  role: string
  token: string
}

type AuthContextType = {
  user: AuthUser | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | null>(null)

const STORAGE_KEY = 'sar_auth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      try {
        return JSON.parse(stored) as AuthUser
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
    return null
  })

  const login = useCallback(async (username: string, password: string) => {
    const res = await fetch('/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    if (!res.ok) {
      const text = await res.text().catch(() => res.statusText)
      let message = 'Login failed'
      try {
        const json = JSON.parse(text)
        message = json.message || message
      } catch {
        message = text || message
      }
      throw new Error(message)
    }
    const data = await res.json()
    const authUser: AuthUser = {
      username: data.username,
      fullName: data.fullName,
      role: data.role,
      token: data.token,
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(authUser))
    setUser(authUser)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setUser(null)
  }, [])

  // Handle 401 responses globally — force logout
  useEffect(() => {
    const handler = (e: Event) => {
      if ((e as CustomEvent).detail === 401) {
        logout()
      }
    }
    window.addEventListener('auth-error', handler)
    return () => window.removeEventListener('auth-error', handler)
  }, [logout])

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
