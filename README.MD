## 환경 설정

npm install 실행

node_modules/@trufflesuite/web3-provider-engine/subproviders/rpc.js 파일에 jwt 설정

## 테스트

Swagger UI 주소 :  http://localhost:5000/swagger-ui.html#/

#### 테스트 시나리오

1. 인증서 발급
2. 구매자 : 구매 요청 (인증서 파일 입력)
    1. 인증서 검증
    2. Deposit
3. 판매자 : 구매 요청 확인
    1. Deposit 확인
    2. 소유권 이전
4. 구매자 : 구매 확정
    1. 소유권 이전 확인
    2. Withdraw 허가
5. 판매자 : 대금 수령
    1. Withdraw
    
테스트 계정 주소 :

Default:
`0xca1fe1c6382be7563f7a57a53932a23f702a43a5`
(/src/main/resources/credentials/ca1fe1c6382be7563f7a57a53932a23f702a43a5.json)

Buyer:
`0x2bc06b1a8a1cbddf3c63ae3e78f9ffc9d38c303c`
(/src/main/resources/credentials/2bc06b1a8a1cbddf3c63ae3e78f9ffc9d38c303c.json)

Seller:
`0x36c7744371a858542c5cf51adc040492bfa48487`
(/src/main/resources/credentials/36c7744371a858542c5cf51adc040492bfa48487.json)

1. 인증서 발급 : POST /battery/certificate

2. 인증서 검증 : GTE /battery/certificate

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
