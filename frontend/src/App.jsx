import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Shell from './components/layout/Shell';
import Observatory from './pages/Observatory';
import PatientExplorer from './pages/PatientExplorer';
import PatientProfile from './pages/PatientProfile';
import SimulationLab from './pages/SimulationLab';
import InterventionCompare from './pages/InterventionCompare';
import Prescription from './pages/Prescription';
import WhatIfSimulator from './pages/WhatIfSimulator';
import XAIExplainer from './pages/XAIExplainer';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Shell />}>
          <Route index element={<Observatory />} />
          <Route path="/patients" element={<PatientExplorer />} />
          <Route path="/patients/:id" element={<PatientProfile />} />
          <Route path="/simulate" element={<SimulationLab />} />
          <Route path="/compare" element={<InterventionCompare />} />
          <Route path="/prescribe" element={<Prescription />} />
          <Route path="/what-if" element={<WhatIfSimulator />} />
          <Route path="/explain" element={<XAIExplainer />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
