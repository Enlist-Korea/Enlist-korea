// src/api/bonus.js
const BASE_URL = ""; // Vite 프록시 전제

// ▶ 테스트용 목 스위치(백엔드와 연결시 false로 전환)
export const USE_MOCK = false;

let AUTH_TOKEN = null;
export function setAuthToken(t){ AUTH_TOKEN = t || null; }

async function http(path, { method="GET", headers={}, body } = {}){
  const h = { Accept: "application/json", ...headers };
  if (AUTH_TOKEN) h.Authorization = `Bearer ${AUTH_TOKEN}`;
  const res = await fetch(`${BASE_URL}${path}`, { method, headers: h, body });
  const isJSON = (res.headers.get("content-type")||"").includes("application/json");
  const data = isJSON ? await res.json() : await res.text();
  if (!res.ok) throw new Error(typeof data === "string" ? data : `HTTP ${res.status}`);
  return data;
}





// 군/세부 카테고리 (정적)
export async function fetchBonusRules(){
  return {
    branches: [
      { id:"army", label:"육군", subcats:[
        { id:"army-tech-admin", label:"기술행정병" },
        { id:"army-tech-admin-year", label:"기술행정병(연단위)" },
        { id:"army-specialist", label:"전문특기병" },
      ]},
      { id:"navy", label:"해군", subcats:[
        { id:"navy-tech", label:"기술병" },
        { id:"navy-specialist", label:"전문특기병" },
      ]},
      { id:"marine", label:"해병대", subcats:[
        { id:"marine-tech", label:"기술병" },
        { id:"marine-specialist", label:"전문특기병" },
      ]},
      { id:"airforce", label:"공군", subcats:[
        { id:"airforce-tech-specialist", label:"기술병/전문특기병" },
      ]},
    ],
  };
}

/* =========================
 * 테스트용 더미 응답 생성기
 * - 백엔드가 없다면 여기서 점수 계산 흉내
 * ========================= */
function mockCalcScores(payload){
  // 아주 단순한 가중치로 섹션별 점수 흉내
  const qualScore =
    payload.qualificationCategory === "drive" ? 45 :
    payload.qualificationLabel?.includes("기사") ? 50 :
    payload.qualificationLabel?.includes("산업기사") ? 45 :
    payload.qualificationLabel?.includes("기능사") ? 40 : 20;

  const majorScore =
    payload.majorTrack === "비전공" ? 26 :
    payload.majorLevel?.includes("4학년") ? 40 :
    payload.majorLevel?.includes("3학년") ? 36 :
    payload.majorLevel?.includes("2학년") ? 32 : 28;

  const attdScore =
    payload.absences <= 0 ? 5 :
    payload.absences <= 4 ? 4 :
    payload.absences <= 8 ? 3 : 2;

  // 가산점: ()안 숫자 추출해서 더하고 최대 10 제한
  const pointOf = (txt) => {
    const m = /\((\d)점/.exec(txt || "");
    return m ? Number(m[1]) : 0;
  };
  const rawBonus =
    (payload.bonusSelected || []).reduce((sum, it) => sum + pointOf(it.label), 0);
  const bonusScore = Math.min(10, rawBonus);

  const total = qualScore + majorScore + attdScore + bonusScore;
  return { qualScore, majorScore, attdScore, bonusScore, total };
}

// ▶ 백엔드 호출(목/실서버 자동 전환)
export async function requestBranchScores(body){
  if (USE_MOCK){
    // 300ms 지연 뒤 더미 응답
    await new Promise(r => setTimeout(r, 300));
    const s = mockCalcScores(body);
    return {
      branch: body.branchLabel,
      subcat: body.subcatLabel,
      ...s,
    };
  }
  // 실제 서버
  return http("/api/score/branches",{
    method:"POST",
    headers:{ "Content-Type":"application/json" },
    body: JSON.stringify(body),
  });
}

/* =========================
 * 폼 프리필(테스트용)
 * - 페이지 진입 시 자동 채워서 화면 확인
 * ========================= */
export const mockPrefill = {
  qual:  { qCategory:"nat", qGrade:"기사이상", qRelation:"direct" },
  major: { mTrack:"전공",  mLevel:"4학년 수료이상" },
  attd:  { aAbsent:"1-4" },
  bonus: {
    bExp:"6개월 이상~1년 미만 (1점)",
    bOnline:"",
    bVeteran:"해당 (3점)",
    bMulti:"3인 (2점)",
    bBenefit:"",
    bBlood:"2회 (2점)",
    bVol:"",
    bDispo:"",
    bImmig:"",
    bDrivequal:"해당 (2점)"
  }
};

export async function requestScore(dto) {
 
  if (USE_MOCK) {
    return 0;
  }

  return http("/api/crawl/scores", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
}