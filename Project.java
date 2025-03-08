import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import ai.onnxruntime.*;

/**
AI/ML model security by checking the model file’s integrity using SHA-256. 
If the hash matches the expected value, the model is considered safe; otherwise, it warns about potential tampering.
**/

public class ModelSecurityCheck {

    // Method to compute SHA-256 hash of a file
    public static String calculateSHA256(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(new File(filePath));
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            fis.close();
            
            // Convert hash bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest.digest()) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Error computing SHA-256: " + e.getMessage());
            return null;
        }
    }

    // Method to verify model integrity
    public static boolean verifyModelIntegrity(String modelPath, String expectedHash) {
        String fileHash = calculateSHA256(modelPath);
        return fileHash != null && fileHash.equalsIgnoreCase(expectedHash);
    }

    // Method to load and run the AI model using ONNX Runtime
    public static void runInference(String modelPath) {
        try (OrtEnvironment env = OrtEnvironment.getEnvironment();
             OrtSession session = env.createSession(modelPath, new OrtSession.SessionOptions())) {

            System.out.println("✅ Model loaded successfully. Ready for inference.");
            // Add inference logic here if needed

        } catch (OrtException e) {
            System.err.println("❌ Failed to load model: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String modelPath = "model.onnx"; // Model file
        String expectedHash = "5d41402abc4b2a76b9719d911017c592"; // Replace with actual SHA-256 hash

        if (verifyModelIntegrity(modelPath, expectedHash)) {
            System.out.println("✅ Model integrity verified. Safe to use.");
            runInference(modelPath);
        } else {
            System.err.println("❌ Model integrity check failed! Possible tampering detected.");
        }
    }
}
