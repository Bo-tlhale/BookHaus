import { spawn } from 'node:child_process'
import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import path from 'node:path'

const chromePath = process.env.CHROME_PATH || 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe'
const email = process.env.BOOKHAUS_SCREENSHOT_EMAIL
const password = process.env.BOOKHAUS_SCREENSHOT_PASSWORD
const baseUrl = process.env.BOOKHAUS_FRONTEND_URL || 'http://localhost:5173'
const port = 9223
const profileDir = await mkdtemp(path.join(tmpdir(), 'bookhaus-screenshots-'))
const outputDir = path.resolve('docs', 'screenshots')

if (!email || !password) {
  throw new Error('Set BOOKHAUS_SCREENSHOT_EMAIL and BOOKHAUS_SCREENSHOT_PASSWORD before running this script.')
}

await mkdir(outputDir, { recursive: true })
const chrome = spawn(chromePath, [
  '--headless=new',
  '--disable-gpu',
  '--hide-scrollbars',
  '--no-first-run',
  '--no-default-browser-check',
  `--remote-debugging-port=${port}`,
  `--user-data-dir=${profileDir}`,
  '--window-size=1440,1100',
  'about:blank',
], { stdio: 'ignore' })

const delay = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds))

async function retry(operation, attempts = 40) {
  let lastError
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    try { return await operation() } catch (error) { lastError = error; await delay(250) }
  }
  throw lastError
}

let socket
try {
  await retry(async () => {
    const response = await fetch(`http://127.0.0.1:${port}/json/version`)
    if (!response.ok) throw new Error(`Chrome returned ${response.status}`)
    return response.json()
  })
  const targetResponse = await fetch(`http://127.0.0.1:${port}/json/new?${encodeURIComponent(`${baseUrl}/login`)}`, { method: 'PUT' })
  const target = await targetResponse.json()

  socket = new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => {
    socket.addEventListener('open', resolve, { once: true })
    socket.addEventListener('error', reject, { once: true })
  })

  let commandId = 0
  const pending = new Map()
  socket.addEventListener('message', ({ data }) => {
    const message = JSON.parse(data)
    if (!message.id || !pending.has(message.id)) return
    const { resolve, reject } = pending.get(message.id)
    pending.delete(message.id)
    if (message.error) reject(new Error(message.error.message))
    else resolve(message.result)
  })

  function send(method, params = {}) {
    commandId += 1
    return new Promise((resolve, reject) => {
      pending.set(commandId, { resolve, reject })
      socket.send(JSON.stringify({ id: commandId, method, params }))
    })
  }

  await send('Page.enable')
  await send('Runtime.enable')
  await send('Emulation.setDeviceMetricsOverride', {
    width: 1440,
    height: 1100,
    deviceScaleFactor: 1,
    mobile: false,
  })

  async function navigate(route, settle = 1800) {
    await send('Page.navigate', { url: `${baseUrl}${route}` })
    const targetUrl = new URL(route, baseUrl)
    await retry(async () => {
      const result = await send('Runtime.evaluate', {
        expression: `document.readyState === 'complete' && location.origin === ${JSON.stringify(baseUrl)} && location.pathname === ${JSON.stringify(targetUrl.pathname)} && location.search === ${JSON.stringify(targetUrl.search)}`,
        returnByValue: true,
      })
      if (!result.result.value) throw new Error('Page is still loading')
    }, 60)
    await delay(settle)
  }

  await navigate('/login', 500)
  const loginExpression = `(async () => {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(${JSON.stringify({ email, password })})
    });
    if (!response.ok) throw new Error('Screenshot login failed: ' + response.status + ' ' + await response.text());
    const body = await response.json();
    sessionStorage.setItem('bookhaus_token', body.token);
    return true;
  })()`
  const login = await send('Runtime.evaluate', { expression: loginExpression, awaitPromise: true, returnByValue: true })
  if (login.exceptionDetails) throw new Error(`Unable to authenticate screenshot session: ${login.exceptionDetails.exception?.description || login.exceptionDetails.text}`)

  const captures = [
    ['/', 'home-dashboard.png'],
    ['/library', 'personal-library.png'],
    ['/search?q=Dune', 'book-discovery.png', true],
    ['/library/4', 'book-detail-journal.png'],
    ['/shelves', 'custom-shelves.png'],
    ['/journal', 'reading-journal.png'],
    ['/goals', 'reading-goals.png'],
    ['/stats', 'reading-insights.png'],
    ['/customize', 'customize-reading-room.png'],
  ]
  const requestedCapture = process.env.BOOKHAUS_CAPTURE_ONLY
  const selectedCaptures = requestedCapture
    ? captures.filter(([, filename]) => filename === requestedCapture)
    : captures

  for (const [route, filename, runSearch] of selectedCaptures) {
    await navigate(route, runSearch ? 800 : 2200)
    if (runSearch) {
      await delay(750)
      await retry(async () => {
        const result = await send('Runtime.evaluate', {
          expression: `document.querySelectorAll('.search-result-card').length > 0`,
          returnByValue: true,
        })
        if (!result.result.value) throw new Error('Book search is still loading')
      }, 120)
      await delay(1200)
    }
    await send('Runtime.evaluate', { expression: 'window.scrollTo(0, 0)' })
    const screenshot = await send('Page.captureScreenshot', { format: 'png', fromSurface: true })
    await writeFile(path.join(outputDir, filename), Buffer.from(screenshot.data, 'base64'))
  }
} finally {
  if (socket?.readyState === WebSocket.OPEN) socket.close()
  chrome.kill()
  await delay(1000)
  await rm(profileDir, { recursive: true, force: true }).catch(() => undefined)
}

console.log(`Captured BookHaus screenshots in ${outputDir}`)
