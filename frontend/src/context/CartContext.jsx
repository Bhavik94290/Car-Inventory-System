import { createContext, useContext, useEffect, useState } from 'react'

const CartContext = createContext(null)
const STORAGE_KEY = 'cart'

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      return stored ? JSON.parse(stored) : []
    } catch {
      return []
    }
  })

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  }, [items])

  const addToCart = (vehicle, qty = 1) => {
    setItems((prev) => {
      const maxQty = vehicle.quantity
      const existing = prev.find((i) => i.vehicleId === vehicle.id)
      if (existing) {
        const nextQty = Math.min(existing.quantity + qty, maxQty)
        return prev.map((i) => (i.vehicleId === vehicle.id ? { ...i, quantity: nextQty, stock: maxQty } : i))
      }
      return [
        ...prev,
        {
          vehicleId: vehicle.id,
          make: vehicle.make,
          model: vehicle.model,
          category: vehicle.category,
          price: vehicle.price,
          imageUrl: vehicle.imageUrl,
          stock: maxQty,
          quantity: Math.min(qty, maxQty),
        },
      ]
    })
  }

  const removeFromCart = (vehicleId) =>
    setItems((prev) => prev.filter((i) => i.vehicleId !== vehicleId))

  const updateQuantity = (vehicleId, quantity) =>
    setItems((prev) =>
      prev.map((i) =>
        i.vehicleId === vehicleId ? { ...i, quantity: Math.max(1, Math.min(quantity, i.stock)) } : i
      )
    )

  const clearCart = () => setItems([])

  const cartCount = items.reduce((sum, i) => sum + i.quantity, 0)
  const cartTotal = items.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)

  return (
    <CartContext.Provider
      value={{ items, addToCart, removeFromCart, updateQuantity, clearCart, cartCount, cartTotal }}
    >
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
