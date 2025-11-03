# 군 가산점 계산 페이지 (Frontend)

군 분류(육군/해군/해병대/공군) 및 세부 카테고리를 선택하고, 모달에서 **자격증/면허 · 전공/경력 · 출결 · 가산점 항목**을 입력한 뒤 **백엔드 API**로 점수 계산을 요청하는 프론트엔드입니다.  
(프론트: React + Vite / 백엔드: 기존 운영 API 사용)

- 옵션 로드: `GET /api/score/options`
- 점수 계산: `POST /api/score/branches`
- 군/세부 카테고리 데이터는 프론트 더미로 시작(향후 API 연동 가능)

---

## 실행 방법

> 전제: [Node.js](https://nodejs.org/ko/) 설치

의존성 설치
```bash
npm install
```

개발 서버 실행
```bash
npm run dev
```
Vite 개발 서버 주소(예: `http://localhost:5173`)로 접속하여 페이지를 확인합니다.

---

## 폴더 구조

```
your-project/
├─ public/                              # 정적 리소스(파비콘/OG 이미지 등)
│  └─ favicon.ico                       # (선택) 브라우저 탭 아이콘
├─ src/                                 # 프론트엔드 소스
│  ├─ api/
│  │  └─ bonus.js                       # 점수 옵션/계산 API 호출 + 군 분류 더미
│  ├─ components/
│  │  ├─ BranchTabs.jsx                 # 군 분류 탭 UI(육군/해군/해병대/공군)
│  │  ├─ SubcatChips.jsx                # 세부 카테고리 칩 UI
│  │  ├─ BonusModal.jsx                 # 입력 모달(+ /api 연동 폼)
│  │  └─ ResultCard.jsx                 # 결과 요약 카드(총점/선택 요약)
│  ├─ pages/
│  │  └─ BonusPage.jsx                  # 메인 화면: 탭/칩/모달/결과 조립·상태
│  ├─ utils/
│  │  └─ bonusUtils.js                  # 점수 포맷 등 공용 함수
│  ├─ assets/                           # (선택) 프론트 전용 이미지/아이콘
│  ├─ style.css                         # 프로젝트 공통 스타일
│  ├─ App.jsx                           # 앱 루트(라우팅 기점)
│  └─ main.jsx                          # React 진입점(#root 마운트)
├─ index.html                           # Vite 엔트리 HTML
├─ vite.config.js                       # Vite 개발 서버/프록시 설정
├─ .env.example                         # 환경변수 샘플(VITE_API_BASE_URL 등)
├─ .gitignore                           # 빌드 산출물/환경파일 무시
└─ README.md                            # 실행/구성 문서(본 파일)
```

> 이미지로 표시하고 싶다면 `public/` 아래에 다이어그램을 두고 다음과 같이 참조하세요:  
> `![폴더 구조](folder_structure.png)`

---

## 데이터 흐름 (Front → Back)

1. **페이지 진입**: `BonusPage` 렌더 → 군/세부 카테고리 더미 초기화  
2. **입력 모달 오픈**: “가산 항목 입력하기” 클릭 → `BonusModal` 표시  
3. **옵션 로드**: 모달 열릴 때 `GET /api/score/options` 호출(자격증/전공 선택지)  
4. **사용자 입력**: 자격증/전공/출결/가산 항목 입력  
5. **계산 요청**: “점수 계산 요청” → `POST /api/score/branches`로 서버 전송  
6. **결과 반영**: 응답 수신 → 성공/실패 메시지 표시(필요 시 `ResultCard`에 점수 반영)

---

## 환경 설정

### 1) Vite 프록시 사용(개발 권장)
프론트에서 `/api/...`로 호출하면 Vite가 백엔드로 프록시 ⇒ CORS 이슈 없음.

```js
// vite.config.js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",  // ← 백엔드 주소
        changeOrigin: true,
      },
    },
  },
});
```

### 2) 절대 경로 호출(.env 사용)
프록시 대신 환경변수로 백엔드 주소 설정:

```bash
# .env.example (레포에 커밋 O) — 팀원 공유용 샘플
VITE_API_BASE_URL=https://api.example.com
```

팀원/개발자는 복사 후 실제 주소로 설정:
```bash
cp .env.example .env
# .env (커밋 X)
VITE_API_BASE_URL=http://localhost:8080
```

`src/api/bonus.js`는 `VITE_API_BASE_URL`이 있으면 절대경로, 없으면 프록시(`/api`)를 사용합니다.

---

## FAQ

- **`api/score/options` 파일이 없는데요?**  
  프론트에 파일이 아니라 **백엔드 라우트(URL)** 입니다.  
  없다면 백엔드 구현이 필요하거나, 임시로 프론트에 더미 데이터를 넣고 개발을 진행할 수 있습니다.

- **`favicon.ico`는 필수인가요?**  
  필수는 아니지만 있으면 브라우저 탭 아이콘이 표시되어 좋습니다. `public/`에 두고 `index.html`에서 링크하세요.

- **`.env.example`은 꼭 있어야 하나요?**  
  필수는 아니지만 팀 협업 시 강력 추천합니다. 필요한 환경변수 키를 공유하고 실제 값은 `.env`(커밋 금지)에 둡니다.

---

## 추후 확장

- 서버 응답 구조에 맞춰 `ResultCard`에 **점수/세부 내역 즉시 반영**
- `bonusSelected`를 체크박스/셀렉트로 구현해 **가산 항목 세분화**
- 군/카테고리 더미 → **서버 데이터 연동**
- 입력 유효성 검증/로딩 스켈레톤/에러 메시지 UX 고도화
