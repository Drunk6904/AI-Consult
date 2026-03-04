import requests
import json

# Xinference API endpoint
BASE_URL = "http://localhost:9998/v1"

def list_models():
    """列出所有模型"""
    url = f"{BASE_URL}/models"
    response = requests.get(url)
    
    if response.status_code == 200:
        print("Current models:")
        print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    else:
        print(f"Error listing models: {response.status_code}")
        print(f"Response: {response.text}")

if __name__ == "__main__":
    list_models()
