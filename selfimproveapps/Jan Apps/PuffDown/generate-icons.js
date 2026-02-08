const sharp = require('sharp');

const sizes = [
  { size: 1024, name: 'icon-1024.png' },
  { size: 512, name: 'icon-512.png' },
  { size: 192, name: 'icon-192.png' },
  { size: 180, name: 'apple-touch-icon.png' }
];

function makeSvg(size) {
  const s = size / 100;
  const cx = size / 2;
  const cy = size / 2 + 2 * s;
  const sw = 3 * s;

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
  <rect width="${size}" height="${size}" fill="#0A0A0A"/>
  <!-- Trachea -->
  <line x1="${cx}" y1="${cy - 28*s}" x2="${cx}" y2="${cy - 10*s}"
    stroke="#2DD4BF" stroke-width="${sw}" stroke-linecap="round"/>
  <!-- Left lung -->
  <path d="M ${cx},${cy - 10*s}
    C ${cx - 10*s},${cy - 10*s} ${cx - 28*s},${cy - 15*s} ${cx - 28*s},${cy + 5*s}
    C ${cx - 28*s},${cy + 25*s} ${cx - 10*s},${cy + 28*s} ${cx - 5*s},${cy + 10*s}"
    fill="none" stroke="#2DD4BF" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round"/>
  <!-- Right lung -->
  <path d="M ${cx},${cy - 10*s}
    C ${cx + 10*s},${cy - 10*s} ${cx + 28*s},${cy - 15*s} ${cx + 28*s},${cy + 5*s}
    C ${cx + 28*s},${cy + 25*s} ${cx + 10*s},${cy + 28*s} ${cx + 5*s},${cy + 10*s}"
    fill="none" stroke="#2DD4BF" stroke-width="${sw}" stroke-linecap="round" stroke-linejoin="round"/>
  <!-- Cloud puffs -->
  <circle cx="${cx - 4*s}" cy="${cy - 34*s}" r="${3*s}" fill="#2DD4BF" opacity="0.4"/>
  <circle cx="${cx + 3*s}" cy="${cy - 38*s}" r="${2.5*s}" fill="#2DD4BF" opacity="0.4"/>
  <circle cx="${cx - 1*s}" cy="${cy - 43*s}" r="${2*s}" fill="#2DD4BF" opacity="0.4"/>
</svg>`;
}

async function generate() {
  for (const { size, name } of sizes) {
    const svg = makeSvg(size);
    await sharp(Buffer.from(svg))
      .png()
      .toFile(name);
    console.log(`Created ${name} (${size}x${size})`);
  }
  console.log('All icons generated!');
}

generate().catch(console.error);
