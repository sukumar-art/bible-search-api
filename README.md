# Seek & Find Scripture — Spring Boot Backend

## How to Deploy on Railway (No command line needed)

### STEP 1 — Create GitHub Repository
1. Go to github.com and sign in
2. Click the + button (top right) → New repository
3. Name: bible-search-api
4. Set to Public
5. Do NOT check any other options
6. Click Create repository

### STEP 2 — Upload These Files to GitHub
1. On your new repo page, click "uploading an existing file"
2. Extract this ZIP on your computer
3. Open the extracted folder
4. Select ALL files inside (Ctrl+A) — you should see:
   - Dockerfile
   - railway.toml
   - pom.xml
   - .gitignore
   - README.md
   - src/ (folder)
5. Drag them all into the GitHub browser upload area
6. Click "Commit changes"

### STEP 3 — Deploy on Railway
1. Go to railway.app
2. Click Login with GitHub
3. Click New Project
4. Click Deploy from GitHub repo
5. Select bible-search-api
6. Railway detects the Dockerfile automatically
7. Click Deploy Now

### STEP 4 — Add API Key
1. Click your service in Railway
2. Click Variables tab
3. Add: ANTHROPIC_API_KEY = sk-ant-your-key-here
4. Click Add (Railway auto-restarts)

### STEP 5 — Get Your Public URL
1. Click Settings tab
2. Scroll to Networking
3. Click Generate Domain
4. Copy the URL

### STEP 6 — Test
Open in browser: https://YOUR-URL/api/health
You should see: {"status":"UP","service":"Seek & Find Scripture API"}

### STEP 7 — Update Vercel Frontend
1. Go to vercel.com → your project
2. Settings → Environment Variables
3. Add: VITE_API_URL = https://YOUR-RAILWAY-URL
4. Save → Redeploy
