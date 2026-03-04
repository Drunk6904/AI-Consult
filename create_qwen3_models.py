import requests
import json

# Xinference API endpoint
BASE_URL = "http://localhost:9998/v1"

def create_model(model_name, model_type, model_path):
    """创建模型"""
    url = f"{BASE_URL}/models"
    
    # 对于不同类型的模型，使用不同的参数格式
    if model_type == "rerank":
        # 重排序模型可能不需要parameters字段
        payload = {
            "model_name": model_name,
            "model_type": model_type
        }
    else:
        payload = {
            "model_name": model_name,
            "model_type": model_type,
            "parameters": {
                "model_path": model_path
            }
        }
    
    print(f"Creating {model_type} model: {model_name}")
    print(f"Model path: {model_path}")
    
    response = requests.post(url, json=payload)
    
    if response.status_code == 200:
        print(f"✅ Success: {model_name} created")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
    else:
        print(f"❌ Error: {response.status_code}")
        print(f"Response: {response.text}")
    
    print("=" * 50)

def list_models():
    """列出所有模型"""
    url = f"{BASE_URL}/models"
    response = requests.get(url)
    
    if response.status_code == 200:
        print("Current models:")
        print(json.dumps(response.json(), indent=2))
    else:
        print(f"Error listing models: {response.status_code}")
    
    print("=" * 50)

if __name__ == "__main__":
    print("=== Creating Qwen3 models in Xinference ===")
    print()
    
    # 创建语言模型 (LLM) - Qwen3 1.7B
    # 尝试使用"text-generation"作为模型类型
    create_model("Qwen3-1.7B", "text-generation", "Qwen/Qwen3-1.7B")
    
    # 创建嵌入模型 (Embedding) - Qwen3 0.6B
    # 使用正确的模型名称
    create_model("Qwen3-Embedding-0.6B", "embedding", "Qwen/Qwen3-Embedding-0.6B")
    
    # 创建重排序模型 (Reranker) - Qwen3 0.6B
    # 使用正确的模型名称
    create_model("Qwen3-Reranker-0.6B", "rerank", "Qwen/Qwen3-Reranker-0.6B")
    
    print("=== Final model list ===")
    list_models()
