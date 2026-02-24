/* ============================================================
   MANO AMISE — XAI Risk Explainer
   
   Explains WHY the LSTM predicts a certain risk class using
   Integrated Gradients attribution. Shows:
   - Temporal heatmap (which days × which features matter)
   - Feature importance ranking
   - Human-readable explanation
   ============================================================ */
import { useState, useCallback, useMemo } from 'react';
import { useApi } from '../hooks/useApi';
import { listPatients, getPatient, explainRisk } from '../api/client';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
    ResponsiveContainer, Cell
} from 'recharts';
import { motion, AnimatePresence } from 'framer-motion';
import {
    ScanSearch, Loader, AlertTriangle, ShieldCheck,
    ShieldAlert, TrendingUp, TrendingDown
} from 'lucide-react';

/* ── Color scale for heatmap ─────────────────────── */
function getHeatColor(normalized, attribution) {
    // Positive attributions (risk-increasing) = red shades
    // Negative attributions (risk-decreasing) = teal/green shades
    const intensity = Math.min(normalized, 1);
    if (attribution >= 0) {
        return `rgba(255, 76, 76, ${0.1 + intensity * 0.8})`;
    }
    return `rgba(45, 212, 191, ${0.1 + intensity * 0.8})`;
}

/* ── Build patient state (same as WhatIfSimulator) ── */
function buildPatientState(patient) {
    if (!patient) return null;

    const dynamic_history = [];
    for (let i = 0; i < 7; i++) {
        dynamic_history.push({
            sleep_hours: patient.sleep_duration ?? 6.5 + (Math.random() - 0.5),
            sleep_quality: patient.sleep_quality ?? 0.6 + (Math.random() - 0.5) * 0.2,
            heart_rate: patient.heart_rate ?? 75 + (Math.random() - 0.5) * 10,
            stress_level: patient.stress_level ?? 0.5 + (Math.random() - 0.5) * 0.2,
        });
    }

    const static_features = Array(20).fill(0).map((_, i) => {
        if (i === 0) return (patient.age ?? 30) / 100;
        if (i === 1) return patient.gender === 'Female' ? 1.0 : 0.0;
        return Math.random() * 0.5;
    });

    return {
        static_data: { features: static_features },
        dynamic_history,
    };
}

const FEATURES = ["Sleep Duration", "Sleep Quality", "Heart Rate", "Stress Level"];
const DAYS = [1, 2, 3, 4, 5, 6, 7];

/* ══════════════════════════════════════════════════
   MAIN COMPONENT
   ══════════════════════════════════════════════════ */
export default function XAIExplainer() {
    const { data: patientsResp, loading: patientsLoading } = useApi(listPatients, { key: 'xai-patients' });
    const patients = patientsResp?.patients || patientsResp || [];

    const [selectedId, setSelectedId] = useState(null);
    const [patientState, setPatientState] = useState(null);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    /* ── Load patient and run explanation ──────── */
    const handleSelectPatient = useCallback(async (id) => {
        setSelectedId(id);
        setResult(null);
        setError(null);
        setLoading(true);

        const { data: ptData, error: ptErr } = await getPatient(id);
        if (ptErr) { setError(ptErr); setLoading(false); return; }

        const state = buildPatientState(ptData);
        setPatientState(state);

        // Auto-run explanation
        const { data, error: xErr } = await explainRisk(state);
        setLoading(false);

        if (xErr) { setError(xErr); return; }
        setResult(data);
    }, []);

    /* ── Build heatmap grid data ──────────────── */
    const heatmapData = useMemo(() => {
        if (!result) return null;

        // Build a 2D grid: features × days
        const grid = {};
        result.temporal_attributions.forEach(a => {
            if (!grid[a.feature]) grid[a.feature] = {};
            grid[a.feature][a.day] = { attribution: a.attribution, normalized: a.normalized };
        });
        return grid;
    }, [result]);

    /* ── Bar chart data for feature rankings ──── */
    const rankingChartData = useMemo(() => {
        if (!result) return [];
        return result.feature_rankings.map(f => ({
            feature: f.feature,
            importance: f.total_attribution,
            direction: f.direction,
        }));
    }, [result]);

    /* ── Risk colors ─────────────────────────── */
    const riskColor = (cls) => ({
        Low: 'var(--safe)', Medium: 'var(--caution)', High: 'var(--risk)'
    }[cls] || 'var(--text-muted)');

    const RiskIcon = result?.risk_prediction?.current_risk_class === 'High'
        ? ShieldAlert
        : ShieldCheck;

    return (
        <motion.div
            className="page"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.4 }}
        >
            {/* Header */}
            <header className="page-header">
                <div>
                    <h1 style={{ display: 'flex', alignItems: 'center', gap: 'var(--sp-2)' }}>
                        <ScanSearch size={28} />
                        Risk Explainer
                    </h1>
                    <p className="data-label" style={{ marginTop: 'var(--sp-1)' }}>
                        Integrated Gradients attribution — understand why the LSTM predicts a risk class
                    </p>
                </div>
            </header>

            {/* Patient Selector */}
            <div className="glass-card" style={{ marginBottom: 'var(--sp-5)' }}>
                <div className="data-label" style={{ marginBottom: 'var(--sp-2)' }}>Select Patient</div>
                {patientsLoading ? (
                    <div className="shimmer" style={{ height: 40, borderRadius: 8 }} />
                ) : (
                    <select
                        value={selectedId || ''}
                        onChange={(e) => handleSelectPatient(parseInt(e.target.value))}
                        style={{
                            width: '100%',
                            padding: 'var(--sp-2) var(--sp-3)',
                            background: 'var(--surface)',
                            border: '1px solid var(--border)',
                            borderRadius: 8,
                            color: 'var(--text-primary)',
                            fontSize: '0.95rem',
                            cursor: 'pointer',
                        }}
                    >
                        <option value="">— Choose a patient —</option>
                        {(Array.isArray(patients) ? patients : []).map(p => (
                            <option key={p.id} value={p.id}>
                                {p.name || `Patient #${p.id}`} {p.age ? `(${p.age}y)` : ''}
                            </option>
                        ))}
                    </select>
                )}
            </div>

            {/* Loading */}
            {loading && (
                <div className="glass-card" style={{ textAlign: 'center', padding: 'var(--sp-6)' }}>
                    <Loader size={32} className="spin" color="var(--predict)" />
                    <p style={{ marginTop: 'var(--sp-2)', color: 'var(--text-secondary)' }}>
                        Computing Integrated Gradients (50 steps)...
                    </p>
                </div>
            )}

            {/* Error */}
            {error && (
                <div className="glass-card" style={{
                    borderColor: 'var(--risk)', color: 'var(--risk)', marginBottom: 'var(--sp-5)',
                }}>
                    <AlertTriangle size={16} style={{ marginRight: 'var(--sp-2)', verticalAlign: 'middle' }} />
                    {typeof error === 'string' ? error : 'Explanation failed. Ensure backend is running.'}
                </div>
            )}

            {/* Results */}
            <AnimatePresence>
                {result && !loading && (
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.5 }}
                    >
                        {/* Risk Prediction Card */}
                        <div className="glass-card" style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 'var(--sp-4)',
                            marginBottom: 'var(--sp-5)',
                            borderLeft: `3px solid ${riskColor(result.risk_prediction.current_risk_class)}`,
                        }}>
                            <RiskIcon
                                size={36}
                                color={riskColor(result.risk_prediction.current_risk_class)}
                            />
                            <div style={{ flex: 1 }}>
                                <div style={{
                                    fontSize: '1.5rem',
                                    fontWeight: 700,
                                    color: riskColor(result.risk_prediction.current_risk_class),
                                    fontFamily: 'var(--font-data)',
                                }}>
                                    {result.risk_prediction.current_risk_class} Risk
                                </div>
                                <div className="data-label">
                                    {(result.risk_prediction.confidence * 100).toFixed(0)}% confidence
                                    &nbsp;•&nbsp;
                                    Low {(result.risk_prediction.probabilities[0] * 100).toFixed(0)}%
                                    &nbsp; Med {(result.risk_prediction.probabilities[1] * 100).toFixed(0)}%
                                    &nbsp; High {(result.risk_prediction.probabilities[2] * 100).toFixed(0)}%
                                </div>
                            </div>
                        </div>

                        {/* Explanation */}
                        <div className="glass-card" style={{
                            marginBottom: 'var(--sp-5)',
                            borderLeft: '3px solid var(--predict)',
                        }}>
                            <p style={{ fontSize: '0.95rem', lineHeight: 1.7 }}>
                                {result.explanation}
                            </p>
                        </div>

                        {/* Temporal Attribution Heatmap */}
                        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: 'var(--sp-2)' }}>
                            Temporal Attribution Heatmap
                        </h2>
                        <p className="data-label" style={{ marginBottom: 'var(--sp-3)' }}>
                            Red = pushes toward higher risk • Teal = pushes toward lower risk • Intensity = magnitude
                        </p>

                        <div className="glass-card" style={{
                            marginBottom: 'var(--sp-5)',
                            padding: 'var(--sp-3)',
                            overflowX: 'auto',
                        }}>
                            <table style={{
                                width: '100%',
                                borderCollapse: 'separate',
                                borderSpacing: 4,
                            }}>
                                <thead>
                                    <tr>
                                        <th style={{
                                            textAlign: 'left',
                                            fontSize: '0.75rem',
                                            color: 'var(--text-muted)',
                                            padding: '4px 8px',
                                            fontWeight: 500,
                                        }}>Feature</th>
                                        {DAYS.map(d => (
                                            <th key={d} style={{
                                                textAlign: 'center',
                                                fontSize: '0.75rem',
                                                color: 'var(--text-muted)',
                                                padding: '4px 8px',
                                                fontWeight: 500,
                                                minWidth: 60,
                                            }}>Day {d}</th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody>
                                    {heatmapData && FEATURES.map(feat => (
                                        <tr key={feat}>
                                            <td style={{
                                                fontSize: '0.8rem',
                                                color: 'var(--text-secondary)',
                                                padding: '4px 8px',
                                                fontWeight: 500,
                                                whiteSpace: 'nowrap',
                                            }}>{feat}</td>
                                            {DAYS.map(day => {
                                                const cell = heatmapData[feat]?.[day];
                                                if (!cell) return <td key={day} />;
                                                return (
                                                    <td key={day} style={{
                                                        textAlign: 'center',
                                                        background: getHeatColor(cell.normalized, cell.attribution),
                                                        borderRadius: 6,
                                                        padding: '8px 4px',
                                                        fontSize: '0.7rem',
                                                        fontFamily: 'var(--font-data)',
                                                        color: cell.normalized > 0.5 ? '#fff' : 'var(--text-secondary)',
                                                        transition: 'all var(--duration-normal) var(--ease-out)',
                                                    }}>
                                                        {cell.attribution > 0 ? '+' : ''}{cell.attribution.toFixed(4)}
                                                    </td>
                                                );
                                            })}
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Feature Importance Ranking */}
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))',
                            gap: 'var(--sp-4)',
                            marginBottom: 'var(--sp-5)',
                        }}>
                            {/* Bar Chart */}
                            <div className="glass-card" style={{ padding: 'var(--sp-3)' }}>
                                <div className="data-label" style={{ marginBottom: 'var(--sp-3)' }}>
                                    Feature Importance (Total |Attribution|)
                                </div>
                                <ResponsiveContainer width="100%" height={200}>
                                    <BarChart data={rankingChartData} layout="vertical">
                                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                                        <XAxis
                                            type="number"
                                            tick={{ fill: 'var(--text-muted)', fontSize: 10 }}
                                            axisLine={{ stroke: 'var(--border)' }}
                                        />
                                        <YAxis
                                            type="category"
                                            dataKey="feature"
                                            tick={{ fill: 'var(--text-secondary)', fontSize: 11 }}
                                            axisLine={{ stroke: 'var(--border)' }}
                                            width={110}
                                        />
                                        <Tooltip
                                            contentStyle={{
                                                background: 'var(--card)',
                                                border: '1px solid var(--border)',
                                                borderRadius: 8,
                                                color: 'var(--text-primary)',
                                                fontSize: '0.8rem',
                                            }}
                                        />
                                        <Bar dataKey="importance" radius={[0, 4, 4, 0]}>
                                            {rankingChartData.map((entry, i) => (
                                                <Cell
                                                    key={i}
                                                    fill={entry.direction === 'risk-increasing'
                                                        ? 'var(--risk)' : 'var(--safe)'}
                                                    fillOpacity={0.75}
                                                />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>

                            {/* Ranked List */}
                            <div className="glass-card" style={{ padding: 'var(--sp-3)' }}>
                                <div className="data-label" style={{ marginBottom: 'var(--sp-3)' }}>
                                    Attribution Direction
                                </div>
                                {result.feature_rankings.map((f, i) => {
                                    const isRiskIncreasing = f.direction === 'risk-increasing';
                                    const DirIcon = isRiskIncreasing ? TrendingUp : TrendingDown;
                                    const dirColor = isRiskIncreasing ? 'var(--risk)' : 'var(--safe)';

                                    return (
                                        <div key={f.feature} style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 'var(--sp-3)',
                                            padding: 'var(--sp-2) var(--sp-3)',
                                            background: i === 0 ? 'rgba(255,255,255,0.03)' : 'transparent',
                                            borderRadius: 8,
                                            marginBottom: 'var(--sp-1)',
                                        }}>
                                            <span style={{
                                                fontFamily: 'var(--font-data)',
                                                fontSize: '0.85rem',
                                                color: 'var(--text-muted)',
                                                width: 20,
                                            }}>#{f.rank}</span>
                                            <div style={{ flex: 1 }}>
                                                <div style={{ fontSize: '0.9rem', fontWeight: 500 }}>
                                                    {f.feature}
                                                </div>
                                                <div className="data-label" style={{ fontSize: '0.7rem' }}>
                                                    total: {f.total_attribution.toFixed(4)}
                                                </div>
                                            </div>
                                            <DirIcon size={18} color={dirColor} />
                                            <span style={{
                                                fontSize: '0.75rem',
                                                color: dirColor,
                                                fontFamily: 'var(--font-data)',
                                            }}>
                                                {f.direction.replace('-', ' ')}
                                            </span>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Static Feature Attributions */}
                        {result.static_attributions?.length > 0 && (
                            <div className="glass-card" style={{ marginBottom: 'var(--sp-5)' }}>
                                <div className="data-label" style={{ marginBottom: 'var(--sp-3)' }}>
                                    Top Demographic Factor Impacts (Static Features)
                                </div>
                                <div style={{
                                    display: 'flex',
                                    flexWrap: 'wrap',
                                    gap: 'var(--sp-2)',
                                }}>
                                    {result.static_attributions.map((s, i) => (
                                        <div key={i} style={{
                                            padding: 'var(--sp-2) var(--sp-3)',
                                            background: s.attribution > 0
                                                ? 'rgba(255, 76, 76, 0.1)'
                                                : 'rgba(45, 212, 191, 0.1)',
                                            borderRadius: 8,
                                            fontSize: '0.8rem',
                                            fontFamily: 'var(--font-data)',
                                            border: `1px solid ${s.attribution > 0 ? 'rgba(255,76,76,0.2)' : 'rgba(45,212,191,0.2)'}`,
                                        }}>
                                            <span style={{ color: 'var(--text-muted)' }}>F{s.feature_index}: </span>
                                            <span style={{
                                                color: s.attribution > 0 ? 'var(--risk)' : 'var(--safe)',
                                                fontWeight: 600,
                                            }}>
                                                {s.attribution > 0 ? '+' : ''}{s.attribution.toFixed(4)}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>

            {/* Empty State */}
            {!selectedId && !patientsLoading && !loading && (
                <div className="glass-card" style={{ textAlign: 'center', padding: 'var(--sp-8)' }}>
                    <ScanSearch size={48} color="var(--text-muted)" style={{ marginBottom: 'var(--sp-3)', opacity: 0.3 }} />
                    <p style={{ fontSize: '1.1rem', color: 'var(--text-secondary)', marginBottom: 'var(--sp-2)' }}>
                        Select a patient to explain their risk prediction
                    </p>
                    <p className="data-label">
                        Integrated Gradients will reveal which features and days drive the LSTM&apos;s decision
                    </p>
                </div>
            )}
        </motion.div>
    );
}
