# 채팅 재연결 복구 흐름 그림

## 재연결 이후 최신 메시지 복구

```mermaid
sequenceDiagram
    participant UI as 채팅 화면
    participant WS as STOMP 클라이언트
    participant API as 메시지 조회 API

    WS--xUI: 웹소켓 종료 또는 오류
    UI->>UI: 재연결 중 상태 표시
    WS->>WS: 5초 간격 자동 재연결
    WS->>UI: 연결 성공 콜백
    UI->>WS: 현재 채팅방 재구독
    UI->>API: 최신 메시지 50개 조회
    par 조회 중 새 메시지 도착
        WS-->>UI: 실시간 메시지
    and 조회 응답
        API-->>UI: 현재 방의 최신 메시지
    end
    UI->>UI: messageId 기준 병합 및 중복 제거
```

재구독이 최신 조회보다 먼저다. 따라서 조회 중 새 메시지는 웹소켓으로 받을 수 있다. 같은 메시지가 두 경로로 들어와도 `messageId`가 같으면 하나만 남긴다.

## 같은 방 메시지의 저장 순서

```mermaid
sequenceDiagram
    participant A as 트랜잭션 A
    participant Room as ChatRoom 행
    participant B as 트랜잭션 B

    A->>Room: 비관적 쓰기 잠금 획득
    A->>A: 메시지 ID 100 저장
    B->>Room: 잠금 요청
    Note over B,Room: A가 끝날 때까지 대기
    A->>Room: 커밋 후 잠금 해제
    Room-->>B: 잠금 획득
    B->>B: 메시지 ID 101 저장
    B->>Room: 커밋 후 잠금 해제
```

같은 채팅방의 메시지는 방 행 잠금으로 저장과 커밋 순서가 직렬화된다. 다른 채팅방은 다른 행을 사용하므로 서로 기다리지 않는다.

## 선택한 방식과 제외한 방식

```mermaid
flowchart TD
    A["웹소켓 재연결 성공"] --> B["현재 방 재구독"]
    B --> C["기존 API로 최신 50개 조회"]
    C --> D["messageId로 중복 제거"]
    D --> E["최근 누락 메시지 복구 완료"]

    F["afterMessageId API"] -. "사용하지 않음" .-> C
    G["roomSequence 컬럼"] -. "사용하지 않음" .-> C
```

전역 메시지 ID의 빈 구간은 문제가 아니다. 최신 조회는 항상 현재 채팅방으로 제한된다. 대신 최근 누락이 50개를 넘지 않는다는 가정을 명시적으로 받아들이며, 이 가정이 바뀌면 페이지 반복 조회나 방별 연속 번호를 다시 검토한다.
