package com.example.demo.controller;

import com.example.demo.model.Banner;
import com.example.demo.model.Coupon;
import com.example.demo.model.OtherService;
import com.example.demo.model.OtherServiceItem;
import com.example.demo.model.OurServiceCard;
import com.example.demo.model.QuickService;
import com.example.demo.repository.BannerRepository;
import com.example.demo.repository.CouponRepository;
import com.example.demo.repository.OtherServiceItemRepository;
import com.example.demo.repository.OtherServiceRepository;
import com.example.demo.repository.OurServiceCardRepository;
import com.example.demo.repository.QuickServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@CrossOrigin
@RequiredArgsConstructor
public class AppConfigController {
    private final BannerRepository bannerRepository;
    private final OtherServiceRepository otherServiceRepository;
    private final OtherServiceItemRepository otherServiceItemRepository;
    private final CouponRepository couponRepository;
    private final QuickServiceRepository quickServiceRepository;
    private final OurServiceCardRepository ourServiceCardRepository;

    @GetMapping("/banners")
    public List<Banner> getBanners() {
        return bannerRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @GetMapping("/other-services")
    public List<OtherService> getOtherServices() {
        return otherServiceRepository.findByActiveTrue();
    }

    @GetMapping("/other-services/{id}/items")
    public List<OtherServiceItem> getOtherServiceItems(@PathVariable Long id) {
        if (!otherServiceRepository.existsById(id)) return List.of();
        try { return otherServiceItemRepository.findByOtherServiceId(id); }
        catch (RuntimeException ex) { return List.of(); }
    }

    @GetMapping("/other-services/{id}")
    public OtherService getOtherServiceById(@PathVariable Long id) {
        return otherServiceRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Service not found: " + id));
    }

    @GetMapping("/coupons")
    public List<Coupon> getCoupons() {
        return couponRepository.findByActiveTrue();
    }

    @GetMapping("/quick-services")
    public List<QuickService> getQuickServices() {
        return quickServiceRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @GetMapping("/our-services")
    public List<OurServiceCard> getOurServices() {
        try { return ourServiceCardRepository.findByActiveTrueOrderBySortOrderAsc(); }
        catch (RuntimeException ex) { return List.of(); }
    }
}
