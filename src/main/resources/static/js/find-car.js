/**
 * AI Car Rental System Find Car Page Controller
 * Handles AI natural language query parsing and traditional filter fallbacks.
 */
document.addEventListener('DOMContentLoaded', () => {
    initFindCarPage();
    initMobileNav();
});

function initFindCarPage() {
    const aiForm = document.getElementById('ai-search-form');
    const aiInput = document.getElementById('ai-query-input');
    const aiBtn = document.getElementById('ai-search-btn');
    const aiClearBtn = document.getElementById('ai-clear-btn');
    const suggestionChips = document.querySelectorAll('.suggestion-chip');

    // Initial load of all cars
    loadAllCars();

    // AI Form Submit
    if (aiForm) {
        aiForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const query = aiInput.value.trim();
            if (!query) return;
            await executeAiSearch(query, aiBtn);
            if (aiClearBtn) aiClearBtn.style.display = 'inline-flex';
        });
    }

    // Suggestion Chips
    suggestionChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const promptText = chip.getAttribute('data-prompt') || chip.textContent.trim().replace(/^"|"$/g, '');
            aiInput.value = promptText;
            executeAiSearch(promptText, aiBtn);
            if (aiClearBtn) aiClearBtn.style.display = 'inline-flex';
        });
    });

    // Reset / Show All Vehicles
    if (aiClearBtn) {
        aiClearBtn.addEventListener('click', () => {
            if (aiInput) aiInput.value = '';
            aiClearBtn.style.display = 'none';
            hideAiCriteria();
            loadAllCars();
        });
    }
}

async function loadAllCars() {
    const resultsContainer = document.getElementById('search-results-grid');
    const resultCount = document.getElementById('results-count');
    if (!resultsContainer) return;

    try {
        const cars = await Api.getCars();
        renderCarsList(cars, resultsContainer, resultCount);
    } catch (err) {
        console.error('Error fetching cars:', err);
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3 class="empty-state-title">Unable to search cars</h3>
                <p class="empty-state-desc">Unable to connect to the fleet vehicle service. Please check your network or try again later.</p>
            </div>
        `;
    }
}

async function executeAiSearch(query, btn) {
    const resultsContainer = document.getElementById('search-results-grid');
    const resultCount = document.getElementById('results-count');
    const originalBtnHtml = btn ? btn.innerHTML : '';

    try {
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = `<span class="loading-spinner"></span> Analyzing with AI...`;
        }

        resultsContainer.innerHTML = `
            <div class="empty-state">
                <div class="loading-spinner" style="width: 36px; height: 36px; border-width: 3px; margin-bottom: 1rem;"></div>
                <h3 class="empty-state-title">Gemini AI is processing your request...</h3>
                <p class="empty-state-desc">Extracting location, car type, transmission, and budget constraints.</p>
            </div>
        `;

        const response = await Api.aiSearch(query);
        const { criteria, cars } = response;

        // Display the structured criteria extracted by AI
        displayAiCriteria(criteria);

        // Render matching cars
        renderCarsList(cars, resultsContainer, resultCount, true);

    } catch (err) {
        console.error('AI search failed:', err);
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3 class="empty-state-title">AI Search Error</h3>
                <p class="empty-state-desc">${escapeHtml(err.message || 'Unable to process AI search at this moment.')}</p>
            </div>
        `;
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = originalBtnHtml;
        }
    }
}

async function executeFilterSearch(form) {
    const resultsContainer = document.getElementById('search-results-grid');
    const resultCount = document.getElementById('results-count');

    hideAiCriteria();

    const formData = new FormData(form);
    const filters = {
        location: formData.get('location'),
        carType: formData.get('carType'),
        fuelType: formData.get('fuelType'),
        transmission: formData.get('transmission'),
        maxPrice: formData.get('maxPrice'),
        minSeats: formData.get('minSeats')
    };

    try {
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <div class="loading-spinner" style="width: 32px; height: 32px; margin-bottom: 1rem;"></div>
                <h3 class="empty-state-title">Filtering inventory...</h3>
            </div>
        `;

        const cars = await Api.searchCars(filters);
        renderCarsList(cars, resultsContainer, resultCount, false);
    } catch (err) {
        console.error('Search failed:', err);
        resultsContainer.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3 class="empty-state-title">Search Failed</h3>
                <p class="empty-state-desc">${escapeHtml(err.message)}</p>
            </div>
        `;
    }
}

function displayAiCriteria(criteria) {
    const box = document.getElementById('ai-criteria-card');
    const chipsContainer = document.getElementById('ai-criteria-chips');
    if (!box || !chipsContainer || !criteria) return;

    const items = [];
    if (criteria.location) items.push(`<strong>Location:</strong> ${escapeHtml(criteria.location)}`);
    if (criteria.carType) items.push(`<strong>Type:</strong> ${escapeHtml(criteria.carType)}`);
    if (criteria.fuelType) items.push(`<strong>Fuel:</strong> ${escapeHtml(criteria.fuelType)}`);
    if (criteria.transmission) items.push(`<strong>Transmission:</strong> ${escapeHtml(criteria.transmission)}`);
    if (criteria.maxPricePerDay) items.push(`<strong>Max Price:</strong> ₹${criteria.maxPricePerDay.toLocaleString('en-IN')}/day`);
    if (criteria.minSeats) items.push(`<strong>Min Seats:</strong> ${criteria.minSeats}`);
    if (criteria.preferredBrand) items.push(`<strong>Brand:</strong> ${escapeHtml(criteria.preferredBrand)}`);

    if (items.length === 0) {
        items.push(`<span>Broad search (no strict filters extracted)</span>`);
    }

    chipsContainer.innerHTML = items.map(it => `<span class="criteria-item">${it}</span>`).join('');
    box.style.display = 'block';
}

function hideAiCriteria() {
    const box = document.getElementById('ai-criteria-card');
    if (box) box.style.display = 'none';
}

function renderCarsList(cars, container, countEl, isAi = false) {
    if (countEl) {
        countEl.textContent = `${cars.length} ${cars.length === 1 ? 'vehicle' : 'vehicles'} found`;
    }

    if (!cars || cars.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🔍</div>
                <h3 class="empty-state-title">No matching cars found</h3>
                <p class="empty-state-desc">Try loosening your criteria or search a different city.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = cars.map(createCarCardHtml).join('');
}

function initMobileNav() {
    const toggle = document.querySelector('.menu-toggle');
    const links = document.querySelector('.nav-links');
    if (toggle && links) {
        toggle.addEventListener('click', () => {
            links.classList.toggle('open');
        });
    }
}
