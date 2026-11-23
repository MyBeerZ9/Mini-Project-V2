/**
 * AI Car Rental System Home Page Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    initFeaturedCars();
    initMobileNav();
});

async function initFeaturedCars() {
    const container = document.getElementById('featured-cars-container');
    if (!container) return;

    try {
        const cars = await Api.getFeaturedCars();
        if (!cars || cars.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🚗</div>
                    <h3 class="empty-state-title">No cars available yet</h3>
                    <p class="empty-state-desc">Be the first to list your vehicle on car rent!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = cars.map(createCarCardHtml).join('');
    } catch (err) {
        console.error('Error loading featured cars:', err);
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3 class="empty-state-title">Unable to load cars</h3>
                <p class="empty-state-desc">Please ensure the Spring Boot server is running on port 8080.</p>
            </div>
        `;
    }
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
