/**
 * AI Car Rental System - Car Details Controller
 * Features:
 * - Interactive Availability Calendar with month navigation
 * - Real-time booking collision & conflict detection
 * - Live rental pricing calculations
 * - Complete booking reservation workflow & printable receipt modal
 */
document.addEventListener('DOMContentLoaded', () => {
    initCarDetailsPage();
    initMobileNav();
});

let currentCar = null;
let currentBookedRanges = [];
let calViewDate = new Date(); // Tracks the displayed month/year

async function initCarDetailsPage() {
    const params = new URLSearchParams(window.location.search);
    const carId = params.get('id');

    const contentArea = document.getElementById('details-content');
    const loader = document.getElementById('details-loader');

    if (!carId) {
        showError('No car ID specified in the URL. Please choose a vehicle from the fleet.');
        return;
    }

    try {
        // Fetch Car Details and Existing Booked Dates in parallel
        const [car, bookedRanges] = await Promise.all([
            Api.getCarById(carId),
            Api.getBookedRanges(carId).catch(() => [])
        ]);

        currentCar = car;
        currentBookedRanges = bookedRanges || [];

        populateCarDetails(car);
        initCalendarControls();
        initBookingForm();
        renderCalendar();
        renderReservedBadges();

    } catch (err) {
        console.error('Failed to load car details:', err);
        showError(err.message || 'Vehicle not found or no longer available.');
    }

    function populateCarDetails(car) {
        const imgUrl = (typeof getExactCarImage === 'function') ? getExactCarImage(car) : (car.imageUrl || 'images/cars/hyundai-creta.png');
        const fallbackImg = 'images/cars/hyundai-creta.png';

        // Hero Image & Title
        const heroImg = document.getElementById('detail-hero-img');
        if (heroImg) {
            heroImg.src = imgUrl;
            heroImg.alt = car.carName;
            heroImg.onerror = () => { heroImg.src = fallbackImg; };
        }

        document.title = `${car.carName} - AI CAR RENTAL SYSTEM`;
        setText('detail-brand-badge', `${car.brand} • ${car.carType}`);
        setText('detail-title', car.carName);
        setText('detail-price', `₹${car.pricePerDay.toLocaleString('en-IN')}`);
        setText('detail-location', car.location);

        // Specs Matrix
        setText('spec-transmission', car.transmission);
        setText('spec-fuel', car.fuelType);
        setText('spec-seats', `${car.seats} Persons`);
        setText('spec-year', car.year);
        setText('spec-type', car.carType);
        setText('spec-city', car.location);

        // Description
        setText('detail-description', car.description || 'No additional description provided by the owner for this vehicle.');

        // Owner Info
        setText('owner-name', car.ownerName);
        setText('owner-email', car.ownerEmail);

        // Modal car name
        setText('modal-car-name', car.carName);

        if (contentArea) contentArea.style.display = 'block';
        if (loader) loader.style.display = 'none';
    }

    function initCalendarControls() {
        const prevBtn = document.getElementById('cal-prev-btn');
        const nextBtn = document.getElementById('cal-next-btn');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                calViewDate.setMonth(calViewDate.getMonth() - 1);
                renderCalendar();
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                calViewDate.setMonth(calViewDate.getMonth() + 1);
                renderCalendar();
            });
        }
    }

    function initBookingForm() {
        const form = document.getElementById('car-booking-form');
        const startDateInput = document.getElementById('book-start-date');
        const endDateInput = document.getElementById('book-end-date');
        const printBtn = document.getElementById('print-booking-btn');
        const closeModalBtn = document.getElementById('close-modal-btn');
        const modal = document.getElementById('booking-modal');

        // Restrict min dates to today
        const todayStr = getIsoDateString(new Date());
        if (startDateInput) {
            startDateInput.min = todayStr;
            startDateInput.addEventListener('change', () => {
                if (endDateInput) {
                    endDateInput.min = startDateInput.value || todayStr;
                    if (endDateInput.value && endDateInput.value < startDateInput.value) {
                        endDateInput.value = startDateInput.value;
                    }
                }
                handleDateRangeChange();
            });
        }

        if (endDateInput) {
            endDateInput.min = todayStr;
            endDateInput.addEventListener('change', () => {
                handleDateRangeChange();
            });
        }

        // Form Submission
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await submitBookingForm(form);
            });
        }

        // Modal Actions
        if (printBtn) {
            printBtn.addEventListener('click', () => {
                window.print();
            });
        }

        if (closeModalBtn && modal) {
            closeModalBtn.addEventListener('click', () => {
                modal.style.display = 'none';
            });
        }

        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.style.display = 'none';
                }
            });
        }
    }

    function showError(msg) {
        if (loader) loader.style.display = 'none';
        if (contentArea) {
            contentArea.style.display = 'block';
            contentArea.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">⚠️</div>
                    <h3 class="empty-state-title">Car Details Unavailable</h3>
                    <p class="empty-state-desc">${escapeHtml(msg)}</p>
                    <a href="find-car.html" class="btn btn-primary" style="margin-top: 1.5rem;">Browse All Cars</a>
                </div>
            `;
        }
    }
}

/**
 * Render Month Grid in Calendar
 */
function renderCalendar() {
    const daysGrid = document.getElementById('cal-days-grid');
    const monthLabel = document.getElementById('cal-month-label');
    if (!daysGrid || !monthLabel) return;

    const year = calViewDate.getFullYear();
    const month = calViewDate.getMonth();

    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    monthLabel.textContent = `${monthNames[month]} ${year}`;

    daysGrid.innerHTML = '';

    const firstDayIndex = new Date(year, month, 1).getDay(); // 0 = Sun
    const totalDaysInMonth = new Date(year, month + 1, 0).getDate();

    const todayStr = getIsoDateString(new Date());
    const selStart = document.getElementById('book-start-date')?.value;
    const selEnd = document.getElementById('book-end-date')?.value;

    // Blank cells before the 1st
    for (let i = 0; i < firstDayIndex; i++) {
        const emptyCell = document.createElement('div');
        emptyCell.className = 'cal-day-cell empty';
        daysGrid.appendChild(emptyCell);
    }

    // Month days
    for (let day = 1; day <= totalDaysInMonth; day++) {
        const cell = document.createElement('div');
        cell.className = 'cal-day-cell';
        cell.textContent = day;

        const cellDate = new Date(year, month, day);
        const cellDateStr = getIsoDateString(cellDate);

        // Check if date is in the past
        if (cellDateStr < todayStr) {
            cell.classList.add('past');
            cell.title = 'Past date';
        } else {
            // Check if date is booked
            const isBooked = isDateInBookedRanges(cellDateStr, currentBookedRanges);

            if (isBooked) {
                cell.classList.add('booked');
                cell.title = 'Already Reserved / Unavailable';
            } else {
                cell.classList.add('available');
                cell.title = 'Available for booking - Click to select';

                // Click to quickly pick dates
                cell.addEventListener('click', () => {
                    handleCalendarDayClick(cellDateStr);
                });
            }

            // Selection highlights
            if (selStart && cellDateStr === selStart) {
                cell.classList.add('selected-start');
            }
            if (selEnd && cellDateStr === selEnd) {
                cell.classList.add('selected-end');
            }
            if (selStart && selEnd && cellDateStr > selStart && cellDateStr < selEnd) {
                cell.classList.add('in-range');
            }
        }

        daysGrid.appendChild(cell);
    }
}

/**
 * Handle quick clicking on calendar days
 */
function handleCalendarDayClick(dateStr) {
    const startInput = document.getElementById('book-start-date');
    const endInput = document.getElementById('book-end-date');
    if (!startInput || !endInput) return;

    if (!startInput.value || (startInput.value && endInput.value)) {
        startInput.value = dateStr;
        endInput.value = '';
    } else if (startInput.value && !endInput.value) {
        if (dateStr >= startInput.value) {
            endInput.value = dateStr;
        } else {
            startInput.value = dateStr;
            endInput.value = '';
        }
    }

    handleDateRangeChange();
    renderCalendar();
}

/**
 * Checks if a specific YYYY-MM-DD string collides with any booked range
 */
function isDateInBookedRanges(dateStr, ranges) {
    if (!ranges || !ranges.length) return false;
    for (const r of ranges) {
        if (r.status === 'CANCELLED') continue;
        if (dateStr >= r.startDate && dateStr <= r.endDate) {
            return true;
        }
    }
    return false;
}

/**
 * Display readable tags for existing upcoming reservations
 */
function renderReservedBadges() {
    const container = document.getElementById('reserved-tags-box');
    const list = document.getElementById('reserved-badges-list');
    if (!container || !list) return;

    if (!currentBookedRanges || currentBookedRanges.length === 0) {
        container.style.display = 'none';
        return;
    }

    const activeRanges = currentBookedRanges.filter(r => r.status !== 'CANCELLED');
    if (activeRanges.length === 0) {
        container.style.display = 'none';
        return;
    }

    container.style.display = 'block';
    list.innerHTML = activeRanges.map(r => `
        <span class="reserved-badge">
            🔒 ${formatDisplayDate(r.startDate)} → ${formatDisplayDate(r.endDate)}
        </span>
    `).join('');
}

/**
 * Real-Time Date Range Validation & Conflict Detection
 */
async function handleDateRangeChange() {
    const startDate = document.getElementById('book-start-date')?.value;
    const endDate = document.getElementById('book-end-date')?.value;
    const feedbackBox = document.getElementById('conflict-feedback');
    const feedbackIcon = document.getElementById('conflict-icon');
    const feedbackText = document.getElementById('conflict-text');
    const submitBtn = document.getElementById('submit-booking-btn');
    const priceSummary = document.getElementById('booking-price-summary');
    const daysCountEl = document.getElementById('summary-days-count');
    const totalPriceEl = document.getElementById('summary-total-price');

    renderCalendar();

    if (!startDate || !endDate) {
        if (feedbackBox) feedbackBox.style.display = 'none';
        if (priceSummary) priceSummary.style.display = 'none';
        if (submitBtn) submitBtn.disabled = true;
        return;
    }

    if (endDate < startDate) {
        showFeedback('danger', '❌', 'Return date cannot be earlier than pickup date.');
        if (submitBtn) submitBtn.disabled = true;
        if (priceSummary) priceSummary.style.display = 'none';
        return;
    }

    // Local overlap check against current booked ranges
    const hasLocalConflict = checkRangeOverlap(startDate, endDate, currentBookedRanges);

    if (hasLocalConflict) {
        const conflict = hasLocalConflict;
        showFeedback(
            'danger',
            '⚠️',
            `CONFLICT DETECTED: This car is already booked from ${formatDisplayDate(conflict.startDate)} to ${formatDisplayDate(conflict.endDate)}. Please pick different dates.`
        );
        if (submitBtn) submitBtn.disabled = true;
        if (priceSummary) priceSummary.style.display = 'none';
        return;
    }

    // Call backend API for double validation
    try {
        const check = await Api.checkAvailability(currentCar.id, startDate, endDate);
        if (!check.available) {
            showFeedback('danger', '⚠️', check.message || 'Vehicle unavailable for the selected dates.');
            if (submitBtn) submitBtn.disabled = true;
            if (priceSummary) priceSummary.style.display = 'none';
        } else {
            const totalDays = check.totalDays || calculateDays(startDate, endDate);
            const totalPrice = check.estimatedPrice || (totalDays * currentCar.pricePerDay);

            showFeedback('success', '✓', `Available! Zero booking conflicts for ${totalDays} ${totalDays === 1 ? 'day' : 'days'}.`);
            if (submitBtn) submitBtn.disabled = false;

            if (priceSummary && daysCountEl && totalPriceEl) {
                priceSummary.style.display = 'flex';
                daysCountEl.textContent = totalDays;
                totalPriceEl.textContent = `₹${totalPrice.toLocaleString('en-IN')}`;
            }
        }
    } catch (err) {
        // Fallback to client-side calculated availability if offline
        const totalDays = calculateDays(startDate, endDate);
        const totalPrice = totalDays * currentCar.pricePerDay;
        showFeedback('success', '✓', `Dates are clear! ${totalDays} days selected.`);
        if (submitBtn) submitBtn.disabled = false;
        if (priceSummary && daysCountEl && totalPriceEl) {
            priceSummary.style.display = 'flex';
            daysCountEl.textContent = totalDays;
            totalPriceEl.textContent = `₹${totalPrice.toLocaleString('en-IN')}`;
        }
    }

    function showFeedback(type, icon, text) {
        if (!feedbackBox) return;
        feedbackBox.style.display = 'flex';
        feedbackBox.className = `conflict-alert ${type}`;
        if (feedbackIcon) feedbackIcon.textContent = icon;
        if (feedbackText) feedbackText.textContent = text;
    }
}

/**
 * Submit Booking Reservation
 */
async function submitBookingForm(form) {
    const submitBtn = document.getElementById('submit-booking-btn');
    const modal = document.getElementById('booking-modal');

    const startDate = document.getElementById('book-start-date')?.value;
    const endDate = document.getElementById('book-end-date')?.value;
    const customerName = document.getElementById('book-name')?.value;
    const customerEmail = document.getElementById('book-email')?.value;
    const customerPhone = document.getElementById('book-phone')?.value;
    const notes = document.getElementById('book-notes')?.value;

    if (!startDate || !endDate || !customerName || !customerEmail || !customerPhone) {
        alert('Please fill out all required booking fields.');
        return;
    }

    const origBtnText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Locking Reservation... ⏳';

    try {
        const payload = {
            carId: currentCar.id,
            startDate: startDate,
            endDate: endDate,
            customerName: customerName.trim(),
            customerEmail: customerEmail.trim(),
            customerPhone: customerPhone.trim(),
            notes: notes ? notes.trim() : null
        };

        const booking = await Api.createBooking(payload);

        // Populate receipt modal
        setText('modal-ref-code', booking.bookingReference);
        setText('modal-car-name', currentCar.carName);
        setText('modal-dates', `${formatDisplayDate(booking.startDate)} → ${formatDisplayDate(booking.endDate)}`);
        setText('modal-days', `${booking.totalDays} Days`);
        setText('modal-customer', `${booking.customerName} (${booking.customerEmail})`);
        setText('modal-total-amount', `₹${booking.totalPrice.toLocaleString('en-IN')}`);

        // Show receipt modal
        if (modal) modal.style.display = 'flex';

        // Refresh booked ranges and calendar
        currentBookedRanges = await Api.getBookedRanges(currentCar.id).catch(() => currentBookedRanges);
        renderCalendar();
        renderReservedBadges();

        // Reset form inputs
        form.reset();
        document.getElementById('booking-price-summary').style.display = 'none';
        document.getElementById('conflict-feedback').style.display = 'none';

    } catch (err) {
        console.error('Booking failed:', err);
        const feedbackBox = document.getElementById('conflict-feedback');
        const feedbackIcon = document.getElementById('conflict-icon');
        const feedbackText = document.getElementById('conflict-text');
        if (feedbackBox) {
            feedbackBox.style.display = 'flex';
            feedbackBox.className = 'conflict-alert danger';
            if (feedbackIcon) feedbackIcon.textContent = '❌';
            if (feedbackText) feedbackText.textContent = err.message || 'Booking conflict: Vehicle already reserved.';
        }
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = origBtnText;
    }
}

/**
 * Checks overlap between [reqStart, reqEnd] and an array of booked ranges
 */
function checkRangeOverlap(reqStart, reqEnd, ranges) {
    if (!ranges || !ranges.length) return null;
    for (const r of ranges) {
        if (r.status === 'CANCELLED') continue;
        if (reqStart <= r.endDate && reqEnd >= r.startDate) {
            return r;
        }
    }
    return null;
}

function calculateDays(startStr, endStr) {
    const s = new Date(startStr);
    const e = new Date(endStr);
    const diff = Math.ceil((e - s) / (1000 * 60 * 60 * 24));
    return Math.max(1, diff + 1);
}

function getIsoDateString(d) {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

function formatDisplayDate(dateStr) {
    if (!dateStr) return '-';
    try {
        const parts = dateStr.split('-');
        if (parts.length === 3) {
            const date = new Date(parts[0], parts[1] - 1, parts[2]);
            return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
        }
        return dateStr;
    } catch {
        return dateStr;
    }
}

function setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, m => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    })[m]);
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
