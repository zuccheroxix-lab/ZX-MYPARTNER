const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const crypto = require('crypto');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const RELEASES_DIR = path.join(__dirname, 'releases');

const RELEASE_APK_FILENAME = 'DYNIMETIZE_ZX-v2.2.0-release.apk';
const DEBUG_APK_FILENAME = 'DYNIMETIZE_ZX-v2.2.0-debug.apk';

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.css': 'text/css',
  '.js': 'application/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.apk': 'application/vnd.android.package-archive',
};

// Global error handlers so server never dies
process.on('uncaughtException', (err) => {
  console.error('[UNCAUGHT_EXCEPTION]', err.message);
});
process.on('unhandledRejection', (reason) => {
  console.error('[UNHANDLED_REJECTION]', reason);
});

function getReleaseApkPath() {
  const candidates = [
    path.join(RELEASES_DIR, RELEASE_APK_FILENAME),
    path.join(RELEASES_DIR, 'app-release.apk'),
    path.join(PUBLIC_DIR, RELEASE_APK_FILENAME),
    path.join(PUBLIC_DIR, 'app-release.apk'),
    path.join(__dirname, 'app/build/outputs/apk/release/app-release.apk'),
  ];
  for (const c of candidates) {
    if (fs.existsSync(c)) return c;
  }
  return candidates[0];
}

function getDebugApkPath() {
  const candidates = [
    path.join(RELEASES_DIR, DEBUG_APK_FILENAME),
    path.join(RELEASES_DIR, 'app-debug.apk'),
    path.join(PUBLIC_DIR, DEBUG_APK_FILENAME),
    path.join(PUBLIC_DIR, 'app-debug.apk'),
    path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk'),
  ];
  for (const c of candidates) {
    if (fs.existsSync(c)) return c;
  }
  return candidates[0];
}

function calculateSha256(filePath) {
  try {
    if (!fs.existsSync(filePath)) return '';
    const fileBuffer = fs.readFileSync(filePath);
    return crypto.createHash('sha256').update(fileBuffer).digest('hex');
  } catch (e) {
    return '';
  }
}

function serveApkFile(req, res, filePath, downloadFilename) {
  if (!fs.existsSync(filePath)) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end(`APK File (${downloadFilename}) not found. Please run build_apks.sh to generate.`);
    return;
  }

  const stat = fs.statSync(filePath);
  const fileSize = stat.size;
  const range = req.headers.range;

  if (range) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
    const chunksize = end - start + 1;

    res.writeHead(206, {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': `attachment; filename="${downloadFilename}"`,
    });

    const fileStream = fs.createReadStream(filePath, { start, end });
    fileStream.on('error', (e) => console.error('Stream error:', e.message));
    fileStream.pipe(res);
  } else {
    res.writeHead(200, {
      'Content-Length': fileSize,
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': `attachment; filename="${downloadFilename}"`,
      'Accept-Ranges': 'bytes',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
    });

    if (req.method === 'HEAD') {
      res.end();
      return;
    }

    const fileStream = fs.createReadStream(filePath);
    fileStream.on('error', (e) => console.error('Stream error:', e.message));
    fileStream.pipe(res);
  }
}

const server = http.createServer((req, res) => {
  // CORS & Safety Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', '*');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  let pathname = decodeURIComponent(parsedUrl.pathname);

  // APK Debug Download Endpoints
  if (
    pathname === `/${DEBUG_APK_FILENAME}` ||
    pathname === '/app-debug.apk' ||
    pathname === '/download-debug' ||
    pathname === '/debug'
  ) {
    const debugApk = getDebugApkPath();
    serveApkFile(req, res, debugApk, DEBUG_APK_FILENAME);
    return;
  }

  // APK Release Download Endpoints
  if (
    pathname === `/${RELEASE_APK_FILENAME}` ||
    pathname === '/download' ||
    pathname === '/download-release' ||
    pathname === '/download-latest' ||
    pathname === '/app-release.apk' ||
    pathname === '/release'
  ) {
    const releaseApk = getReleaseApkPath();
    serveApkFile(req, res, releaseApk, RELEASE_APK_FILENAME);
    return;
  }

  // API Status & Overview Endpoint
  if (pathname === '/api/status' || pathname === '/api/overview') {
    const releaseApk = getReleaseApkPath();
    const releaseExists = fs.existsSync(releaseApk);
    const releaseSize = releaseExists ? fs.statSync(releaseApk).size : 0;
    const releaseSha256 = releaseExists ? calculateSha256(releaseApk) : '';

    const debugApk = getDebugApkPath();
    const debugExists = fs.existsSync(debugApk);
    const debugSize = debugExists ? fs.statSync(debugApk).size : 0;
    const debugSha256 = debugExists ? calculateSha256(debugApk) : '';

    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(
      JSON.stringify({
        appName: 'DYNIMETIZE ZX',
        applicationId: 'com.aistudio.zxdashboard.zxapp',
        version: '2.2.0',
        versionCode: 220,
        developer: 'ZUCCHERO XANN',
        developerId: 'ZX-DEV-001',
        githubRepo: 'https://github.com/zuccheroxix-lab/ZX-MYPARTNER',
        githubReleaseUrl: 'https://github.com/zuccheroxix-lab/ZX-MYPARTNER/releases/latest',
        releaseApk: {
          filename: RELEASE_APK_FILENAME,
          exists: releaseExists,
          size: releaseSize,
          sha256: releaseSha256,
          downloadUrl: `/${RELEASE_APK_FILENAME}`,
          altDownloadUrl: '/app-release.apk',
        },
        debugApk: {
          filename: DEBUG_APK_FILENAME,
          exists: debugExists,
          size: debugSize,
          sha256: debugSha256,
          downloadUrl: `/${DEBUG_APK_FILENAME}`,
          altDownloadUrl: '/app-debug.apk',
        },
      })
    );
    return;
  }

  // Static files in public/
  if (pathname === '/' || pathname === '') {
    pathname = '/index.html';
  }

  const safePath = path.normalize(path.join(PUBLIC_DIR, pathname));
  if (!safePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  if (fs.existsSync(safePath) && fs.statSync(safePath).isFile()) {
    const ext = path.extname(safePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(safePath).pipe(res);
  } else {
    // Fallback to index.html
    const indexPath = path.join(PUBLIC_DIR, 'index.html');
    if (fs.existsSync(indexPath)) {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
      fs.createReadStream(indexPath).pipe(res);
    } else {
      res.writeHead(404);
      res.end('Not Found');
    }
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[DYNIMETIZE ZX SERVER] Running on http://0.0.0.0:${PORT}`);
  console.log(`  - Release APK: ${RELEASE_APK_FILENAME}`);
  console.log(`  - Debug APK:   ${DEBUG_APK_FILENAME}`);
});
