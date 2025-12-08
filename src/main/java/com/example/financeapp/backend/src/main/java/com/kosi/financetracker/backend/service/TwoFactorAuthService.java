package com.kosi.financetracker.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
public class TwoFactorAuthService {

    @Value("${app.2fa.issuer:FinanceTracker}")
    private String issuer;

    @Value("${app.2fa.qr-code-size:200}")
    private int qrCodeSize;

    private final GoogleAuthenticator googleAuthenticator;

    public TwoFactorAuthService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    public String generateQRCodeUrl(String email, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            issuer,
            email,
            googleAuthenticator.createCredentials(secret)
        );
    }

    public String generateQRCodeImage(String email, String secret) throws WriterException, IOException {
        String qrCodeUrl = generateQRCodeUrl(email, secret);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        byte[] qrCodeBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        Random random = new SecureRandom();
        
        for (int i = 0; i < count; i++) {
            int code = 10000000 + random.nextInt(90000000);
            codes.add(String.valueOf(code));
        }
        
        return codes;
    }

    public boolean verifyBackupCode(String storedCodes, String inputCode) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return false;
        }
        
        String[] codes = storedCodes.split(",");
        for (String code : codes) {
            if (code.trim().equals(inputCode.trim())) {
                return true;
            }
        }
        
        return false;
    }

    public String removeBackupCode(String storedCodes, String usedCode) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return "";
        }
        
        String[] codes = storedCodes.split(",");
        StringBuilder remaining = new StringBuilder();
        
        for (String code : codes) {
            if (!code.trim().equals(usedCode.trim())) {
                if (remaining.length() > 0) {
                    remaining.append(",");
                }
                remaining.append(code.trim());
            }
        }
        
        return remaining.toString();
    }

    public int countBackupCodes(String storedCodes) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return 0;
        }
        return storedCodes.split(",").length;
    }
}