let selectedId = null;

async function loadDocuments() {
    const box = document.getElementById("documents");
    box.innerHTML = "Loading...";

    const response = await fetch("/api/documents");
    const docs = await response.json();

    if (!docs.length) {
        box.innerHTML = "<p>No documents uploaded yet.</p>";
        return;
    }

    box.innerHTML = docs.map(d => `
        <div class="document">
            <input type="radio" name="target" value="${d.id}"
                   onchange="selectedId=this.value">
            <div class="document-name">
                <strong>${escapeHtml(d.fileName)}</strong>
                <div class="small">${d.fileType} · ${formatBytes(d.size)}</div>
            </div>
            <button class="delete" onclick="deleteDocument('${d.id}')">Delete</button>
        </div>
    `).join("");
}

async function upload() {
    const input = document.getElementById("fileInput");
    const message = document.getElementById("uploadMessage");

    if (!input.files.length) {
        message.innerHTML = '<div class="error">Choose a PDF or DOCX file.</div>';
        return;
    }

    const form = new FormData();
    form.append("file", input.files[0]);

    const response = await fetch("/api/documents/upload", {
        method: "POST",
        body: form
    });

    const data = await response.json();

    if (!response.ok) {
        message.innerHTML = `<div class="error">${escapeHtml(data.error || "Upload failed")}</div>`;
        return;
    }

    message.innerHTML = '<div class="success">Document uploaded successfully.</div>';
    input.value = "";
    await loadDocuments();
}

async function deleteDocument(id) {
    await fetch("/api/documents/" + id, { method: "DELETE" });
    if (selectedId === id) selectedId = null;
    await loadDocuments();
}

async function analyzeSelected() {
    const results = document.getElementById("results");
    const loading = document.getElementById("loading");

    if (!selectedId) {
        results.innerHTML = '<div class="error">Select a target document first.</div>';
        return;
    }

    loading.innerHTML = "Analyzing...";
    results.innerHTML = "";

    const response = await fetch("/api/analyze/" + selectedId, { method: "POST" });
    const data = await response.json();
    loading.innerHTML = "";

    if (!response.ok) {
        results.innerHTML = `<div class="error">${escapeHtml(data.error || "Analysis failed")}</div>`;
        return;
    }

    if (!data.results.length) {
        results.innerHTML = "<p>There are not enough documents to compare. Upload at least two.</p>";
        return;
    }

    let html = `
        <h3>Target: ${escapeHtml(data.targetDocument)}</h3>
        <table>
        <thead>
        <tr>
            <th>Source Document</th>
            <th>Overall</th>
            <th>Cosine</th>
            <th>Exact</th>
            <th>Partial</th>
            <th>Unique</th>
            <th>Class</th>
        </tr>
        </thead><tbody>
    `;

    for (const r of data.results) {
        html += `
        <tr>
            <td><strong>${escapeHtml(r.sourceDocument)}</strong></td>
            <td>${overall(r).toFixed(2)}%</td>
            <td>${r.cosineSimilarity.toFixed(2)}%</td>
            <td>${r.exactMatchPercentage.toFixed(2)}%</td>
            <td>${r.partialMatchPercentage.toFixed(2)}%</td>
            <td>${r.uniqueContentPercentage.toFixed(2)}%</td>
            <td><span class="badge">${escapeHtml(r.classification || classify(r))}</span></td>
        </tr>
        <tr><td colspan="7">${renderMatches(r)}</td></tr>`;
    }

    html += `</tbody></table>`;

    if (data.reportFile) {
        html += `<p><a href="/api/reports/${encodeURIComponent(data.reportFile)}">
                    <button>Download PDF Report</button></a></p>`;
    }

    results.innerHTML = html;
}

function overall(r) {
    return Math.min(100, .70*r.cosineSimilarity + .20*r.exactMatchPercentage + .10*r.partialMatchPercentage);
}

function classify(r) {
    const x = overall(r);
    return x >= 70 ? "HIGH" : x >= 40 ? "MEDIUM" : "LOW";
}

function renderMatches(r) {
    let html = "";

    if (r.exactMatches && r.exactMatches.length) {
        html += "<strong>Exact matches</strong>";
        r.exactMatches.forEach(m => {
            html += `<div class="match">
                <strong>Rabin-Karp:</strong> ${escapeHtml(m.text)}
                <div class="small">Target tokens ${m.targetTokenStart}-${m.targetTokenEnd}</div>
            </div>`;
        });
    }

    if (r.partialMatches && r.partialMatches.length) {
        html += "<strong>Partial matches</strong>";
        r.partialMatches.forEach(m => {
            html += `<div class="match">
                <strong>LCS:</strong> ${escapeHtml(m.text)}
                <div class="small">Common token count: ${m.length}</div>
            </div>`;
        });
    }

    return html || "<span class='small'>No passage-level match reported.</span>";
}

function formatBytes(bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024*1024) return (bytes/1024).toFixed(1) + " KB";
    return (bytes/(1024*1024)).toFixed(1) + " MB";
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, c => ({
        "&":"&amp;", "<":"&lt;", ">":"&gt;", '"':"&quot;", "'":"&#039;"
    }[c]));
}

loadDocuments();
