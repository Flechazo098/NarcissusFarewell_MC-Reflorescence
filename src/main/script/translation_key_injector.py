import os
import re
import json

def extract_config_keys(server_config_path):
    """从 ServerConfig.java 文件中提取所有配置字段名，并保持原始顺序"""
    try:
        with open(server_config_path, "r", encoding="utf-8") as f:
            content = f.read()

        # 更全面的正则表达式，匹配所有字段声明
        # 这个模式会先匹配可能的注解，然后匹配 public 字段声明
        pattern = re.compile(
            r'(?:@ConfigEntry\.(?:[\w\.]+)(?:\([^)]*\))?\s+)*'  # 匹配所有可能的注解
            r'public\s+(?!static|void|class|interface|enum)'    # 匹配 public 但排除方法和静态字段
            r'(?:[\w<>\.]+\s+)+(\w+)\s*[=;]',                   # 匹配字段类型和名称
            re.MULTILINE | re.DOTALL
        )

        # 提取所有字段名，保持原始顺序
        field_names = pattern.findall(content)

        print(f"找到 {len(field_names)} 个配置字段")
        print(f"字段列表: {', '.join(field_names)}")

        # 生成翻译键和工具提示键，保持原始顺序
        all_keys = []
        for name in field_names:
            main_key = f"text.autoconfig.narcissus_server.option.{name}"
            tooltip_key = f"{main_key}@tooltip"
            all_keys.extend([main_key, tooltip_key])

        return all_keys
    except Exception as e:
        print(f"提取配置键时出错: {e}")
        return []

def add_keys_to_lang_files(lang_dir, keys):
    """向语言文件添加翻译键，按照原始顺序"""
    if not keys:
        print("没有找到要添加的键")
        return

    # 遍历语言目录下的所有 JSON 文件
    for filename in os.listdir(lang_dir):
        if filename.endswith('.json'):
            file_path = os.path.join(lang_dir, filename)
            print(f"处理文件: {file_path}")

            try:
                # 读取 JSON 文件
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = json.loads(f.read())

                # 创建一个新的有序字典，保留原有内容
                new_content = {}

                # 先添加非配置项的键
                for key, value in content.items():
                    if not key.startswith("text.autoconfig.narcissus_server.option."):
                        new_content[key] = value

                # 按照原始顺序添加配置项键
                added_count = 0
                for key in keys:
                    if key in content:
                        new_content[key] = content[key]
                    else:
                        new_content[key] = ""
                        added_count += 1

                # 将更新后的内容写回文件
                with open(file_path, 'w', encoding='utf-8') as f:
                    json.dump(new_content, f, indent=2, ensure_ascii=False)

                print(f"已成功更新文件: {file_path}，添加了 {added_count} 个新键")
            except Exception as e:
                print(f"处理文件 {file_path} 时出错: {e}")

def main():
    # 项目根目录
    project_root = r"F:\code\mcmod\project\NarcissusFarewell_MC-Fabric"

    # ServerConfig.java 文件路径
    server_config_path = os.path.join(project_root, "src", "main", "java", "xin", "vanilla", "narcissus", "config", "ServerConfig.java")

    # 语言文件目录
    lang_dir = os.path.join(project_root, "src", "main", "resources", "assets", "narcissus_farewell", "lang")

    # 检查文件和目录是否存在
    if not os.path.exists(server_config_path):
        print(f"ServerConfig.java 文件不存在: {server_config_path}")
        server_config_path = input("请输入 ServerConfig.java 的完整路径: ")

    if not os.path.exists(lang_dir):
        print(f"语言文件目录不存在: {lang_dir}")
        lang_dir = input("请输入语言文件目录的完整路径: ")

    # 提取配置键
    keys = extract_config_keys(server_config_path)
    print(f"从 ServerConfig.java 中提取了 {len(keys)} 个翻译键")

    # 添加键到语言文件
    add_keys_to_lang_files(lang_dir, keys)
    print("处理完成!")

if __name__ == "__main__":
    main()