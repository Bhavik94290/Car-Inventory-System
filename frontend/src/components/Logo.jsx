export default function Logo({ size = 34 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 34 34" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <defs>
        <linearGradient id="logo-gradient" x1="0" y1="0" x2="34" y2="34">
          <stop stopColor="#22b8b2" />
          <stop offset="1" stopColor="#1a8f8a" />
        </linearGradient>
      </defs>
      <rect width="34" height="34" rx="9" fill="url(#logo-gradient)" />
      <path
        d="M6,21 L8,21 L10,15 Q11,13 13,13 L21,13 Q23,13 24,15 L26,21 L27,21 Q28,21 28,22 L28,24 Q28,25 27,25 L26,25 Q26,27 24,27 Q22,27 22,25 L12,25 Q12,27 10,27 Q8,27 8,25 L7,25 Q6,25 6,24 L6,22 Q6,21 6,21 Z"
        fill="#f5f7f9"
      />
      <path
        d="M10.3,15.6 Q11.2,14.4 13,14.4 L21,14.4 Q22.8,14.4 23.7,15.6 L24.9,19.4 L9.1,19.4 Z"
        fill="#1a8f8a"
      />
      <circle cx="10" cy="25.5" r="2.1" fill="#0c1118" />
      <circle cx="24" cy="25.5" r="2.1" fill="#0c1118" />
    </svg>
  )
}
