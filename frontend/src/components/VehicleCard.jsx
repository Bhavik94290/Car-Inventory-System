export default function VehicleCard({ vehicle, canPurchase, onPurchase, purchasing }) {
  const outOfStock = vehicle.quantity === 0
  const priceFmt = Number(vehicle.price).toLocaleString('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0,
  })

  return (
    <article className={`car-card ${outOfStock ? 'sold-out' : ''}`}>
      <div className="car-card-top">
        <span className="car-category">{vehicle.category}</span>
        <span className={`stock-pill ${outOfStock ? 'stock-out' : 'stock-in'}`}>
          {outOfStock ? 'Out of stock' : `${vehicle.quantity} in stock`}
        </span>
      </div>
      <h3 className="car-title">{vehicle.make} <span>{vehicle.model}</span></h3>
      <div className="car-price">{priceFmt}</div>
      {canPurchase ? (
        <button
          className="btn btn-solid btn-block"
          disabled={outOfStock || purchasing}
          onClick={() => onPurchase(vehicle.id)}
        >
          {outOfStock ? 'Out of stock' : purchasing ? 'Purchasing…' : 'Purchase'}
        </button>
      ) : (
        <p className="muted small">Log in to purchase</p>
      )}
    </article>
  )
}
