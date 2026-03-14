import { NavLink, Route, Routes } from 'react-router-dom'
import './App.css'
import { Dashboard } from './pages/Dashboard'
import { BatchList } from './pages/BatchList'
import { BatchDetail } from './pages/BatchDetail'
import { ActivityDetail } from './pages/ActivityDetail'
import { ActivityWizard } from './pages/ActivityWizard'

function App() {
  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">◈</span>
          <span className="brand-text">FinCEN SAR</span>
        </div>
        <nav className="sidebar-nav">
          <NavLink to="/" end>
            Dashboard
          </NavLink>
          <NavLink to="/batches">Batches</NavLink>
        </nav>
        <div className="sidebar-footer">
          <span className="version-tag">v2.0 · Phase 2</span>
        </div>
      </aside>

      <main className="main-content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/batches" element={<BatchList />} />
          <Route path="/batches/:batchId" element={<BatchDetail />} />
          <Route path="/activities/:activityId" element={<ActivityDetail />} />
          <Route path="/activities/:activityId/wizard/:step" element={<ActivityWizard />} />
        </Routes>
      </main>
    </div>
  )
}

export default App

