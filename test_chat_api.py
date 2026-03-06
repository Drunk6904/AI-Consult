import requests
import json

# 测试1: 未注册用户，提供user参数
def test_unregistered_user_with_user():
    url = "http://localhost:8080/api/v1/chat/completions"
    headers = {"Content-Type": "application/json"}
    data = {
        "query": "你好",
        "user": "test_user"
    }
    
    response = requests.post(url, headers=headers, json=data)
    print("测试1: 未注册用户，提供user参数")
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

# 测试2: 未注册用户，不提供user参数
def test_unregistered_user_no_user():
    url = "http://localhost:8080/api/v1/chat/completions"
    headers = {"Content-Type": "application/json"}
    data = {
        "query": "你好"
    }
    
    response = requests.post(url, headers=headers, json=data)
    print("测试2: 未注册用户，不提供user参数")
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

# 测试3: 模拟已注册用户（获取JWT token）
def test_registered_user():
    # 先登录获取token
    auth_url = "http://localhost:8080/api/v1/auth/login"
    auth_data = {
        "username": "admin",
        "password": "admin"
    }
    
    auth_response = requests.post(auth_url, json=auth_data)
    if auth_response.status_code == 200:
        token = auth_response.json().get("token")
        print(f"获取到token: {token}")
        
        # 使用token发送聊天请求
        chat_url = "http://localhost:8080/api/v1/chat/completions"
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}"
        }
        chat_data = {
            "query": "你好",
            "user": "admin"
        }
        
        chat_response = requests.post(chat_url, headers=headers, json=chat_data)
        print("测试3: 已注册用户，使用JWT token")
        print(f"状态码: {chat_response.status_code}")
        print(f"响应: {json.dumps(chat_response.json(), indent=2, ensure_ascii=False)}")
        print()
    else:
        print("登录失败，无法测试已注册用户")
        print(f"登录响应: {json.dumps(auth_response.json(), indent=2, ensure_ascii=False)}")
        print()

if __name__ == "__main__":
    test_unregistered_user_with_user()
    test_unregistered_user_no_user()
    test_registered_user()
