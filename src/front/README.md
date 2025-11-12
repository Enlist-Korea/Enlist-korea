# Enlist-Korea (병무청 모집병 조회 서비스)

병무청 공공 API를 활용하여, 현재 지원 가능한 모집병 정보를 한눈에 조회할 수 있는 웹 애플리케이션입니다.
![alt text](/img/MainPage-Screenshot.png)
![alt text](/img/DetailPage-Screenshot.png)

## 프로젝트 소개

`Enlist-Korea`는 복잡한 병무청 모집병 웹사이트를 탐색할 필요 없이, 사용자가 원하는 조건의 모집병 공고를 쉽고 빠르게 찾을 수 있도록 돕기 위해 시작되었습니다.

## ✨ 주요 기능

프로젝트의 핵심 기능은 다음과 같습니다.

- **모집 공고 조회**: 현재 `모집중`이거나 `모집예정`인 공고 목록을 카드 형태로 제공합니다. (`ListPage.jsx`)
- **실시간 필터링**: 사용자의 입력에 따라 즉각적으로 결과를 필터링합니다. (`ListPage.jsx`)
  - 특기명 검색 (예: "포병레이더")
  - 군종 (육군, 해군, 공군, 해병대)
  - 모집 구분 (기술행정병, 일반병 등)
  - 모집 상태 (모집중, 모집예정)
- **상세 정보 제공**: 카드를 클릭하면 해당 특기의 상세 페이지로 이동합니다. (`DetailPage.jsx`)
- **탭(Tab) 기반 정보 분류**: 상세 페이지 내에서 관련 정보를 3개의 탭으로 나누어 제공합니다. (`DetailPage.jsx`)
  - 군사특기임무 및 설명
  - 이달의 모집 계획 (기능 구현 예정)
  - 관련 공지사항
- **동적 상태 계산**: 모집 시작일과 종료일을 기준으로 '모집중', '모집예정', '마감' 및 'D-day'를 자동으로 계산합니다. (`api.js`, `dateUtils.js`)

## ⚙️실행 방법

1. **전제조건**: [Node.js](https://nodejs.org/ko/) (LTS 버전 권장) 설치
2. **저장소 복제(Clone)**

   ```bash
   git clone [저장소 URL]
   cd Enlist-Korea
   ```

3. **의존성 라이브러리 설치**

   ```bash
   npm install
   ```

4. **개발 서버 실행**

   ```bash
   npm run dev
   ```

5. 터미널에 출력된 주소(보통 `http://localhost:5173`)로 접속합니다.

## 폴더 구조

```bash
Enlist-korea
├─ .prettierrc.json
├─ eslint.config.js
├─ index.html
├─ package-lock.json
├─ package.json
├─ README.md
├─ src
│  ├─ api
│  │  └─ api.js # DB에서 보내주는 데이터 가공
│  ├─ App.jsx
│  ├─ components
│  │  ├─ Card.jsx # 카드 컴포넌트
│  │  └─ Loader.jsx # 로딩 스피너
│  ├─ css # CSS Modules 방식 적용
│  │  ├─ Card.module.css
│  │  ├─ DetailPage.module.css
│  │  ├─ global.css
│  │  ├─ ListPage.module.css
│  │  └─ Loader.module.css
│  ├─ data
│  │  ├─ mmaData.json # 군사특기임무 정보
│  │  ├─ mmaNotices.json  # 공지사항 정보
│  │  └─ MockData.js # 임시 목업 데이터
│  ├─ hooks # 사용자 정의 Hook
│  │  └─ useSpecialtyData.js
│  ├─ main.jsx
│  ├─ pages
│  │  ├─ DetailPage.jsx   # 자세히 보기 버튼 클릭 시 이동
│  │  └─ ListPage.jsx     # 메인 페이지 (모집병 정보 나열)
│  └─ utils
│     ├─ Constants.js     # 상수 객체 파일
│     └─ dateUtils.js     # 날짜 계산 함수
├─ UML.puml
└─ vite.config.js

```

## 🌊 현재 데이터 흐름 (Front-end Only)

현재는 백엔드 서버 없이 프론트엔드에서 Mock Data를 사용하여 전체 흐름이 진행됩니다.

1. **리스트 페이지 (ListPage)**
   1. `ListPage.jsx`가 마운트되면 `useEffect`가 `loadData()`를 호출합니다.
   2. `loadData()`는 `api/api.js`의 `fetchRecruitments()` 함수를 실행합니다.
   3. `fetchRecruitments()`는 `data/MockData.js`에서 데이터를 가져옵니다.
   4. `utils/dateUtils.js`의 `getRecruitmentStatus()`를 사용해 각 항목에 `status` (모집중/모집예정)와 `daysRemainingText` (D-day)를 동적으로 추가합니다.
   5. `status`가 '모집마감'이 아닌 공고만 필터링하여 `ListPage.jsx`에 반환합니다.
   6. 사용자가 필터(검색어, 군종 등)를 변경하면 `useEffect`가 다시 동작하여 `filteredItems` 상태를 업데이트하고 화면을 다시 그립니다.

2. **상세 페이지 (DetailPage)**
   1. 사용자가 `Card.jsx`의 '자세히 보기' 버튼을 클릭합니다.
   2. `react-router-dom`이 URL을 `/details/[특기코드]?name=[특기명]`으로 변경하고 `DetailPage.jsx`를 렌더링합니다.
   3. `DetailPage.jsx`는 `useParams` Hook으로 URL의 `id` (특기코드)를 가져옵니다.
   4. 가져온 `id`를 `hooks/useSpecialtyData.js` Hook에 전달합니다.
   5. `useSpecialtyData` Hook은 `data/mmaData.json` 파일을 읽어 `id`가 일치하는 특기 정보를 찾아 반환합니다.
   6. `DetailPage.jsx`는 반환된 데이터를 받아 3개의 탭(임무, 계획, 공지)에 맞게 렌더링합니다. (공지사항 탭은 `data/mmaNotices.json` 참조)

## 향후 계획

### 실제 API 연결

- `api/api.js`가 MockData 대신 실제 백엔드 API 서버와 통신하도록 수정

### 세부정보 스크립트화

- `DetailPage`의 세부 정보를 주기적으로 호출하는 스크립트 작업 구현

### 가산점 계산 서비스

- Back-End의 계산식을 바탕으로 사용자가 드롭박스 메뉴를 선택하면 가산점을 계산하여 편리하게 이용할 수 있도록 합니다.
