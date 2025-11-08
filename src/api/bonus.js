// src/api/bonus.js

const BASE_URL = ""; // 프록시 사용 전제. (절대경로 쓰려면 .env로 주입)

// (선택) 인증 토큰을 헤더에 붙일 수 있도록 entry 제공
let AUTH_TOKEN = null;
export function setAuthToken(token) { AUTH_TOKEN = token || null; }

// 공통 fetch 래퍼: 에러/파싱 일원화
async function http(path, { method = "GET", headers = {}, body } = {}) {
  const h = { Accept: "application/json", ...headers };
  if (AUTH_TOKEN) h.Authorization = `Bearer ${AUTH_TOKEN}`;

  const res = await fetch(`${BASE_URL}${path}`, { method, headers: h, body });
  const isJSON = (res.headers.get("content-type") || "").includes("application/json");
  const data = isJSON ? await res.json() : await res.text();
  if (!res.ok) throw new Error(typeof data === "string" ? data : `HTTP ${res.status}`);
  return data;
}

//  군/세부 카테고리
export async function fetchBonusRules() {
  return {
    branches: [
      { id: "army", label: "육군", subcats: [
        { id: "army-tech-admin", label: "기술행정병" },
        { id: "army-tech-admin-year", label: "기술행정병(연단위)" },
        { id: "army-specialist", label: "전문특기병" },
      ]},
      { id: "navy", label: "해군", subcats: [
        { id: "navy-tech", label: "기술병" },
        { id: "navy-specialist", label: "전문특기병" },
      ]},
      { id: "marine", label: "해병대", subcats: [
        { id: "marine-tech", label: "기술병" },
        { id: "marine-specialist", label: "전문특기병" },
      ]},
      { id: "airforce", label: "공군", subcats: [
        { id: "airforce-tech-specialist", label: "기술병/전문특기병" },
      ]},
    ],
  };
}

// [백엔드 API #1] 옵션 로드: 자격증/전공 셀렉트에 사용
export function getScoreOptions() {
  // GET /api/score/options → { qualifications:[], majors:[] }
  return http("/api/score/options");
}

// [백엔드 API #2] 점수 계산/저장: 모달에서 입력 제출 시 호출
export function requestBranchScores(body) {
  // POST /api/score/branches
  // body: { qualificationLabel, majorTrack, majorLevel, absences, bonusSelected }
  return http("/api/score/branches", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}
