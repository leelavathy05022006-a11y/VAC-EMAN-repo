package com.emergencymed.chatbot.service;

import com.emergencymed.chatbot.dto.ChatbotMessageResponse;
import com.emergencymed.chatbot.dto.ExtractedQueryEntities;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotHealthKnowledgeService {

    public static final String HEALTH_DISCLAIMER =
            "I'm an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.";

    public static final String CHECK_PHARMACIES_OFFER =
            "Would you like me to check nearby pharmacies for an appropriate medicine?";

    /**
     * Generates a clinically sound, conversational response for reported symptoms.
     */
    public void generateSymptomGuidance(ExtractedQueryEntities entities, String cleanMessage, ChatbotMessageResponse response) {
        String symptom = entities.getSymptom();
        if (symptom == null) {
            symptom = "general";
        }

        StringBuilder sb = new StringBuilder();

        switch (symptom.toLowerCase()) {
            case "headache":
                sb.append("I understand you're experiencing a headache. Headaches can be triggered by stress, tension, dehydration, lack of sleep, eye strain from screens, or sinus congestion.\n\n");
                sb.append("📋 Important Follow-up Questions:\n");
                sb.append("• How long have you had this headache, and how intense is it (mild, moderate, or severe)?\n");
                sb.append("• Are you experiencing any red-flag warning signs such as a sudden 'thunderclap' onset, fever with neck stiffness, vision changes, confusion, or numbness? (If yes, please seek urgent medical care immediately).\n");
                sb.append("• Do you have any known drug allergies, or are you currently taking other medications?\n\n");
                sb.append("🌿 Safe Self-Care Suggestions:\n");
                sb.append("• Rest in a quiet, dimly lit, well-ventilated room.\n");
                sb.append("• Drink 1–2 glasses of water, as mild dehydration is one of the most common causes of headaches.\n");
                sb.append("• Apply a cool, damp washcloth across your forehead or temples to ease tension.\n\n");
                sb.append("💊 Over-the-Counter (OTC) Options:\n");
                sb.append("For mild to moderate tension headaches, **Paracetamol 500mg or 650mg (Dolo / Calpol)** is a widely used over-the-counter analgesic.\n");
                sb.append("*Precautions:* Do not exceed 4,000 mg in 24 hours. Never combine multiple paracetamol-containing products. If the headache persists beyond 2–3 days or worsens, please consult a physician.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;

            case "hand_pain":
            case "ointment":
                sb.append("I understand your hand is hurting. Hand and arm pain can result from muscle strain, repetitive activity, minor sprains, joint inflammation, or localized trauma.\n\n");
                sb.append("📋 Clinical Assessment Check:\n");
                sb.append("• Did you experience a recent fall, impact, or sudden injury to the hand?\n");
                sb.append("• Is there noticeable swelling, bruising, visible deformity, numbness/tingling, or difficulty moving your fingers or wrist? (If so, an in-person doctor evaluation or X-ray is strongly advised rather than applying topical products).\n");
                sb.append("• How long has the discomfort been present?\n\n");
                sb.append("🌿 Safe Self-Care Steps (R.I.C.E. Principles):\n");
                sb.append("• **Rest:** Give your hand a break and avoid gripping or repetitive typing.\n");
                sb.append("• **Ice / Cold Compress:** Apply an ice pack wrapped in a clean cloth for 15 minutes at a time to reduce acute inflammation.\n");
                sb.append("• **Elevation:** Keep your hand slightly elevated on a pillow to reduce throbbing.\n\n");
                sb.append("🧴 Topical Ointment & Pain Relief Options:\n");
                sb.append("For mild muscular aches or superficial joint tenderness, a topical pain-relief gel or ointment (such as **Diclofenac diethylamine gel**, or herbal balms like **Volini, Moov, or Iodex**) can be applied gently over the painful area.\n");
                sb.append("*Precautions:* Apply only to intact, unbroken skin. Never apply on open cuts, burns, or wounds. Avoid contact with eyes, nose, or mouth, and wash your hands thoroughly after applying.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;

            case "fever_throat":
            case "fever":
                sb.append("I hear that you have a fever and sore throat. These symptoms frequently indicate an upper respiratory viral or bacterial infection.\n\n");
                sb.append("📋 Important Follow-up Questions:\n");
                sb.append("• What is your body temperature, and for how many days have you had the fever?\n");
                sb.append("• Do you have difficulty swallowing saliva, breathing difficulty, or an unexplained rash? (These require immediate doctor assessment).\n");
                sb.append("• What is your age group, and are you pregnant or managing any chronic medical conditions?\n\n");
                sb.append("🌿 Safe Self-Care Measures:\n");
                sb.append("• Drink plenty of warm fluids (water, clear soups, warm herbal tea) to keep your throat moist and avoid dehydration.\n");
                sb.append("• Gargle with warm salt water (1/2 teaspoon of salt in a glass of warm water) 3 to 4 times a day to soothe throat inflammation.\n");
                sb.append("• Prioritize complete physical rest to allow your immune system to recover.\n\n");
                sb.append("💊 Over-the-Counter (OTC) Guidance:\n");
                sb.append("• **Paracetamol 500mg or 650mg** can help bring down body temperature and alleviate throat soreness.\n");
                sb.append("*Critical Medical Advice:* Antibiotics (such as Amoxicillin or Azithromycin) require a doctor's examination and prescription; they do not treat viral infections and must never be taken without a prescription. Seek emergency care if your fever exceeds 103°F (39.4°C) or lasts more than 3 days.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;

            case "cold":
            case "cough":
                sb.append("I understand you are dealing with a cold. Common colds typically cause a runny or stuffy nose, sneezing, scratchy throat, and mild cough.\n\n");
                sb.append("📋 Helpful Questions:\n");
                sb.append("• Is your cough dry or productive (producing phlegm)?\n");
                sb.append("• Are you experiencing chest tightness, wheezing, or shortness of breath?\n\n");
                sb.append("🌿 Safe Self-Care Guidance:\n");
                sb.append("• Steam inhalation helps clear congested nasal passages.\n");
                sb.append("• Drink warm fluids like warm ginger water or soups.\n");
                sb.append("• Saline nasal sprays or drops help loosen mucus naturally without medication interactions.\n\n");
                sb.append("💊 OTC Medicine Options:\n");
                sb.append("• For sneezing, allergic runny nose, and watery eyes: **Cetirizine 10mg** (a non-sedating antihistamine).\n");
                sb.append("• For a dry, tickly cough: **Dextromethorphan Syrup 100ml** (an OTC cough suppressant).\n");
                sb.append("*Precautions:* Antihistamines can cause drowsiness in some people; avoid driving or operating machinery after taking them.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;

            case "acidity":
            case "stomach_pain":
                sb.append("I understand you have stomach discomfort or acidity. This often presents as heartburn, a sour taste in the throat, or upper abdominal burning.\n\n");
                sb.append("📋 Follow-up Questions:\n");
                sb.append("• Is the pain a burning sensation in the upper chest/stomach related to meals, or a sharp localized pain?\n");
                sb.append("• Are you experiencing severe pain, vomiting, or black stools? (These are red-flag signs requiring immediate emergency medical evaluation).\n\n");
                sb.append("🌿 Safe Self-Care Tips:\n");
                sb.append("• Eat small, frequent meals rather than large, heavy portions.\n");
                sb.append("• Avoid oily, spicy, acidic foods, caffeine, and carbonated beverages.\n");
                sb.append("• Avoid lying down flat for at least 2 hours after eating.\n\n");
                sb.append("💊 OTC Options:\n");
                sb.append("Antacid liquid or tablets can provide rapid relief by neutralizing excess stomach acid. For recurrent reflux, medications like **Omeprazole 20mg** or **Pantoprazole 40mg** reduce acid secretion, but prescription guidance is recommended for ongoing use.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;

            default:
                sb.append("I understand you are experiencing discomfort. Health symptoms can vary widely based on duration, severity, and personal medical history.\n\n");
                sb.append("📋 Could you tell me a little more?\n");
                sb.append("• What specific symptoms are you feeling, and how long have they been present?\n");
                sb.append("• Is the discomfort mild, moderate, or severe?\n");
                sb.append("• Are you taking any regular medications or managing allergies?\n\n");
                sb.append("🌿 General Health Advice:\n");
                sb.append("• Ensure adequate hydration and physical rest.\n");
                sb.append("• If you develop red-flag symptoms such as chest pain, breathlessness, high fever, or severe weakness, seek emergency medical attention (Call 108 / 112) immediately.\n\n");
                sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
                sb.append(CHECK_PHARMACIES_OFFER);
                break;
        }

        response.setReplyText(sb.toString().trim());
        response.setQuickReplies(List.of(
                "Yes, check nearby pharmacies",
                "I have a headache",
                "My hand is hurting",
                "Do you have Dolo?",
                "Order this medicine"
        ));
    }

    /**
     * Explains brands (e.g. Dolo, Calpol, Volini, Omez) with exact generic equivalents, strength, and precautions.
     */
    public void generateBrandExplanation(String brandName, ChatbotMessageResponse response) {
        StringBuilder sb = new StringBuilder();
        String b = brandName.toLowerCase();

        if (b.contains("dolo") || b.contains("calpol") || b.contains("crocin") || b.contains("pacimol")) {
            sb.append("Yes, we have information on **Dolo / Calpol / Crocin**!\n\n");
            sb.append("• **Active Ingredient:** Paracetamol (Acetaminophen)\n");
            sb.append("• **Available Strengths:** Commonly 650 mg (e.g., Dolo 650) and 500 mg tablets\n");
            sb.append("• **Dosage Form:** Oral Tablet\n");
            sb.append("• **Category:** Analgesic (pain reliever) & Antipyretic (fever reducer)\n");
            sb.append("• **Intended Use:** Temporary relief of mild-to-moderate pain (headache, body aches) and reduction of fever.\n\n");
            sb.append("⚠️ *Crucial Safety Precautions:*\n");
            sb.append("1. Do not exceed 4,000 mg of Paracetamol in a 24-hour period.\n");
            sb.append("2. Do not consume multiple medicines containing paracetamol simultaneously to avoid liver toxicity.\n");
            sb.append("3. For persistent high fever (>3 days) or unexplained pain, consult a healthcare provider.\n\n");
        } else if (b.contains("volini") || b.contains("moov") || b.contains("iodex") || b.contains("omnigel")) {
            sb.append("Yes, we have information on **Volini / Moov / Iodex**!\n\n");
            sb.append("• **Product Type:** Topical Pain Relief Gel / Spray / Ointment\n");
            sb.append("• **Active Ingredients:** Typically contains Diclofenac Diethylamine, Methyl Salicylate, and Menthol, or herbal anti-inflammatory oils.\n");
            sb.append("• **Intended Use:** Localized relief for muscular strain, neck/shoulder stiffness, sprains, and joint discomfort.\n\n");
            sb.append("⚠️ *Crucial Safety Precautions:*\n");
            sb.append("1. Apply gently to intact, unbroken skin only. Never apply to wounds, cuts, or rashes.\n");
            sb.append("2. Keep away from eyes, nose, and mouth. Wash hands thoroughly after application.\n");
            sb.append("3. If swelling, deformity, or severe joint pain is present, an in-person medical evaluation is advised.\n\n");
        } else if (b.contains("omez") || b.contains("pan 40") || b.contains("pantocid")) {
            sb.append("Yes, we have information on **Omez / Pan 40**!\n\n");
            sb.append("• **Active Ingredient:** Omeprazole (20 mg) / Pantoprazole (40 mg)\n");
            sb.append("• **Dosage Form:** Gastro-resistant Capsule / Tablet\n");
            sb.append("• **Category:** Proton Pump Inhibitor (PPI) / Antacid Regulator\n");
            sb.append("• **Intended Use:** Relief from heartburn, acid reflux (GERD), and gastric hyperacidity.\n\n");
            sb.append("⚠️ *Crucial Safety Precautions:*\n");
            sb.append("These are prescription-strength acid regulators. Take typically before breakfast as advised by a doctor. Long-term use requires medical supervision.\n\n");
        } else if (b.contains("electral") || b.contains("ors")) {
            sb.append("Yes, we have information on **Electral / ORS**!\n\n");
            sb.append("• **Active Ingredient:** WHO-formula Oral Rehydration Salts (Sodium Chloride, Potassium Chloride, Sodium Citrate, Dextrose)\n");
            sb.append("• **Dosage Form:** Powder Sachet (21.8g to dissolve in 1 litre of clean drinking water)\n");
            sb.append("• **Intended Use:** Prevention and treatment of dehydration caused by diarrhoea, vomiting, heat exhaustion, or illness.\n\n");
        } else {
            sb.append("We have details on **").append(brandName).append("** in our pharmaceutical knowledge base.\n\n");
            sb.append("Please verify the exact product, strength, dosage form, and intended medical use with a licensed doctor or pharmacist.\n\n");
        }

        sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
        sb.append(CHECK_PHARMACIES_OFFER);

        response.setReplyText(sb.toString().trim());
        response.setQuickReplies(List.of(
                "Yes, check nearby pharmacies",
                "I want to order Dolo",
                "Difference between 500 and 650",
                "Is delivery available?"
        ));
    }

    /**
     * Explains the differences between medicines, brands vs generics, and strengths.
     */
    public void generateMedicineComparison(String cleanMessage, ChatbotMessageResponse response) {
        StringBuilder sb = new StringBuilder();
        String lower = cleanMessage.toLowerCase();

        if (lower.contains("500") || lower.contains("650") || lower.contains("paracetamol") || lower.contains("dolo")) {
            sb.append("🔍 **Comparison: Paracetamol 500 mg vs Paracetamol 650 mg (Dolo 650)**\n\n");
            sb.append("1. **Active Ingredient:** Both contain identical active molecule: Paracetamol (Acetaminophen).\n");
            sb.append("2. **Strength Difference:** 500 mg is the standard baseline adult dose for mild headaches or low-grade fever. 650 mg contains 30% more active ingredient per tablet, providing stronger relief for higher fever or moderate pain.\n");
            sb.append("3. **Daily Limit:** Regardless of tablet strength, the maximum safe adult daily dose is 4,000 mg within 24 hours (e.g. no more than 6 tablets of 650mg or 8 tablets of 500mg in 24 hours).\n");
            sb.append("4. **Suitability:** For mild pain or elderly individuals, 500 mg is often sufficient and gentler. For higher fever, doctors commonly recommend 650 mg.\n\n");
        } else if (lower.contains("brand") || lower.contains("generic")) {
            sb.append("🔍 **Comparison: Brand Name vs Generic Medicines**\n\n");
            sb.append("1. **Active Molecule:** Generic medicines and brand-name drugs contain the exact same active therapeutic ingredient, strength, and dosage form.\n");
            sb.append("2. **Efficacy & Safety:** When approved by regulatory authorities (such as CDSCO / FDA), generic medicines have identical clinical efficacy, quality, and safety.\n");
            sb.append("3. **Cost:** Generic medicines are usually significantly more affordable because they do not carry marketing or brand patent expenses.\n\n");
        } else if (lower.contains("ointment") || lower.contains("tablet") || lower.contains("gel")) {
            sb.append("🔍 **Comparison: Oral Pain Tablets vs Topical Ointments / Gels**\n\n");
            sb.append("1. **Oral Tablets (e.g. Paracetamol / Ibuprofen):** Travel through the digestive system and bloodstream, treating systemic pain or fever throughout the whole body.\n");
            sb.append("2. **Topical Ointments (e.g. Diclofenac / Volini):** Act locally at the site of applied muscle or joint strain, with minimal absorption into the bloodstream, reducing stomach side-effects.\n\n");
        } else {
            sb.append("🔍 **Medicine & Dosage Form Comparison**\n\n");
            sb.append("Medicines differ by their active pharmaceutical ingredient, therapeutic class, strength, and route of administration (tablet, capsule, syrup, or topical ointment).\n");
            sb.append("Always choose the formulation tailored to your specific symptoms, age group, and medical history.\n\n");
        }

        sb.append("⚠️ ").append(HEALTH_DISCLAIMER).append("\n\n");
        sb.append(CHECK_PHARMACIES_OFFER);

        response.setReplyText(sb.toString().trim());
        response.setQuickReplies(List.of(
                "Yes, check nearby pharmacies",
                "Do you have Dolo?",
                "Order this medicine"
        ));
    }
}
