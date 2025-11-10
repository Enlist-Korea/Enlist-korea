// src/pages/BonusPage.jsx
import React, { useEffect, useMemo, useState } from "react";
import { fetchBonusRules, requestBranchScores } from "../api/bonus";
import BranchTabs from "../components/BranchTabs";
import SubcatChips from "../components/SubcatChips";
import ResultCard from "../components/ResultCard";
import SectionModal from "../components/SectionModal";

/* ─────────────────────────────────────────────
 * 1) 병과별 입력 규칙
 * ───────────────────────────────────────────── */
const FORM_RULES = {
  "육군": {
    "기술행정병": {
      qualification: { use: true, allowCategories: ["nat","ws","gen","none"], showRelation: true },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"] }
    },
    "기술행정병(연단위)": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bVeteran","bMulti","bBenefit","bBlood","bVol"] }
    },
    "전문특기병": {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bVeteran","bDrivequal"] }
    }
  },
  "해군": {
    "기술병": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bVeteran","bMulti","bBlood","bVol"] }
    },
    "전문특기병": {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bDrivequal","bVeteran"] }
    }
  },
  "해병대": {
    "기술병": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bVeteran","bBlood","bVol"] }
    },
    "전문특기병": {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bDrivequal","bVeteran"] }
    }
  },
  "공군": {
    "기술병/전문특기병": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major:        { use: true },
      attendance:   { use: true },
      bonus:        { use: true, show: ["bExp","bVeteran","bMulti","bBlood","bVol"] }
    }
  }
};
const getRuleFor = (branchLabel, subcatLabel) =>
  FORM_RULES?.[branchLabel]?.[subcatLabel] ?? {
    qualification: { use: true, allowCategories: ["nat","ws","gen","drive","none"], showRelation: true },
    major:        { use: true },
    attendance:   { use: true },
    bonus:        { use: true, show: ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"] }
  };

/* ─────────────────────────────────────────────
 * 2) 테스트용 계산기 (팝업 저장 → 카드 점수 즉시 반영)
 *    - 백엔드 붙일 때는 USE_MOCK=false로 끄면 됨
 * ───────────────────────────────────────────── */
const USE_MOCK = true;

// (임시) 기술자격/면허 점수
const calcQualMock = (f) => {
  if (!f) return 0;
  const baseByGrade = {
    "기사이상": 50, "산업기사": 45, "기능사": 40,
    "L6, L5": 50, "L4, L3": 45, "L2": 40,
    "공인": 30, "일반": 26,
    "1종 대형/특수": 42, "기타(차량운전분야 특기 한함)": 40, // 운전면허는 50스케일에 맞춰 약식
    "미소지": 0,
  };
  const base = baseByGrade[f.qGrade] ?? 0;
  const relAdj = f.qRelation === "indirect" ? -5 : 0; // 간접관련 -5 (임시)
  return Math.max(0, Math.min(50, base + relAdj));      // 섹션 만점 50
};

// (임시) 전공 점수
const calcMajorMock = (f) => {
  if (!f) return 0;
  const level = {
    "4학년 수료이상": 40, "4학년 재학": 38, "3학년 수료": 36, "3학년 재학": 34,
    "2학년 수료": 32, "2학년 재학": 30, "1학년 수료": 28, "1학년 재학": 26,
    "3년 수료": 40, "3년 재학": 38, "2년 수료": 36, "2년 재학": 34,
    "고졸": 20, "한국폴리텍대학 2년 이상 수료": 32,
    "한국폴리텍대학 1년 이상 수료": 30, "직업능력개발원 6개월 이상 수료": 26
  }[f.mLevel] ?? 0;
  const trackAdj = f.mTrack === "비전공" ? -12 : 0;
  return Math.max(0, Math.min(40, level + trackAdj));   // 섹션 만점 40
};

// (임시) 출결 점수
const calcAttdMock = (f) => {
  if (!f) return 0;
  return { "0": 5, "1-4": 4, "5-8": 3, "9+": 2 }[f.aAbsent] ?? 0; // 만점 5
};

// (임시) 가산점 점수
const calcBonusMock = (f) => {
  if (!f) return 0;
  const pick = (v) => (/\((\d)점/.exec(v || "")?.[1] ? Number(/\((\d)점/.exec(v)[1]) : 0);
  const sum = [
    pick(f.bExp), pick(f.bOnline), pick(f.bVeteran), pick(f.bMulti),
    pick(f.bBenefit), pick(f.bDispo), pick(f.bImmig), pick(f.bDrivequal),
    Math.min(3, pick(f.bBlood) + pick(f.bVol)), // 헌혈+봉사 합산 최대 3
  ].reduce((a,b)=>a+b,0);
  return Math.min(10, sum); // 섹션 만점 10
};

/* ─────────────────────────────────────────────
 * 3) BonusPage
 * ───────────────────────────────────────────── */
export default function BonusPage() {
  const params = useMemo(() => new URLSearchParams(window.location.search), []);

  // 군/세부 카테고리
  const [branches, setBranches] = useState([]);
  const [branch, setBranch] = useState("");
  const [subcat, setSubcat] = useState("");

  // 섹션 폼 & 소계 점수
  const [qualForm, setQualForm]   = useState(null);
  const [majorForm, setMajorForm] = useState(null);
  const [attdForm, setAttdForm]   = useState(null);
  const [bonusForm, setBonusForm] = useState(null);

  const [qualScore, setQualScore]   = useState(0);
  const [majorScore, setMajorScore] = useState(0);
  const [attdScore, setAttdScore]   = useState(0);
  const [bonusScore, setBonusScore] = useState(0);

  // 모달
  const [modalOpen, setModalOpen] = useState(false);
  const [modalSection, setModalSection] = useState(null); // "qual" | "major" | "attd" | "bonus"

  // 안내/총점
  const [message, setMessage] = useState("");
  const [total, setTotal] = useState(0);

  // 초기 데이터
  useEffect(() => {
    fetchBonusRules().then((data) => {
      setBranches(data.branches || []);
      const qBranch = params.get("branch");
      const qSubcat = params.get("subcat");
      const first = data.branches?.[0];
      const b = data.branches.find(x => x.id === qBranch) || first;
      setBranch(b?.id || "");
      const sList = b?.subcats || [];
      const s = sList.find(x => x.id === qSubcat) || sList[0];
      setSubcat(s?.id || "");
    });
  }, []);

  const currentBranch = useMemo(
    () => branches.find((b) => b.id === branch) || { label:"", subcats: [] },
    [branches, branch]
  );
  useEffect(() => {
    if (currentBranch.subcats?.length) setSubcat(currentBranch.subcats[0].id);
  }, [branch]);

  const subcatLabel =
    currentBranch.subcats.find((s) => s.id === subcat)?.label || "";
  const branchLabel = currentBranch.label || "";

  // 모달 제어
  const openSec  = (sec) => { setModalSection(sec); setModalOpen(true); };
  const closeSec = () => { setModalOpen(false); setModalSection(null); };

  // 팝업 저장 → 해당 카드 점수 즉시 반영 (테스트용)
  const handleSave = (section, form) => {
    if (section === "qual")  {
      setQualForm(form);
      if (USE_MOCK) setQualScore(calcQualMock(form));
    }
    if (section === "major") {
      setMajorForm(form);
      if (USE_MOCK) setMajorScore(calcMajorMock(form));
    }
    if (section === "attd")  {
      setAttdForm(form);
      if (USE_MOCK) setAttdScore(calcAttdMock(form));
    }
    if (section === "bonus") {
      setBonusForm(form);
      if (USE_MOCK) setBonusScore(calcBonusMock(form));
    }
    setMessage(`${section} 입력 저장됨`);
    closeSec();
  };

  // 결과 확인(총점 계산)
  async function handleRequestScore() {
    // 백엔드 준비 전: 테스트용 합산(소계 합)
    if (USE_MOCK) {
      setTotal(
        (Number(qualScore)||0) +
        (Number(majorScore)||0) +
        (Number(attdScore)||0) +
        (Number(bonusScore)||0)
      );
      setMessage("테스트 합산 완료");
      return;
    }

    // === 백엔드 호출(준비 완료 시) ===
    try {
      // 출결 구간을 백엔드 기대값으로 변환(예시)
      const absences =
        attdForm?.aAbsent === "0"   ? 0 :
        attdForm?.aAbsent === "1-4" ? 4 :
        attdForm?.aAbsent === "5-8" ? 8 : 9;

      // 가산 선택 단순화
      const bonusSelected = [];
      const pushIf = (key, labelText) => {
        const v = bonusForm?.[key];
        if (v && v !== "") bonusSelected.push({ category: key, label: v || labelText });
      };
      ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"]
        .forEach(k => pushIf(k, k));

      const payload = {
        qualificationLabel:  qualForm?.qGrade || "",
        majorTrack:          majorForm?.mTrack || "전공",
        majorLevel:          majorForm?.mLevel || "",
        absences,
        bonusSelected,
        branchLabel,
        subcatLabel,
        qualificationCategory: qualForm?.qCategory || "",
        qualificationRelation: qualForm?.qRelation || "",
      };

      const data = await requestBranchScores(payload);
      // 총점만 사용(백엔드 구조에 맞춰 조정)
      let next = 0;
      if (typeof data === "number") next = data;
      else if (Array.isArray(data)) next = Number(data[0]?.total ?? data[0]?.score ?? 0);
      else if (data && typeof data === "object") next = Number(data.total ?? data.score ?? 0);
      setTotal(Number.isFinite(next) ? next : 0);
      setMessage("점수 계산 완료");
    } catch (e) {
      console.error(e);
      setMessage(`점수 계산 실패: ${e?.message || "알 수 없는 오류"}`);
    }
  }

  return (
    <div className="page container">
      {/* 헤더 */}
      <div className="header">
        <div>
          <h1 className="title">군 가산점 계산</h1>
          {message && <p className="desc" style={{ color: "#cbd6ff" }}>{message}</p>}
        </div>
        <button className="btn-reset" onClick={() => window.location.reload()}>초기화</button>
      </div>

      {/* 군 분류 */}
      <section className="section">
        <h3 className="section-title">군 분류</h3>
        <BranchTabs branches={branches} value={branch} onChange={setBranch} />
      </section>

      {/* 세부 카테고리 */}
      <section className="section">
        <h3 className="section-title">세부 카테고리</h3>
        <SubcatChips items={currentBranch.subcats} value={subcat} onChange={setSubcat} />
      </section>

      {/* 4개 섹션 카드 (각 카드 상단에 소계 점수) */}
      <section className="section grid cards-2x2">
  <div className="card">
    <h3 className="section-title" style={{ display:"flex", justifyContent:"space-between", alignItems:"center" }}>
      <span>기술자격/면허</span>
      <div className="kpi"><div className="value">{qualScore.toFixed(2)}</div><div className="unit">점</div></div>
    </h3>
    <p className="desc">자격 분류/등급/관련도를 입력하세요.</p>
    <button className="btn-primary" style={{ width:"100%" }} onClick={() => openSec("qual")}>
      입력하기
    </button>
  </div>

  <div className="card">
    <h3 className="section-title" style={{ display:"flex", justifyContent:"space-between", alignItems:"center" }}>
      <span>전공</span>
      <div className="kpi"><div className="value">{majorScore.toFixed(2)}</div><div className="unit">점</div></div>
    </h3>
    <p className="desc">전공/학력 상태를 선택하세요.</p>
    <button className="btn-primary" style={{ width:"100%" }} onClick={() => openSec("major")}>
      입력하기
    </button>
  </div>

  <div className="card">
    <h3 className="section-title" style={{ display:"flex", justifyContent:"space-between", alignItems:"center" }}>
      <span>출결사항</span>
      <div className="kpi"><div className="value">{attdScore.toFixed(2)}</div><div className="unit">점</div></div>
    </h3>
    <p className="desc">결석 일수 구간을 선택하세요.</p>
    <button className="btn-primary" style={{ width:"100%" }} onClick={() => openSec("attd")}>
      입력하기
    </button>
  </div>

  <div className="card">
    <h3 className="section-title" style={{ display:"flex", justifyContent:"space-between", alignItems:"center" }}>
      <span>가산점</span>
      <div className="kpi"><div className="value">{bonusScore.toFixed(2)}</div><div className="unit">점</div></div>
    </h3>
    <p className="desc">해당되는 가산 항목을 선택하세요.</p>
    <button className="btn-primary" style={{ width:"100%" }} onClick={() => openSec("bonus")}>
      입력하기
    </button>
  </div>
</section>

      {/* 결과 카드(총점만 + 결과 확인 버튼 내장) */}
      <section className="section">
        <ResultCard
          branchLabel={branchLabel}
          subcatLabel={subcatLabel}
          total={total}
          onCompute={handleRequestScore}
          disabled={!qualForm || !majorForm || !attdForm}
        />
      </section>

      {/* 섹션 모달 (현재 군/세부 라벨에 맞춰 규칙 적용) */}
      <SectionModal
        open={modalOpen}
        section={modalSection}
        onClose={closeSec}
        onSave={handleSave}
        initial={
          modalSection === "qual"  ? qualForm  :
          modalSection === "major" ? majorForm :
          modalSection === "attd"  ? attdForm  :
          modalSection === "bonus" ? bonusForm : null
        }
        branchLabel={branchLabel}
        subcatLabel={subcatLabel}
      />
    </div>
  );
}
