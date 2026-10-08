const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');
const crypto = require('crypto');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const RELEASES_DIR = path.join(__dirname, 'releases');
const APK_FILENAME = 'DYNIMETIZE_ZX-v2.2.0-release.apk';
const APK_PATH = path.join(RELEASES_DIR, APK_FILENAME);

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

function getActiveApkPath() {
  if (fs.existsSync(APK_PATH)) return APK_PATH;
  const fallbackNamed = path.join(RELEASES_DIR, 'app-release.apk');
  if (fs.existsSync(fallbackNamed)) return fallbackNamed;
  const pubNamed = path.join(PUBLIC_DIR, APK_FILENAME);
  if (fs.existsSync(pubNamed)) return pubNamed;
  const pubFallback = path.join(PUBLIC_DIR, 'app-release.apk');
  if (fs.existsSync(pubFallback)) return pubFallback;
  return APK_PATH;
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

  // APK Download Endpoints (Supporting latest named APK, /download, /app-release.apk)
  if (
    pathname === `/${APK_FILENAME}` ||
    pathname === '/download' ||
    pathname === '/download-latest' ||
    pathname === '/app-release.apk' ||
    pathname.endsWith('.apk')
  ) {
    const activeApk = getActiveApkPath();
    if (!fs.existsSync(activeApk)) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK File not found on server. Please run build_apks.sh to generate.');
      return;
    }

    const stat = fs.statSync(activeApk);
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
        'Content-Disposition': `attachment; filename="${APK_FILENAME}"`,
      });

      const fileStream = fs.createReadStream(activeApk, { start, end });
      fileStream.on('error', (e) => console.error('Stream error:', e.message));
      fileStream.pipe(res);
    } else {
      res.writeHead(200, {
        'Content-Length': fileSize,
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': `attachment; filename="${APK_FILENAME}"`,
        'Accept-Ranges': 'bytes',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
      });

      if (req.method === 'HEAD') {
        res.end();
        return;
      }

      const fileStream = fs.createReadStream(activeApk);
      fileStream.on('error', (e) => console.error('Stream error:', e.message));
      fileStream.pipe(res);
    }
    return;
  }

  // API Status Endpoint
  if (pathname === '/api/status') {
    const activeApk = getActiveApkPath();
    const apkExists = fs.existsSync(activeApk);
    const size = apkExists ? fs.statSync(activeApk).size : 0;
    const sha256 = apkExists ? calculateSha256(activeApk) : '';
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(
      JSON.stringify({
        status: 'APK RELEASE READY',
        filename: APK_FILENAME,
        exists: apkExists,
        size: size,
        version: '2.2.0',
        versionCode: 220,
        applicationId: 'com.aistudio.zxdashboard.zxapp',
        sha256: sha256,
        downloadUrl: `/${APK_FILENAME}`,
        githubReleaseUrl: 'https://github.com/zuccheroxann/zx-dashboard/releases/latest',
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
  console.log(`[DYNIMETIZE ZX SERVER] Running on http://0.0.0.0:${PORT} - Serving ${APK_FILENAME}`);
});
