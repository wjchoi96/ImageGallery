# KakaoGallery
- email address : 1406073@naver.com <br />

## Introduction
포털 사이트 Daum 플랫폼에서 이미지 검색과 검색된 이미지를 보관함에 저장하는 안드로이드 어플리케이션
1. 검색어에 걸맞는 이미지, 비디오 검색 결과를 섬네일 이미지로 표시
2. 저장하고 싶은 이미지는 이미지 보관함에 저장 

## Docs
 - [스케쥴 관리](https://full-growth-4d2.notion.site/KakaoGallery-e1de93d4a6cb452989253006bc06e59d)

## Development Environment
- kotlin
- Android Studio Bumblebee | 2021.1.1 Patch 2

## Application Version
- minSdkVersion : 23
- targetSdkVersion : 31

## APIs
### Rx
- RxKotlin <br /> 
  https://github.com/ReactiveX/RxKotlin 
- RxAndroid <br />
  https://github.com/Reactivex/Rxandroid/wiki 

### 네트워크 통신
- Retrofit2 <br />
  https://github.com/square/retrofit
- Retrofit2 RxJava adapter <br />
  https://github.com/square/retrofit/tree/master/retrofit-adapters/rxjava3 
- Retrofit2 Gson Converter <br />
  https://github.com/square/retrofit/tree/master/retrofit-converters/gson

#### HTTP 요청 및 응답 데이터 로그
- OkHttp Logging Interceptor <br />
  https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor 
  
### 이미지 검색
- 카카오 이미지 검색 api : Kakao Image Search Api <br />
  https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-image 
- 카카오 비디오 검색 api : Kakao Video Search Api <br />
  https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide#search-video

### 이미지 불러오기
- Glide <br />
  https://github.com/bumptech/glide 
  
## Package
``` 
📂app
 ┣ 📂data
 ┃ ┣ 📂datasource
 ┃ ┣ 📂entity
 ┃ ┣ 📂local 
 ┃ ┣ 📂remote 
 ┃ ┣ 📂repository
 ┃ ┗ 📂util
 ┣ 📂domain
 ┃ ┣ 📂model
 ┃ ┣ 📂repository
 ┃ ┗ 📂util
 ┗ 📂presentaion
   ┣ 📂application
   ┣ 📂di
   ┣ 📂network
   ┣ 📂ui
   ┣ 📂util
   ┗ 📂viewmodel
```

