#!/usr/bin/env node

// Generate PWA icons for Game Over Tracker
// Creates 192x192 and 512x512 PNG icons

const fs = require('fs');
const path = require('path');

// Simple PNG encoder - creates valid PNG directly
function createPNG(width, height, pixelData) {
  const zlib = require('zlib');
  const crc32 = require('crypto').createHash('sha256'); // placeholder
  
  // For simplicity, use node-canvas if available, otherwise create a data URL placeholder
  // Since we don't have canvas, we'll create the icon using base64-encoded PNG data
  return null; // Will use SVG to PNG conversion instead
}

// Create SVG-based icons that can be converted
function createSVGIcon(size) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<svg width="${size}" height="${size}" viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg">
  <!-- Dark background -->
  <rect width="${size}" height="${size}" fill="#0f172a"/>
  
  <!-- Red dumbbell icon -->
  <g transform="translate(${size/2}, ${size/2})">
    <!-- Left weight plate -->
    <rect x="${-size*0.35}" y="${-size*0.15}" width="${size*0.15}" height="${size*0.3}" rx="${size*0.02}" fill="#ef4444"/>
    
    <!-- Bar -->
    <rect x="${-size*0.12}" y="${-size*0.08}" width="${size*0.24}" height="${size*0.16}" rx="${size*0.02}" fill="#dc2626"/>
    
    <!-- Right weight plate -->
    <rect x="${size*0.2}" y="${-size*0.15}" width="${size*0.15}" height="${size*0.3}" rx="${size*0.02}" fill="#ef4444"/>
    
    <!-- Accent lines for depth -->
    <line x1="${-size*0.35}" y1="${-size*0.08}" x2="${-size*0.2}" y2="${-size*0.08}" stroke="#fca5a5" stroke-width="${size*0.02}" stroke-linecap="round"/>
    <line x1="${size*0.2}" y1="${-size*0.08}" x2="${size*0.35}" y2="${-size*0.08}" stroke="#fca5a5" stroke-width="${size*0.02}" stroke-linecap="round"/>
  </g>
</svg>`;
}

// Write SVG files
const rootDir = path.join(__dirname);
fs.writeFileSync(path.join(rootDir, 'icon-192.svg'), createSVGIcon(192));
fs.writeFileSync(path.join(rootDir, 'icon-512.svg'), createSVGIcon(512));

console.log('SVG icons generated. Use an online converter or sharp library to convert to PNG.');
console.log('For now, we will use SVG directly in the manifest and rely on browser support.');
