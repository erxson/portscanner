# portscanner
fork of chinese minecraft/http/tcp port scanner

# start
`java -jar scanner.jar`

# config
```yml
ScanHostAddress:
 - 135.181.126.129 # f7.joinserver.xyz
MinPort: 21
MaxPort: 65535
ScanDelay: 1
AddressThreads: 1
ScanThreads: 64
ConnectTimeout: 2000
ReadTimeout: 2000
OutputFile: "output/%time%.txt"
ShowFails: false
ShowStats: true

LogCurrentIP: false
LogTCP: false
LogHTTP: false

LogKonterStriker: false
LogMinecraft: true
LogPlayerList: true
LogVersion: true

MotdSearch: []
ModsSearch: []
TitleSearch: []
TitleExclude: []
VersionProtocol: 0
```
