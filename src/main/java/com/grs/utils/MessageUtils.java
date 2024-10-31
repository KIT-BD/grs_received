package com.grs.utils;

import java.util.WeakHashMap;
import java.util.Map;

public class MessageUtils {

    public static Map getNotificationMessagesByGrievanceForwardingAction(String action, Long grievanceId, String trackingNumber) {
        String toText, clickAction;
        switch (action) {
            case "NEW":
                toText = "New Grievance (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "ACCEPTED":
                toText = "Grievance (" + trackingNumber + ") accepted";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "SEND_FOR_OPINION":
                toText = "Opinion asked by GRO (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "STATEMENT_ANSWERED":
                toText = "Statement given (" + trackingNumber + ")";
                clickAction = "/viewGrievances.do?id=" + grievanceId + "#complaintMovementHistory";
                break;
            case "REJECTED":
                toText = "Grievance (" + trackingNumber + ") has been rejected";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "CLOSED_ACCUSATION_PROVED":
                toText = "Grievance (" + trackingNumber + ") closed as correct";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            case "CLOSED_ACCUSATION_INCORRECT":
                toText = "Grievance (" + trackingNumber + ") closed as incorrect";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
            default:
                toText = "Grievance (" + trackingNumber + ") proceeded to next step";
                clickAction = "/viewGrievances.do?id=" + grievanceId;
                break;
        }
        WeakHashMap<String, String> result = new WeakHashMap();
        result.put("toText", toText);
        result.put("clickAction", clickAction);
        return result;
    }

    public static String getCustomLayerEnglishName(String nameBng) {
        switch (nameBng) {
            case "কর্তৃপক্ষ / অথোরিটি":
                return "Authority";
            case "ইনস্টিটিউট":
                return "Institute";
            case "অন্যান্য প্রতিষ্ঠান":
                return "Other Institutions";
            case "একাডেমি/ প্রশিক্ষণ কেন্দ্র":
                return "Academy / Training Center";
            case "ব্যুরো":
                return "Bureau";
            case "কর্পোরেশন":
                return "Corporation";
            case "কাউন্সিল":
                return "Council";
            case "কমিশন":
                return "Commission";
            case "কোম্পানি":
                return "Company";
            case "পরিদপ্তর":
                return "Directorate";
            case "পরিষদ":
                return "Council";
            case "প্লান্ট/স্টেশন/ফিল্ড":
                return "Plant / Station / Field";
            case "প্রোগ্রাম / প্রকল্প":
                return "Program / Project";
            case "মন্ত্রণালয়":
                return "Ministry";
            case "অধিদপ্তর":
                return "Department";
            case "বিভাগীয় কার্যালয়":
                return "Divisional Office";
            case "জেলা কার্যালয়":
                return "District Office";
            case "উপজেলা কার্যালয়":
                return "Upazila Office";
            case "উপজেলা ভূমি অফিস":
                return "Upazila Land Office";
            case "বিভাগ":
                return "Division";
            case "সংস্থা":
                return "Agency";
            case "ইউনিট":
                return "Unit";
            case "বোর্ড":
                return "Board";
            case "আঞ্চলিক কার্যালয়":
                return "Regional Office";
            case "ফাউন্ডেশন":
                return "Foundation";
            case "বিশ্ববিদ্যালয়":
                return "University";
            case "কলেজ":
                return "College";
            case "স্কুল":
                return "School";
            case "টেকনিক্যাল/ভোকেশনাল প্রতিষ্ঠান":
                return "Technical / Vocational Institution";
            case "জোনাল অফিস":
                return "Zonal Office";
            case "মেট্রোপলিটন":
                return "Metropolitan";
            case "মিশন ও অন্যান্য":
                return "Mission and Others";
            case "সার্কেল অফিস":
                return "Circle Office";
            default:
                return ""; // Fallback for unknown entries
        }
    }

}
