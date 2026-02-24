/* ============================================================
   MANO AMISE — Backend API Client
   
   All API calls go through this module. Provides optimistic
   error handling and consistent response formatting.
   ============================================================ */

const API_BASE = 'http://localhost:8000/api/v1';

/**
 * Core fetch wrapper with error handling.
 * Returns { data, error } — never throws.
 */
async function request(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options,
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            return { data: null, error: err.detail || `HTTP ${response.status}` };
        }

        // Handle 204 No Content
        if (response.status === 204) return { data: null, error: null };

        const data = await response.json();
        return { data, error: null };
    } catch (err) {
        return { data: null, error: err.message || 'Network error' };
    }
}

// ─── Health ─────────────────────────────────────────
export const getHealth = () =>
    request('/health', { method: 'GET' });

// ─── Patients ───────────────────────────────────────
export const listPatients = (skip = 0, limit = 50) =>
    request(`/patients?skip=${skip}&limit=${limit}`);

export const getPatient = (id) =>
    request(`/patients/${id}`);

export const createPatient = (patient) =>
    request('/patients', { method: 'POST', body: JSON.stringify(patient) });

export const updatePatient = (id, patient) =>
    request(`/patients/${id}`, { method: 'PUT', body: JSON.stringify(patient) });

export const deletePatient = (id) =>
    request(`/patients/${id}`, { method: 'DELETE' });

// ─── Simulation ─────────────────────────────────────
export const predictRisk = (patientState) =>
    request('/simulation/predict_risk', {
        method: 'POST',
        body: JSON.stringify(patientState),
    });

export const simulateIntervention = (payload) =>
    request('/simulation/simulate_intervention', {
        method: 'POST',
        body: JSON.stringify(payload),
    });

export const simulateBatch = (payload) =>
    request('/simulation/simulate_batch', {
        method: 'POST',
        body: JSON.stringify(payload),
    });

export const prescribeAI = (patientState) =>
    request('/simulation/prescribe_ai', {
        method: 'POST',
        body: JSON.stringify(patientState),
    });

// ─── What-If Simulator ──────────────────────────────
export const simulateWhatIf = (payload) =>
    request('/whatif/what_if', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
