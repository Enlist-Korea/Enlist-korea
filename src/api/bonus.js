// src/api/bonus.js
const BASE_URL = ""; // 프록시 전제

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


export function requestBranchScores(body){
  return http("/api/score/branches",{
    method:"POST",
    headers:{ "Content-Type":"application/json" },
    body: JSON.stringify(body),
  });
}
