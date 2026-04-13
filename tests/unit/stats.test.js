'use strict';

const { getExerciseStats } = require('../../src/core');

const makeEntry = (phase, day, name, setNum, date, w, r) => ({
    key: `${phase}-${day}-${name}-s${setNum}`,
    date,
    w: String(w),
    r: String(r),
});

describe('getExerciseStats', () => {
    test('returns empty stats for no matching history', () => {
        const result = getExerciseStats([], 1, 'Bench Press');
        expect(result.sessions).toBe(0);
        expect(result.avgData).toEqual([]);
        expect(result.sessionLog).toEqual([]);
        expect(result.bestWeight).toBe(0);
        expect(result.trend).toBe(0);
    });

    test('only includes entries for the correct phase', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(2, 'Monday', 'Bench Press', 1, '2026-04-01', 120, 6),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.sessions).toBe(1);
        expect(result.bestWeight).toBe(100);
    });

    test('counts sessions correctly across multiple dates', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Bench Press', 2, '2026-04-01', 105, 8),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-08', 107, 10),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.sessions).toBe(2);
    });

    test('calculates bestWeight correctly', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Bench Press', 2, '2026-04-01', 110, 8),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-08', 107, 10),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.bestWeight).toBe(110);
    });

    test('calculates avgWeight per session correctly', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Bench Press', 2, '2026-04-01', 110, 8),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.avgData[0].avgWeight).toBe(105); // (100+110)/2
    });

    test('sessionLog is sorted newest first', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-08', 107, 10),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-15', 112, 10),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.sessionLog[0].date).toBe('2026-04-15');
        expect(result.sessionLog[2].date).toBe('2026-04-01');
    });

    test('avgData is sorted oldest first', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-15', 112, 10),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.avgData[0].date).toBe('04-01');
        expect(result.avgData[1].date).toBe('04-15');
    });

    test('calculates positive trend correctly', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-08', 110, 10),
        ];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.trend).toBe(10); // 10% increase
    });

    test('trend is 0 when only one session', () => {
        const history = [makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10)];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.trend).toBe(0);
    });

    test('does not cross-contaminate exercise names', () => {
        const history = [
            makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10),
            makeEntry(1, 'Monday', 'Incline Press', 1, '2026-04-01', 80, 10),
        ];
        const bench = getExerciseStats(history, 1, 'Bench Press');
        expect(bench.bestWeight).toBe(100);
        expect(bench.sessions).toBe(1);
    });

    test('sessionLog entries include key field on each set', () => {
        const history = [makeEntry(1, 'Monday', 'Bench Press', 1, '2026-04-01', 100, 10)];
        const result = getExerciseStats(history, 1, 'Bench Press');
        expect(result.sessionLog[0].entries[0].key).toBe('1-Monday-Bench Press-s1');
    });
});
