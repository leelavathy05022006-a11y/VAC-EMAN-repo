-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: emergency_med_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sender` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `intent` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chat_messages_session_created` (`session_id`,`created_at`),
  KEY `idx_chat_messages_created` (`created_at`),
  CONSTRAINT `fk_chat_messages_session` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (1,'34be9f37-a774-4a94-8f4c-5f7a3de4cbf2','USER','CHECK_STOCK','Is Paracetamol 650 available in Adyar?','2026-10-12 13:52:37'),(2,'34be9f37-a774-4a94-8f4c-5f7a3de4cbf2','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 13:52:37'),(3,'ffcf64b9-f852-4cd5-9c80-6c9fe3ae4f32','USER','CHECK_STOCK','Is Paracetamol 650 available in Adyar?','2026-10-12 13:53:36'),(4,'ffcf64b9-f852-4cd5-9c80-6c9fe3ae4f32','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 13:53:36'),(5,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:15:40'),(6,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:15:40'),(7,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','NEARBY_PHARMACIES','Nearby Chennai Pharmacies','2026-10-12 14:15:42'),(8,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-12 14:15:42'),(9,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Check Medicine Stock','2026-10-12 14:15:45'),(10,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','No direct stock found for Zero Stock Medicine within 15 km in Chennai. You may search in a wider radius (up to 50 km) or check nearby localities.','2026-10-12 14:15:45'),(11,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:15:45'),(12,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:15:45'),(13,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Check Medicine Stock','2026-10-12 14:25:19'),(14,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','No direct stock found for Zero Stock Medicine within 15 km in Chennai. You may search in a wider radius (up to 50 km) or check nearby localities.','2026-10-12 14:25:19'),(15,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:25:34'),(16,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:25:34'),(17,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:25:36'),(18,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:25:36'),(19,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:25:37'),(20,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:25:37'),(21,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Check Medicine Stock','2026-10-12 14:26:04'),(22,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','No direct stock found for Zero Stock Medicine within 15 km in Chennai. You may search in a wider radius (up to 50 km) or check nearby localities.','2026-10-12 14:26:04'),(23,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:26:14'),(24,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:26:14'),(25,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','UNKNOWN','aspirin','2026-10-12 14:27:33'),(26,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','UNKNOWN','I\'m here to help with emergency medicine availability, pharmacy contact numbers, navigation, and order status. You can ask me things like:\n• \"Is Paracetamol 650 available in Adyar?\"\n• \"Find nearby pharmacies in T. Nagar\"\n• \"Track my order REQ-****-001\"\n• \"What are the payment options?\"','2026-10-12 14:27:33'),(27,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','UNKNOWN','cetirizine','2026-10-12 14:28:10'),(28,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','UNKNOWN','I\'m here to help with emergency medicine availability, pharmacy contact numbers, navigation, and order status. You can ask me things like:\n• \"Is Paracetamol 650 available in Adyar?\"\n• \"Find nearby pharmacies in T. Nagar\"\n• \"Track my order REQ-****-001\"\n• \"What are the payment options?\"','2026-10-12 14:28:10'),(29,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','UNKNOWN','find cetirizine','2026-10-12 14:28:46'),(30,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','UNKNOWN','I\'m here to help with emergency medicine availability, pharmacy contact numbers, navigation, and order status. You can ask me things like:\n• \"Is Paracetamol 650 available in Adyar?\"\n• \"Find nearby pharmacies in T. Nagar\"\n• \"Track my order REQ-****-001\"\n• \"What are the payment options?\"','2026-10-12 14:28:46'),(31,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-12 14:29:00'),(32,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-12 14:29:00'),(33,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','Check Medicine Stock','2026-10-12 14:29:11'),(34,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','No direct stock found for Zero Stock Medicine within 15 km in Chennai. You may search in a wider radius (up to 50 km) or check nearby localities.','2026-10-12 14:29:11'),(35,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','NEARBY_PHARMACIES','Nearby Chennai Pharmacies','2026-10-12 14:29:44'),(36,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-12 14:29:44'),(37,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','HOW_TO_REQUEST','How to request emergency medicines?','2026-10-12 14:29:53'),(38,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','HOW_TO_REQUEST','📋 How to Request Emergency Medicines:\n1. Search for your required medicine by name and locality.\n2. Choose a verified pharmacy with available stock.\n3. Click \"Request Emergency Delivery\" or \"Reserve for Self-Pickup\" to proceed to the official fulfillment request form (/request/new.html).\n\n🔒 Safety Notice: For security and data integrity, all reservations and emergency delivery requests are placed exclusively via the verified request form with OTP protection.','2026-10-12 14:29:53'),(39,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','ORDER_STATUS','Track my order REQ-****-001','2026-10-12 14:30:04'),(40,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','ORDER_STATUS','Here is the latest status for your request REQ-****-001:\n• Status: DELIVERED\n• Medicine: Paracetamol 650 mg (2 units)\n• Pharmacy: MediCare Pharmacy\n• Payment: CASH_ON_DELIVERY\n• Delivery Partner: Ramesh K\n• Handover OTP: 5821\nCreated on: 2026-10-11 13:50','2026-10-12 14:30:04'),(41,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','PAYMENT_HELP','Payment Help','2026-10-12 14:30:09'),(42,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','PAYMENT_HELP','💳 Payment Options on Emergency Medicine Network:\n1. Self-Pickup: Pay at the pharmacy counter directly upon collection (Cash or supported in-store card/UPI).\n2. Emergency Delivery: Cash on Delivery (COD) to the verified delivery partner upon physical medicine inspection and OTP verification.\n\n⚠️ Notice: We do not process online advance payments or store credit card details. All transactions are settled directly upon medicine handover.','2026-10-12 14:30:09'),(43,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','USER','CHECK_STOCK','headache medicine','2026-10-12 14:30:43'),(44,'832da54d-ed7c-4731-b1f2-68d6b1b338e1','BOT','CHECK_STOCK','Which medication are you looking for? You can tell me the medicine name (for example, \'Paracetamol 650 mg\', \'Amoxicillin\', or \'Adrenaline\') and your locality.','2026-10-12 14:30:43'),(45,'1bdb7308-d58d-46c9-b5cc-b812850d6232','USER','SYMPTOM_ADVICE','I have a headache. What should I do?','2026-10-13 19:19:25'),(46,'1bdb7308-d58d-46c9-b5cc-b812850d6232','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:19:25'),(47,'10d520d7-92ac-4f9e-855e-6fb6b427a1ba','USER','SYMPTOM_ADVICE','My hand is hurting. Is there any ointment?','2026-10-13 19:19:40'),(48,'10d520d7-92ac-4f9e-855e-6fb6b427a1ba','BOT','SYMPTOM_ADVICE','I\'m sorry to hear your hand is hurting. Hand and wrist pain can happen due to muscle strain, repetitive movement, or a minor sprain.\n\n🌿 **Recommended Safe Self-Care (R.I.C.E Protocol):**\n• **Rest:** Give your hand a rest and avoid lifting heavy objects or typing intensely.\n• **Ice:** Apply an ice pack wrapped in a clean cloth for 15–20 minutes at a time to reduce swelling.\n• **Compression:** You may use a gentle crepe bandage if there is minor wrist weakness, but don\'t wrap it too tightly.\n• **Elevation:** Keep your hand elevated on a pillow when sitting or resting.\n\n🧴 **Regarding Ointments & Topical Relief:**\n• Topical pain relief gels (such as **Diclofenac gel** or soothing herbal balms like Volini or Moov) can provide localized temporary relief for muscular aches or minor sprains without upsetting your stomach.\n• **Precautions:** Do not apply pain ointments on open cuts, wounds, or sensitive skin. Wash your hands after applying.\n\n⚠️ **Warning Signs:** If your hand has visible deformity, severe swelling, numbness in your fingers, or if you had a fall and cannot move your wrist, please get an X-ray/medical assessment to rule out a fracture.\n❓ How did the pain start (e.g. typing, lifting, or a fall)?\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine or pain relief gel?','2026-10-13 19:19:40'),(49,'69d9d808-b1db-4e86-b87e-a0be9640af96','USER','CHECK_STOCK','Find medicine near my college','2026-10-13 19:19:57'),(50,'69d9d808-b1db-4e86-b87e-a0be9640af96','BOT','CHECK_STOCK','📍 Location Needed: Could you please tell me which college or area/locality you are located near (for example, Guindy, Adyar, T. Nagar, or click the GPS button)? That way I can accurately find the nearest pharmacies for you.','2026-10-13 19:19:57'),(51,'acdc8772-a002-4d17-9b60-d4e569ee784a','USER','CHECK_STOCK','Do you have Dolo?','2026-10-13 19:20:19'),(52,'acdc8772-a002-4d17-9b60-d4e569ee784a','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:20:19'),(53,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','SYMPTOM_ADVICE','I have a headache. What should I do?','2026-10-13 19:23:49'),(54,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:23:49'),(55,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','NEARBY_PHARMACIES','Yes, check nearby pharmacies','2026-10-13 19:24:48'),(56,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-13 19:24:48'),(57,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','SYMPTOM_ADVICE','My hand is hurting. Is there any ointment?','2026-10-13 19:27:20'),(58,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','SYMPTOM_ADVICE','I\'m sorry to hear your hand is hurting. Hand and wrist pain can happen due to muscle strain, repetitive movement, or a minor sprain.\n\n🌿 **Recommended Safe Self-Care (R.I.C.E Protocol):**\n• **Rest:** Give your hand a rest and avoid lifting heavy objects or typing intensely.\n• **Ice:** Apply an ice pack wrapped in a clean cloth for 15–20 minutes at a time to reduce swelling.\n• **Compression:** You may use a gentle crepe bandage if there is minor wrist weakness, but don\'t wrap it too tightly.\n• **Elevation:** Keep your hand elevated on a pillow when sitting or resting.\n\n🧴 **Regarding Ointments & Topical Relief:**\n• Topical pain relief gels (such as **Diclofenac gel** or soothing herbal balms like Volini or Moov) can provide localized temporary relief for muscular aches or minor sprains without upsetting your stomach.\n• **Precautions:** Do not apply pain ointments on open cuts, wounds, or sensitive skin. Wash your hands after applying.\n\n⚠️ **Warning Signs:** If your hand has visible deformity, severe swelling, numbness in your fingers, or if you had a fall and cannot move your wrist, please get an X-ray/medical assessment to rule out a fracture.\n❓ How did the pain start (e.g. typing, lifting, or a fall)?\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine or pain relief gel?','2026-10-13 19:27:20'),(59,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','CHECK_STOCK','Do you have Dolo?','2026-10-13 19:27:24'),(60,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Chennai:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:27:24'),(61,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','CHECK_STOCK','Find Paracetamol in Adyar','2026-10-13 19:27:28'),(62,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:27:28'),(63,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','SYMPTOM_ADVICE','I have a headache. What should I do?','2026-10-13 19:33:25'),(64,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:33:25'),(65,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','CHECK_STOCK','Do you have Dolo?','2026-10-13 19:37:31'),(66,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Chennai:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:37:31'),(67,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','UNKNOWN','Order from Fortis Health Store','2026-10-13 19:39:00'),(68,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','UNKNOWN','Thank you for sharing that context. For mild symptoms with no red flags, safe self-care (hydration, rest, and warm fluids) remains key. If symptoms persist beyond 2-3 days or worsen, please consult a healthcare professional.\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:39:00'),(69,'78f0a52a-062a-42de-9d8b-b7011b3fc901','USER','SYMPTOM_ADVICE','I have a headache. What should I do?','2026-10-13 19:52:32'),(70,'78f0a52a-062a-42de-9d8b-b7011b3fc901','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:52:32'),(71,'78f0a52a-062a-42de-9d8b-b7011b3fc901','USER','CHECK_STOCK','Do you have Dolo?','2026-10-13 19:52:32'),(72,'78f0a52a-062a-42de-9d8b-b7011b3fc901','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:52:32'),(73,'78f0a52a-062a-42de-9d8b-b7011b3fc901','USER','ORDER_WORKFLOW','Order this medicine','2026-10-13 19:52:32'),(74,'78f0a52a-062a-42de-9d8b-b7011b3fc901','BOT','ORDER_WORKFLOW','To place your order for **Paracetamol 650 mg** (1 units) from **MediCare Pharmacy**:\n\nPlease provide your full delivery address and nearby landmark.\n*(You can also say \'Pickup\' if you\'d like to collect it yourself)*','2026-10-13 19:52:32'),(75,'78f0a52a-062a-42de-9d8b-b7011b3fc901','USER','DELIVERY_OPTIONS','My name is Priya, phone is 98******10, deliver to 25 Gandhi Nagar, Adyar, near school','2026-10-13 19:52:32'),(76,'78f0a52a-062a-42de-9d8b-b7011b3fc901','BOT','DELIVERY_OPTIONS','📋 **Order Summary:**\n• **Pharmacy:** MediCare Pharmacy (12 Lattice Bridge Rd, Adyar, Chennai)\n• **Medicine:** Paracetamol 650 mg (650mg)\n• **Quantity:** 1 units\n• **Medicine Price:** ₹28.50 × 1 = ₹28.50\n• **Delivery Fee:** ₹30.00\n• **Estimated Total:** ₹58.50\n• **Fulfilment:** Emergency Delivery\n• **Recipient:** Priya | 98******10\n• **Address:** 25 Gandhi Nagar, Adyar, near school (Landmark: school)\n• **Payment Mode:** Cash on Delivery (COD)\n\n**Would you like to confirm this order?** (Reply \'Yes\' or \'Confirm\' to place the order)','2026-10-13 19:52:32'),(77,'78f0a52a-062a-42de-9d8b-b7011b3fc901','USER','ORDER_WORKFLOW','Yes, confirm order','2026-10-13 19:52:32'),(78,'78f0a52a-062a-42de-9d8b-b7011b3fc901','BOT','ORDER_WORKFLOW','✅ **Order Confirmed Successfully!**\n• **Order Reference:** REQ-****-005\n• **Status:** CONFIRMED\n• **Handover OTP:** 2455\n• **Medicine:** Paracetamol 650 mg (1 units)\n• **Pharmacy:** MediCare Pharmacy\n• **Payment Mode:** CASH_ON_DELIVERY\n• **Delivery Partner:** Lifeline Express Partner\n\nYou can track your order anytime by asking: \'Track my order REQ-****-005\'.','2026-10-13 19:52:32'),(79,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','USER','CHECK_STOCK','Check Amoxicillin 500 mg in Adyar','2026-10-13 19:53:04'),(80,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','BOT','CHECK_STOCK','⚠️ Rx Prescription Required: Valid prescription will be verified by the pharmacy prior to handover.\n\nFound 2 verified pharmacies holding stock for Amoxicillin 500 mg near Adyar:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:53:04'),(81,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','USER','ORDER_WORKFLOW','Order this medicine. Self pickup at counter. My name is Suresh, phone is 98******10','2026-10-13 19:53:04'),(82,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','BOT','ORDER_WORKFLOW','📋 **Order Summary:**\n• **Pharmacy:** MediCare Pharmacy (12 Lattice Bridge Rd, Adyar, Chennai)\n• **Medicine:** Amoxicillin 500 mg (500mg)\n• **Quantity:** 1 units\n• **Medicine Price:** ₹65.00 × 1 = ₹65.00\n• **Delivery Fee:** ₹0.00 (Free for Self-Pickup)\n• **Estimated Total:** ₹65.00\n• **Fulfilment:** Self-Pickup\n• **Recipient:** Suresh | 98******10\n• **Payment Mode:** Pay at Pharmacy Counter\n\n⚠️ **Prescription Notice:** This medicine requires a valid prescription to be shown before handover.\n\n**Would you like to confirm this order?** (Reply \'Yes\' or \'Confirm\' to place the order)','2026-10-13 19:53:04'),(83,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','USER','ORDER_WORKFLOW','Confirm order','2026-10-13 19:53:04'),(84,'6ce636a4-92e8-46ed-bf03-21e52ffbce11','BOT','ORDER_WORKFLOW','✅ **Order Placed Successfully!**\n• **Order Reference:** REQ-****-006\n• **Status:** PENDING PRESCRIPTION VERIFICATION\n• **Handover OTP:** 9658\n\n⚠️ **Prescription Requirement:** Amoxicillin 500 mg is a Schedule H prescription drug. Under medical safety regulations, the pharmacy will verify your valid doctor\'s prescription upon collection or delivery handover.\n\nYou can track your order anytime by asking: \'Track my order REQ-****-006\'.','2026-10-13 19:53:04'),(85,'f6a0eaa6-bf45-47f6-99d8-558eb56c5de3','USER','SYMPTOM_ADVICE','Enakku thala vali irukku','2026-10-13 19:53:27'),(86,'f6a0eaa6-bf45-47f6-99d8-558eb56c5de3','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 19:53:27'),(87,'ef113ca3-c66c-4488-b616-888761b21be2','USER','SYMPTOM_ADVICE','Kai valikuthu, ointment irukka?','2026-10-13 19:53:27'),(88,'ef113ca3-c66c-4488-b616-888761b21be2','BOT','SYMPTOM_ADVICE','I\'m sorry to hear your hand is hurting. Hand and wrist pain can happen due to muscle strain, repetitive movement, or a minor sprain.\n\n🌿 **Recommended Safe Self-Care (R.I.C.E Protocol):**\n• **Rest:** Give your hand a rest and avoid lifting heavy objects or typing intensely.\n• **Ice:** Apply an ice pack wrapped in a clean cloth for 15–20 minutes at a time to reduce swelling.\n• **Compression:** You may use a gentle crepe bandage if there is minor wrist weakness, but don\'t wrap it too tightly.\n• **Elevation:** Keep your hand elevated on a pillow when sitting or resting.\n\n🧴 **Regarding Ointments & Topical Relief:**\n• Topical pain relief gels (such as **Diclofenac gel** or soothing herbal balms like Volini or Moov) can provide localized temporary relief for muscular aches or minor sprains without upsetting your stomach.\n• **Precautions:** Do not apply pain ointments on open cuts, wounds, or sensitive skin. Wash your hands after applying.\n\n⚠️ **Warning Signs:** If your hand has visible deformity, severe swelling, numbness in your fingers, or if you had a fall and cannot move your wrist, please get an X-ray/medical assessment to rule out a fracture.\n❓ How did the pain start (e.g. typing, lifting, or a fall)?\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine or pain relief gel?','2026-10-13 19:53:27'),(89,'6a296784-6c4b-4ad8-a991-71a4c8eb583d','USER','CHECK_STOCK','I want to dolo','2026-10-13 19:55:52'),(90,'6a296784-6c4b-4ad8-a991-71a4c8eb583d','BOT','CHECK_STOCK','Found 4 verified pharmacies holding stock for Paracetamol 650 mg near Chennai:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-13 19:55:52'),(91,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','NEARBY_PHARMACIES','Yes, check nearby pharmacies','2026-10-13 20:05:39'),(92,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-13 20:05:39'),(93,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','SYMPTOM_ADVICE','i have headache','2026-10-13 20:07:24'),(94,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-13 20:07:24'),(95,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','USER','NEARBY_PHARMACIES','Yes, check nearby pharmacies','2026-10-13 20:08:42'),(96,'fa791e96-e6c0-4b5f-8b2c-24b3a9646917','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-13 20:08:42'),(97,'3576cbc6-01aa-4f19-8a07-dab4e160098e','USER','ORDER_WORKFLOW','Order this medicine','2026-10-14 08:43:56'),(98,'3576cbc6-01aa-4f19-8a07-dab4e160098e','BOT','ORDER_WORKFLOW','Which medicine would you like to order? Please tell me the medicine name and strength.','2026-10-14 08:43:56'),(99,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','USER','UNKNOWN','I would daipatic tablates','2026-10-14 11:56:10'),(100,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','BOT','UNKNOWN','I\'m here to help as your AI Health and Pharmacy Assistant! You can ask me:\n• Health concerns: \"I have a headache\", \"My hand is hurting. Is there any ointment?\"\n• Check medicines: \"Do you have Dolo?\", \"Is Paracetamol available in Adyar?\"\n• Compare medicines: \"What is the difference between Paracetamol 650 and 500?\"\n• Order medicines: \"I want to order Dolo\", \"Order this medicine\"\n• Track orders: \"Track my order REQ-****-001\"','2026-10-14 11:56:10'),(101,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','USER','CHECK_STOCK','I would daibetic medicine','2026-10-14 11:59:17'),(102,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','BOT','CHECK_STOCK','Which medication are you looking for? You can tell me the medicine name (for example, \'Paracetamol 650 mg\', \'Amoxicillin\', or \'Adrenaline\') and your locality.','2026-10-14 11:59:17'),(103,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','USER','CHECK_STOCK','Adrenaline','2026-10-14 12:00:07'),(104,'60f4f1c9-784e-4486-a9b1-48f7965f0ebb','BOT','CHECK_STOCK','⚠️ Rx Prescription Required: Valid prescription will be verified by the pharmacy prior to handover.\n\nFound 2 verified pharmacies holding stock for Adrenaline 1 mg/ml Injection near Chennai:\n\nPlease review the pharmacy details below. Confirm stock by calling before travelling.','2026-10-14 12:00:07'),(105,'51570bae-ef2e-4ab6-a017-162cd5cd463a','USER','SYMPTOM_ADVICE','I have a headache. What should I do?','2026-10-14 12:16:43'),(106,'51570bae-ef2e-4ab6-a017-162cd5cd463a','BOT','SYMPTOM_ADVICE','I hear that you\'re dealing with a headache. Let\'s take a look at some safe steps you can take:\n\n🌿 **Safe Self-Care Steps:**\n• Rest in a quiet, dimly lit room and close your eyes.\n• Stay well-hydrated by drinking water — mild dehydration is a very common trigger for headaches.\n• Apply a cool or warm cloth gently to your forehead or the back of your neck.\n• Take a break from digital screens (phones, laptops) and bright lighting.\n\n💊 **Over-the-Counter (OTC) Information:**\n• For mild-to-moderate tension headache, an over-the-counter pain reliever like **Paracetamol (500 mg or 650 mg)** is commonly used by adults without liver conditions.\n• **Precautions:** Never exceed 4,000 mg in 24 hours. Do not combine multiple medications that both contain paracetamol.\n\n❓ **To help provide better guidance:**\n• How long have you had this headache, and is it mild, throbbing, or severe?\n• Do you have any red-flag symptoms like sudden vision changes, stiff neck, high fever, or vomiting? *(If so, please seek emergency medical care immediately!)*\n\n⚠️ Disclaimer: I\'m an AI health assistant, not a doctor. Medicine suitability depends on your symptoms, age, medical history, and other medicines you take. Please confirm with a qualified doctor or pharmacist before using a medicine.\n\nWould you like me to check nearby pharmacies for an appropriate medicine?','2026-10-14 12:16:43'),(107,'51570bae-ef2e-4ab6-a017-162cd5cd463a','USER','NEARBY_PHARMACIES','Yes, check nearby pharmacies','2026-10-14 12:16:47'),(108,'51570bae-ef2e-4ab6-a017-162cd5cd463a','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-14 12:16:47'),(109,'51570bae-ef2e-4ab6-a017-162cd5cd463a','USER','NEARBY_PHARMACIES','Order from MediCare Pharmacy','2026-10-14 12:17:03'),(110,'51570bae-ef2e-4ab6-a017-162cd5cd463a','BOT','NEARBY_PHARMACIES','Approved pharmacies near Chennai (within 15 km):\nYou can call the pharmacy, get turn-by-turn navigation routes, or view complete pharmacy hours.','2026-10-14 12:17:03');
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_sessions`
--

DROP TABLE IF EXISTS `chat_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_sessions` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_active_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chat_sessions_user` (`user_id`,`created_at`),
  CONSTRAINT `fk_chat_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_sessions`
--

LOCK TABLES `chat_sessions` WRITE;
/*!40000 ALTER TABLE `chat_sessions` DISABLE KEYS */;
INSERT INTO `chat_sessions` VALUES ('10d520d7-92ac-4f9e-855e-6fb6b427a1ba',1,'2026-10-13 19:19:40','2026-10-13 19:19:40'),('1bdb7308-d58d-46c9-b5cc-b812850d6232',1,'2026-10-13 19:19:25','2026-10-13 19:19:25'),('34be9f37-a774-4a94-8f4c-5f7a3de4cbf2',1,'2026-10-12 13:52:37','2026-10-12 13:52:37'),('3576cbc6-01aa-4f19-8a07-dab4e160098e',1,'2026-10-14 08:43:56','2026-10-14 08:43:56'),('51570bae-ef2e-4ab6-a017-162cd5cd463a',1,'2026-10-14 12:16:43','2026-10-14 12:17:03'),('60f4f1c9-784e-4486-a9b1-48f7965f0ebb',1,'2026-10-14 11:56:10','2026-10-14 12:00:07'),('69d9d808-b1db-4e86-b87e-a0be9640af96',1,'2026-10-13 19:19:57','2026-10-13 19:19:57'),('6a296784-6c4b-4ad8-a991-71a4c8eb583d',1,'2026-10-13 19:55:52','2026-10-13 19:55:52'),('6ce636a4-92e8-46ed-bf03-21e52ffbce11',1,'2026-10-13 19:53:04','2026-10-13 19:53:04'),('78f0a52a-062a-42de-9d8b-b7011b3fc901',1,'2026-10-13 19:52:32','2026-10-13 19:52:32'),('832da54d-ed7c-4731-b1f2-68d6b1b338e1',1,'2026-10-12 14:15:40','2026-10-12 14:30:43'),('acdc8772-a002-4d17-9b60-d4e569ee784a',1,'2026-10-13 19:20:19','2026-10-13 19:20:19'),('ef113ca3-c66c-4488-b616-888761b21be2',1,'2026-10-13 19:53:27','2026-10-13 19:53:27'),('f6a0eaa6-bf45-47f6-99d8-558eb56c5de3',1,'2026-10-13 19:53:27','2026-10-13 19:53:27'),('fa791e96-e6c0-4b5f-8b2c-24b3a9646917',1,'2026-10-13 19:23:49','2026-10-13 20:08:42'),('ffcf64b9-f852-4cd5-9c80-6c9fe3ae4f32',1,'2026-10-12 13:53:36','2026-10-12 13:53:36');
/*!40000 ALTER TABLE `chat_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_logs`
--

DROP TABLE IF EXISTS `contact_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pharmacy_id` bigint NOT NULL,
  `medicine_id` bigint DEFAULT NULL,
  `contact_type` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_contact_logs_user` (`user_id`),
  KEY `fk_contact_logs_medicine` (`medicine_id`),
  KEY `idx_contact_logs_pharmacy_created` (`pharmacy_id`,`created_at`),
  CONSTRAINT `fk_contact_logs_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`),
  CONSTRAINT `fk_contact_logs_pharmacy` FOREIGN KEY (`pharmacy_id`) REFERENCES `pharmacies` (`id`),
  CONSTRAINT `fk_contact_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_logs`
--

LOCK TABLES `contact_logs` WRITE;
/*!40000 ALTER TABLE `contact_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `contact_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `delivery_partner_profiles`
--

DROP TABLE IF EXISTS `delivery_partner_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `delivery_partner_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `vehicle_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `vehicle_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `driving_license_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `service_city` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT 'Chennai',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_delivery_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `delivery_partner_profiles`
--

LOCK TABLES `delivery_partner_profiles` WRITE;
/*!40000 ALTER TABLE `delivery_partner_profiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `delivery_partner_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'300','m3 search contact logs','SQL','V300__m3_search_contact_logs.sql',-481756361,'root','2026-10-11 05:38:05',334,1),(2,'500','chatbot sessions messages','SQL','V500__chatbot_sessions_messages.sql',1362255480,'root','2026-10-12 08:14:53',103,1),(3,'100','m1 users profiles','SQL','V100__m1_users_profiles.sql',816085882,'root','2026-10-14 14:52:28',247,1),(4,'110','m1 pharmacies delivery profiles','SQL','V110__m1_pharmacies_delivery_profiles.sql',1167821822,'root','2026-10-14 14:52:28',58,0);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hospital_profiles`
--

DROP TABLE IF EXISTS `hospital_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hospital_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `hospital_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `registration_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hospital_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pincode` char(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `registration_no` (`registration_no`),
  CONSTRAINT `fk_hospital_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hospital_profiles`
--

LOCK TABLES `hospital_profiles` WRITE;
/*!40000 ALTER TABLE `hospital_profiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `hospital_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medicines`
--

DROP TABLE IF EXISTS `medicines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicines` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `generic_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `manufacturer` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dosage_form` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `strength` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `requires_prescription` tinyint(1) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicines`
--

LOCK TABLES `medicines` WRITE;
/*!40000 ALTER TABLE `medicines` DISABLE KEYS */;
INSERT INTO `medicines` VALUES (1,'Paracetamol 650 mg','Paracetamol','GSK Healthcare','Tablet','650mg',NULL,0,1,'2026-10-11 11:11:38'),(2,'Paracetamol 500 mg','Paracetamol','Cipla','Tablet','500mg',NULL,0,1,'2026-10-11 11:11:38'),(3,'Adrenaline 1 mg/ml Injection','Epinephrine','Pfizer','Injection','1mg/ml',NULL,1,1,'2026-10-11 11:11:38'),(4,'Amoxicillin 500 mg','Amoxicillin','Novartis','Capsule','500mg',NULL,1,1,'2026-10-11 11:11:38'),(5,'Zero Stock Medicine','RareCompound','Specialty Lab','Tablet','25mg',NULL,0,1,'2026-10-11 11:11:38');
/*!40000 ALTER TABLE `medicines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient_profiles`
--

DROP TABLE IF EXISTS `patient_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `blood_group` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pincode` char(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  `emergency_contact_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_phone` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient_profiles`
--

LOCK TABLES `patient_profiles` WRITE;
/*!40000 ALTER TABLE `patient_profiles` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient_profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pharmacies`
--

DROP TABLE IF EXISTS `pharmacies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pharmacies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `licence_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` decimal(9,6) NOT NULL,
  `longitude` decimal(9,6) NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_approved` tinyint(1) DEFAULT '1',
  `opens_24x7` tinyint(1) DEFAULT '0',
  `opening_time` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '08:00',
  `closing_time` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '22:00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `licence_number` (`licence_number`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pharmacies`
--

LOCK TABLES `pharmacies` WRITE;
/*!40000 ALTER TABLE `pharmacies` DISABLE KEYS */;
INSERT INTO `pharmacies` VALUES (1,'MediCare Pharmacy','TN-CHE-2024-001','12 Lattice Bridge Rd, Adyar, Chennai',13.003500,80.257000,'9000000001',1,0,'08:00','23:00','2026-10-11 11:11:38'),(2,'Apollo Pharmacy Adyar','TN-CHE-2024-002','45 Gandhi Nagar, Adyar, Chennai',13.008000,80.252000,'9000000002',1,1,'00:00','23:59','2026-10-11 11:11:38'),(3,'Fortis Health Store','TN-CHE-2024-003','10 Sardar Patel Rd, Guindy, Chennai',13.010000,80.220000,'9000000003',1,0,'22:00','06:00','2026-10-11 11:11:38'),(4,'New Life Chemist','TN-CHE-2024-004','88 Anna Salai, T. Nagar, Chennai',13.041800,80.234100,'9000000004',1,0,'09:00','21:00','2026-10-11 11:11:38'),(5,'Unapproved Demo Pharmacy','TN-CHE-PENDING-005','100 Beach Rd, Besant Nagar, Chennai',13.000500,80.266000,'9000000005',0,0,'09:00','21:00','2026-10-11 11:11:38'),(6,'Suburban Medicos','TN-CHE-2024-006','15 GST Road, Tambaram, Chennai',12.924900,80.100000,'9000000006',1,0,'08:00','22:00','2026-10-11 11:11:38');
/*!40000 ALTER TABLE `pharmacies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_history`
--

DROP TABLE IF EXISTS `search_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `medicine_id` bigint DEFAULT NULL,
  `query_text` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `user_lat` decimal(6,3) DEFAULT NULL,
  `user_lng` decimal(6,3) DEFAULT NULL,
  `radius_km` decimal(4,1) DEFAULT NULL,
  `sort_by` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `results_count` int DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_search_history_medicine` (`medicine_id`),
  KEY `idx_search_history_user_created` (`user_id`,`created_at`),
  CONSTRAINT `fk_search_history_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`),
  CONSTRAINT `fk_search_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_history`
--

LOCK TABLES `search_history` WRITE;
/*!40000 ALTER TABLE `search_history` DISABLE KEYS */;
INSERT INTO `search_history` VALUES (2,1,1,'Paracetamol 650 mg',5,13.001,80.257,10.0,'distance',4,'2026-10-11 11:12:42'),(3,1,2,'Paracetamol 500 mg',6,13.023,80.149,10.0,'distance',0,'2026-10-11 11:16:48'),(4,1,2,'Paracetamol 500 mg',6,13.023,80.149,10.0,'distance',0,'2026-10-11 11:16:51'),(5,1,2,'Paracetamol 500 mg',5,13.023,80.149,10.0,'distance',0,'2026-10-11 11:19:09'),(6,1,2,'Paracetamol 500 mg',5,13.023,80.149,10.0,'distance',0,'2026-10-11 11:19:15'),(7,1,1,'Paracetamol 650 mg',5,13.023,80.149,10.0,'distance',2,'2026-10-11 11:19:34'),(8,1,2,'Paracetamol 500 mg',53,13.023,80.148,20.0,'distance',2,'2026-10-11 11:28:42'),(9,1,2,'Paracetamol 500 mg',53,13.023,80.148,20.0,'distance',2,'2026-10-11 11:28:49'),(10,1,1,'Paracetamol 650 mg',1,13.001,80.257,10.0,'distance',4,'2026-10-11 20:09:47'),(11,1,1,'Paracetamol 650 mg',5,13.023,80.149,10.0,'distance',2,'2026-10-11 20:15:09'),(12,1,1,'Paracetamol 650 mg',1,13.083,80.271,25.0,'distance',4,'2026-10-12 08:50:45'),(13,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:02:18'),(14,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:03:07'),(15,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:08:20'),(16,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:10:43'),(17,1,1,'Paracetamol 650 mg',1,13.083,80.271,25.0,'distance',4,'2026-10-12 09:33:51'),(18,1,1,'Paracetamol 650 mg',1,13.083,80.271,25.0,'distance',4,'2026-10-12 09:33:51'),(19,1,1,'Paracetamol 650 mg',1,13.083,80.271,25.0,'distance',4,'2026-10-12 09:33:59'),(20,1,1,'Paracetamol 650 mg',1,13.083,80.271,25.0,'distance',4,'2026-10-12 09:33:59'),(21,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:41:44'),(22,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:41:45'),(23,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 09:41:48'),(24,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'delivery',0,'2026-10-12 09:41:55'),(25,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'price',0,'2026-10-12 09:42:00'),(26,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'quantity',0,'2026-10-12 09:42:04'),(27,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'quantity',0,'2026-10-12 09:42:07'),(28,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'quantity',0,'2026-10-12 09:42:08'),(29,1,1,'Paracetamol 650 mg',9,13.024,80.030,5.0,'quantity',0,'2026-10-12 09:43:32'),(30,1,1,'Paracetamol 650 mg',9,13.024,80.030,5.0,'quantity',0,'2026-10-12 09:43:34'),(31,1,2,'Paracetamol 500 mg',9,13.024,80.030,5.0,'quantity',0,'2026-10-12 09:43:40'),(32,1,1,'Paracetamol 650 mg',9,13.024,80.030,5.0,'quantity',0,'2026-10-12 09:43:41'),(33,1,1,'Paracetamol 650 mg',9,13.001,80.257,5.0,'quantity',3,'2026-10-12 09:44:11'),(34,1,1,'Paracetamol 650 mg',9,13.001,80.257,5.0,'distance',3,'2026-10-12 09:44:59'),(35,1,2,'Paracetamol 500 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 11:12:02'),(36,1,2,'Paracetamol 500 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-12 11:15:43'),(37,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 13:52:37'),(38,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 13:53:36'),(39,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:15:40'),(40,1,5,'Zero Stock Medicine',1,13.083,80.271,15.0,'distance',0,'2026-10-12 14:15:45'),(41,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:15:45'),(42,1,5,'Zero Stock Medicine',1,13.083,80.271,15.0,'distance',0,'2026-10-12 14:25:19'),(43,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:25:34'),(44,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:25:36'),(45,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:25:37'),(46,1,5,'Zero Stock Medicine',1,13.083,80.271,15.0,'distance',0,'2026-10-12 14:26:04'),(47,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:26:14'),(48,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-12 14:29:00'),(49,1,5,'Zero Stock Medicine',1,13.083,80.271,15.0,'distance',0,'2026-10-12 14:29:11'),(50,1,1,'Paracetamol 650 mg',2,13.083,80.271,10.0,'distance',4,'2026-10-13 17:00:15'),(51,1,1,'Paracetamol 650 mg',5,13.023,80.149,10.0,'distance',2,'2026-10-13 17:03:33'),(52,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-13 19:20:19'),(53,1,1,'Paracetamol 650 mg',1,13.083,80.271,15.0,'distance',4,'2026-10-13 19:27:24'),(54,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-13 19:27:28'),(55,1,1,'Paracetamol 650 mg',1,13.083,80.271,15.0,'distance',4,'2026-10-13 19:37:31'),(56,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-13 19:52:32'),(57,1,1,'Paracetamol 650 mg',1,13.004,80.257,15.0,'distance',4,'2026-10-13 19:52:32'),(58,1,4,'Amoxicillin 500 mg',1,13.004,80.257,15.0,'distance',2,'2026-10-13 19:53:04'),(59,1,4,'Amoxicillin 500 mg',1,13.004,80.257,15.0,'distance',2,'2026-10-13 19:53:04'),(60,1,1,'Paracetamol 650 mg',2,13.083,80.271,10.0,'distance',4,'2026-10-13 19:53:49'),(61,1,3,'Adrenaline 1 mg/ml Injection',5,13.023,80.149,10.0,'distance',0,'2026-10-13 19:55:16'),(62,1,1,'Paracetamol 650 mg',1,13.083,80.271,15.0,'distance',4,'2026-10-13 19:55:52'),(63,1,1,'Paracetamol 650 mg',5,13.024,80.030,10.0,'distance',0,'2026-10-14 08:45:16'),(64,1,1,'Paracetamol 650 mg',5,13.024,80.030,5.0,'distance',0,'2026-10-14 08:45:20'),(65,1,3,'Adrenaline 1 mg/ml Injection',1,13.083,80.271,15.0,'distance',2,'2026-10-14 12:00:07'),(66,1,3,'Adrenaline 1 mg/ml Injection',5,13.024,80.030,5.0,'distance',0,'2026-10-14 12:10:13'),(67,1,3,'Adrenaline 1 mg/ml Injection',5,13.024,80.030,5.0,'distance',0,'2026-10-14 12:10:30'),(68,1,3,'Adrenaline 1 mg/ml Injection',5,13.024,80.030,5.0,'distance',0,'2026-10-14 12:10:41'),(69,1,3,'Adrenaline 1 mg/ml Injection',5,13.024,80.030,5.0,'distance',0,'2026-10-14 12:10:45'),(70,1,3,'Adrenaline 1 mg/ml Injection',5,13.024,80.030,5.0,'distance',0,'2026-10-14 12:11:00'),(71,1,3,'Adrenaline 1 mg/ml Injection',3,13.024,80.030,5.0,'distance',0,'2026-10-14 12:14:00'),(72,1,3,'Adrenaline 1 mg/ml Injection',3,13.024,80.030,5.0,'distance',0,'2026-10-14 12:14:02'),(73,1,5,'Zero Stock Medicine',3,13.024,80.030,5.0,'distance',0,'2026-10-14 12:14:50'),(74,1,5,'Zero Stock Medicine',3,13.024,80.030,5.0,'distance',0,'2026-10-14 12:14:54'),(75,1,5,'Zero Stock Medicine',3,13.024,80.030,5.0,'distance',0,'2026-10-14 12:14:57'),(76,1,5,'Zero Stock Medicine',3,13.024,80.030,10.0,'distance',0,'2026-10-14 12:15:00'),(77,1,5,'Zero Stock Medicine',3,13.024,80.030,10.0,'distance',0,'2026-10-14 12:15:01');
/*!40000 ALTER TABLE `search_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_email` (`email`),
  KEY `idx_users_role_active` (`role`,`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Deepika Patient','patient@demo.in','$2a$10$hash','PATIENT','9876543210',13.001200,80.256500,1,'2026-10-11 11:11:38');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-14 20:25:51
