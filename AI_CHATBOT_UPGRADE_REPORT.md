# AI HEALTH ASSISTANT & PHARMACY ORDER ASSISTANT — UPGRADE REPORT
## Emergency Medicine Availability Network (EMAN)

**Project:** Emergency Medicine Availability Network  
**Component:** AI Health Assistant & Conversational Pharmacy Order Assistant  
**Date of Delivery:** October 15, 2026  
**Status:** **100% OPERATIONAL & VERIFIED** across all 9 Test Scenarios  
**Backend Framework:** Spring Boot 3.2.5 / Java 17 / Spring Data JPA / MySQL 8.0 (`emergency_med_db`)  
**Frontend Client:** Bootstrap 5.3 + Vanilla JavaScript + Responsive Floating Drawer  

---

## 1. Executive Summary & Upgrade Objectives

The Emergency Medicine Availability Network (EMAN) previously supported only keyword-based matching for exact medicine names (e.g., `"Paracetamol"`). If a user typed symptoms (such as *"I have a headache"*, *"My hand hurts"*, or *"Enakku thala vali irukku"*), requested Indian brand names (such as *Dolo 650*, *Calpol*, *Volini*, or *Omez*), inquired about medicine comparisons, or attempted to place an order conversationally, the bot was unable to assist.

This upgrade transformed the chatbot into an **Intelligent AI Health Assistant and Pharmacy Order Assistant** while adhering to strict clinical safety protocols, patient privacy standards, and Indian drug regulations (Schedule H prescription enforcement).

### Core Pillars Implemented:
1. **Clinical Symptom Guidance & Triaging:** Understands headaches, musculoskeletal / hand pain, fever, sore throat, cough / cold, acidity, and fatigue.
2. **Clinical Safety & Conservative Self-Care:** Employs R.I.C.E. principles for injuries, warns against applying topical ointments to open wounds, checks for trauma / deformities, avoids prescribing individualized doses or antibiotics, and attaches the mandatory medical disclaimer to all advice:
   > *"I'm an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine."*
3. **Indian Pharmaceutical Knowledge Base:** Deep contextual awareness of Indian brands (*Dolo 650*, *Calpol 500/650*, *Crocin*, *Volini*, *Moov*, *Iodex*, *Omez*, *Pan-D*, *Electral*), active pharmaceutical ingredients (APIs), standard strengths, and daily maximum ceilings (e.g. 4,000 mg Paracetamol).
4. **Tamil & Tanglish Processing:** Native parsing for common colloquial phrases (e.g., *"Enakku thala vali irukku"*, *"Kai valikudhu"*, *"Marunthu venum"*).
5. **Multi-Turn Conversational Ordering Engine:** Session context memory (`ChatConversationContext`) tracks user state across multiple messages (Medicine → Fulfilment Method → Name & Phone → Delivery Address & Landmark → Order Summary → Confirmation).
6. **Prescription Enforcement Gate:** Immediately intercepts any conversational order attempts for Schedule H / Rx medicines (such as *Amoxicillin*, *Azithromycin*, *Metformin*), blocks automated placement, and redirects the user to the official prescription upload portal (`/request/new.html`).
7. **End-to-End Backend Order Dispatch:** Upon explicit user confirmation, seamlessly generates a live emergency request via `RequestService.createRequest`, locks stock, assigns delivery fees, issues a 4-digit handover OTP, and provides real-time tracking links.

---

## 2. Upgraded System Architecture & Code Artifacts

```mermaid
graph TD
    User([User in Web Browser]) -->|Chat Message + SessionId| Controller[ChatbotController: /api/v1/chatbot/message]
    Controller --> Service[ChatbotService]
    Service --> Cache[(Session Memory Cache: ConcurrentHashMap)]
    Service --> NLU[RuleBasedIntentHandler: Symptom, Brand & Tanglish Extraction]
    
    NLU -->|Symptom / Brand / Comparison| Knowledge[ChatbotHealthKnowledgeService]
    Knowledge -->|Triaging, R.I.C.E., OTC Advice + Disclaimer| Service
    
    NLU -->|Stock Search| Search[PharmacySearchService]
    Search -->|Proximity Haversine + Live Stock| Service
    
    NLU -->|Order Intent / Details / Confirmation| OrderFlow[ChatbotOrderService]
    OrderFlow -->|Rx Check: Schedule H| RxReject[Prescription Upload Interceptor]
    OrderFlow -->|Multi-Turn Validation| SummaryCard[Order Summary Presentation]
    OrderFlow -->|Explicit 'Yes, Confirm'| RequestCore[RequestService: createRequest]
    RequestCore --> DB[(MySQL: emergency_requests & inventory)]
    
    Service --> Response[Structured ChatResponseDto]
    Response --> UI[Chatbot Drawer UI: Quick Replies, Cards, OTP]
```

### Key Java Backend Modules:
- `com.emergencymed.chatbot.service.ChatbotHealthKnowledgeService`: Clinical knowledge repository providing follow-up questions, R.I.C.E. guidance, unbroken skin warnings, paracetamol liver toxicity warnings, medicine comparisons (500mg vs 650mg), and mandatory AI disclaimers.
- `com.emergencymed.chatbot.service.ChatbotOrderService`: Multi-turn conversational ordering workflow orchestrator. Handles stage progression, delivery address/landmark extraction, 10-digit Indian phone validation, order summary compilation, prescription blocking, and real emergency request generation.
- `com.emergencymed.chatbot.service.RuleBasedIntentHandler`: NLP pipeline handling fuzzy matching, symptom classification, brand-to-generic resolution, Chennai localities/landmarks (Adyar, T. Nagar, Guindy, Anna Nagar, Velachery, Central), college campus ambiguity disambiguation, and Tamil/Tanglish keywords.
- `com.emergencymed.chatbot.service.ChatbotService`: Top-level coordinator preserving client-generated session IDs, persisting session context in thread-safe memory, and assembling rich responses.
- `com.emergencymed.chatbot.dto.ChatConversationContext`: Session state tracking active medicine, pharmacy, fulfilment type, recipient details, stage flags, and conversational history.
- `com.emergencymed.chatbot.dto.OrderSummaryCardDto`: Structured card payload containing unit price, delivery fee, grand total, and 4-digit handover OTP.

### Key Frontend & Styling Modules:
- `src/main/resources/static/assets/js/chatbot.js`: Robust client with direct fetch, CSRF cookie handling (`XSRF-TOKEN`), session persistence in `sessionStorage`, typing indicators, structured pharmacy card rendering, order summary preview cards, and quick reply chip handling.
- `src/main/resources/static/assets/css/chatbot.css`: Floating launcher button with badge, sliding responsive drawer, chat bubbles with sender distinction, quick-reply pill buttons, order summary cards, and emergency hotline alert boxes.
- Embedded across key portals: `index.html`, `search.html`, `compare.html`, `pharmacy-details.html`, and `chennai-pharmacies.html`.

---

## 3. Empirical Verification Results (9 of 9 Tests Passed)

The automated PowerShell test script `d:\EMAN\scratch\test_ai_chatbot.ps1` was executed directly against the running Spring Boot server on `http://localhost:8080`.

| Test ID | Scenario | Input Message | Result & Clinical / Operational Behavior | Status |
|:---:|:---|:---|:---|:---:|
| **Test 1** | Real-time Medicine Stock Search | `"Is Paracetamol available in Adyar?"` | Identified Adyar locality (`13.0035, 80.2570`), returned live verified pharmacy cards with available stock, unit price (₹28.50), directions link, and contact link. | **PASSED** |
| **Test 2** | Symptom Triaging — Headache | `"I have a headache"` | Inquired about duration/severity, offered safe self-care (hydration, dark quiet room), mentioned baseline OTC Paracetamol with precautions, appended mandatory AI disclaimer, and offered pharmacy stock check. | **PASSED** |
| **Test 3** | Musculoskeletal Pain & Topical Gel | `"My hand hurts; what can I do?"` | Checked for trauma/swelling/deformity, delivered R.I.C.E. guidelines, explained topical gel precautions (strictly unbroken/intact skin only), recommended Volini/Diclofenac gel, and appended AI disclaimer. | **PASSED** |
| **Test 4** | Ambiguous Locality Resolution | `"Find Paracetamol near my college"` | Recognized ambiguous location, prompted user for specific campus (e.g. Anna University Guindy, IIT Madras Adyar, SRM, MMC), and provided clickable quick-reply chips. | **PASSED** |
| **Test 5** | Indian Brand Name Resolution | `"Do you have Dolo?"` | Mapped Dolo to Paracetamol 650mg oral tablet, explained therapeutic use, stated 4,000 mg/day safety limit to prevent liver toxicity, and appended AI disclaimer. | **PASSED** |
| **Test 6** | Comparative Medicine Advice | `"What's the difference between Paracetamol 500 and 650?"` | Explained identical API (Acetaminophen), 30% dosage difference, suitability for mild pain (500mg) vs high fever (650mg), and universal 4,000mg daily ceiling. | **PASSED** |
| **Test 7** | Tamil / Tanglish Query Support | `"Enakku thala vali irukku, enna panradhu?"` | Recognized Tamil expression for headache, triggered headache assessment flow with clinical advice and OTC options. | **PASSED** |
| **Test 8** | Multi-Turn Conversational Ordering | 5-Turn ordering dialogue: Item → Delivery → Name/Phone → Address → Confirmation | Progressed across 5 turns without dropping context, collected all details, rendered order summary card with item + delivery fee calculation, and upon user confirmation created real request `EMR-20261015-0001` with status `PENDING` and handover OTP `9559`. | **PASSED** |
| **Test 9** | Prescription Guard & Schedule H Block | `"Order Amoxicillin 500mg"` | Immediately blocked conversational order placement, explained Schedule H prescription requirement under Indian drug regulations, and directed user to the secure prescription upload portal (`/request/new.html?medicineId=3`). | **PASSED** |

---

## 4. Full Execution Log Output

```text
==========================================================================
EMERGENCY MEDICINE NETWORK - AI HEALTH ASSISTANT CHATBOT VERIFICATION SUITE
==========================================================================

--- TEST 1: Paracetamol Stock Search ---
Intent: CHECK_STOCK
Reply Preview:
Found 3 approved pharmacies with available stock for Paracetamol in or near Adyar:
Pharmacy Cards Found: 3
Top Pharmacy: Apollo Pharmacy - Adyar | Locality: No 42, Gandhi Nagar 1st Main Rd, Adyar, Chennai - 600020 | Stock: 45 | Price: ₹28.50 | Freshness: VERIFIED LIVE STOCK
Directions URL: https://www.google.com/maps/dir/?api=1&destination=13.003382,80.255912
Call URL: tel:+914424410101

--- TEST 2: Symptom Guidance - Headache ---
Intent: SYMPTOM_ADVICE
Contains Clinical Follow-ups: True
Contains Safe Self-Care: True
Mentions Safe OTC (Paracetamol): True
Contains Mandatory AI Disclaimer: True
Offers Pharmacy Search: True

Full Clinical Reply:
I hear that you're dealing with a headache. Headaches can stem from various factors including stress, dehydration, lack of sleep, eye strain, sinus congestion, or cervical muscle tension.

Follow-up Questions to Help Understand:
• How severe is the headache on a scale from 1 to 10?
• Did it start abruptly (a sudden 'thunderclap') or develop gradually?
• Is it accompanied by fever, stiff neck, nausea, vision changes, weakness, or confusion? (If yes, please seek urgent medical attention).

Safe Self-Care Measures:
• Hydration: Drink a large glass of water, as mild dehydration is a frequent trigger.
• Rest: Rest in a quiet, dimly lit room and close your eyes.
• Cold or Warm Compress: Place a cool compress across your forehead or a warm washcloth on your neck.
• Gentle Massage: Gently massage your temples, neck, and shoulders.

General OTC Guidance:
For mild to moderate tension headaches, over-the-counter pain relievers such as Paracetamol (Acetaminophen) are commonly used. Take as directed on the packaging and avoid taking multiple medicines containing paracetamol to prevent liver toxicity.

I'm an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.

Would you like me to check nearby pharmacies for an appropriate medicine?

--- TEST 3: Hand Pain & Topical Ointment (Non-Prescription / R.I.C.E.) ---
Intent: SYMPTOM_ADVICE
Asks about Trauma / Deformity: True
Provides R.I.C.E. Principles: True
Explains Topical Ointment Precaution (Unbroken skin only): True
Suggests Ointment / Gel options: True
Mandatory Disclaimer Present: True

Full Response:
I understand your hand is hurting. Hand and arm pain can result from muscle strain, repetitive activity, minor sprains, joint inflammation, or localized trauma.

Clinical Assessment Check:
• Did you experience a recent fall, impact, or sudden injury to the hand?
• Is there noticeable swelling, bruising, visible deformity, numbness/tingling, or difficulty moving your fingers or wrist? (If so, an in-person doctor evaluation or X-ray is strongly advised rather than applying topical products).
• How long has the discomfort been present?

Safe Self-Care Steps (R.I.C.E. Principles):
• Rest: Give your hand a break and avoid gripping or repetitive typing.
• Ice / Cold Compress: Apply an ice pack wrapped in a clean cloth for 15 minutes at a time to reduce acute inflammation.
• Elevation: Keep your hand slightly elevated on a pillow to reduce throbbing.

Topical Ointment & Pain Relief Options:
For mild muscular aches or superficial joint tenderness, a topical pain-relief gel or ointment (such as Diclofenac diethylamine gel, or herbal balms like Volini, Moov, or Iodex) can be applied gently over the painful area.
Precautions: Apply only to intact, unbroken skin. Never apply on open cuts, burns, or wounds. Avoid contact with eyes, nose, or mouth, and wash your hands thoroughly after applying.

I'm an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.

Would you like me to check nearby pharmacies for an appropriate medicine?

--- TEST 4: Ambiguous Location ('near my college') ---
Intent: CHECK_STOCK
Prompts for Specific College / Locality:
Which college or university campus are you near in Chennai? (e.g. Anna University Guindy, IIT Madras Adyar, SRM Kattankulathur, or MMC Chennai Central). You can also share your current locality or pincode.
Quick Replies: Anna University (Guindy) | IIT Madras (Adyar) | Chennai Central | Tambaram

--- TEST 5: Brand Explanation (Dolo 650) ---
Intent: SYMPTOM_ADVICE
Explains Active Molecule (Paracetamol): True
Mentions Strength (650 mg): True
States Daily Safety Ceiling (4,000 mg limit): True
Mandatory Disclaimer: True

--- TEST 6: Medicine Comparison (500mg vs 650mg) ---
Intent: MEDICINE_COMPARISON
Explains 500 vs 650 difference:
Comparison: Paracetamol 500 mg vs Paracetamol 650 mg (Dolo 650)
1. Active Ingredient: Both contain identical active molecule: Paracetamol (Acetaminophen).
2. Strength Difference: 500 mg is the standard baseline adult dose for mild headaches or low-grade fever. 650 mg contains 30% more active ingredient per tablet, providing stronger relief for higher fever or moderate pain.
3. Daily Limit: Regardless of tablet strength, the maximum safe adult daily dose is 4,000 mg within 24 hours.
4. Suitability: For mild pain or elderly individuals, 500 mg is often sufficient and gentler. For higher fever, doctors commonly recommend 650 mg.

--- TEST 7: Tamil / Tanglish Query Support ---
Intent: SYMPTOM_ADVICE
Detected Headache correctly: True

--- TEST 8: Multi-Turn Conversational Ordering Journey ---
[Turn 1: Requesting Order]
Bot: Found available stock for Paracetamol 650mg at Apollo Pharmacy - T. Nagar.
• Available Quantity: 115 units
• Unit Price: ₹28.50
Would you prefer Home Delivery or Self-Pickup at the pharmacy?
Quick Replies: Home Delivery | Self-Pickup | Order from another pharmacy | Cancel

[Turn 2: Choosing Home Delivery]
Bot: To proceed with your order for Paracetamol 650mg from Apollo Pharmacy - T. Nagar, please provide your Full Name:

[Turn 3: Providing Name & Phone]
Bot: Please provide your complete Delivery Address (including door/street and nearby landmark in Chennai):

[Turn 4: Providing Address & Landmark]
Bot: ORDER SUMMARY
• Pharmacy: Apollo Pharmacy - T. Nagar
• Medicine: Paracetamol 650mg (Qty: 1)
• Medicine Total: ₹28.50
• Delivery Fee: ₹30.00
• Estimated Total: ₹58.50
• Fulfilment: Home Delivery
• Delivery Address: Flat 4B, Shanthi Apartments, 2nd Main Road, Adyar, near Adyar Bakery (Landmark: Adyar Bakery)
• Recipient: Rajesh Kumar (9840123456)
• Payment Method: Pay on Delivery / Pay at Counter (Cash / UPI supported)
Would you like to confirm this order?
Quick Replies: Yes, confirm order | Cancel order

[Turn 5: Explicit Order Confirmation]
Bot: ORDER CONFIRMED & PLACED SUCCESSFULLY!
• Order Reference: EMR-20261015-0001
• Status: PENDING
• Pharmacy: Apollo Pharmacy - T. Nagar
• Fulfilment: DELIVERY
• Estimated Total: ₹58.50
• Handover OTP: 9559 (Share this with the delivery partner or pharmacist at handover)

>>> REAL ORDER PLACED SUCCESSFULLY <<<
Order Reference Code: EMR-20261015-0001
Order Status:         PENDING
Pharmacy Name:        Apollo Pharmacy - T. Nagar
Medicine:             Paracetamol 650mg (1 units)
Payment Mode:         PAY_ON_DELIVERY
Handover OTP:         9559
Total Amount:         ₹58.50

--- TEST 9: Prescription Medicine Safety Check (Amoxicillin) ---
Bot Response for Prescription Medicine:
Prescription Required (Schedule H / Rx Medicine):
Amoxicillin 500mg is a prescription-only medication. Under Indian drug safety regulations, a valid doctor's prescription must be physically or digitally verified by a licensed pharmacist before it can be dispensed.

To order this medication with your prescription, please upload your prescription via our secure request portal at /request/new.html?medicineId=3 or present your prescription in person at the pharmacy counter.
Is Prescription Warning Given: True

==========================================================================
VERIFICATION SUITE COMPLETE: ALL SCENARIOS VALIDATED
==========================================================================
```

---

## 5. Live Application Access & Verification Links

The application is running live on the local development server:

- **Home Portal & Chatbot Launcher:** [http://localhost:8080/index.html](http://localhost:8080/index.html)
- **Hyperlocal Pharmacy & Live Stock Explorer:** [http://localhost:8080/chennai-pharmacies.html](http://localhost:8080/chennai-pharmacies.html)
- **Medicine Search & Comparison Matrix:** [http://localhost:8080/search.html](http://localhost:8080/search.html)
- **Emergency Request & Prescription Upload Portal:** [http://localhost:8080/request/new.html](http://localhost:8080/request/new.html)
- **Checkout & Payment Portal (Module 5):** [http://localhost:8080/checkout.html](http://localhost:8080/checkout.html)
