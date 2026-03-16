package com.example.demo.controller;

import com.example.demo.model.OurServiceCard;
import com.example.demo.model.QuickService;
import com.example.demo.repository.OurServiceCardRepository;
import com.example.demo.repository.QuickServiceRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
@RequiredArgsConstructor
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final OurServiceCardRepository ourServiceCardRepository;
    private final QuickServiceRepository quickServiceRepository;

    @Getter
    @Setter
    public static class ChatRequest {
        private String message;
        private String language;
        private List<Map<String, String>> history;
    }

    @Getter
    @Setter
    public static class ChatResponse {
        private String reply;
        private String language;
        private String bookingAction;   // null or "OPEN_BOOKING"
        private Long bookingServiceId;
        private String bookingServiceName;

        public ChatResponse(String reply, String language) {
            this.reply = reply;
            this.language = language;
        }
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String msg = req.getMessage() == null ? "" : req.getMessage().toLowerCase().trim();
        String lang = req.getLanguage() == null ? "en" : req.getLanguage();
        log.info("AI chat request: lang={}, message={}", lang, req.getMessage());

        // Check booking intent first
        BookingIntent intent = detectBookingIntent(msg);
        if (intent != null) {
            ChatResponse resp = new ChatResponse(buildBookingIntentReply(intent, lang), lang);
            resp.setBookingAction("OPEN_BOOKING");
            resp.setBookingServiceId(intent.serviceId);
            resp.setBookingServiceName(intent.serviceName);
            return resp;
        }

        return new ChatResponse(generateReply(msg, lang), lang);
    }

    private static class BookingIntent {
        Long serviceId;
        String serviceName;
        BookingIntent(Long id, String name) { this.serviceId = id; this.serviceName = name; }
    }

    private BookingIntent detectBookingIntent(String msg) {
        boolean hasBookKeyword = containsAny(msg,
                "book", "booking", "schedule", "hire", "need", "want", "get",
                "బుక్", "బుకింగ్", "కావాలి", "పిలవండి");
        if (!hasBookKeyword) return null;

        // Dynamic lookup from DB first
        List<OurServiceCard> cards = ourServiceCardRepository.findByActiveTrueOrderBySortOrderAsc();
        for (OurServiceCard card : cards) {
            if (msg.contains(card.getName().toLowerCase())) {
                return new BookingIntent(card.getId(), card.getName());
            }
        }
        List<QuickService> qs = quickServiceRepository.findByActiveTrueOrderBySortOrderAsc();
        for (QuickService q : qs) {
            if (msg.contains(q.getName().toLowerCase())) {
                return new BookingIntent(q.getId(), q.getName());
            }
        }

        // Fallback keyword matching
        if (containsAny(msg, "plumb", "pipe", "leak", "water", "ప్లంబర్"))
            return new BookingIntent(null, "Plumber");
        if (containsAny(msg, "electric", "wiring", "switch", "fan", "light", "ఎలక్ట్రీషియన్"))
            return new BookingIntent(null, "Electrician");
        if (containsAny(msg, "carpenter", "wood", "door", "furniture", "కార్పెంటర్"))
            return new BookingIntent(null, "Carpenter");
        if (containsAny(msg, "paint", "wall", "colour", "color", "పెయింటర్"))
            return new BookingIntent(null, "Painter");
        if (containsAny(msg, "mason", "brick", "plaster", "tile", "మేసన్"))
            return new BookingIntent(null, "Mason");
        if (containsAny(msg, "ac ", "air condition", "cooling", "ac repair"))
            return new BookingIntent(null, "AC Repair");

        return null;
    }

    private String buildBookingIntentReply(BookingIntent intent, String lang) {
        boolean te = "te".equals(lang);
        String name = intent.serviceName != null ? intent.serviceName : "service";
        return te
            ? name + " బుకింగ్ తెరుస్తున్నాం... దయచేసి వివరాలు నమోదు చేయండి."
            : "Opening " + name + " booking for you! Please fill in your details.";
    }

    private String generateReply(String msg, String lang) {
        boolean te = "te".equals(lang);

        if (containsAny(msg, "book", "booking", "schedule", "appointment", "బుక్", "బుకింగ్")) {
            return te
                ? "సేవ బుక్ చేయడానికి: హోమ్ పేజ్ నుండి సేవ ఎంచుకోండి → 'బుక్ చేయండి' నొక్కండి → వివరాలు నమోదు చేయండి → నిర్ధారించండి. మీకు 30 నిమిషాల్లో కన్ఫర్మేషన్ కాల్ వస్తుంది."
                : "To book a service: Go to Home → Select a service → Tap 'Book' → Fill details → Confirm. You'll get a confirmation call within 30 minutes. Make sure you're logged in.";
        }
        if (containsAny(msg, "plumb", "pipe", "leak", "water", "tap", "toilet", "ప్లంబర్", "పైప్", "నీళ్ళు")) {
            return te
                ? "ప్లంబర్ సేవ ₹400 నుండి మొదలవుతుంది. లీకేజ్ విషయంలో మెయిన్ వాల్వ్ బంద్ చేయండి. హోమ్‌లో 'ప్లంబర్ బుక్ చేయండి' అని అడగండి."
                : "Plumber service starts from ₹400. For urgent leaks, turn off your main water valve. Say 'Book plumber' and I'll open the booking for you!";
        }
        if (containsAny(msg, "electric", "wiring", "switch", "power", "fan", "light", "ఎలక్ట్రీషియన్", "కరెంట్")) {
            return te
                ? "ఎలక్ట్రీషియన్ సేవ ₹350 నుండి. వైరింగ్ పనులు మీరే ప్రయత్నించకండి. 'ఎలక్ట్రీషియన్ బుక్ చేయండి' అని అడగండి."
                : "Electrician service from ₹350. Never attempt wiring yourself. Say 'Book electrician' and I'll open the booking!";
        }
        if (containsAny(msg, "carpenter", "wood", "door", "furniture", "కార్పెంటర్")) {
            return te
                ? "కార్పెంటర్ సేవ ₹500 నుండి. తలుపు, ఫర్నిచర్, షెల్ఫ్ పనులు చేస్తాం."
                : "Carpenter service from ₹500. Door repairs, furniture work, shelf fixing. Say 'Book carpenter' to proceed!";
        }
        if (containsAny(msg, "paint", "wall", "colour", "color", "పెయింటర్")) {
            return te
                ? "పెయింటింగ్ సేవ ₹700 నుండి. ఇంటీరియర్, ఎక్స్‌టీరియర్ రెండూ. 'పెయింటర్ బుక్' అని అడగండి."
                : "Painting service from ₹700. Interior & exterior both available. Say 'Book painter' to start!";
        }
        if (containsAny(msg, "wallet", "cashback", "balance", "withdraw", "వాలెట్", "క్యాష్‌బ్యాక్")) {
            return te
                ? "ప్రతి బుకింగ్‌పై 10% క్యాష్‌బ్యాక్. ₹500+ అయిన తర్వాత విత్‌డ్రా చేయవచ్చు. ప్రొఫైల్ → వాలెట్."
                : "Earn 10% cashback on every booking. Withdraw when balance reaches ₹500+. Go to Profile → Wallet.";
        }
        if (containsAny(msg, "address", "location", "చిరునామా")) {
            return te
                ? "ప్రొఫైల్ → చిరునామాలు నుండి జోడించవచ్చు, సవరించవచ్చు."
                : "Go to Profile → Addresses to add or edit your addresses.";
        }
        if (containsAny(msg, "price", "cost", "charge", "how much", "ధర", "ఎంత")) {
            return te
                ? "సేవల ధరలు:\n• ప్లంబర్: ₹400+\n• ఎలక్ట్రీషియన్: ₹350+\n• కార్పెంటర్: ₹500+\n• మేసన్: ₹600+\n• పెయింటర్: ₹700+"
                : "Service rates:\n• Plumber: ₹400+\n• Electrician: ₹350+\n• Carpenter: ₹500+\n• Mason: ₹600+\n• Painter: ₹700+";
        }
        if (containsAny(msg, "cancel", "refund", "రద్దు")) {
            return te
                ? "బుకింగ్ రద్దు కోసం ప్రొఫైల్ → నా ఆర్డర్లు చూడండి లేదా WhatsApp ద్వారా సంప్రదించండి."
                : "To cancel, go to Profile → My Orders or reach us on WhatsApp support.";
        }
        if (containsAny(msg, "coupon", "discount", "offer", "కూపన్")) {
            return te
                ? "ప్రొఫైల్ → కూపన్లు నుండి అందుబాటులో ఉన్న ఆఫర్లు చూడండి."
                : "Check Profile → Coupons for available offers and discounts!";
        }
        if (containsAny(msg, "worker", "job", "professional", "పని", "కార్మికుడు")) {
            return te
                ? "కార్మికుడిగా చేరాలంటే: ప్రొఫైల్ → 'మాతో పని చేయండి' నొక్కండి."
                : "To join as a worker: Profile → 'Work with us' → Submit application.";
        }
        if (containsAny(msg, "login", "account", "register", "లాగిన్")) {
            return te
                ? "మొబైల్ + పాస్‌వర్డ్ తో లాగిన్ అవ్వండి. కొత్త అకౌంట్ కోసం 'నమోదు' ఎంచుకోండి."
                : "Login with mobile + password. For new account choose 'Sign Up'.";
        }
        return te
            ? "మీ ప్రశ్నకు ధన్యవాదాలు! సేవ బుక్ చేయాలంటే 'ప్లంబర్ బుక్ చేయండి' లాగా అడగండి లేదా హోమ్ పేజ్ చూడండి."
            : "Thanks! To book a service just say something like 'Book electrician' and I'll open it for you. Or browse the Home page!";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
