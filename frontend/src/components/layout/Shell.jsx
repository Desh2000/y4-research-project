import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function Shell() {
    return (
        <div className="app-shell">
            <Sidebar />
            <main className="app-canvas">
                <Outlet />
            </main>
        </div>
    );
}
