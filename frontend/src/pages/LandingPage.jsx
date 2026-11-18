import {Link} from 'react-router-dom';
import {ArrowRight,BarChart3,Building2,CalendarCheck2,CheckCircle2,Handshake,ShieldCheck} from 'lucide-react';

export default function LandingPage() {
  return (
    <div className="estate-landing">
      <header className="landing-nav">
        <Link to="/" className="landing-brand" aria-label="EstateFlow home">
          <img src="/estateflow-mark.svg" alt="" />
          <span>Estate<strong>Flow</strong></span>
        </Link>
        <nav className="landing-menu" aria-label="Main navigation">
          <a href="#overview">Overview</a>
          <a href="#features">Features</a>
          <a href="#solution">Solutions</a>
        </nav>
        <div className="landing-actions">
          <Link className="landing-signin" to="/login">Sign in</Link>
          <Link className="landing-primary" to="/login">Try EstateFlow <ArrowRight size={15}/></Link>
        </div>
      </header>

      <main>
        <section className="landing-hero" id="overview">
          <div className="landing-copy">
            <p className="landing-kicker"><span/> Property intelligence, simplified</p>
            <h1>Property management, <em>made beautifully simple.</em></h1>
            <p className="landing-description">Manage properties, nurture leads, coordinate visits and close deals from one intelligent real estate workspace.</p>
            <div className="landing-hero-actions">
              <Link className="landing-primary landing-primary-large" to="/login">Get started <ArrowRight size={17}/></Link>
              <a className="landing-secondary" href="#features">Explore the platform</a>
            </div>
            <div className="landing-proof">
              <span><CheckCircle2 size={16}/> Portfolio ready</span>
              <span><CheckCircle2 size={16}/> Lead pipeline</span>
              <span><CheckCircle2 size={16}/> Deal insights</span>
            </div>
          </div>
          <div className="landing-visual" aria-label="Modern apartment interior">
            <div className="landing-photo">
              <div className="landing-photo-tag"><Building2 size={15}/> Live portfolio</div>
              <div className="landing-property-card">
                <div><span>Featured residence</span><strong>Skyline Heights</strong></div>
                <p>Available <b>₹ 1.2 Cr</b></p>
              </div>
            </div>
          </div>
        </section>

        <section className="landing-section" id="features">
          <div className="landing-section-heading">
            <p>EstateFlow workspace</p>
            <h2>From first enquiry to <em>final signature</em></h2>
          </div>
          <div className="landing-feature-grid">
            <article>
              <span className="landing-feature-icon navy"><Building2 size={22}/></span>
              <h3>Property portfolio</h3>
              <p>Present every residence with rich details, availability, gallery views and clear ownership.</p>
            </article>
            <article>
              <span className="landing-feature-icon teal"><Handshake size={22}/></span>
              <h3>Connected sales</h3>
              <p>Match customers with specialists and keep every negotiation moving with a focused CRM pipeline.</p>
            </article>
            <article>
              <span className="landing-feature-icon rose"><CalendarCheck2 size={22}/></span>
              <h3>Visit scheduling</h3>
              <p>Coordinate property visits, agent availability and customer confirmations in one place.</p>
            </article>
            <article>
              <span className="landing-feature-icon gold"><BarChart3 size={22}/></span>
              <h3>Clear analytics</h3>
              <p>Understand inventory, pipeline health and team performance through practical live insights.</p>
            </article>
          </div>
        </section>

        <section className="landing-workflow" id="solution">
          <div>
            <p className="landing-kicker"><span/> Built for every role</p>
            <h2>One platform for <em>property teams.</em></h2>
          </div>
          <div className="landing-role-list">
            <article><ShieldCheck size={20}/><div><strong>Administrators</strong><p>Control inventory, users, records and company performance.</p></div></article>
            <article><Handshake size={20}/><div><strong>Agents</strong><p>Manage requests, follow-ups, visits and customer negotiations.</p></div></article>
            <article><Building2 size={20}/><div><strong>Customers</strong><p>Discover homes, connect with agents and track purchase progress.</p></div></article>
          </div>
        </section>
      </main>

      <footer className="landing-footer">
        <Link to="/" className="landing-brand"><img src="/estateflow-mark.svg" alt="" /><span>Estate<strong>Flow</strong></span></Link>
        <p>© 2026 EstateFlow. Real estate, organized.</p>
        <Link to="/login">Workspace sign in</Link>
      </footer>
    </div>
  );
}
