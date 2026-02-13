#!/usr/bin/env python3
"""
Generate PNG icons for Game Over Workout Tracker PWA
Creates 192x192, 512x512, and 96x96 PNG icons with proper colors and branding.
"""

import os
from PIL import Image, ImageDraw

def create_icon(size, maskable=False):
    """Create a Game Over branded dumbbell icon"""
    # Create image with transparent background for maskable icons
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0) if maskable else (15, 23, 42, 255))
    draw = ImageDraw.Draw(img)
    
    # Scale factors
    center_x, center_y = size // 2, size // 2
    scale = size / 512  # Reference to 512px base
    
    # Dumbbell dimensions (scaled)
    plate_width = int(80 * scale)
    plate_height = int(160 * scale)
    bar_width = int(130 * scale)
    bar_height = int(70 * scale)
    bar_radius = int(15 * scale)
    
    # Dumbbell positions
    left_plate_x = center_x - int(180 * scale)
    right_plate_x = center_x + int(100 * scale)
    plate_y = center_y - plate_height // 2
    bar_y = center_y - bar_height // 2
    
    # Colors
    red_light = (239, 68, 68, 255)  # #ef4444
    red_dark = (220, 38, 38, 255)   # #dc2626
    red_accent = (252, 165, 165, 255)  # #fca5a5
    
    # Draw left weight plate
    draw.rectangle(
        [left_plate_x, plate_y, left_plate_x + plate_width, plate_y + plate_height],
        fill=red_light,
        outline=red_dark,
        width=max(1, int(3 * scale))
    )
    
    # Draw center bar
    bar_x = center_x - bar_width // 2
    draw.rectangle(
        [bar_x, bar_y, bar_x + bar_width, bar_y + bar_height],
        fill=red_dark,
        outline=red_accent,
        width=max(1, int(2 * scale))
    )
    
    # Draw right weight plate
    draw.rectangle(
        [right_plate_x, plate_y, right_plate_x + plate_width, plate_y + plate_height],
        fill=red_light,
        outline=red_dark,
        width=max(1, int(3 * scale))
    )
    
    # Add accent lines for dimension
    line_width = max(1, int(8 * scale))
    left_accent_x = left_plate_x + int(10 * scale)
    right_accent_x = right_plate_x + int(10 * scale)
    
    draw.line(
        [(left_accent_x, bar_y), (left_accent_x, bar_y + bar_height)],
        fill=red_accent,
        width=line_width
    )
    draw.line(
        [(right_accent_x, bar_y), (right_accent_x, bar_y + bar_height)],
        fill=red_accent,
        width=line_width
    )
    
    return img

def main():
    """Generate all required icon sizes"""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    sizes = {
        'icon-96.png': 96,
        'icon-192.png': 192,
        'icon-512.png': 512,
        'icon-192-maskable.png': 192,
        'icon-512-maskable.png': 512,
    }
    
    for filename, size in sizes.items():
        maskable = 'maskable' in filename
        print(f"Generating {filename} ({size}x{size}, maskable={maskable})...")
        
        icon = create_icon(size, maskable=maskable)
        filepath = os.path.join(script_dir, filename)
        icon.save(filepath, 'PNG')
        print(f"  ✓ Saved to {filepath}")
    
    print("\n✓ All icons generated successfully!")

if __name__ == '__main__':
    main()
