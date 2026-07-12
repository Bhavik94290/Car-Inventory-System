import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'
import Logo from './Logo.jsx'

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const { cartCount, clearCart } = useCart()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    clearCart()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <Logo />
        <span className="brand-mark">Car Dealership Inventory</span>
      </Link>
      <nav className="nav-links">
        <Link to="/">Showroom</Link>
        {isAdmin ? (
          <Link to="/admin">Admin</Link>
        ) : (
          <>
            {user && <Link to="/orders">My Orders</Link>}
            {user && (
              <Link to="/cart" className="cart-link">
                🛒 Cart
                {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
              </Link>
            )}
          </>
        )}
        {user ? (
          <>
            <span className="nav-user">
              {user.name} <em>({user.role})</em>
            </span>
            <button className="btn btn-outline" onClick={handleLogout}>Log out</button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>
            <Link to="/register" className="btn btn-solid">Register</Link>
          </>
        )}
      </nav>
    </header>
  )
}
