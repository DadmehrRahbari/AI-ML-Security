#include <iostream>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <openssl/sha.h>
#include <onnxruntime/core/session/onnxruntime_cxx_api.h>

/*
AI/ML model security by checking the model file’s integrity using SHA-256. 
If the hash matches the expected value, the model is considered safe; otherwise, it warns about potential tampering. 
*/

// Function to calculate SHA-256 hash of a file
std::string calculateSHA256(const std::string& filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "Error opening file: " << filename << std::endl;
        return "";
    }

    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    char buffer[4096];
    while (file.read(buffer, sizeof(buffer))) {
        SHA256_Update(&sha256, buffer, file.gcount());
    }
    SHA256_Update(&sha256, buffer, file.gcount());
    
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_Final(hash, &sha256);
    
    std::ostringstream result;
    for (unsigned char c : hash) {
        result << std::hex << std::setw(2) << std::setfill('0') << (int)c;
    }
    return result.str();
}

// Verify model file integrity
bool verifyModelIntegrity(const std::string& modelPath, const std::string& expectedHash) {
    std::string fileHash = calculateSHA256(modelPath);
    return fileHash == expectedHash;
}

void runInference(const std::string& modelPath) {
    Ort::Env env(ORT_LOGGING_LEVEL_WARNING, "MLModel");
    Ort::SessionOptions session_options;

    try {
        Ort::Session session(env, modelPath.c_str(), session_options);
        std::cout << "Model loaded successfully. Running inference..." << std::endl;
        // Add inference logic here
    } catch (const std::exception& e) {
        std::cerr << "Failed to load model: " << e.what() << std::endl;
    }
}

int main() {
    std::string modelPath = "model.onnx";  // AI/ML model file
    std::string expectedHash = "5d41402abc4b2a76b9719d911017c592"; // Replace with actual hash

    if (verifyModelIntegrity(modelPath, expectedHash)) {
        std::cout << "Model integrity verified. Safe to use.\n";
        runInference(modelPath);
    } else {
        std::cerr << "Model integrity check failed! Possible tampering detected.\n";
    }
    return 0;
}
