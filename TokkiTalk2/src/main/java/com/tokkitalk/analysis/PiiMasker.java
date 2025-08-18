package com.tokkitalk.analysis;

import java.util.regex.Pattern;

import com.tokkitalk.analysis.dto.AnalyzeRequest;

public class PiiMasker {
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(01[016789]|02|0[3-9][0-9])[-. ]?([0-9]{3,4})[-. ]?([0-9]{4})");
    private static final Pattern ACCOUNT = Pattern.compile("[0-9]{2,6}-?[0-9]{2,8}-?[0-9]{2,8}");
    private static final Pattern SNS = Pattern.compile("@[A-Za-z0-9_]{3,15}");

    public static AnalyzeRequest mask(AnalyzeRequest in) {
        AnalyzeRequest out = new AnalyzeRequest();
        out.input_type = in.input_type;
        out.options = in.options;
        if (in.text != null) {
            String masked = in.text;
            masked = EMAIL.matcher(masked).replaceAll("[EMAIL]");
            masked = PHONE.matcher(masked).replaceAll("[PHONE]");
            masked = ACCOUNT.matcher(masked).replaceAll("[ACCOUNT]");
            masked = SNS.matcher(masked).replaceAll("[HANDLE]");
            out.text = masked;
        }
        out.image_base64 = in.image_base64; // image not persisted; masking N/A here
        return out;
    }
}


