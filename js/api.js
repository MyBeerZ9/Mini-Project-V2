/**
 * AI Car Rental System API Client
 * Centralized HTTP service for interacting with the Spring Boot REST backend.
 *
 * Supports:
 *  1. Local frontend dev servers (Live Server :5500, Vite/Node :3000) -> http://localhost:8080/api
 *  2. Cloudflare Pages or remote frontend hosting -> Railway backend domain
 *  3. Same-origin Spring Boot deployment -> /api
 *  4. Manual override via window.CARVIA_API_BASE or localStorage.getItem('CARVIA_API_URL')
 */
const DEFAULT_RAILWAY_BACKEND_URL = 'https://carvia-v2-mini-project-g2.up.railway.app';

function normalizeApiBase(rawUrl) {
    const trimmed = rawUrl.trim().replace(/\/+$/, '');
    return trimmed.endsWith('/api') ? trimmed : `${trimmed}/api`;
}

function resolveApiBase() {
    // 1. Explicit window or localStorage override
    if (typeof window !== 'undefined' && window.CARVIA_API_BASE) {
        return normalizeApiBase(window.CARVIA_API_BASE);
    }
    if (typeof localStorage !== 'undefined') {
        const stored = localStorage.getItem('CARVIA_API_URL');
        if (stored) return normalizeApiBase(stored);
    }

    if (typeof window === 'undefined') return '/api';

    const hostname = window.location.hostname;
    const port = window.location.port;

    // 2. Local frontend development on a separate port (e.g. 5500, 3000)
    if ((hostname === 'localhost' || hostname === '127.0.0.1') && port !== '8080' && port !== '') {
        return 'http://localhost:8080/api';
    }

    // 3. Cloudflare Pages or external remote frontend
    const isCloudflare = hostname.endsWith('.pages.dev');
    const isExternalFrontend = !['localhost', '127.0.0.1'].includes(hostname) && !hostname.endsWith('.railway.app');

    if (isCloudflare || isExternalFrontend) {
        return normalizeApiBase(DEFAULT_RAILWAY_BACKEND_URL);
    }

    // 4. Same-origin (Spring Boot hosting static files or matching domain)
    return '/api';
}

const API_BASE = resolveApiBase();

const Api = {
    /**
     * Get all active car listings
     */
    async getCars() {
        const res = await fetch(`${API_BASE}/cars`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Get top featured cars for homepage
     */
    async getFeaturedCars() {
        const res = await fetch(`${API_BASE}/cars/featured`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Get car details by ID
     */
    async getCarById(id) {
        const res = await fetch(`${API_BASE}/cars/${id}`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Create a new car listing
     */
    async createCar(carData) {
        const res = await fetch(`${API_BASE}/cars`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(carData)
        });
        if (!res.ok) {
            const errData = await res.json().catch(() => null);
            if (errData && errData.fieldErrors) {
                const firstMsg = Object.values(errData.fieldErrors)[0];
                throw new Error(firstMsg);
            }
            throw new Error((errData && errData.message) || 'Failed to list car');
        }
        return res.json();
    },

    /**
     * Search cars with traditional query filters
     */
    async searchCars(filters) {
        const params = new URLSearchParams();
        for (const [key, val] of Object.entries(filters)) {
            if (val !== null && val !== undefined && val !== '') {
                params.append(key, val);
            }
        }
        const res = await fetch(`${API_BASE}/cars/search?${params.toString()}`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * AI-based natural language car search powered by Gemini & Spring Boot
     */
    async aiSearch(query) {
        const res = await fetch(`${API_BASE}/ai/search`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query })
        });
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Create a new car booking reservation
     */
    async createBooking(bookingData) {
        const res = await fetch(`${API_BASE}/bookings`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookingData)
        });
        if (!res.ok) {
            const errData = await res.json().catch(() => null);
            if (errData && errData.fieldErrors) {
                const firstMsg = Object.values(errData.fieldErrors)[0];
                throw new Error(firstMsg);
            }
            throw new Error((errData && errData.message) || 'Failed to create booking reservation');
        }
        return res.json();
    },

    /**
     * Get bookings by customer email
     */
    async getBookingsByEmail(email) {
        const res = await fetch(`${API_BASE}/bookings/user?email=${encodeURIComponent(email)}`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Get booking by reference ID
     */
    async getBookingByReference(ref) {
        const res = await fetch(`${API_BASE}/bookings/reference/${encodeURIComponent(ref)}`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Cancel a booking reservation
     */
    async cancelBooking(id) {
        const res = await fetch(`${API_BASE}/bookings/${id}/cancel`, {
            method: 'PUT'
        });
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Get booked date ranges for a specific vehicle
     */
    async getBookedRanges(carId) {
        const res = await fetch(`${API_BASE}/bookings/car/${carId}/booked-ranges`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    },

    /**
     * Check if vehicle is available between start and end dates
     */
    async checkAvailability(carId, startDate, endDate) {
        const res = await fetch(`${API_BASE}/bookings/car/${carId}/check-availability?startDate=${encodeURIComponent(startDate)}&endDate=${encodeURIComponent(endDate)}`);
        if (!res.ok) throw new Error(await parseError(res));
        return res.json();
    }
};

async function parseError(res) {
    try {
        const data = await res.json();
        return data.message || `Request failed with status ${res.status}`;
    } catch {
        return `Request failed with status ${res.status}`;
    }
}

/**
 * Shared helper to resolve accurate, exact car photos
 */
function getExactCarImage(car) {
    if (!car) return 'images/cars/hyundai-creta.png';
    if (car.imageUrl && !car.imageUrl.includes('unsplash.com')) {
        return car.imageUrl;
    }
    const name = (car.carName || '').toLowerCase();
    if (name.includes('creta')) return 'images/cars/hyundai-creta.png';
    if (name.includes('seltos')) return 'images/cars/kia-seltos.jpg';
    if (name.includes('nexon ev')) return 'images/cars/tata-nexon-ev.png';
    if (name.includes('nexon')) return 'images/cars/tata-nexon.jpg';
    if (name.includes('swift')) return 'images/cars/maruti-swift.jpg';
    if (name.includes('city')) return 'images/cars/honda-city.jpg';
    if (name.includes('innova')) return 'images/cars/toyota-innova.jpg';
    if (name.includes('xuv700')) return 'images/cars/mahindra-xuv700.png';
    if (name.includes('punch')) return 'images/cars/tata-punch.png';
    if (name.includes('hector')) return 'images/cars/mg-hector.png';
    if (name.includes('baleno')) return 'images/cars/maruti-baleno.jpg';
    if (name.includes('fortuner')) return 'images/cars/toyota-fortuner.jpg';
    if (name.includes('bmw') || name.includes('3 series')) return 'images/cars/bmw-3series.jpg';
    if (name.includes('verna')) return 'images/cars/hyundai-verna.jpg';
    return car.imageUrl || 'images/cars/hyundai-creta.png';
}

/**
 * Shared helper to render a car card element
 */
function createCarCardHtml(car) {
    const imgUrl = getExactCarImage(car);
    const fallbackImg = 'images/cars/hyundai-creta.png';

    return `
        <div class="car-card">
            <div class="car-card-img-wrap">
                <img class="car-card-img" src="${escapeHtml(imgUrl)}" alt="${escapeHtml(car.carName)}" loading="lazy" onerror="this.onerror=null;this.src='${fallbackImg}'" />
                <span class="car-tag-badge">${escapeHtml(car.carType)}</span>
            </div>
            <div class="car-card-body">
                <div class="car-card-header">
                    <h3 class="car-card-title">${escapeHtml(car.carName)}</h3>
                </div>
                <div class="car-card-specs-row">
                    ${escapeHtml(car.carType)} • ${escapeHtml(car.transmission)} • ${escapeHtml(car.fuelType)}
                </div>
                <div class="car-card-meta">
                    <span class="car-card-meta-item">
                        📍 ${escapeHtml(car.location)}
                    </span>
                    <span class="car-card-meta-item">
                        👥 ${car.seats} Seats
                    </span>
                    <span class="car-card-meta-item">
                        📅 ${car.year}
                    </span>
                </div>
                <div class="car-card-footer">
                    <div class="car-card-price">
                        <span class="amount">₹${car.pricePerDay.toLocaleString('en-IN')}</span>
                        <span class="period">per day</span>
                    </div>
                    <a href="car-details.html?id=${car.id}" class="btn btn-outline btn-sm">
                        View Details →
                    </a>
                </div>
            </div>
        </div>
    `;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
