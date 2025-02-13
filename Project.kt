import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import ai.onnxruntime.*

/*
AI/ML model security by checking the model file’s integrity using SHA-256. 
If the hash matches the expected value, the model is considered safe; otherwise, it warns about potential tampering.
*/

object ModelSecurityCheck {

    // Function to compute SHA-256 hash of a file
    fun calculateSHA256(filePath: String): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fis = FileInputStream(File(filePath))
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            fis.close()

            // Convert hash bytes to hex string
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            println("Error computing SHA-256: ${e.message}")
            null
        }
    }

    // Function to verify model integrity
    fun verifyModelIntegrity(modelPath: String, expectedHash: String): Boolean {
        val fileHash = calculateSHA256(modelPath)
        return fileHash != null && fileHash.equals(expectedHash, ignoreCase = true)
    }

    // Function to load and run the AI model using ONNX Runtime
    fun runInference(modelPath: String) {
        try {
            OrtEnvironment.getEnvironment().use { env ->
                env.createSession(modelPath, OrtSession.SessionOptions()).use { session ->
                    println("✅ Model loaded successfully. Ready for inference.")
                    // Add inference logic here if needed
                }
            }
        } catch (e: OrtException) {
            println("❌ Failed to load model: ${e.message}")
        }
    }
}

fun main() {
    val modelPath = "model.onnx"  // Actual model file
    val expectedHash = "5d41402abc4b2a76b9719d911017c592"  // Replace with actual SHA-256 hash

    if (ModelSecurityCheck.verifyModelIntegrity(modelPath, expectedHash)) {
        println("✅ Model integrity verified. Safe to use.")
        ModelSecurityCheck.runInference(modelPath)
    } else {
        println("❌ Model integrity check failed! Possible tampering detected.")
    }
}
