# portscanner
fork of chinese minecraft/http/tcp port scanner

# start
`java -jar scanner.jar`

# config
```yml
MinPort: 20000
MaxPort: 30000
ScanHostAddress:
 - 135.181.126.129 # f7.joinserver.xyz
ScanDelay: 1
AddressThreads: 1
ScanThreads: 100
ConnectTimeout: 2000
ReadTimeout: 2000
OutputFile: "pizda/%time%.txt"
ShowFails: false

LogCurrentIP: true
LogTCP: false
LogHTTP: false

LogKonterStriker: false
LogMinecraft: true
LogPlayerList: true
LogVersion: true

VersionProtocol: 0
TitleSearch: []
TitleExclude: []
```
