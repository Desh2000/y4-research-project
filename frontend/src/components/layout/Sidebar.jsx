import { NavLink, useLocation } from 'react-router-dom';
import {
    Activity,
    Users,
    FlaskConical,
    BarChart3,
    Stethoscope,
    Waypoints,
    SlidersHorizontal,
    ScanSearch,
    Zap
} from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { getHealth } from '../../api/client';

const NAV_ITEMS = [
    { path: '/', icon: Activity, label: 'Observatory' },
    { path: '/patients', icon: Users, label: 'Patients' },
    { path: '/simulate', icon: FlaskConical, label: 'Simulation Lab' },
    { path: '/compare', icon: BarChart3, label: 'Intervention Compare' },
    { path: '/prescribe', icon: Stethoscope, label: 'AI Prescription' },
    { path: '/what-if', icon: SlidersHorizontal, label: 'What-If Simulator' },
    { path: '/explain', icon: ScanSearch, label: 'XAI Explainer' },
    { path: '/next-action', icon: Zap, label: 'Next Best Action' },
];

export default function Sidebar() {
    const location = useLocation();
    const { data: health } = useApi(getHealth, { key: 'health' });

    const systemStatus = health?.status === 'healthy' ? 'operational' : 'degraded';

    return (
        <aside className="sidebar">
            {/* Brand */}
            <div className="sidebar-brand">
                <div className="sidebar-brand-icon">
                    <Waypoints size={20} color="#fff" />
                </div>
                <div>
                    <div className="sidebar-brand-text">MANŌ</div>
                    <div className="sidebar-brand-sub">AMISE Engine</div>
                </div>
            </div>

            {/* Navigation */}
            <nav className="sidebar-nav">
                {NAV_ITEMS.map(({ path, icon: Icon, label }) => (
                    <NavLink
                        key={path}
                        to={path}
                        className={({ isActive }) =>
                            `sidebar-link ${isActive ? 'active' : ''}`
                        }
                        end={path === '/'}
                    >
                        <Icon size={20} />
                        <span>{label}</span>
                    </NavLink>
                ))}
            </nav>

            {/* System Pulse */}
            <div className="sidebar-footer">
                <div className="sidebar-pulse">
                    <span className={`pulse-dot ${systemStatus === 'degraded' ? 'degraded' : ''}`} />
                    <span>System {systemStatus}</span>
                </div>
            </div>
        </aside>
    );
}
