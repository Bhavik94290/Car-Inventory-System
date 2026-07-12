import { resolveCategoryStyle } from '../utils/categoryStyle.js'

export default function CarIllustration({ category, className = '' }) {
  const style = resolveCategoryStyle(category)

  return (
    <div
      className={`car-illustration ${className}`}
      style={{ background: `linear-gradient(135deg, ${style.gradient[0]}, ${style.gradient[1]})` }}
    >
      <span className="car-illustration-badge" aria-hidden="true">{style.emoji}</span>
      <svg viewBox="0 0 240 140" className="car-illustration-svg" role="img" aria-label={`${category || 'Vehicle'} illustration`}>
        <ellipse cx="120" cy="112" rx="88" ry="9" fill="rgba(0,0,0,0.18)" />
        <path d="M70,65 L85,35 Q90,30 100,30 L140,30 Q150,30 155,35 L170,65 Z" fill={style.car} opacity="0.95" />
        <rect x="18" y="63" width="204" height="34" rx="17" fill={style.car} />
        <path d="M80,60 L92,40 Q95,37 100,37 L140,37 Q145,37 148,40 L160,60 Z" fill="rgba(15,20,30,0.28)" />
        <line x1="120" y1="37" x2="120" y2="60" stroke="rgba(15,20,30,0.35)" strokeWidth="2" />
        <circle cx="68" cy="98" r="17" fill="#171c26" />
        <circle cx="68" cy="98" r="7" fill={style.car} />
        <circle cx="172" cy="98" r="17" fill="#171c26" />
        <circle cx="172" cy="98" r="7" fill={style.car} />
        <rect x="206" y="74" width="10" height="6" rx="3" fill="#e0a100" />
        <rect x="20" y="74" width="8" height="6" rx="3" fill="#b3362a" />
      </svg>
    </div>
  )
}
