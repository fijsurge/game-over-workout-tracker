// Generate PNG icons using Node.js with pure JavaScript PNG encoding
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

// Helper: CRC32 calculation for PNG chunks
function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc = (crc >>> 8) ^ ((crc ^ buf[i]) & 0xff);
    for (let j = 0; j < 8; j++) {
      crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

// Create a simple PNG with solid color background and basic shapes
function createSimplePNG(width, height, bgR, bgG, bgB) {
  // Create RGBA scanlines (all pixels same color for simplicity)
  const pixelData = Buffer.alloc(width * height * 4);
  
  // Fill with background color
  for (let i = 0; i < pixelData.length; i += 4) {
    pixelData[i] = bgR;     // R
    pixelData[i + 1] = bgG; // G
    pixelData[i + 2] = bgB; // B
    pixelData[i + 3] = 255; // A (opaque)
  }

  // Simple pattern: draw a red rectangle (dumbbell approximation)
  const rectX1 = Math.floor(width * 0.2);
  const rectX2 = Math.floor(width * 0.8);
  const rectY1 = Math.floor(height * 0.35);
  const rectY2 = Math.floor(height * 0.65);

  for (let y = rectY1; y < rectY2; y++) {
    for (let x = rectX1; x < rectX2; x++) {
      const idx = (y * width + x) * 4;
      pixelData[idx] = 239;     // R - red (#ef4444)
      pixelData[idx + 1] = 68;  // G
      pixelData[idx + 2] = 68;  // B
      pixelData[idx + 3] = 255; // A
    }
  }

  // PNG signature
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // IHDR chunk
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;   // bit depth
  ihdr[9] = 6;   // color type (RGBA)
  ihdr[10] = 0;  // compression
  ihdr[11] = 0;  // filter
  ihdr[12] = 0;  // interlace

  const ihdrChunk = Buffer.concat([
    Buffer.from('IHDR'),
    ihdr
  ]);
  const ihdrCrc = Buffer.alloc(4);
  ihdrCrc.writeUInt32BE(crc32(ihdrChunk), 0);

  // IDAT chunk (image data)
  const filterData = Buffer.alloc(height * (1 + width * 4));
  for (let y = 0; y < height; y++) {
    filterData[y * (1 + width * 4)] = 0; // filter type: none
    for (let x = 0; x < width; x++) {
      const srcIdx = (y * width + x) * 4;
      const dstIdx = y * (1 + width * 4) + 1 + x * 4;
      pixelData.copy(filterData, dstIdx, srcIdx, srcIdx + 4);
    }
  }

  // Compress with zlib
  const compressedSync = zlib.deflateSync(filterData);

  const idatChunk = Buffer.concat([
    Buffer.from('IDAT'),
    compressedSync
  ]);
  const idatCrc = Buffer.alloc(4);
  idatCrc.writeUInt32BE(crc32(idatChunk), 0);

  // IEND chunk
  const iendChunk = Buffer.from('IEND');
  const iendCrc = Buffer.alloc(4);
  iendCrc.writeUInt32BE(crc32(iendChunk), 0);

  // Build final PNG
  const lengths = {
    IHDR: Buffer.alloc(4),
    IDAT: Buffer.alloc(4),
    IEND: Buffer.alloc(4)
  };
  lengths.IHDR.writeUInt32BE(13, 0);
  lengths.IDAT.writeUInt32BE(compressedSync.length, 0);
  lengths.IEND.writeUInt32BE(0, 0);

  return Buffer.concat([
    signature,
    lengths.IHDR, ihdrChunk, ihdrCrc,
    lengths.IDAT, idatChunk, idatCrc,
    lengths.IEND, iendChunk, iendCrc
  ]);
}

// Generate icons
const dir = path.dirname(__filename);
const sizes = [96, 192, 512];

sizes.forEach(size => {
  const png = createSimplePNG(size, size, 15, 23, 42); // Dark background #0f172a
  const filepath = path.join(dir, `icon-${size}.png`);
  fs.writeFileSync(filepath, png);
  console.log(`✓ Generated ${filepath}`);

  // Also create maskable variant
  const maskablePath = path.join(dir, `icon-${size}-maskable.png`);
  fs.writeFileSync(maskablePath, png);
  console.log(`✓ Generated ${maskablePath}`);
});

console.log('\n✓ All icons generated successfully!');
