// Game Over Workout Tracker — Test Data Generator
// Paste this into the browser console while the app is open, then refresh.
//
// Generates ~8 weeks of realistic workout history across all 3 phases
// with progressive overload and natural variation.

(function generateTestData() {
    const APP_ID = 'game-over-tracker-v5';

    // Base weights per exercise (starting point, will progress upward)
    const exerciseProfiles = {
        // Phase 1 — Conditioning (12-15 reps, lighter weight)
        "Incline Barbell Press":    { base: 95,  increment: 5,  variance: 5 },
        "Flat Barbell Press":       { base: 115, increment: 5,  variance: 5 },
        "Dumbbell Flyes":           { base: 25,  increment: 2.5, variance: 2.5 },
        "Push Ups":                 { base: 0,   increment: 0,  variance: 0 },
        "Crunches":                 { base: 0,   increment: 0,  variance: 0 },
        "Reverse Crunches":         { base: 0,   increment: 0,  variance: 0 },
        "Squats":                   { base: 135, increment: 10, variance: 5 },
        "Stiff Leg Deadlift":       { base: 115, increment: 5,  variance: 5 },
        "Leg Curl":                 { base: 70,  increment: 5,  variance: 5 },
        "Leg Press":                { base: 200, increment: 10, variance: 10 },
        "Leg Extension":            { base: 70,  increment: 5,  variance: 5 },
        "Standing Calf Raise":      { base: 135, increment: 5,  variance: 5 },
        "Seated Calf Raise":        { base: 70,  increment: 5,  variance: 5 },
        "Bent Over Barbell Row":    { base: 95,  increment: 5,  variance: 5 },
        "1-Arm Dumbbell Row":       { base: 40,  increment: 5,  variance: 2.5 },
        "Cable Pull Downs":         { base: 100, increment: 5,  variance: 5 },
        "Wide Grip Pull Downs":     { base: 90,  increment: 5,  variance: 5 },
        "Dumbbell Shrugs":          { base: 50,  increment: 5,  variance: 5 },
        "Hyperextensions":          { base: 25,  increment: 5,  variance: 5 },
        "Standing Barbell Curls":   { base: 55,  increment: 2.5, variance: 2.5 },
        "Preacher Curls":           { base: 45,  increment: 2.5, variance: 2.5 },
        "Hammer Curls":             { base: 25,  increment: 2.5, variance: 2.5 },
        "Triceps Press Down":       { base: 60,  increment: 5,  variance: 5 },
        "Over Head Extensions":     { base: 40,  increment: 2.5, variance: 2.5 },
        "Weighted Dips":            { base: 25,  increment: 5,  variance: 5 },
        "V-ups":                    { base: 0,   increment: 0,  variance: 0 },
        "Leg Raises":               { base: 0,   increment: 0,  variance: 0 },
        "Dumbbell Military Press":  { base: 35,  increment: 5,  variance: 2.5 },
        "Front DB Raise":           { base: 15,  increment: 2.5, variance: 2.5 },
        "Side Lateral Raise":       { base: 15,  increment: 2.5, variance: 2.5 },
        "Rear Delt Machine":        { base: 60,  increment: 5,  variance: 5 },
        // Phase 2 extras
        "Pec Deck":                 { base: 90,  increment: 5,  variance: 5 },
        "High Row Wide Grip":       { base: 100, increment: 5,  variance: 5 },
        "Close Grip Low Row":       { base: 100, increment: 5,  variance: 5 },
        "Dumbbell Pullovers":       { base: 35,  increment: 2.5, variance: 2.5 },
        "Barbell Shrugs":           { base: 135, increment: 10, variance: 5 },
        "Dumbbell Curls":           { base: 30,  increment: 2.5, variance: 2.5 },
        "Concentration Curls":      { base: 20,  increment: 2.5, variance: 2.5 },
        "Close Grip Press":         { base: 95,  increment: 5,  variance: 5 },
        "V-Bar Press Down":         { base: 65,  increment: 5,  variance: 5 },
        "Single Arm Over Head Ext": { base: 20,  increment: 2.5, variance: 2.5 },
        // Phase 3 extras
        "Barbell Rows":             { base: 135, increment: 10, variance: 5 },
        "One Arm Dumbbell Rows":    { base: 55,  increment: 5,  variance: 5 },
        "Barbell Curls":            { base: 75,  increment: 5,  variance: 5 },
        "Overhead Extension":       { base: 55,  increment: 5,  variance: 5 },
        "V-Ups":                    { base: 0,   increment: 0,  variance: 0 },
        "Overhead Press":           { base: 95,  increment: 5,  variance: 5 },
        "DB Front Raise":           { base: 20,  increment: 2.5, variance: 2.5 },
        "Rear Delt Fly":            { base: 20,  increment: 2.5, variance: 2.5 },
    };

    const programData = {
        1: { targetRepsMin: 12, targetRepsMax: 15, workouts: {
            "Monday": [{ name: "Incline Barbell Press", sets: 4 }, { name: "Flat Barbell Press", sets: 4 }, { name: "Dumbbell Flyes", sets: 4 }, { name: "Push Ups", sets: 3 }, { name: "Crunches", sets: 4 }, { name: "Reverse Crunches", sets: 4 }],
            "Tuesday": [{ name: "Squats", sets: 4 }, { name: "Stiff Leg Deadlift", sets: 4 }, { name: "Leg Curl", sets: 4 }, { name: "Leg Press", sets: 4 }, { name: "Leg Extension", sets: 4 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }],
            "Wednesday": [{ name: "Bent Over Barbell Row", sets: 4 }, { name: "1-Arm Dumbbell Row", sets: 4 }, { name: "Cable Pull Downs", sets: 4 }, { name: "Wide Grip Pull Downs", sets: 4 }, { name: "Dumbbell Shrugs", sets: 4 }, { name: "Hyperextensions", sets: 4 }],
            "Friday": [{ name: "Standing Barbell Curls", sets: 4 }, { name: "Preacher Curls", sets: 4 }, { name: "Hammer Curls", sets: 3 }, { name: "Triceps Press Down", sets: 4 }, { name: "Over Head Extensions", sets: 4 }, { name: "Weighted Dips", sets: 3 }, { name: "V-ups", sets: 4 }, { name: "Leg Raises", sets: 4 }],
            "Saturday": [{ name: "Dumbbell Military Press", sets: 4 }, { name: "Front DB Raise", sets: 4 }, { name: "Side Lateral Raise", sets: 4 }, { name: "Rear Delt Machine", sets: 4 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }]
        }},
        2: { targetRepsMin: 6, targetRepsMax: 10, workouts: {
            "Monday": [{ name: "Incline Barbell Press", sets: 6 }, { name: "Flat Barbell Press", sets: 6 }, { name: "Pec Deck", sets: 4 }, { name: "Push Ups", sets: 3 }, { name: "V-ups", sets: 4 }, { name: "Leg Raises", sets: 4 }],
            "Tuesday": [{ name: "Squats", sets: 6 }, { name: "Stiff Leg Deadlift", sets: 6 }, { name: "Leg Curl", sets: 4 }, { name: "Leg Press", sets: 6 }, { name: "Leg Extension", sets: 4 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }],
            "Wednesday": [{ name: "High Row Wide Grip", sets: 6 }, { name: "Close Grip Low Row", sets: 6 }, { name: "Wide Grip Pull Downs", sets: 6 }, { name: "Dumbbell Pullovers", sets: 4 }, { name: "Barbell Shrugs", sets: 6 }, { name: "Hyperextensions", sets: 4 }],
            "Friday": [{ name: "Dumbbell Curls", sets: 6 }, { name: "Preacher Curls", sets: 6 }, { name: "Concentration Curls", sets: 3 }, { name: "Close Grip Press", sets: 6 }, { name: "V-Bar Press Down", sets: 6 }, { name: "Single Arm Over Head Ext", sets: 3 }],
            "Saturday": [{ name: "Dumbbell Military Press", sets: 6 }, { name: "Front DB Raise", sets: 6 }, { name: "Side Lateral Raise", sets: 6 }, { name: "Rear Delt Machine", sets: 6 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }]
        }},
        3: { targetRepsMin: 4, targetRepsMax: 6, workouts: {
            "Monday": [{ name: "Incline Barbell Press", sets: 6 }, { name: "Flat Barbell Press", sets: 6 }, { name: "Dumbbell Flyes", sets: 4 }, { name: "Push Ups", sets: 3 }, { name: "Crunches", sets: 4 }, { name: "Reverse Crunches", sets: 4 }],
            "Tuesday": [{ name: "Squats", sets: 6 }, { name: "Stiff Leg Deadlift", sets: 6 }, { name: "Leg Curl", sets: 4 }, { name: "Leg Press", sets: 6 }, { name: "Leg Extension", sets: 4 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }],
            "Wednesday": [{ name: "Barbell Rows", sets: 6 }, { name: "One Arm Dumbbell Rows", sets: 6 }, { name: "Cable Pull Downs", sets: 4 }, { name: "Wide Grip Pull Downs", sets: 6 }, { name: "Dumbbell Shrugs", sets: 6 }, { name: "Hyperextensions", sets: 4 }],
            "Friday": [{ name: "Barbell Curls", sets: 6 }, { name: "Preacher Curls", sets: 6 }, { name: "Hammer Curls", sets: 3 }, { name: "Triceps Press Down", sets: 6 }, { name: "Overhead Extension", sets: 6 }, { name: "Weighted Dips", sets: 3 }, { name: "V-Ups", sets: 4 }, { name: "Leg Raises", sets: 4 }],
            "Saturday": [{ name: "Overhead Press", sets: 6 }, { name: "DB Front Raise", sets: 6 }, { name: "Side Lateral Raise", sets: 5 }, { name: "Rear Delt Fly", sets: 6 }, { name: "Standing Calf Raise", sets: 4 }, { name: "Seated Calf Raise", sets: 4 }]
        }}
    };

    // Day-of-week to actual calendar dates (8 weeks starting 2025-12-15)
    const dayMap = { "Monday": 1, "Tuesday": 2, "Wednesday": 3, "Friday": 5, "Saturday": 6 };

    function getDatesForDay(dayName, weeksBack) {
        const dates = [];
        const today = new Date(2026, 1, 11); // Feb 11, 2026
        const targetDow = dayMap[dayName];

        // Find the most recent occurrence of this day
        let d = new Date(today);
        d.setDate(d.getDate() - ((d.getDay() - targetDow + 7) % 7));

        for (let w = 0; w < weeksBack; w++) {
            const date = new Date(d);
            date.setDate(date.getDate() - (w * 7));
            dates.push(date.toISOString().split('T')[0]);
        }
        return dates.reverse(); // oldest first
    }

    function rand(min, max) {
        return Math.round((Math.random() * (max - min) + min) * 2) / 2; // round to nearest 0.5
    }

    const history = [];
    const currentLogs = {};

    // Phase schedule: Phase 1 weeks 1-3, Phase 2 weeks 4-6, Phase 3 weeks 7-8
    const phaseWeeks = { 1: [0, 1, 2], 2: [3, 4, 5], 3: [6, 7] };

    for (const [phaseStr, phase] of Object.entries(programData)) {
        const phaseNum = parseInt(phaseStr);
        const weekIndices = phaseWeeks[phaseNum];
        // Weight multiplier: Phase 2 is heavier than Phase 1, Phase 3 heaviest
        const phaseMultiplier = phaseNum === 1 ? 1.0 : phaseNum === 2 ? 1.2 : 1.4;

        for (const [dayName, exercises] of Object.entries(phase.workouts)) {
            const allDates = getDatesForDay(dayName, 8);
            const phaseDates = weekIndices.map(wi => allDates[wi]).filter(Boolean);

            exercises.forEach(ex => {
                const profile = exerciseProfiles[ex.name];
                if (!profile) {
                    console.warn(`No profile for: ${ex.name}`);
                    return;
                }

                phaseDates.forEach((date, dateIdx) => {
                    const isBodyweight = profile.base === 0;
                    // Progressive overload: weight increases per session
                    const progressWeight = profile.base * phaseMultiplier + (profile.increment * dateIdx);

                    for (let s = 1; s <= ex.sets; s++) {
                        const logKey = `${phaseNum}-${dayName}-${ex.name}-s${s}`;

                        // Later sets get slightly fewer reps (fatigue)
                        const fatigueDrop = Math.floor(s / 2);
                        const repsMin = phase.targetRepsMin;
                        const repsMax = phase.targetRepsMax;
                        const reps = Math.max(repsMin, Math.round(rand(repsMin, repsMax) - fatigueDrop));

                        // Weight varies slightly per set, drops a bit on last sets
                        let weight;
                        if (isBodyweight) {
                            weight = 0;
                        } else {
                            const setDropoff = s > (ex.sets - 1) ? profile.variance : 0;
                            weight = Math.max(5, Math.round((progressWeight - setDropoff + rand(-profile.variance, profile.variance)) / 2.5) * 2.5);
                        }

                        const entry = {
                            w: String(weight),
                            r: String(reps),
                            key: logKey,
                            date: date
                        };
                        history.push(entry);

                        // Use latest session data as current logs
                        if (dateIdx === phaseDates.length - 1) {
                            currentLogs[logKey] = { w: String(weight), r: String(reps) };
                        }
                    }
                });
            });
        }
    }

    // Sort history by date
    history.sort((a, b) => a.date.localeCompare(b.date));

    // Save to localStorage
    localStorage.setItem(`${APP_ID}_history`, JSON.stringify(history));
    localStorage.setItem(`${APP_ID}_logs`, JSON.stringify(currentLogs));

    console.log(`[Test Data] Generated ${history.length} history entries across 8 weeks`);
    console.log(`[Test Data] Current logs: ${Object.keys(currentLogs).length} sets`);
    console.log('[Test Data] Refresh the page to see the data');
})();
