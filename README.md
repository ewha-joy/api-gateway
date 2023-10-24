# JOY툰
- <a href = "http://27.96.131.221:3000/" > ~~조이툰 이동~~ </a> (현재 서버막아 놓음)
- NFT 기반 웹툰수익 거래 종합 플랫폼입니다. 블록체인 기술인 NFT를 활용해 웹툰 소장권을 자유롭게 거래하고, 토큰 소유자들이 NFT소유 비율에 따라 웹툰 유료열람 수익료를 분배받을 수 있는 새로운 웹툰 종합 플랫폼을 제시합니다.
- NFT Block chain 거래는 Klaytn 을 사용하였습니다.
<br>

## 실행 영상
<a href="https://drive.google.com/file/d/1oVxSsZW-N6JOUlBfHC3bcAGjB3oIVyK4/view?pli=1" target="_blank">
  <picture>
  <img src = "https://github.com/ewha-joy/README/assets/37402084/148be777-6ad2-48b0-98a2-40410167a9cc" width= "600"/> 
  </picture>
</a>
<br>
<br>

## 아키텍처
<img src = "https://github.com/ewha-joy/README/assets/37402084/f4f9af90-2393-4ecb-aea0-92832a307ea3" width= "600"/> 
<br>
<br>


## 사용 기술 및 라이브러리
1. MSA 아키텍처
2. MySQL DB 설계
3. NCP 클라우드 인프라 구축
4. Spring Boot Backend
    1. Eureka Service Discovery
    2. Spring Cloud Config Server
    3. Spring Cloud Bus : `RabbitMQ`
    4. Spring Cloud Gateway
        1. 인증 필터링
            1. 첫 토큰 발행 요청(로그인 요청)을 받으면 인증 서버로 전달해서 Access token과 Refresh Token 받아와서 Client에 전달
            2. Client로부터 Refresh Token을 받아서 확인하고, 유효하면 인증서버에서 새로운 access token을 발급받은 뒤 Client에 전달하고 유효하지 않으면 로그인 하라고 알려주기
        2. 유효하지 않은 접속차단 등 보안 설정
    5. Micro Service
        1. User-service
            1. Spring security
            2. Spring JPA `MySQL`
            3. 로그인 및 인증 관련 CRUD
            4. 유저 캐시 충전 및 환불 기능
            5. Refresh Token과 Access Token 발행 
            6. 로그인시 `Redis`에 Refresh Token 저장
        2. Webtoon-service
            1. 웹툰 회차 및 썸네일 CRUD
            2. 찜 & 댓글 & 별점 CRUD
            3. 웹툰 랭킹 및 개인화 추천 CRUD
        3. Cash-service
            1. 웹툰 유료열람권 구매 기능
            2. 캐시내역 CRUD (feat. `MongoDB`)
    6. Micro Service간 통신
        1. RestTemplate
        2. Feign Client
    7. Event- Driven 아키텍처
        1. `Kafka` Producer & Consumer : 일대다 관계의 통신이 필요한 경우에 사용
        2. `Kafka Connecter`: Source & Sink Connector 생성
        3. mysql - mysql
        4. mong db →  mysql  : Mongo DB source Connector
        5. mysql → mongo db : MySQL sink Connector
    8. Circuit Breaker : `Resilience4j`
5. `ELK` + `Redis` : 로그 스트림 & 모니터링
6. `Sleuth` + `Zipkin` : 분산추적
7. `Docker` + `k8s` + `Jenkins` : CI/CD 구축
<br>

## 실행환경
- java -version : java version "11.0.11" 2021-04-20 LTS
- mvn -v : Apache Maven 3.8.1
- node -v : v15.14.0
- npm -v : 7.20.5
<br>


### 로컬 코드 실행

- <a href = https://github.com/EWHA-JOY> EWHA-JOY </a> 레포지토리 git clone
- 로컬환경에서 백엔드와 프론트엔드 실행 (단, db는 클라우드를 이용함)

1) config-server 실행
2) eureka-server 실행
3) api-gateway 실행
4) cash-service, user-service, webtoon-service 실행
5) NFT-server npm install 후 실행
6) FrontEnd npm install 후 실행
7) 아래의 아이디/비밀번호 사용가능
    - 작가권한 : author_joy1886 / joyjoy1886* 
    - 유저권한 : joy_user_1886 / joyjoy1886*
<br>

### 서버사이드 주소

- <a href = https://115.85.183.11:30000/> ~~아코디언 UI~~ </a>(현재 서버막아 놓음)
- <a href =  "http://49.50.173.118:5601/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h%2Fh,to:now))&_a=(columns:!(),filters:!(),index:cf61f730-fd7f-11eb-a784-f185264fd9b4,interval:auto,query:(language:kuery,query:''),sort:!(!('@timestamp',desc)))"> ~~Kibana (로그 모니터링) 이동~~ </a>(현재 서버막아 놓음)
- <a href = "http://49.50.173.118:5601/app/dashboards#/view/15297bd0-fd85-11eb-a784-f185264fd9b4?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-24h%2Fh,to:now)) "> ~~Kibana (로그 시각화) 이동~~ </a>(현재 서버막아 놓음)
- <a href = "http://49.50.173.118:9411/zipkin/?lookback=7d&endTs=1628991349983&limit=10" > ~~Zipkin (로그 분산추적) 이동~~ </a>(현재 서버막아 놓음)
- <a href = "http://49.50.173.118:5601/app/dashboards#/view/7da31f10-f53b-11eb-a784-f185264fd9b4?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-7d%2Fd,to:now))"> ~~Kibana (로그 분산추적 시각화) 이동~~ </a>(현재 서버막아 놓음)
- <a href = "http://49.50.175.33:9021/clusters/OSV17sK-RG2YtxRL0wdE9A/management/topics?topic_dir=DESC&topic_sort=status"> ~~Kafka 모니터링~~ </a>(현재 서버막아 놓음)
- <a href = "http://101.101.218.57:15672/#"> ~~Rabbit MQ~~ </a>(현재 서버막아 놓음)


