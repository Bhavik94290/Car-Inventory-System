import { createContext, useContext, useEffect, useState } from 'react'
import api from '../api/client.js'

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

  // Cart items are a snapshot taken when "Add to Cart" was clicked. If an
  // admin changes a price/stock (or deletes a vehicle) afterwards, the cart
  // would keep showing stale numbers — and checkout always charges the
  // *current* server-side price regardless, so a stale display could show
  // one amount and charge another. Re-fetch each item against the live
  // catalog and reconcile before showing the cart or starting checkout.
  const syncWithCatalog = async () => {
    if (items.length === 0) return { changed: [], removed: [] }

    const results = await Promise.all(
      items.map((item) =>
        api.get(`/vehicles/${item.vehicleId}`)
          .then(({ data }) => ({ item, vehicle: data }))
          .catch(() => ({ item, vehicle: null }))
      )
    )

    const changed = []
    const removed = []
    const nextItems = []

    for (const { item, vehicle } of results) {
      if (!vehicle || vehicle.quantity <= 0) {
        removed.push({ ...item, reason: vehicle ? 'out of stock' : 'no longer available' })
        continue
      }
      if (Number(vehicle.price) !== Number(item.price) || vehicle.quantity !== item.stock) {
        changed.push({ make: vehicle.make, model: vehicle.model, oldPrice: item.price, newPrice: vehicle.price })
      }
      nextItems.push({
        ...item,
        make: vehicle.make,
        model: vehicle.model,
        category: vehicle.category,
        price: vehicle.price,
        imageUrl: vehicle.imageUrl,
        stock: vehicle.quantity,
        quantity: Math.min(item.quantity, vehicle.quantity),
      })
    }

    setItems(nextItems)
    return { changed, removed }
  }

  const cartCount = items.reduce((sum, i) => sum + i.quantity, 0)
  const cartTotal = items.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)

  return (
    <CartContext.Provider
      value={{ items, addToCart, removeFromCart, updateQuantity, clearCart, syncWithCatalog, cartCount, cartTotal }}
    >
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
