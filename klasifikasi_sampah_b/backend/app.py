from flask import Flask, request, jsonify
import numpy as np
from PIL import Image
import io
from tensorflow.keras.models import load_model
from flask_cors import CORS

app = Flask(__name__)

# Aktifkan CORS untuk frontend tertentu (gantilah sesuai dengan domain frontend Anda)
CORS(app, resources={r"/classify": {"origins": "http://localhost:3000"}})

# Muat model (pastikan path model benar)
try:
    model = load_model('model/cnn_model1.h5')
    print("Model berhasil dimuat.")
except Exception as e:
    print(f"Error loading model: {e}")
    model = None

@app.route('/')
def home():
    return "Server is running!"

@app.route('/classify', methods=['POST'])
def classify():
    # Periksa apakah model sudah dimuat
    if model is None:
        return jsonify({'error': 'Model is not loaded properly'}), 500

    # Periksa apakah ada file gambar yang diunggah
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400

    file = request.files['image']

    # Periksa apakah file memiliki nama
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    # Periksa apakah file yang diupload adalah gambar (PNG, JPEG, atau JPG)
    if file and (file.filename.endswith('.png') or file.filename.endswith('.jpg') or file.filename.endswith('.jpeg')):
        try:
            # Membaca gambar dari file yang diunggah menggunakan PIL
            img = Image.open(io.BytesIO(file.read()))
            img = img.convert('RGB')  # Pastikan gambar dalam format RGB

            # Resize gambar ke ukuran yang sesuai dengan input model
            img = img.resize((224, 224))

            # Konversi gambar menjadi array numpy
            img_array = np.array(img) / 255.0  # Normalisasi gambar

            # Tambahkan dimensi batch (karena model membutuhkan input batch)
            img_array = np.expand_dims(img_array, axis=0)

            # Prediksi
            predictions = model.predict(img_array)
            class_idx = int(np.argmax(predictions, axis=1)[0])

            # Kembalikan hasil klasifikasi
            return jsonify({'class': class_idx, 'confidence': float(predictions[0][class_idx])}), 200

        except Exception as e:
            return jsonify({'error': f"Error processing image: {str(e)}"}), 500
    else:
        return jsonify({'error': 'Invalid image format. Please upload PNG, JPG, or JPEG image.'}), 400

if __name__ == '__main__':
    # Pastikan server dapat diakses dari jaringan lain
    app.run(host='0.0.0.0', port=5000, debug=True)
