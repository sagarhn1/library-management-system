export function PlaceholderPage({ title, eyebrow }) {
  return (
    <section className="content-section">
      <p className="eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      <p className="muted">This page is reserved for the next implementation step.</p>
    </section>
  )
}
