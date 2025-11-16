# ⚙️ CÀI ĐẶT JAVA_HOME VĨNh VIỄN

## ⚠️ VẤN ĐỀ

Hiện tại mỗi lần mở terminal mới phải chạy:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

---

## ✅ GIẢI PHÁP: SET JAVA_HOME VĨNh VIỄN

### **Cách 1: Qua System Properties (Khuyến nghị)**

1. **Mở System Properties:**
   - Nhấn `Windows + R`
   - Gõ: `sysdm.cpl`
   - Nhấn Enter

2. **Vào Environment Variables:**
   - Click tab **Advanced**
   - Click button **Environment Variables...**

3. **Tạo JAVA_HOME (System variables):**
   - Trong phần **System variables** (phần dưới)
   - Click **New...**
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-17`
   - Click **OK**

4. **Update PATH:**
   - Trong **System variables**, tìm biến `Path`
   - Click **Edit...**
   - Click **New**
   - Thêm: `%JAVA_HOME%\bin`
   - Di chuyển lên đầu list (quan trọng!)
   - Click **OK**

5. **Apply changes:**
   - Click **OK** trên tất cả dialogs
   - **Đóng và mở lại terminal**

6. **Verify:**
   ```powershell
   java -version
   mvn -version
   ```
   
   Cả 2 phải hiển thị Java 17!

---

### **Cách 2: Qua PowerShell (Nhanh)**

```powershell
# Set JAVA_HOME vĩnh viễn
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# Update PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = "%JAVA_HOME%\bin;" + $currentPath
[System.Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
```

**Lưu ý:** Cần chạy PowerShell **as Administrator**

---

### **Cách 3: Qua Command Prompt (Admin)**

```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-17" /M
setx PATH "%JAVA_HOME%\bin;%PATH%" /M
```

---

## 🔍 VERIFY INSTALLATION

Sau khi set xong, **đóng và mở lại terminal**, rồi chạy:

```powershell
# Check JAVA_HOME
echo $env:JAVA_HOME
# Output: C:\Program Files\Java\jdk-17

# Check Java version
java -version
# Output: java version "17.0.12"

# Check Maven Java version
mvn -version
# Output: Java version: 17.0.12
```

---

## 🚀 RUN PROJECT

Sau khi set JAVA_HOME vĩnh viễn, chỉ cần chạy:

```powershell
cd C:\Users\Admin\Documents\PQ_FE-main

# Build
mvn clean install

# Run
mvn spring-boot:run
```

Không cần set JAVA_HOME mỗi lần nữa! 🎉

---

## ⚠️ TROUBLESHOOTING

### **Maven vẫn dùng Java 8**

**Nguyên nhân:** PATH có nhiều Java version, Java 8 đứng trước Java 17

**Giải pháp:**
1. Mở Environment Variables
2. Edit biến `Path`
3. Di chuyển `%JAVA_HOME%\bin` lên **đầu tiên**
4. Xóa các path Java 8 cũ (nếu có)
5. Restart terminal

---

### **Không tìm thấy jdk-17**

**Kiểm tra đường dẫn:**
```powershell
dir "C:\Program Files\Java"
```

**Nếu Java 17 ở đường dẫn khác:**
- Update JAVA_HOME với đường dẫn đúng
- Ví dụ: `C:\Program Files\Java\jdk-17.0.12`

---

### **Permission denied khi set**

**Giải pháp:**
- Chạy PowerShell/CMD **as Administrator**
- Hoặc dùng Cách 1 (System Properties)

---

## 📝 NOTES

1. **JAVA_HOME** phải trỏ đến thư mục JDK (không phải JRE)
2. **PATH** phải có `%JAVA_HOME%\bin` ở đầu
3. **Restart terminal** sau khi thay đổi
4. **Restart IDE** (IntelliJ, Eclipse) nếu dùng

---

## ✅ CHECKLIST

- [ ] Set JAVA_HOME = `C:\Program Files\Java\jdk-17`
- [ ] Update PATH với `%JAVA_HOME%\bin`
- [ ] Di chuyển `%JAVA_HOME%\bin` lên đầu PATH
- [ ] Restart terminal
- [ ] Verify: `java -version` → Java 17
- [ ] Verify: `mvn -version` → Java 17
- [ ] Test: `mvn clean compile` → SUCCESS

---

## 🎯 DONE!

Sau khi hoàn thành, bạn có thể:
- Build project bất cứ lúc nào
- Không cần set JAVA_HOME mỗi lần
- Maven tự động dùng Java 17

**Happy Coding!** 🚀
