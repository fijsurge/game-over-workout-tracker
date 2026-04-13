'use strict';

const { makeLogKey, mergeHistory, deleteOneHistoryEntry } = require('../../src/core');

describe('makeLogKey', () => {
    test('formats key correctly', () => {
        expect(makeLogKey(1, 'Monday', 'Bench Press', 2)).toBe('1-Monday-Bench Press-s2');
    });

    test('works for all phases', () => {
        expect(makeLogKey(3, 'Friday', 'Deadlifts', 1)).toBe('3-Friday-Deadlifts-s1');
    });
});

describe('mergeHistory', () => {
    const base = [
        { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '100', r: '10' },
        { key: '1-Monday-Bench Press-s2', date: '2026-04-01', w: '105', r: '8' },
    ];

    test('appends new entries with new key+date', () => {
        const newEntry = { key: '1-Monday-Bench Press-s1', date: '2026-04-08', w: '102', r: '10' };
        const result = mergeHistory(base, [newEntry]);
        expect(result).toHaveLength(3);
        expect(result).toContainEqual(newEntry);
    });

    test('replaces existing entry on same key+date', () => {
        const updated = { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '110', r: '9' };
        const result = mergeHistory(base, [updated]);
        expect(result).toHaveLength(2);
        const s1 = result.find(e => e.key === '1-Monday-Bench Press-s1');
        expect(s1.w).toBe('110');
    });

    test('handles empty existing history', () => {
        const entry = { key: '1-Monday-Squat-s1', date: '2026-04-01', w: '200', r: '5' };
        expect(mergeHistory([], [entry])).toEqual([entry]);
    });

    test('handles empty new entries', () => {
        expect(mergeHistory(base, [])).toEqual(base);
    });

    test('deduplicates multiple new entries against existing', () => {
        const newEntries = [
            { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '115', r: '8' },
            { key: '1-Monday-Bench Press-s3', date: '2026-04-01', w: '95', r: '10' },
        ];
        const result = mergeHistory(base, newEntries);
        expect(result).toHaveLength(3); // s2 from base + s1 replaced + s3 new
        expect(result.find(e => e.key.endsWith('-s1')).w).toBe('115');
    });
});

describe('deleteOneHistoryEntry', () => {
    const history = [
        { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '100', r: '10' },
        { key: '1-Monday-Bench Press-s2', date: '2026-04-01', w: '100', r: '10' },
        { key: '1-Monday-Bench Press-s1', date: '2026-04-08', w: '105', r: '10' },
    ];

    test('removes the first matching entry only', () => {
        const result = deleteOneHistoryEntry(history, '1-Monday-Bench Press-s1', '2026-04-01');
        expect(result).toHaveLength(2);
        expect(result.find(e => e.key === '1-Monday-Bench Press-s1' && e.date === '2026-04-01')).toBeUndefined();
        // other s1 on different date is still there
        expect(result.find(e => e.key === '1-Monday-Bench Press-s1' && e.date === '2026-04-08')).toBeDefined();
    });

    test('returns same array reference when no match', () => {
        const result = deleteOneHistoryEntry(history, '1-Monday-Squat-s1', '2026-04-01');
        expect(result).toBe(history);
    });

    test('does not remove entries with same key but different date', () => {
        const result = deleteOneHistoryEntry(history, '1-Monday-Bench Press-s1', '2026-04-01');
        expect(result.find(e => e.date === '2026-04-08')).toBeDefined();
    });

    test('does not remove entries with same date but different key', () => {
        const result = deleteOneHistoryEntry(history, '1-Monday-Bench Press-s1', '2026-04-01');
        expect(result.find(e => e.key === '1-Monday-Bench Press-s2')).toBeDefined();
    });

    test('handles duplicate key+date entries by removing only first', () => {
        const withDupe = [
            { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '100', r: '10' },
            { key: '1-Monday-Bench Press-s1', date: '2026-04-01', w: '100', r: '10' },
        ];
        const result = deleteOneHistoryEntry(withDupe, '1-Monday-Bench Press-s1', '2026-04-01');
        expect(result).toHaveLength(1);
    });
});
