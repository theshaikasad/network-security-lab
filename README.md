# network-security-lab
# Network Security Lab - Complete Setup Guide

## Prerequisites

### 1. Java Development Kit (JDK) 11 or higher
```bash
java -version
```

### 2. Apache Maven
```bash
mvn -version
```

### 3. Wireshark/TShark (for packet capture)
```bash
# Ubuntu/Debian
sudo apt-get install wireshark tshark

# MacOS
brew install wireshark

# Windows - Download from wireshark.org
```

### 4. libpcap (Linux/Mac) or WinPcap/Npcap (Windows)
```bash
# Ubuntu/Debian
sudo apt-get install libpcap-dev

# MacOS (usually pre-installed)
brew install libpcap
```

---

## Question 1: Telnet/SSH Server-Client with Packet Capture

### Project Structure
```
network-security-lab/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           ├── TelnetServer.java
│           ├── TelnetClient.java
│           ├── PcapAnalyzer.java
│           ├── SSHServer.java
│           ├── SSHClient.java
│           └── EchoShellFactory.java
```

### Step 1: Setup Maven Project

1. **Create project directory:**
```bash
mkdir network-security-lab
cd network-security-lab
```

2. **Create the pom.xml file** with the provided Maven dependencies

3. **Create the source directory structure:**
```bash
mkdir -p src/main/java
```

4. **Copy all Java files** to `src/main/java/`

5. **Compile the project:**
```bash
mvn clean compile
```

### Step 2: Run Telnet Server-Client Demo

#### Terminal 1 - Start Telnet Server
```bash
mvn exec:java -Dexec.mainClass="TelnetServer"
```
**Output:** `Server is listening on port 2323`

#### Terminal 2 - Start Packet Capture
```bash
# Capture packets on loopback interface
sudo tshark -i lo -w telnet_capture.pcap -f "tcp port 2323"

# On Windows:
tshark -i "Adapter for loopback traffic capture" -w telnet_capture.pcap -f "tcp port 2323"
```

#### Terminal 3 - Connect Using Telnet
```bash
# Using system telnet
telnet localhost 2323

# OR use the Java client
mvn exec:java -Dexec.mainClass="TelnetClient"
```

#### Test the Connection
Type messages in the telnet client:
```
Hello World
This is a test message
Sensitive data in plain text
exit
```

#### Stop Packet Capture
Press `Ctrl+C` in Terminal 2 to stop tshark

### Step 3: Analyze PCAP File
```bash
# Compile and run the analyzer
mvn exec:java -Dexec.mainClass="PcapAnalyzer" -Dexec.args="telnet_capture.pcap"
```

**Expected Output:**
- ✅ You will see all captured packets
- ✅ Plain text messages are visible in the payload
- ✅ Both hex dump and ASCII representation shown
- ⚠️ **SECURITY NOTE: Data is transmitted in plain text!**

### Step 4: Run SSH Server-Client Demo

#### Terminal 1 - Start SSH Server
```bash
mvn exec:java -Dexec.mainClass="SSHServer"
```

**Credentials shown:**
- Username: `admin`
- Password: `password`
- Port: `2222`

#### Terminal 2 - Start Packet Capture
```bash
sudo tshark -i lo -w ssh_capture.pcap -f "tcp port 2222"
```

#### Terminal 3 - Connect SSH Client
```bash
# Using the Java SSH client
mvn exec:java -Dexec.mainClass="SSHClient"

# OR use system SSH client
ssh -p 2222 admin@localhost
# Password: password
```

#### Test SSH Connection
Type the same messages:
```
Hello World
This is a test message
Sensitive data (now encrypted!)
exit
```

#### Stop Packet Capture
Press `Ctrl+C` in Terminal 2

### Step 5: Analyze SSH Packets
```bash
mvn exec:java -Dexec.mainClass="PcapAnalyzer" -Dexec.args="ssh_capture.pcap"
```

**Expected Output:**
- ✅ Encrypted payload data
- ✅ No readable plain text messages
- 🔒 **SECURITY NOTE: Data is encrypted by SSH!**

### Step 6: Compare Telnet vs SSH

#### View in Wireshark GUI:
```bash
wireshark telnet_capture.pcap &
wireshark ssh_capture.pcap &
```

**Observations:**
- **Telnet:** Follow TCP Stream shows plain text
- **SSH:** Follow TCP Stream shows encrypted data
- **Security Comparison:** SSH provides confidentiality, Telnet does not

---

## Question 2: JWT Web Application

### Step 1: Setup Project
If using the same project, just add `JWTServer.java` to `src/main/java/`

### Step 2: Compile
```bash
mvn clean compile
```

### Step 3: Run JWT Server
```bash
mvn exec:java -Dexec.mainClass="JWTServer"
```
**Output:** `JWT Server started on port 8080`

### Step 4: Access Web Interface
Open your browser and navigate to:
```
http://localhost:8080
```

### Step 5: Test JWT Authentication

#### Test 1: Login
1. Click **"Login"** button with default credentials:
   - Username: `admin`
   - Password: `password`
2. Observe the JWT token generated
3. Note the expiration time (1 hour from now)

#### Test 2: Access Protected Resource
1. Click **"Access Protected Data"**
2. Should succeed if logged in
3. Shows user information from JWT claims

#### Test 3: Verify Token
1. Click **"Verify Current Token"**
2. Shows token validation details
3. Displays issued and expiration timestamps

#### Test 4: Test Invalid Token
1. Clear your token (refresh page without logging in)
2. Try accessing protected resource
3. Should receive **"No token provided"** error

### Step 6: Test with cURL (Command Line)

#### Login Request:
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```
Save the token from response.

#### Access Protected Resource:
```bash
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### Verify Token:
```bash
curl http://localhost:8080/api/verify \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### Test Invalid Credentials:
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}'
```
**Expected:** `{"message":"Invalid credentials"}`

---

## Common Issues and Solutions

### Issue 1: Permission Denied (Packet Capture)
**Solution:**
```bash
# Linux - Add user to wireshark group
sudo usermod -a -G wireshark $USER
sudo chmod +x /usr/bin/dumpcap

# Or run with sudo
sudo mvn exec:java -Dexec.mainClass="PcapAnalyzer" -Dexec.args="capture.pcap"
```

### Issue 2: Port Already in Use
**Solution:**
```bash
# Check what's using the port
sudo lsof -i :2323  # or :2222 or :8080

# Kill the process
kill -9 <PID>
```

### Issue 3: Maven Dependencies Not Downloading
**Solution:**
```bash
mvn dependency:purge-local-repository
mvn clean install
```

### Issue 4: SSH Connection Refused
**Solution:**
- Ensure SSH server is running
- Check firewall settings
- Verify port 2222 is available

### Issue 5: PCAP File Not Found
**Solution:**
```bash
# Check current directory
ls -l *.pcap

# Use absolute path
mvn exec:java -Dexec.mainClass="PcapAnalyzer" -Dexec.args="/full/path/to/capture.pcap"
```

---

## Understanding the Output

### Telnet Packet Analysis
```
Packet #5
Protocol: TCP
Source Port: 54321
Destination Port: 2323

--- PAYLOAD (Plain Text) ---
Hello World
--- END PAYLOAD ---

✅ Message is readable - Security Risk!
```

### SSH Packet Analysis
```
Packet #12
Protocol: TCP
Source Port: 54567
Destination Port: 2222

--- PAYLOAD (Plain Text) ---
�k�2�8��d�f�...��
--- END PAYLOAD ---

✅ Message is encrypted - Secure!
```

### JWT Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": 1729876543210,
  "message": "Login successful"
}
```

**Token Structure:**
- **Header:** Algorithm (HS256)
- **Payload:** User claims (username, exp, iat)
- **Signature:** HMAC signature for verification

---

## Additional Experiments

### Experiment 1: Token Expiration
Modify `EXPIRATION_TIME` in `JWTServer.java` to 10 seconds:
```java
private static final long EXPIRATION_TIME = 10000; // 10 seconds
```
Test token expiration by waiting and trying to access protected resource.

### Experiment 2: Custom Claims
Add custom claims to JWT:
```java
.claim("role", "admin")
.claim("department", "IT")
```

### Experiment 3: Different Ports
Change server ports and capture on different ports to avoid conflicts.

---

## Clean Up

```bash
# Stop all Java processes
pkill -f "java.*TelnetServer"
pkill -f "java.*SSHServer"
pkill -f "java.*JWTServer"

# Remove capture files
rm *.pcap

# Clean Maven build
mvn clean
```

---

## Learning Outcomes

### ✅ Question 1:
- Understanding plain text vs encrypted communication
- Packet capture and analysis with tshark/Wireshark
- Security implications of Telnet vs SSH
- Analyzing network traffic programmatically

### ✅ Question 2:
- JWT authentication workflow
- Token generation and validation
- Stateless authentication
- RESTful API security
