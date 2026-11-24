package com.example.carrental.service;

import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Car;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final com.example.carrental.repository.PartnerRepository partnerRepository;

    public DataInitializationService(CarRepository carRepository,
                                   BookingRepository bookingRepository,
                                   com.example.carrental.repository.PartnerRepository partnerRepository) {
        this.carRepository = carRepository;
        this.bookingRepository = bookingRepository;
        this.partnerRepository = partnerRepository;
    }

    @Override
    public void run(String... args) {
        seedSamplePartner();

        if (carRepository.count() == 0) {
            logger.info("Database is empty. Seeding 14 realistic sample cars for college viva demonstration...");
            seedSampleCars();
            logger.info("Successfully seeded {} cars into the database.", carRepository.count());
        } else {
            logger.info("Database already contains {} cars. Updating image URLs and compliance dates if needed...", carRepository.count());
            updateExistingCarImages();
        }

        if (bookingRepository.count() == 0 && carRepository.count() > 0) {
            seedSampleBookings();
        }
    }

    private void seedSamplePartner() {
        if (!partnerRepository.existsByPartnerCodeIgnoreCase("CARVIA-HQ-DELHI")) {
            com.example.carrental.entity.Partner partner = new com.example.carrental.entity.Partner();
            partner.setPartnerName("Apex Mobility Pvt Ltd");
            partner.setPartnerCode("CARVIA-HQ-DELHI");
            partner.setContactEmail("fleet@apexmobility.com");
            partner.setContactPhone("+91 99887 76655");
            partner.setCity("Delhi");
            partner.setRevenueSharePercent(82.5);
            partner.setVerified(true);
            partner.setActive(true);
            partnerRepository.save(partner);
            logger.info("Seeded primary demonstration franchise partner: Apex Mobility Pvt Ltd");
        }
    }

    private void updateExistingCarImages() {
        List<Car> cars = carRepository.findAll();
        boolean updated = false;
        for (Car car : cars) {
            String exactImg = getExactImageForCar(car.getCarName());
            if (exactImg != null && (car.getImageUrl() == null || car.getImageUrl().contains("unsplash.com") || !car.getImageUrl().equals(exactImg))) {
                car.setImageUrl(exactImg);
                carRepository.save(car);
                updated = true;
            }
        }
        if (updated) {
            logger.info("Successfully updated existing car fleet with exact vehicle photographs.");
        }
    }

    private String getExactImageForCar(String name) {
        if (name == null) return null;
        if (name.contains("Creta")) return "images/cars/hyundai-creta.png";
        if (name.contains("Seltos")) return "images/cars/kia-seltos.jpg";
        if (name.contains("Nexon EV")) return "images/cars/tata-nexon-ev.png";
        if (name.contains("Nexon")) return "images/cars/tata-nexon.jpg";
        if (name.contains("Swift")) return "images/cars/maruti-swift.jpg";
        if (name.contains("City")) return "images/cars/honda-city.jpg";
        if (name.contains("Innova")) return "images/cars/toyota-innova.jpg";
        if (name.contains("XUV700")) return "images/cars/mahindra-xuv700.png";
        if (name.contains("Punch")) return "images/cars/tata-punch.png";
        if (name.contains("Hector")) return "images/cars/mg-hector.png";
        if (name.contains("Baleno")) return "images/cars/maruti-baleno.jpg";
        if (name.contains("Fortuner")) return "images/cars/toyota-fortuner.jpg";
        if (name.contains("BMW") || name.contains("3 Series")) return "images/cars/bmw-3series.jpg";
        if (name.contains("Verna")) return "images/cars/hyundai-verna.jpg";
        return null;
    }

    private void seedSampleBookings() {
        carRepository.findAll().stream().findFirst().ifPresent(car -> {
            LocalDate today = LocalDate.now();
            Booking b1 = new Booking();
            b1.setBookingReference("CR-SAMPLE-01");
            b1.setCar(car);
            b1.setCustomerName("Aman Verma");
            b1.setCustomerEmail("aman.verma@example.com");
            b1.setCustomerPhone("+91 98765 43210");
            b1.setStartDate(today.plusDays(2));
            b1.setEndDate(today.plusDays(4));
            b1.setTotalDays(3);
            b1.setTotalPrice(car.getPricePerDay() * 3);
            b1.setStatus("CONFIRMED");
            b1.setNotes("Weekend trip booking demo");

            Booking b2 = new Booking();
            b2.setBookingReference("CR-SAMPLE-02");
            b2.setCar(car);
            b2.setCustomerName("Pooja Nair");
            b2.setCustomerEmail("pooja.nair@example.com");
            b2.setCustomerPhone("+91 98111 22334");
            b2.setStartDate(today.plusDays(10));
            b2.setEndDate(today.plusDays(13));
            b2.setTotalDays(4);
            b2.setTotalPrice(car.getPricePerDay() * 4);
            b2.setStatus("CONFIRMED");
            b2.setNotes("Business travel demo reservation");

            bookingRepository.saveAll(List.of(b1, b2));
            logger.info("Seeded 2 sample demonstration bookings for vehicle: {}", car.getCarName());
        });
    }

    private void seedSampleCars() {
        List<Car> sampleCars = List.of(
                createCar(
                        "Hyundai Creta SX(O)",
                        "Hyundai",
                        "Creta",
                        2023,
                        "SUV",
                        "Petrol",
                        "Automatic",
                        5,
                        "Delhi",
                        2500.0,
                        "Premium compact SUV with panoramic sunroof, ventilated front seats, Bose sound system, and smooth automatic transmission. Ideal for Delhi city and highway drives.",
                        "images/cars/hyundai-creta.png",
                        "Rajesh Sharma",
                        "rajesh.sharma@example.com"
                ),
                createCar(
                        "Kia Seltos GTX Plus",
                        "Kia",
                        "Seltos",
                        2024,
                        "SUV",
                        "Diesel",
                        "Automatic",
                        5,
                        "Gurgaon",
                        2800.0,
                        "Futuristic design with heads-up display, 360-degree camera, and efficient turbo diesel engine delivering incredible torque and mileage.",
                        "images/cars/kia-seltos.jpg",
                        "Aman Verma",
                        "aman.verma@example.com"
                ),
                createCar(
                        "Tata Nexon Fearless",
                        "Tata",
                        "Nexon",
                        2023,
                        "SUV",
                        "Petrol",
                        "Manual",
                        5,
                        "Noida",
                        2200.0,
                        "5-star Global NCAP safety rated compact SUV with dynamic digital cockpit, high ground clearance, and punchy turbo performance.",
                        "images/cars/tata-nexon.jpg",
                        "Priya Singh",
                        "priya.singh@example.com"
                ),
                createCar(
                        "Maruti Swift ZXi",
                        "Maruti",
                        "Swift",
                        2022,
                        "Hatchback",
                        "Petrol",
                        "Manual",
                        5,
                        "Delhi",
                        1500.0,
                        "Peppy and highly economical hatchback. Perfect for navigating busy Delhi lanes, easy parking, and quick day rentals.",
                        "images/cars/maruti-swift.jpg",
                        "Vikas Malhotra",
                        "vikas.m@example.com"
                ),
                createCar(
                        "Honda City ZX",
                        "Honda",
                        "City",
                        2023,
                        "Sedan",
                        "Petrol",
                        "Automatic",
                        5,
                        "Delhi",
                        2600.0,
                        "The quintessential executive sedan with plush leather upholstery, CVT paddle shifters, ultra-refined i-VTEC engine, and supreme rear-seat comfort.",
                        "images/cars/honda-city.jpg",
                        "Sunil Gupta",
                        "sunil.gupta@example.com"
                ),
                createCar(
                        "Toyota Innova Crysta",
                        "Toyota",
                        "Innova Crysta",
                        2022,
                        "MUV",
                        "Diesel",
                        "Manual",
                        7,
                        "Ghaziabad",
                        3200.0,
                        "Legendary 7-seater multi-purpose vehicle built for ultimate family road trips. Ample luggage space, captain seats, and bulletproof reliability.",
                        "images/cars/toyota-innova.jpg",
                        "Harpreet Kaur",
                        "harpreet.k@example.com"
                ),
                createCar(
                        "Mahindra XUV700 AX7 Luxury",
                        "Mahindra",
                        "XUV700",
                        2024,
                        "SUV",
                        "Diesel",
                        "Automatic",
                        7,
                        "Delhi",
                        3800.0,
                        "Flagship 7-seater SUV equipped with Level 2 ADAS, Sony 12-speaker 3D audio, dual 10.25-inch screens, and exhilarating mHawk diesel power.",
                        "images/cars/mahindra-xuv700.png",
                        "Vikramaditya Roy",
                        "vikram.roy@example.com"
                ),
                createCar(
                        "Tata Punch Creative",
                        "Tata",
                        "Punch",
                        2023,
                        "SUV",
                        "Petrol",
                        "Manual",
                        5,
                        "Lucknow",
                        1800.0,
                        "Robust micro-SUV with 5-star crash safety, commanding driving position, 90-degree opening doors, and crisp infotainment system.",
                        "images/cars/tata-punch.png",
                        "Neha Tiwari",
                        "neha.tiwari@example.com"
                ),
                createCar(
                        "MG Hector Sharp Pro",
                        "MG",
                        "Hector",
                        2023,
                        "SUV",
                        "Hybrid",
                        "Automatic",
                        5,
                        "Noida",
                        3000.0,
                        "India's first internet SUV featuring an immense 14-inch HD portrait screen, panoramic glass sky roof, and spacious rear legroom.",
                        "images/cars/mg-hector.png",
                        "Rohit Bansal",
                        "rohit.b@example.com"
                ),
                createCar(
                        "Maruti Baleno Alpha",
                        "Maruti",
                        "Baleno",
                        2023,
                        "Hatchback",
                        "Petrol",
                        "Automatic",
                        5,
                        "Jaipur",
                        1700.0,
                        "Premium hatchback with 360-degree view camera, head-up display, Arkamys sound tuning, and smooth AGS automatic gearbox.",
                        "images/cars/maruti-baleno.jpg",
                        "Ananya Joshi",
                        "ananya.j@example.com"
                ),
                createCar(
                        "Toyota Fortuner 4x4",
                        "Toyota",
                        "Fortuner",
                        2023,
                        "SUV",
                        "Diesel",
                        "Automatic",
                        7,
                        "Gurgaon",
                        4500.0,
                        "Commanding full-size 4x4 SUV with rugged off-road capability, luxurious black leather interior, and authoritative road presence.",
                        "images/cars/toyota-fortuner.jpg",
                        "Sanjay Chawla",
                        "sanjay.c@example.com"
                ),
                createCar(
                        "BMW 3 Series Gran Limousine",
                        "BMW",
                        "3 Series",
                        2024,
                        "Luxury",
                        "Petrol",
                        "Automatic",
                        5,
                        "Delhi",
                        6500.0,
                        "First-class German luxury sedan with extended wheelbase, curved widescreen display, ambient cabin lighting, and pure rear-wheel-drive dynamics.",
                        "images/cars/bmw-3series.jpg",
                        "Karan Kapoor",
                        "karan.kapoor@example.com"
                ),
                createCar(
                        "Tata Nexon EV Max",
                        "Tata",
                        "Nexon EV",
                        2023,
                        "Electric",
                        "Electric",
                        "Automatic",
                        5,
                        "Delhi",
                        2700.0,
                        "Zero-emissions electric SUV with 453 km ARAI range, instant electric acceleration, multi-mode regenerative braking, and silent cruising.",
                        "images/cars/tata-nexon-ev.png",
                        "Aditi Saxena",
                        "aditi.s@example.com"
                ),
                createCar(
                        "Hyundai Verna SX Turbo",
                        "Hyundai",
                        "Verna",
                        2024,
                        "Sedan",
                        "Petrol",
                        "Automatic",
                        5,
                        "Agra",
                        2700.0,
                        "Fastback fast sedan with 160 PS turbo petrol engine, dual 10.25-inch integrated screens, heated/ventilated seats, and futuristic styling.",
                        "images/cars/hyundai-verna.jpg",
                        "Deepak Rawat",
                        "deepak.r@example.com"
                )
        );

        carRepository.saveAll(sampleCars);
    }

    private Car createCar(String name, String brand, String model, int year, String type,
                          String fuel, String transmission, int seats, String location,
                          double price, String desc, String img, String owner, String email) {
        Car car = new Car();
        car.setCarName(name);
        car.setBrand(brand);
        car.setModel(model);
        car.setYear(year);
        car.setCarType(type);
        car.setFuelType(fuel);
        car.setTransmission(transmission);
        car.setSeats(seats);
        car.setLocation(location);
        car.setPricePerDay(price);
        car.setDescription(desc);
        car.setImageUrl(img);
        car.setOwnerName(owner);
        car.setOwnerEmail(email);
        car.setAvailable(true);
        return car;
    }
}
