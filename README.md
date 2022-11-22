# MusicPlayer_kotlin

## Outline

kotlin과 Android Studio를 활용한 MusicPlayer Application

## Development Environment 

| 구 분 | 내 용 |
| --- | --- |
| OS | Windows 11 Home |
| Language | Kotlin 213-1.7.20 |
| Editor | Android Studio 11.0.13 |
| Github | https://github.com/seopit95/MusicPlayer_kotlin.git |

## App execution video

### Music Play
  
<img src = "https://user-images.githubusercontent.com/115531849/203330648-2c89dd69-efe1-4831-9ca9-8a1ad19187fd.gif" width="40%" height="40%">

### MusicPlay Video content
 - 음악 ItemView를 클릭하면 클릭한 음악플레이어로 Intent 처리
 - play를 클릭하면 음악재생, playing 되고 있는 음악은 일시정지
 - 이전 곡 버튼을 클릭하면 음악이 재생중일 땐 다시 처음으로 돌아가는 정지 기능, 00:00초 상태에서는 이전 곡을 불러옴
 - 다음 곡 버튼을 클릭하면 음악이 Playing 중이여도 다음 곡으로 넘어감
 - 한 곡 반복재생 버튼을 클릭하면 해당 곡만 무한반복되도록 Looping 기능 추가

### Player Event

<img src = "https://user-images.githubusercontent.com/115531849/203333231-f3264bd6-ae83-47fc-a3e9-d03070526ce3.gif" width="40%" height="40%">

### Player Event Video content
 - 찜(하트) 버튼을 클릭하면 해당 곡의 MusicPlayer에도 좋아요가 표시되도록 불러옴
 - 찜(하트)이 된 곡들은 메뉴 기능을 통해 찜한 재생목록에서 한 눈에 볼 수 있도록 기능추가
 - 전체 재생목록을 클릭하면 전체 곡들을 불러옴
 - 검색 기능 추가(글자가 입력되는 순간마다 검색기능 발생)
