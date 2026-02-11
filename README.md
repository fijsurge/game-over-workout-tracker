Game Over Tracker

A high-intensity workout tracker based on the Game Over program.

Features

Phase Logic: Automatically adapts targets for Phase 1 (Conditioning), Phase 2 (Growth), and Phase 3 (Strength).

Strength Analytics: Tracks Peak Weight and Average Weight per exercise.

PWA Ready: Installable on iOS and Android via browser.

How to use this repository

This is a single-file application located in index.html.

It uses CDNs for React and Tailwind CSS, so no npm install is required.

Customizations Made

Set counts and exercises have been manually adjusted to match user preferences.

Phase 3 includes specific exercises like Deadlifts and Weighted Dips.

Progress visualization focuses on weight progression rather than just volume.

## Test Data

To generate sample workout data for testing the interface and charts:

1. Open `index.html` in a browser
2. Open the developer console (F12)
3. Paste the contents of `test-data.js` and press Enter
4. Refresh the page

This generates 8 weeks of realistic workout history across all 3 phases with progressive overload, fatigue drop-off, and natural set-to-set variance. The most recent session is pre-loaded as current log inputs.
