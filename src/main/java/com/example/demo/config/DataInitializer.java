package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ServiceItemRepository serviceItemRepository;
    private final UserRepository userRepository;
    private final BannerRepository bannerRepository;
    private final OtherServiceRepository otherServiceRepository;
    private final CouponRepository couponRepository;

    @Override
    public void run(String... args) {
        seedServices();
        seedUsers();
        seedBanners();
        seedOtherServices();
        seedCoupons();
    }

    private void seedServices() {
        if (serviceItemRepository.count() > 0) return;

        serviceItemRepository.save(ServiceItem.builder().name("Electrician").description("Electrical repair and installation").active(true).build());
        serviceItemRepository.save(ServiceItem.builder().name("Plumber").description("Plumbing repair and installation").active(true).build());
        serviceItemRepository.save(ServiceItem.builder().name("AC Repair").description("Air conditioner service").active(true).build());
        serviceItemRepository.save(ServiceItem.builder().name("Carpenter").description("Furniture repair and door fixing").active(true).build());
        serviceItemRepository.save(ServiceItem.builder().name("Painter").description("Home and office painting services").active(true).build());
        serviceItemRepository.save(ServiceItem.builder().name("Other").description("Other service requests").active(true).build());
    }

    private void seedUsers() {
        createUserIfAbsent("9876543210", "Venkatesh", "password1234", UserRole.USER);
        createUserIfAbsent("admin", "Admin", "admin123", UserRole.ADMIN);
        createUserIfAbsent("worker1", "Worker One", "worker123", UserRole.WORKER);
    }

    private void seedBanners() {
        if (bannerRepository.count() > 0) return;
        bannerRepository.save(Banner.builder().title("Electrician Special").imageUrl("https://images.unsplash.com/photo-1581092160607-ee22731a4b52").redirectPath("/service/2").sortOrder(1).active(true).build());
        bannerRepository.save(Banner.builder().title("Plumber Offer").imageUrl("https://images.unsplash.com/photo-1581578731548-c64695cc6952").redirectPath("/service/1").sortOrder(2).active(true).build());
        bannerRepository.save(Banner.builder().title("Painter Promo").imageUrl("https://images.unsplash.com/photo-1562259949-e8e7689d7828").redirectPath("/service/7").sortOrder(3).active(true).build());
    }

    private void seedOtherServices() {
        if (otherServiceRepository.count() > 0) return;
        otherServiceRepository.save(OtherService.builder().name("Online services").menuDetails("Consultation, online troubleshooting, remote support").startPrice("₹299").imageUrl("https://images.unsplash.com/photo-1460925895917-afdab827c52f").active(true).build());
        otherServiceRepository.save(OtherService.builder().name("Interior designing").menuDetails("Room layout, color planning, decor planning").startPrice("₹1,999").imageUrl("https://images.unsplash.com/photo-1484154218962-a197022b5858").active(true).build());
        otherServiceRepository.save(OtherService.builder().name("Waterproof services for home").menuDetails("Terrace coating, bathroom leakage treatment, wall waterproofing").startPrice("₹2,499").imageUrl("https://images.unsplash.com/photo-1505691938895-1758d7feb511").active(true).build());
    }

    private void seedCoupons() {
        if (couponRepository.count() > 0) return;
        couponRepository.save(Coupon.builder().code("WELCOME100").message("Get ₹100 off on first booking").active(true).build());
        couponRepository.save(Coupon.builder().code("WORKDAY50").message("Flat ₹50 off on weekday bookings").active(true).build());
    }

    private void createUserIfAbsent(String mobile, String name, String password, UserRole role) {
        if (userRepository.findByMobile(mobile).isEmpty()) {
            userRepository.save(User.builder()
                    .name(name)
                    .mobile(mobile)
                    .password(password)
                    .role(role)
                    .signupProvider("SEED")
                    .build());
        }
    }
}
