## 환경 설정

npm install 실행

node_modules/@trufflesuite/web3-provider-engine/subproviders/rpc.js 파일에 jwt 설정

## 테스트

Swagger UI 주소 :  http://localhost:5000/swagger-ui.html#/

테스트 계정 주소 :

`0xca1fe1c6382be7563f7a57a53932a23f702a43a5`
(/src/main/resources/ca1fe1c6382be7563f7a57a53932a23f702a43a5.json 월렛 파일의 계정 주소)

## 스마트컨트랙트 배포

(참고 : https://www.trufflesuite.com/docs/truffle/reference/truffle-commands)

Truffle 설치 후 다음 명령 실행

`truffle migrate --network besu`

또는 다음 명령으로 Truffle Console 실행 후 

`truffle console --network besu `

배포 명령 실행

- migrations 폴더 안에 있는 모든 배포 스크립트 실행

`migrate`

- 특정 배포 스크립트만 실행 (예, 4_deploy_erc20.js 만 실)

`migrate --f 4 --to 4`
