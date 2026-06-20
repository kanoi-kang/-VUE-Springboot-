export const setStorage = (key: string, value: unknown): void => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.error('Storage set error:', e)
  }
}

export const getStorage = <T>(key: string, defaultValue: T): T => {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (e) {
    console.error('Storage get error:', e)
    return defaultValue
  }
}

export const removeStorage = (key: string): void => {
  try {
    localStorage.removeItem(key)
  } catch (e) {
    console.error('Storage remove error:', e)
  }
}

export const setSessionStorage = (key: string, value: unknown): void => {
  try {
    sessionStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.error('SessionStorage set error:', e)
  }
}

export const getSessionStorage = <T>(key: string, defaultValue: T): T => {
  try {
    const item = sessionStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (e) {
    console.error('SessionStorage get error:', e)
    return defaultValue
  }
}
