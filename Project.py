import hashlib
import onnxruntime as ort

# AI/ML model security by checking the model file’s integrity using SHA-256. 
#If the hash matches the expected value, the model is considered safe; otherwise, it warns about potential tampering. 

def calculate_sha256(file_path):
    """Calculate the SHA-256 hash of a given file."""
    sha256 = hashlib.sha256()
    try:
        with open(file_path, "rb") as f:
            while chunk := f.read(4096):
                sha256.update(chunk)
        return sha256.hexdigest()
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        return None

def verify_model_integrity(model_path, expected_hash):
    """Verify if the model file matches the expected SHA-256 hash."""
    file_hash = calculate_sha256(model_path)
    if file_hash is None:
        return False
    return file_hash == expected_hash

def run_inference(model_path):
    """Run inference on the model if integrity is verified."""
    try:
        session = ort.InferenceSession(model_path)
        print("Model loaded successfully. Ready for inference.")
        # Add inference logic here (e.g., session.run(...))
    except Exception as e:
        print(f"Failed to load model: {e}")

if __name__ == "__main__":
    model_path = "model.onnx"  # Actual model file
    expected_hash = "5d41402abc4b2a76b9719d911017c592"  # Replace with actual SHA-256 hash

    if verify_model_integrity(model_path, expected_hash):
        print("✅ Model integrity verified. Safe to use.")
        run_inference(model_path)
    else:
        print("❌ Model integrity check failed! Possible tampering detected.")
