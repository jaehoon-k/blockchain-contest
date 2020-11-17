## 테스트

#### Application 실행

###### 실행환경
`Java 11`

`Maven 3.5.3`

다으 명령으로 Application 실행

`mvn spring-boot:run`

Swagger UI 주소 :  http://localhost:5000/swagger-ui.html#/

#### 테스트 시나리오

1. 인증서 발급
2. 구매 요청 (인증서 파일 입력) : 구매자
    1. 인증서 검증
    2. Deposit
3. 구매 요청 확인 : 판매자
    1. Deposit 확인
    2. 소유권 이전
4. 구매 확정 : 구매자
    1. 소유권 이전 확인
    2. Withdraw 허가
5. 대금 수령 : 판매자
    1. Withdraw
    
#### 테스트 계정 주소

Default:
`0xca1fe1c6382be7563f7a57a53932a23f702a43a5`
(/src/main/resources/credentials/ca1fe1c6382be7563f7a57a53932a23f702a43a5.json)

Buyer:
`0x2bc06b1a8a1cbddf3c63ae3e78f9ffc9d38c303c`
(/src/main/resources/credentials/2bc06b1a8a1cbddf3c63ae3e78f9ffc9d38c303c.json)

Seller:
`0x36c7744371a858542c5cf51adc040492bfa48487`
(/src/main/resources/credentials/36c7744371a858542c5cf51adc040492bfa48487.json)

#### 테스트 방법

1. 인증서 발급 : POST /battery/certificate

    - Request Params
        - batteryId : 베터리 아이디
        - dateManufacture : 제조일자
        - manufacturer : 제조사
        - modelNumber : 모델 번호
        - ownerAddr : 인증서 토크을 발급 받을 주소
    - Response
        - owner : 인증서 수요자 주소
        - tokenId : 인증서 토큰 아이디
        - certificate : 인증서 토큰 Metadata json 파일 경로(Token URI)
        - txHash : 트랜잭션 해쉬

2. 구매 요청

    2.1. 인증서 검증 : GET /battery/certificate
    
        - Request Params
            - ownerAddr : 인증서 소유자 주소
            - tokenId : 인증서 토큰 아이디
            - tokenURI : 인증서 토큰 Metadata json 파일 경로
        - Response
            - owner : 인증서 소유자 주소
            - tokenId : 인증서 토큰 아이디
            - tokenURI : 인증서 토큰 Metadata json 파일 경로
            - batteryId : 베터리 아이디
            - isValid": true,
            - modelNumber : 
            - dateManufacture": "12345",
            - manufacturer": "SKI"

    2.2. Deposit : POST /EVToken/depoist
    
        - Request Params
            - amount : deposit할 Token 수
            - sellerAddr : 판매자 주소
        - Response
            - amount : deposit된 토큰 수
            - from(Buyer) : 구매자 주소
            - to(Seller) : 판매자 주소
            
3. 구매 요청 확인

    3.1. Deposit 확인 : GET /EVToken/deposit
    
        - Request Params
            - sellerAddr : 판매자 주소
        - Response
            - amount : deposit된 토큰 수
            - from(Buyer) : 구매자 주소
            - to(Seller) : 판매자 주소
            
    3.2. 소유권 이전 : PUT /battery/certificates/owner
    
        - Request Params
            - fromAddr : 판매자 주소
            - toAddr : 구매자 주소
            - tokenId : 인증서 토큰 아이디
        - Response
            - tokenId : 인증서 토큰 아이디
            - from : 판매자 주소
            - to : 구매자 주소
            - txHash : 트랜잭션 해쉬

4. 구매 확정

    4.1. 소유권 이전 확인 : GET /battery/certificates/owner
    
        - Request Params
            - tokenId : 인증서 토큰 아이디
        - Response
            - owner : 토큰 소유 주소
            - tokenId : 토큰 아이디

    4.2. Withdraw 허가 : POST /EVToken/status
    
        - Request Params
            - sellerAddr : 판매자 주소
            - stat : True(허가), False(불허가)
            
5. 대금 수령 : POST /EVToken/release

        - Request Params
            - sellerAddr : 판매자 주소
        - Response
            - amount : 수령된 토큰 수
            - from(Buyer) : 구매자 주소
            - to(Seller) : 판매자 주소
            
## 스마트컨트랙트 배포

#### 환경 설정

npm install 실행

node_modules/@trufflesuite/web3-provider-engine/subproviders/rpc.js 파일에 jwt 설정

(참고 : https://www.trufflesuite.com/docs/truffle/reference/truffle-commands)

Truffle 설치 후 다음 명령 실행

`truffle migrate --network besu`

또는 다음 명령으로 Truffle Console 실행 후 

`truffle console --network besu `

배포 명령 실행

- migrations 폴더 안에 있는 모든 배포 스크립트 실행

`migrate`

- 특정 배포 스크립트만 실행 (예, 2_deploy_battery_certificates.js 만 실)

`migrate --f 2 --to 2`
