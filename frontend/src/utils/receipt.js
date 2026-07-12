import jsPDF from 'jspdf'

// jsPDF's standard fonts (Helvetica/Times/Courier) use WinAnsi encoding, which
// has no Rupee glyph (U+20B9) — toLocaleString(..., { style: 'currency' })
// renders as garbage in the PDF. Use plain "Rs." instead of the symbol.
const priceFmt = (n) => `Rs. ${Number(n).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`

const MARGIN_X = 48
const RIGHT_EDGE = 548
const PAGE_BOTTOM = 780

/**
 * Builds a one-page-ish PDF receipt for an order and triggers a browser
 * download. Generated entirely client-side (jsPDF) — no backend involved.
 */
export function downloadReceipt(order, user) {
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  let y = 56

  const newPageIfNeeded = () => {
    if (y > PAGE_BOTTOM) {
      doc.addPage()
      y = 56
    }
  }

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(20)
  doc.setTextColor('#0f5e63')
  doc.text('AI·KATA Car Dealership', MARGIN_X, y)

  y += 20
  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)
  doc.setTextColor('#555555')
  doc.text('Payment Receipt', MARGIN_X, y)

  y += 20
  doc.setDrawColor('#e2e2e2')
  doc.line(MARGIN_X, y, RIGHT_EDGE, y)
  y += 26

  doc.setFontSize(11)
  const row = (label, value) => {
    doc.setTextColor('#111111')
    doc.setFont('helvetica', 'bold')
    doc.text(label, MARGIN_X, y)
    doc.setFont('helvetica', 'normal')
    doc.text(String(value), MARGIN_X + 130, y)
    y += 18
  }

  row('Order ID:', order.id)
  row('Date:', new Date(order.createdAt).toLocaleString('en-IN'))
  row('Status:', order.status)
  if (order.razorpayPaymentId) row('Payment ID:', order.razorpayPaymentId)
  if (user?.name) row('Customer:', user.email ? `${user.name} (${user.email})` : user.name)

  y += 10
  doc.setDrawColor('#e2e2e2')
  doc.line(MARGIN_X, y, RIGHT_EDGE, y)
  y += 24

  doc.setFont('helvetica', 'bold')
  doc.setTextColor('#111111')
  doc.text('Vehicle', MARGIN_X, y)
  doc.text('Qty', 340, y)
  doc.text('Unit Price', 390, y)
  doc.text('Subtotal', 480, y)
  y += 8
  doc.line(MARGIN_X, y, RIGHT_EDGE, y)
  y += 18

  doc.setFont('helvetica', 'normal')
  order.items.forEach((item) => {
    newPageIfNeeded()
    const subtotal = Number(item.unitPrice) * item.quantity
    doc.text(`${item.make} ${item.model}`, MARGIN_X, y, { maxWidth: 280 })
    doc.text(String(item.quantity), 340, y)
    doc.text(priceFmt(item.unitPrice), 390, y)
    doc.text(priceFmt(subtotal), 480, y)
    y += 20
  })

  newPageIfNeeded()
  y += 6
  doc.line(MARGIN_X, y, RIGHT_EDGE, y)
  y += 24

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(13)
  doc.text('Total', 390, y)
  doc.text(priceFmt(order.totalAmount), 480, y)

  y += 48
  doc.setFont('helvetica', 'normal')
  doc.setFontSize(10)
  doc.setTextColor('#777777')
  doc.text('Thank you for your purchase!', MARGIN_X, y)

  doc.save(`receipt-${order.id}.pdf`)
}
