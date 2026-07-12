import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <span className="brand-mark">AI·KATA</span>
        <span className="brand-sub">Car Dealership Inventory</span>
      </Link>
      <nav className="nav-links">
        <Link to="/">Showroom</Link>
        {isAdmin && <Link to="/admin">Admin</Link>}
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
