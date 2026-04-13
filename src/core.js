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
    const sessions = Object.keys(byDate).length;
    const firstAvgWeight = avgData[0] ? avgData[0].avgWeight : 0;
    const lastAvgWeight = avgData[avgData.length - 1] ? avgData[avgData.length - 1].avgWeight : 0;
    const trend = firstAvgWeight > 0
        ? Math.round(((lastAvgWeight - firstAvgWeight) / firstAvgWeight) * 100)
        : 0;
    return { name, avgData, setData, sessionLog, sessions, bestWeight,
             firstAvgWeight, lastAvgWeight, trend };
}

if (typeof module !== 'undefined') {
    module.exports = { makeLogKey, mergeHistory, deleteOneHistoryEntry, getExerciseStats };
}
