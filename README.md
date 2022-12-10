# KakaoGallery
- email address : 1406073@naver.com <br />

## Introduction
포털 사이트 Daum 플랫폼에서 이미지 검색과 검색된 이미지를 보관함에 저장하는 안드로이드 어플리케이션
1. 검색어에 걸맞는 이미지, 비디오 검색 결과를 섬네일 이미지로 표시
2. 저장하고 싶은 이미지는 이미지 보관함에 저장 

## Branch
|     Sample     | Description |
| ------------- | ------------- |
| [main](https://github.com/wjchoi96/KakaoGallery/tree/main) | 배포용 |
| [rx](https://github.com/wjchoi96/KakaoGallery/tree/rx) | ver1.0.2 RxJava + LiveData |
| [dev](https://github.com/wjchoi96/KakaoGallery/tree/dev) | 개발용 |

## Reference
 - [프로젝트 관리](https://full-growth-4d2.notion.site/KakaoGallery-e1de93d4a6cb452989253006bc06e59d)
 - [트러블 슈팅 및 문서화 모음](https://full-growth-4d2.notion.site/KakaoGallery-32461f6f8e964c85a847805cb6fb7015)
 - [App Release Link](https://appdistribution.firebase.google.com/pub/i/e608755ba7c9f066)

## Development Environment
- kotlin
- Android Studio Dolphin | 2021.3.1 Patch 1

## Application Version
- minSdkVersion : 24
- targetSdkVersion : 33
  
## Package
``` 
📂app
 ┣ 📂data
 ┃ ┣ 📂datasource
 ┃ ┣ 📂entity
 ┃ ┣ 📂repository
 ┃ ┣ 📂service 
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

