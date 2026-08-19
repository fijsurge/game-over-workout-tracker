'use strict';

/**
 * Build the log key for a single set.
 * Format: {phase}-{day}-{exerciseName}-s{setNum}
 */
function makeLogKey(phase, day, exerciseName, setNum) {
    return `${phase}-${day}-${exerciseName}-s${setNum}`;
}

/**
 * Merge newEntries into existing history.
 * New entries win on key+date conflicts (replace-not-append dedup).
 */
function mergeHistory(existing, newEntries) {
    const base = existing.filter(h => !newEntries.some(e => e.key === h.key && e.date === h.date));
    return [...base, ...newEntries];
}

/**
 * Remove the first history entry matching key+date.
 * Returns the original array (same reference) if no match found.
 */
function deleteOneHistoryEntry(history, key, date) {
    const idx = history.findIndex(e => e.key === key && e.date === date);
    if (idx === -1) return history;
    return [...history.slice(0, idx), ...history.slice(idx + 1)];
}

/**
 * Compute stats for a named exercise across all history.
 * Returns avgData (per-date averages), sessionLog (grouped by date desc),
 * setData (all individual sets), bestWeight, sessions count, trend %.
 */
function getExerciseStats(history, phase, name) {
    const filtered = history.filter(h =>
        h.key.startsWith(`${phase}-`) && h.key.includes(`-${name}-`)
    );
    if (filtered.length === 0) {
        return { name, avgData: [], setData: [], sessionLog: [], sessions: 0,
                 bestWeight: 0, firstAvgWeight: 0, lastAvgWeight: 0, trend: 0 };
    }
    const byDate = {};
    filtered.forEach(h => {
        if (!byDate[h.date]) byDate[h.date] = [];
        byDate[h.date].push({ w: parseFloat(h.w), r: parseFloat(h.r), key: h.key });
    });
    const avgData = Object.entries(byDate).map(([date, entries]) => ({
        date: date.slice(5),
        avgWeight: Math.round((entries.reduce((s, e) => s + e.w, 0) / entries.length) * 10) / 10,
        peakWeight: Math.max(...entries.map(e => e.w)),
        avgReps: Math.round((entries.reduce((s, e) => s + e.r, 0) / entries.length) * 10) / 10
    })).sort((a, b) => a.date.localeCompare(b.date));
    const setData = filtered.map(h => ({
        date: h.date.slice(5), weight: parseFloat(h.w), reps: parseFloat(h.r)
    })).sort((a, b) => a.date.localeCompare(b.date));
    const sessionLog = Object.entries(byDate)
        .sort((a, b) => b[0].localeCompare(a[0]))
        .map(([date, entries]) => ({ date, entries }));
    const bestWeight = Math.max(...filtered.map(h => parseFloat(h.w)));
    const bestReps = Math.max(...filtered.filter(h => parseFloat(h.w) === bestWeight).map(h => parseFloat(h.r)));
    const sessions = Object.keys(byDate).length;
    const firstAvgWeight = avgData[0] ? avgData[0].avgWeight : 0;
    const lastAvgWeight = avgData[avgData.length - 1] ? avgData[avgData.length - 1].avgWeight : 0;
    const trend = firstAvgWeight > 0
        ? Math.round(((lastAvgWeight - firstAvgWeight) / firstAvgWeight) * 100)
        : 0;
    return { name, avgData, setData, sessionLog, sessions, bestWeight, bestReps,
             firstAvgWeight, lastAvgWeight, trend };
}

/**
 * Compare a set being logged against the current personal best (heaviest
 * weight ever, and the most reps done at that exact weight).
 * Returns 'weight' for a new peak weight, 'reps' for matching the best
 * weight with more reps than ever done at it, or null otherwise.
 */
function getSetPRStatus(bestWeight, bestReps, weight, reps) {
    const w = parseFloat(weight);
    const r = parseFloat(reps);
    if (!bestWeight || isNaN(w) || isNaN(r) || w <= 0 || r <= 0) return null;
    if (w > bestWeight) return 'weight';
    if (w === bestWeight && r > bestReps) return 'reps';
    return null;
}

/**
 * Pivot history for one exercise into per-session rows for a multi-set line chart.
 * Returns [{date:'M/DD', s1:w, s1r:r, s2:w, s2r:r, ...}] sorted oldest→newest.
 */
function getSetProgressionData(history, phase, name) {
    const filtered = history.filter(h =>
        h.key.startsWith(`${phase}-`) && h.key.includes(`-${name}-s`)
    );
    const byDate = {};
    filtered.forEach(h => {
        const m = h.key.match(/-s(\d+)$/);
        if (!m) return;
        const s = parseInt(m[1]);
        if (!byDate[h.date]) byDate[h.date] = {};
        byDate[h.date][s] = { w: parseFloat(h.w), r: parseFloat(h.r) };
    });
    return Object.entries(byDate)
        .sort((a, b) => a[0].localeCompare(b[0]))
        .map(([date, sets]) => {
            const row = { date: date.slice(5) };
            Object.entries(sets).forEach(([s, { w, r }]) => {
                row[`s${s}`] = w;
                row[`s${s}r`] = r;
            });
            return row;
        });
}

if (typeof module !== 'undefined') {
    module.exports = { makeLogKey, mergeHistory, deleteOneHistoryEntry, getExerciseStats, getSetProgressionData, getSetPRStatus };
}
