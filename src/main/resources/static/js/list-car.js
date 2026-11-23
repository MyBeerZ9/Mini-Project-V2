/**
 * AI Car Rental System List Your Car Page Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    initListCarForm();
    initMobileNav();
});

function initListCarForm() {
    const form = document.getElementById('list-car-form');
    const submitBtn = document.getElementById('submit-car-btn');
    const alertSuccess = document.getElementById('alert-success');
    const alertDanger = document.getElementById('alert-danger');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        hideAlerts();

        // Extract form values
        const carData = {
            carName: document.getElementById('carName').value.trim(),
            brand: document.getElementById('brand').value.trim(),
            model: document.getElementById('model').value.trim(),
            year: parseInt(document.getElementById('year').value, 10),
            carType: document.getElementById('carType').value,
            fuelType: document.getElementById('fuelType').value,
            transmission: document.getElementById('transmission').value,
            seats: parseInt(document.getElementById('seats').value, 10),
            location: document.getElementById('location').value.trim(),
            pricePerDay: parseFloat(document.getElementById('pricePerDay').value),
            description: document.getElementById('description').value.trim(),
            imageUrl: document.getElementById('imageUrl').value.trim() || getDefaultImage(document.getElementById('carType').value),
            ownerName: document.getElementById('ownerName').value.trim(),
            ownerEmail: document.getElementById('ownerEmail').value.trim()
        };

        // Client validation check
        const validationErr = validateCarData(carData);
        if (validationErr) {
            showDangerAlert(validationErr);
            return;
        }

        const originalBtnHtml = submitBtn.innerHTML;
        try {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="loading-spinner"></span> Publishing Car Listing...`;

            const createdCar = await Api.createCar(carData);

            showSuccessAlert(`Car listing created successfully! Your vehicle "${createdCar.carName}" is now live.`);
            form.reset();

            // Scroll to alert
            alertSuccess.scrollIntoView({ behavior: 'smooth', block: 'center' });

        } catch (err) {
            console.error('Error listing car:', err);
            showDangerAlert(err.message || 'Failed to list car. Please verify all fields and try again.');
            alertDanger.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnHtml;
        }
    });

    function validateCarData(data) {
        if (!data.carName) return 'Please enter the Car Name.';
        if (!data.brand) return 'Please enter the Brand.';
        if (!data.model) return 'Please enter the Model.';
        if (isNaN(data.year) || data.year < 1990 || data.year > 2030) return 'Please enter a valid Year (1990 - 2030).';
        if (!data.carType) return 'Please select a Car Type.';
        if (!data.fuelType) return 'Please select a Fuel Type.';
        if (!data.transmission) return 'Please select a Transmission.';
        if (isNaN(data.seats) || data.seats < 2 || data.seats > 15) return 'Seats must be between 2 and 15.';
        if (!data.location) return 'Please enter the Location.';
        if (isNaN(data.pricePerDay) || data.pricePerDay <= 0) return 'Please enter a valid Price Per Day greater than ₹0.';
        if (!data.ownerName) return 'Please enter your Name.';
        if (!data.ownerEmail || !data.ownerEmail.includes('@')) return 'Please enter a valid Email Address.';
        return null;
    }

    function showSuccessAlert(msg) {
        alertSuccess.innerHTML = `✅ ${escapeHtml(msg)} <a href="find-car.html" style="text-decoration: underline; margin-left: 0.5rem; font-weight: 700;">View in Fleet →</a>`;
        alertSuccess.style.display = 'flex';
    }

    function showDangerAlert(msg) {
        alertDanger.innerHTML = `❌ ${escapeHtml(msg)}`;
        alertDanger.style.display = 'flex';
    }

    function hideAlerts() {
        alertSuccess.style.display = 'none';
        alertDanger.style.display = 'none';
    }

    function getDefaultImage(carType) {
        switch (carType) {
            case 'SUV':
                return 'images/cars/hyundai-creta.png';
            case 'Sedan':
                return 'images/cars/honda-city.jpg';
            case 'Luxury':
                return 'images/cars/bmw-3series.jpg';
            case 'Electric':
                return 'images/cars/tata-nexon-ev.png';
            case 'Hatchback':
                return 'images/cars/maruti-swift.jpg';
            case 'MUV':
                return 'images/cars/toyota-innova.jpg';
            default:
                return 'images/cars/hyundai-creta.png';
        }
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
