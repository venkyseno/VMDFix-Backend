package com.example.demo.controller;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI Chat Controller - provides intelligent responses for service-related queries.
 * Uses a rule-based/keyword approach for reliable on-device responses without
 * requiring an external LLM API key.
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    @Getter
    @Setter
    public static class ChatRequest {
        private String message;
        private String language; // "en" or "te"
        private List<Map<String, String>> history;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> chat(@RequestBody ChatRequest req) {
        String msg = req.getMessage() == null ? "" : req.getMessage().toLowerCase().trim();
        String lang = req.getLanguage() == null ? "en" : req.getLanguage();

        log.info("AI chat request: lang={}, message={}", lang, req.getMessage());

        String reply = generateReply(msg, lang);
        return Map.of("reply", reply, "language", lang);
    }

    private String generateReply(String msg, String lang) {
        boolean te = "te".equals(lang);

        // Booking guidance
        if (containsAny(msg, "book", "booking", "schedule", "appointment", "బుక్", "బుకింగ్")) {
            return te
                ? "సేవ బుక్ చేయడానికి: హోమ్ పేజ్ నుండి సేవ ఎంచుకోండి → 'బుక్ చేయండి' నొక్కండి → మీ సమస్య వివరించండి → నిర్ధారించండి. మీకు 30 నిమిషాల్లో కన్ఫర్మేషన్ కాల్ వస్తుంది. మీరు లాగిన్ అయి ఉండాలి."
                : "To book a service: Go to Home → Select a service → Tap 'Book' → Describe your issue → Confirm. You'll get a confirmation call within 30 minutes. Make sure you're logged in first.";
        }

        // Plumber
        if (containsAny(msg, "plumb", "pipe", "leak", "water", "tap", "bathroom", "toilet", "ప్లంబర్", "పైప్", "నీళ్ళు")) {
            return te
                ? "ప్లంబర్ సేవ కావాలంటే, హోమ్ పేజ్‌లో 'ప్లంబర్' కార్డ్ నొక్కండి. సేవ ధర ₹400 నుండి మొదలవుతుంది. లీకేజ్ విషయంలో, ప్లంబర్ వచ్చే వరకు మెయిన్ వాల్వ్ బంద్ చేయండి."
                : "For plumbing issues, tap the 'Plumber' card on the Home page. Service starts from ₹400. For urgent leaks, turn off your main water valve until the plumber arrives.";
        }

        // Electrician
        if (containsAny(msg, "electric", "wiring", "switch", "power", "fan", "light", "socket", "ఎలక్ట్రీషియన్", "కరెంట్", "వైరింగ్")) {
            return te
                ? "విద్యుత్ సమస్యలకు మా సర్టిఫైడ్ ఎలక్ట్రీషియన్లు అందుబాటులో ఉన్నారు. ₹350 నుండి మొదలవుతుంది. వైరింగ్ పనులు మీరే ప్రయత్నించకండి - నిపుణుడిని పిలవండి."
                : "For electrical issues, our certified electricians are available from ₹350. Never attempt to fix wiring yourself — always call a professional for safety.";
        }

        // Carpenter
        if (containsAny(msg, "carpenter", "wood", "door", "furniture", "shelf", "cabinet", "కార్పెంటర్", "తలుపు", "కర్ర")) {
            return te
                ? "కార్పెంటర్ సేవ ₹500 నుండి మొదలవుతుంది. తలుపు మరమ్మత్తు, ఫర్నిచర్ పని, షెల్ఫ్ ఫిక్సింగ్ అన్నీ చేస్తాం."
                : "Carpenter services start from ₹500. We handle door repairs, furniture work, shelf fixing, and custom carpentry needs.";
        }

        // Painter
        if (containsAny(msg, "paint", "wall", "colour", "color", "interior", "exterior", "పెయింటర్", "రంగు", "గోడ")) {
            return te
                ? "పెయింటింగ్ సేవ ₹700 నుండి మొదలవుతుంది. ఇంటీరియర్, ఎక్స్‌టీరియర్ రెండూ చేస్తాం. ఒక రూమ్ 2-3 గంటల్లో పూర్తవుతుంది."
                : "Painting services start from ₹700. We offer interior and exterior painting. Estimate: one room takes 2–3 hours. Get a free estimate before booking.";
        }

        // Wallet / cashback
        if (containsAny(msg, "wallet", "cashback", "money", "balance", "withdraw", "వాలెట్", "క్యాష్‌బ్యాక్", "బ్యాలెన్స్")) {
            return te
                ? "ప్రతి బుకింగ్ పూర్తయిన తర్వాత మీకు 10% క్యాష్‌బ్యాక్ వస్తుంది. ₹500 పై బ్యాలెన్స్ ఉంటే విత్‌డ్రా చేసుకోవచ్చు. ప్రొఫైల్ → వాలెట్ నుండి చూడవచ్చు."
                : "You earn 10% cashback on every completed booking. Once your balance reaches ₹500, you can withdraw it. Go to Profile → Wallet to check your balance.";
        }

        // Address
        if (containsAny(msg, "address", "location", "delivery", "primary", "చిరునామా", "లొకేషన్")) {
            return te
                ? "ప్రొఫైల్ → చిరునామాలు నుండి చిరునామాలు జోడించవచ్చు, సవరించవచ్చు, తొలగించవచ్చు. ఒక చిరునామాను ప్రాథమికంగా సెట్ చేయవచ్చు."
                : "You can add, edit, or delete addresses from Profile → Addresses. Set one as your primary address for quick bookings.";
        }

        // Cancel
        if (containsAny(msg, "cancel", "cancell", "refund", "రద్దు", "రీఫండ్")) {
            return te
                ? "బుకింగ్ రద్దు చేయడానికి ప్రొఫైల్ → నా ఆర్డర్లు వెళ్ళి ఆర్డర్ వివరాలు చూడండి. నిర్వాహకుడిని సంప్రదించండి. రద్దు విధానం సేవ ప్రారంభానికి ముందు వర్తిస్తుంది."
                : "To cancel a booking, go to Profile → My Orders and view order details. Contact our support team. Cancellations are allowed before service starts.";
        }

        // Price / cost
        if (containsAny(msg, "price", "cost", "charge", "rate", "fee", "how much", "ధర", "చార్జ్", "రేటు", "ఎంత")) {
            return te
                ? "మా సేవల ధరలు:\n• ప్లంబర్: ₹400+\n• ఎలక్ట్రీషియన్: ₹350+\n• కార్పెంటర్: ₹500+\n• మేసన్: ₹600+\n• పెయింటర్: ₹700+\n• ఇతర: ₹300+\nసేవా అవసరాన్ని బట్టి ధర మారవచ్చు."
                : "Our service rates:\n• Plumber: ₹400+\n• Electrician: ₹350+\n• Carpenter: ₹500+\n• Mason: ₹600+\n• Painter: ₹700+\n• Other: ₹300+\nFinal price depends on the scope of work.";
        }

        // Login / account
        if (containsAny(msg, "login", "account", "register", "sign up", "otp", "లాగిన్", "అకౌంట్", "నమోదు")) {
            return te
                ? "లాగిన్ పేజ్‌కు వెళ్ళి మొబైల్ నంబర్ + పాస్‌వర్డ్ తో లాగిన్ అవ్వండి. కొత్త అకౌంట్ కోసం 'నమోదు' ఎంచుకోండి. ఇమెయిల్, గూగుల్ లేదా OTP ద్వారా నమోదు చేయవచ్చు."
                : "Go to the Login page and enter your mobile number + password. For a new account, choose 'Sign Up'. You can register via Email, Google, or Mobile OTP.";
        }

        // Worker / job
        if (containsAny(msg, "worker", "job", "work", "professional", "technician", "కార్మికుడు", "పని", "టెక్నీషియన్")) {
            return te
                ? "కార్మికుడిగా మారాలంటే: ప్రొఫైల్ → 'మాతో పని చేయండి' నొక్కండి → దరఖాస్తు సమర్పించండి. నిర్వాహకుడు ఆమోదిస్తే మీ అకౌంట్ కార్మికుడిగా అప్‌గ్రేడ్ అవుతుంది."
                : "To become a worker: Go to Profile → Tap 'Work with us' → Submit your application with skill type and experience. Admin will review and approve within 24 hours.";
        }

        // coupon
        if (containsAny(msg, "coupon", "discount", "offer", "promo", "కూపన్", "తగ్గింపు", "ఆఫర్")) {
            return te
                ? "ప్రొఫైల్ → కూపన్లు నుండి అందుబాటులో ఉన్న ఆఫర్లు చూడండి. కూపన్ కోడ్ కాపీ చేసి బుకింగ్ సమయంలో వినియోగించండి."
                : "Check Profile → Coupons for available offers. Copy a coupon code and apply it during booking to get a discount.";
        }

        // Default
        return te
            ? "మీ ప్రశ్నకు ధన్యవాదాలు! మా సేవల గురించి మరిన్ని వివరాలకు హోమ్ పేజ్ చూడండి. లేదా ఇలా అడగండి: 'ప్లంబర్ బుక్ చేయడం ఎలా', 'ధర ఎంత', 'చిరునామా ఎలా జోడించాలి'."
            : "Thanks for reaching out! For more info about our services, check the Home page. You can also ask me things like: 'How do I book a plumber?', 'What are the charges?', or 'How do I add an address?'.";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
