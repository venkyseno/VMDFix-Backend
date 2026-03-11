package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminFlowController {

    private final BannerRepository bannerRepository;
    private final OtherServiceRepository otherServiceRepository;
    private final OtherServiceItemRepository otherServiceItemRepository;
    private final OtherServiceOrderRepository otherServiceOrderRepository;
    private final CouponRepository couponRepository;
    private final WorkerApplicationRepository workerApplicationRepository;
    private final UserRepository userRepository;
    private final ServiceCaseRepository serviceCaseRepository;
    private final QuickServiceRepository quickServiceRepository;
    private final OurServiceCardRepository ourServiceCardRepository;

    public AdminFlowController(
            BannerRepository bannerRepository,
            OtherServiceRepository otherServiceRepository,
            OtherServiceItemRepository otherServiceItemRepository,
            OtherServiceOrderRepository otherServiceOrderRepository,
            CouponRepository couponRepository,
            WorkerApplicationRepository workerApplicationRepository,
            UserRepository userRepository,
            ServiceCaseRepository serviceCaseRepository,
            QuickServiceRepository quickServiceRepository,
            OurServiceCardRepository ourServiceCardRepository) {
        this.bannerRepository = bannerRepository;
        this.otherServiceRepository = otherServiceRepository;
        this.otherServiceItemRepository = otherServiceItemRepository;
        this.otherServiceOrderRepository = otherServiceOrderRepository;
        this.couponRepository = couponRepository;
        this.workerApplicationRepository = workerApplicationRepository;
        this.userRepository = userRepository;
        this.serviceCaseRepository = serviceCaseRepository;
        this.quickServiceRepository = quickServiceRepository;
        this.ourServiceCardRepository = ourServiceCardRepository;
    }

    private static String trimOrEmpty(String v) { return v == null ? "" : v.trim(); }

    // ---- BANNERS ----
    @GetMapping("/banners")
    public List<Banner> adminBanners() {
        try { return bannerRepository.findAll(); } catch (RuntimeException ex) { return List.of(); }
    }

    @PostMapping("/banners")
    public Banner saveBanner(@RequestBody Banner banner) {
        if (trimOrEmpty(banner.getTitle()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banner title is required");
        if (trimOrEmpty(banner.getImageUrl()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banner image is required");
        if (banner.getActive() == null) banner.setActive(true);
        if (banner.getSortOrder() == null) banner.setSortOrder(1);
        if (banner.getDisplaySeconds() == null || banner.getDisplaySeconds() <= 0) banner.setDisplaySeconds(5);
        if (trimOrEmpty(banner.getPlacement()).isEmpty()) banner.setPlacement("HOME");
        return bannerRepository.save(banner);
    }

    @PutMapping("/banners/{id}")
    public Banner updateBanner(@PathVariable Long id, @RequestBody Banner payload) {
        Banner b = bannerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (trimOrEmpty(payload.getTitle()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banner title is required");
        if (trimOrEmpty(payload.getImageUrl()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Banner image is required");
        b.setTitle(payload.getTitle());
        b.setImageUrl(payload.getImageUrl());
        b.setRedirectPath(payload.getRedirectPath());
        b.setSortOrder(payload.getSortOrder() == null ? 1 : payload.getSortOrder());
        b.setDisplaySeconds(payload.getDisplaySeconds() == null || payload.getDisplaySeconds() <= 0 ? 5 : payload.getDisplaySeconds());
        b.setPlacement(trimOrEmpty(payload.getPlacement()).isEmpty() ? "HOME" : payload.getPlacement());
        b.setActive(payload.getActive() == null || payload.getActive());
        return bannerRepository.save(b);
    }

    @DeleteMapping("/banners/{id}")
    public void deleteBanner(@PathVariable Long id) { bannerRepository.deleteById(id); }

    // ---- OTHER SERVICES ----
    @GetMapping("/other-services")
    public List<OtherService> adminOtherServices() {
        try { return otherServiceRepository.findAll(); } catch (RuntimeException ex) { return List.of(); }
    }

    @PostMapping("/other-services")
    public OtherService saveOtherService(@RequestBody OtherService s) {
        if (trimOrEmpty(s.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Other service name is required");
        if (trimOrEmpty(s.getImageUrl()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Other service image is required");
        if (s.getActive() == null) s.setActive(true);
        return otherServiceRepository.save(s);
    }

    @PutMapping("/other-services/{id}")
    public OtherService updateOtherService(@PathVariable Long id, @RequestBody OtherService payload) {
        OtherService s = otherServiceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (trimOrEmpty(payload.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        if (trimOrEmpty(payload.getImageUrl()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image required");
        s.setName(payload.getName()); s.setMenuDetails(payload.getMenuDetails());
        s.setImageUrl(payload.getImageUrl()); s.setStartPrice(payload.getStartPrice());
        s.setActive(payload.getActive() == null || payload.getActive());
        return otherServiceRepository.save(s);
    }

    @DeleteMapping("/other-services/{id}")
    public void deleteOtherService(@PathVariable Long id) { otherServiceRepository.deleteById(id); }

    @GetMapping("/other-services/{id}/items")
    public List<OtherServiceItem> getOtherServiceItems(@PathVariable Long id) {
        return otherServiceItemRepository.findByOtherServiceId(id);
    }

    @PostMapping("/other-services/{id}/items")
    public OtherServiceItem addOtherServiceItem(@PathVariable Long id, @RequestBody OtherServiceItem item) {
        if (trimOrEmpty(item.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name required");
        if (item.getPrice() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item price required");
        if (item.getAvailableQuantity() == null || item.getAvailableQuantity() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity required");
        item.setOtherServiceId(id);
        return otherServiceItemRepository.save(item);
    }

    @DeleteMapping("/other-services/items/{itemId}")
    public void deleteOtherServiceItem(@PathVariable Long itemId) { otherServiceItemRepository.deleteById(itemId); }

    @GetMapping("/other-service-orders")
    public List<OtherServiceOrder> allOtherServiceOrders() { return otherServiceOrderRepository.findAll(); }

    // ---- QUICK SERVICES ----
    @GetMapping("/quick-services")
    public List<QuickService> adminQuickServices() {
        try { return quickServiceRepository.findAll(); } catch (RuntimeException ex) { return List.of(); }
    }

    @PostMapping("/quick-services")
    public QuickService saveQuickService(@RequestBody QuickService qs) {
        if (trimOrEmpty(qs.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        if (qs.getActive() == null) qs.setActive(true);
        if (qs.getSortOrder() == null) qs.setSortOrder(99);
        if (qs.getRating() == null) qs.setRating(4.5);
        return quickServiceRepository.save(qs);
    }

    @PutMapping("/quick-services/{id}")
    public QuickService updateQuickService(@PathVariable Long id, @RequestBody QuickService payload) {
        QuickService qs = quickServiceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (trimOrEmpty(payload.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        qs.setName(payload.getName()); qs.setDescription(payload.getDescription());
        qs.setPrice(payload.getPrice()); qs.setGradient(payload.getGradient());
        qs.setImageUrl(payload.getImageUrl()); qs.setTag(payload.getTag());
        qs.setBookings(payload.getBookings()); qs.setRating(payload.getRating());
        qs.setActive(payload.getActive() == null || payload.getActive());
        qs.setSortOrder(payload.getSortOrder() == null ? 99 : payload.getSortOrder());
        return quickServiceRepository.save(qs);
    }

    @DeleteMapping("/quick-services/{id}")
    public void deleteQuickService(@PathVariable Long id) { quickServiceRepository.deleteById(id); }

    // ---- COUPONS ----
    @GetMapping("/coupons")
    public List<Coupon> adminCoupons() { return couponRepository.findAll(); }

    @PostMapping("/coupons")
    public Coupon saveCoupon(@RequestBody Coupon coupon) { return couponRepository.save(coupon); }

    @PutMapping("/coupons/{id}")
    public Coupon updateCoupon(@PathVariable Long id, @RequestBody Coupon payload) {
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setCode(payload.getCode()); c.setMessage(payload.getMessage()); c.setActive(payload.getActive());
        return couponRepository.save(c);
    }

    @DeleteMapping("/coupons/{id}")
    public void deleteCoupon(@PathVariable Long id) { couponRepository.deleteById(id); }

    // ---- WORKERS ----
    @GetMapping("/worker-applications")
    public List<WorkerApplication> workerApplications() { return workerApplicationRepository.findAll(); }

    @PostMapping("/worker-applications/{id}/approve")
    public WorkerApplication approveWorker(@PathVariable Long id) {
        WorkerApplication a = workerApplicationRepository.findById(id).orElseThrow();
        a.setStatus("APPROVED");
        User user = userRepository.findById(a.getUserId()).orElseThrow();
        user.setRole(UserRole.WORKER);
        userRepository.save(user);
        return workerApplicationRepository.save(a);
    }

    @PostMapping("/worker-applications/{id}/reject")
    public WorkerApplication rejectWorker(@PathVariable Long id) {
        WorkerApplication a = workerApplicationRepository.findById(id).orElseThrow();
        a.setStatus("REJECTED");
        return workerApplicationRepository.save(a);
    }

    @GetMapping("/cases")
    public List<ServiceCase> allCases() { return serviceCaseRepository.findAll(); }

    @GetMapping("/users/workers")
    public List<User> allWorkers() {
        return userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.WORKER).toList();
    }

    @PutMapping("/users/workers/{id}")
    public User updateWorker(@PathVariable Long id, @RequestBody User payload) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (u.getRole() != UserRole.WORKER) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a worker");
        u.setName(payload.getName()); u.setMobile(payload.getMobile());
        return userRepository.save(u);
    }

    @DeleteMapping("/users/workers/{id}")
    public void deleteWorker(@PathVariable Long id) { userRepository.deleteById(id); }

    // ---- OUR SERVICE CARDS ----
    @GetMapping("/our-services")
    public List<OurServiceCard> adminOurServices() {
        try { return ourServiceCardRepository.findAll(); } catch (RuntimeException ex) { return List.of(); }
    }

    @PostMapping("/our-services")
    public OurServiceCard saveOurService(@RequestBody OurServiceCard card) {
        if (trimOrEmpty(card.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        if (card.getActive() == null) card.setActive(true);
        if (card.getSortOrder() == null) card.setSortOrder(99);
        return ourServiceCardRepository.save(card);
    }

    @PutMapping("/our-services/{id}")
    public OurServiceCard updateOurService(@PathVariable Long id, @RequestBody OurServiceCard payload) {
        OurServiceCard card = ourServiceCardRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (trimOrEmpty(payload.getName()).isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        card.setName(payload.getName());
        card.setDescription(payload.getDescription());
        card.setPrice(payload.getPrice());
        card.setGradient(payload.getGradient());
        card.setImageUrl(payload.getImageUrl());
        card.setActive(payload.getActive() == null || payload.getActive());
        card.setSortOrder(payload.getSortOrder() == null ? 99 : payload.getSortOrder());
        return ourServiceCardRepository.save(card);
    }

    @DeleteMapping("/our-services/{id}")
    public void deleteOurService(@PathVariable Long id) { ourServiceCardRepository.deleteById(id); }
}
