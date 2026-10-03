const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');
const RELEASES_DIR = path.join(__dirname, 'releases');
const APK_FILENAME = 'DYNIMETIZE_ZX-v2.0.0-release.apk';
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

  // APK Download Endpoints
  if (
    pathname === `/${APK_FILENAME}` ||
    pathname === '/download' ||
    pathname === '/app-release.apk' ||
    pathname.endsWith('.apk')
  ) {
    if (!fs.existsSync(APK_PATH)) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('APK File not found on server.');
      return;
    }

    const stat = fs.statSync(APK_PATH);
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

      const fileStream = fs.createReadStream(APK_PATH, { start, end });
      fileStream.on('error', (e) => console.error('Stream error:', e.message));
      fileStream.pipe(res);
    } else {
      res.writeHead(200, {
        'Content-Length': fileSize,
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': `attachment; filename="${APK_FILENAME}"`,
        'Accept-Ranges': 'bytes',
        'Cache-Control': 'public, max-age=3600',
      });

      if (req.method === 'HEAD') {
        res.end();
        return;
      }

      const fileStream = fs.createReadStream(APK_PATH);
      fileStream.on('error', (e) => console.error('Stream error:', e.message));
      fileStream.pipe(res);
    }
    return;
  }

  // API Status Endpoint
  if (pathname === '/api/status') {
    let apkExists = fs.existsSync(APK_PATH);
    let size = apkExists ? fs.statSync(APK_PATH).size : 0;
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(
      JSON.stringify({
        status: 'APK RELEASE READY',
        filename: APK_FILENAME,
        exists: apkExists,
        size: size,
        version: '2.0.0',
        versionCode: 200,
        applicationId: 'com.aistudio.zxdashboard.zxapp',
        sha256: 'a9dd5a568caa6c66953d0f94d9dbf24b3a18bf408153103f84b0b648ac19cf17',
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
});
