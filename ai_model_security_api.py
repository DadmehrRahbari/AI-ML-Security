import hashlib
import os
from fastapi import FastAPI, File, UploadFile, HTTPException
import onnxruntime as ort

app = FastAPI()

MODEL_DIR = "models"
EXPECTED_HASH = "5d41402abc4b2a76b9719d911017c592"  # Replace with actual hash

# Ensure the models directory exists
os.makedirs(MODEL_DIR, exist_ok=True)

def calculate_sha256(file_path: str) -> str:
    """Calculate SHA-256 hash of a file."""
    sha256 = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(4096):
            sha256.update(chunk)
    return sha256.hexdigest()

@app.post("/upload/")
async def upload_model(file: UploadFile = File(...)):
    """Upload an ONNX model and verify its integrity."""
    file_path = os.path.join(MODEL_DIR, file.filename)

    with open(file_path, "wb") as buffer:
        buffer.write(await file.read())

    file_hash = calculate_sha256(file_path)

    if file_hash != EXPECTED_HASH:
        os.remove(file_path)  # Delete the tampered file
        raise HTTPException(status_code=400, detail="Model integrity check failed! Possible tampering detected.")

    return {"message": "Model uploaded successfully!", "file_name": file.filename, "hash": file_hash}

@app.post("/inference/")
async def run_inference(file_name: str):
    """Run inference if the model integrity is verified."""
    file_path = os.path.join(MODEL_DIR, file_name)

    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="Model file not found.")

    try:
        session = ort.InferenceSession(file_path)
        return {"message": "Model loaded successfully. Ready for inference."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to load model: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
