# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Game Over Workout Tracker — a single-file React PWA for tracking workouts based on the "Game Over" fitness program. Tracks weights/reps across 3 training phases with analytics visualization.

## Development

**No build step, no npm, no dependencies to install.** The entire app lives in `index.html` using CDN-delivered libraries (React 18, Tailwind CSS, Recharts, Babel for JSX).

- **Run:** Open `index.html` in a browser, or serve via any HTTP server for PWA features
- **Edit:** Modify `index.html` directly, refresh browser to see changes
- **No tests or linting configured**

## Architecture

Single-component React app rendered inline via Babel. All code is in one `<script type="text/babel">` block inside `index.html`.

**State management:** React hooks (`useState`, `useEffect`, `useMemo`) with localStorage persistence. App namespace: `game-over-tracker-v5`.

**Storage keys:**
- `game-over-tracker-v5_logs` — current session workout data
- `game-over-tracker-v5_history` — all historical workouts with timestamps

**Exercise log key format:** `${phase}-${day}-${exerciseName}-s${setNumber}` — each set stores weight and reps independently.

**Views:** Two views toggled by `view` state — `'workout'` (logging interface) and `'stats'` (Recharts line chart showing peak/average weight trends per exercise).

**Program structure:** 3 phases (Conditioning/Growth/Strength) with different target rep ranges and set counts. 5-day weekly split (Mon/Tue/Wed/Fri/Sat). Phase 3 has custom exercises (Deadlifts, Weighted Dips). The program data is defined as a `PROGRAMS` object at the top of the script.

**UI components:** All inline — custom SVG icon components, toast notification system with bounce animation, phase selector tabs, day dropdown, exercise cards with per-set inputs.
