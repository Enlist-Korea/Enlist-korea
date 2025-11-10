// src/pages/BonusPage.jsx
import React, { useEffect, useMemo, useState } from "react";
import { fetchBonusRules, requestBranchScores } from "../api/bonus";
import BranchTabs from "../components/BranchTabs";
import SubcatChips from "../components/SubcatChips";
import ResultCard from "../components/ResultCard";
import SectionModal from "../components/SectionModal";

/* ── 병과별 표시 규칙 (필요 시 수정) ───────────────── */
const FORM_RULES = {
  육군: {
    기술행정병: {
      qualification: { use: true, allowCategories: ["nat","ws","gen","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"] }
    },
    "기술행정병(연단위)": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bVeteran","bMulti","bBenefit","bBlood","bVol"] }
    },
    전문특기병: {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bVeteran","bDrivequal"] }
    },
  },
  해군: {
    기술병: {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bVeteran","bMulti","bBlood","bVol"] }
    },
    전문특기병: {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bDrivequal","bVeteran"] }
    },
  },
  해병대: {
    기술병: {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bVeteran","bBlood","bVol"] }
    },
    전문특기병: {
      qualification: { use: true, allowCategories: ["nat","drive","none"], showRelation: false },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bDrivequal","bVeteran"] }
    },
  },
  공군: {
    "기술병/전문특기병": {
      qualification: { use: true, allowCategories: ["nat","gen","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bVeteran","bMulti","bBlood","bVol"] }
    },
  },
};

export default function BonusPage() {
  // 초기 군/세부
  const params = useMemo(() => new URLSearchParams(window.location.search), []);
  const [branches, setBranches] = useState([]);
  const [branch, setBranch] = useState("");   // id
  const [subcat, setSubcat] = useState("");   // id

  // 섹션별 입력 폼
  const [qualForm, setQualForm]   = useState(null);
  const [majorForm, setMajorForm] = useState(null);
  const [attdForm, setAttdForm]   = useState(null);
  const [bonusForm, setBonusForm] = useState(null);

  // 섹션별 소계 점수
  const [qualScore, setQualScore]   = useState(0);
  const [majorScore, setMajorScore] = useState(0);
  const [attdScore, setAttdScore]   = useState(0);
  const [bonusScore, setBonusScore] = useState(0);

  // 총점/안내
  const [score, setScore] = useState(0);
  const [message, setMessage] = useState("");

  // 모달 상태
  const [modalOpen, setModalOpen] = useState(false);
  const [modalSection, setModalSection] = useState(null); // 'qual' | 'major' | 'attd' | 'bonus'

  const [requesting, setRequesting] = useState(false);

  // 규칙 헬퍼
  function getRuleFor(branchLabel, subcatLabel){
    return FORM_RULES?.[branchLabel]?.[subcatLabel] ?? {
      qualification: { use: true, allowCategories: ["nat","ws","gen","drive","none"], showRelation: true },
      major: { use: true }, attendance: { use: true },
      bonus: { use: true, show: ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"] }
    };
  }

  // 데이터 로드 & 쿼리 반영
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

  const subcatLabel = currentBranch.subcats.find((s) => s.id === subcat)?.label || "";
  const branchLabel = currentBranch.label || "";

  // 모달 열기/닫기
  const openSec  = (sec) => { setModalSection(sec); setModalOpen(true); };
  const closeSec = () => { setModalOpen(false); setModalSection(null); };

  // 섹션 저장 (폼만 저장)
  const handleSave = (section, form) => {
    if (section === "qual")  setQualForm(form);
    if (section === "major") setMajorForm(form);
    if (section === "attd")  setAttdForm(form);
    if (section === "bonus") setBonusForm(form);
    setMessage(`${section} 입력 저장됨`);
    closeSec();
  };

  // 백엔드로 payload 조립
  function assemblePayload() {
    const absences =
      attdForm?.aAbsent === "0"   ? 0 :
      attdForm?.aAbsent === "1-4" ? 4 :
      attdForm?.aAbsent === "5-8" ? 8 : 9;

    const bonusSelected = [];
    const pushIf = (key, labelText) => {
      const v = bonusForm?.[key];
      if (v && v !== "") bonusSelected.push({ category: key, label: v || labelText });
    };
    pushIf("bExp", "모집특기 경력");
    pushIf("bOnline", "온라인추천");
    pushIf("bVeteran", "유공자");
    pushIf("bMulti", "다자녀");
    pushIf("bBenefit", "생계급여");
    pushIf("bBlood", "헌혈");
    pushIf("bVol", "봉사");
    pushIf("bDispo", "병역처분변경");
    pushIf("bImmig", "국외이주");
    pushIf("bDrivequal", "운전적성");

    return {
      qualificationLabel:  qualForm?.qGrade || "",
      qualificationCategory: qualForm?.qCategory || "",
      qualificationRelation: qualForm?.qRelation || "",
      majorTrack:          majorForm?.mTrack || "전공",
      majorLevel:          majorForm?.mLevel || "",
      absences,
      bonusSelected,
      branchLabel,
      subcatLabel,
    };
  }

  // 백엔드 요청 → 섹션별 점수 + 총점 반영
  async function handleRequestScore() {
    try {
      setRequesting(true);    
      const payload = assemblePayload();
      const data = await requestBranchScores(payload);

      // ✅ 가능한 응답 포맷을 모두 수용 (백엔드 구조에 맞게 1곳만 쓰여도 OK)
      // 1) 단일 객체: { total, sections: { qualification, major, attendance, bonus } }
      // 2) 단일 객체: { total, qual, major, attd, bonus }
      // 3) 배열: [{ branch:"육군", subcat:"기술...", total, sections:{...}}] → 현재 선택(첫번째) 사용

      const pick = (obj, keys, def = 0) => {
        for (const k of keys) {
          const v = obj?.[k];
          if (typeof v === "number") return v;
        }
        return def;
      };

      let result = data;

      if (Array.isArray(result)) result = result[0] || {};

      // 섹션 점수
      const sec =
        result.sections || result.sectionScores || {
          qualification: result.qual,
          major: result.major,
          attendance: result.attd,
          bonus: result.bonus,
        };

      const nextQual = pick(sec, ["qualification", "qual"]);
      const nextMajor = pick(sec, ["major"]);
      const nextAttd = pick(sec, ["attendance", "attd"]);
      const nextBonus = pick(sec, ["bonus"]);

      setQualScore(Number(nextQual) || 0);
      setMajorScore(Number(nextMajor) || 0);
      setAttdScore(Number(nextAttd) || 0);
      setBonusScore(Number(nextBonus) || 0);

      // 총점
      const nextTotal = pick(result, ["total", "score"]);
      const sumTotal =
        (Number(nextQual)||0) + (Number(nextMajor)||0) + (Number(nextAttd)||0) + (Number(nextBonus)||0);

      setScore(Number.isFinite(nextTotal) && nextTotal > 0 ? nextTotal : sumTotal);
      setMessage("점수 계산 완료");
    } catch (e) {
      console.error(e);
      setMessage(`점수 계산 실패: ${e?.message || "알 수 없는 오류"}`);
      // 실패 시 기존 점수 유지
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

      {/* 4개 섹션 카드: 각 카드 우상단에 소계 점수 표시 */}
      <section className="section grid cols-2">
        <div className="card">
          <div className="section-title" style={{display:"flex",justifyContent:"space-between",alignItems:"center"}}>
            <span>기술자격/면허</span>
            <div className="kpi"><div className="value">{qualScore.toFixed(2)}</div><div className="unit">점</div></div>
          </div>
          <p className="desc">자격 분류/등급/관련도를 입력하세요.</p>
          <button className="btn-primary" style={{ width: "100%" }} onClick={() => openSec("qual")}>
            입력하기
          </button>
        </div>

        <div className="card">
          <div className="section-title" style={{display:"flex",justifyContent:"space-between",alignItems:"center"}}>
            <span>전공</span>
            <div className="kpi"><div className="value">{majorScore.toFixed(2)}</div><div className="unit">점</div></div>
          </div>
          <p className="desc">전공/학력 상태를 선택하세요.</p>
          <button className="btn-primary" style={{ width: "100%" }} onClick={() => openSec("major")}>
            입력하기
          </button>
        </div>

        <div className="card">
          <div className="section-title" style={{display:"flex",justifyContent:"space-between",alignItems:"center"}}>
            <span>출결사항</span>
            <div className="kpi"><div className="value">{attdScore.toFixed(2)}</div><div className="unit">점</div></div>
          </div>
          <p className="desc">결석 일수 구간을 선택하세요.</p>
          <button className="btn-primary" style={{ width: "100%" }} onClick={() => openSec("attd")}>
            입력하기
          </button>
        </div>

        <div className="card">
          <div className="section-title" style={{display:"flex",justifyContent:"space-between",alignItems:"center"}}>
            <span>가산점</span>
            <div className="kpi"><div className="value">{bonusScore.toFixed(2)}</div><div className="unit">점</div></div>
          </div>
          <p className="desc">해당되는 가산 항목을 선택하세요.</p>
          <button className="btn-primary" style={{ width: "100%" }} onClick={() => openSec("bonus")}>
            입력하기
          </button>
        </div>
      </section>

      {/* 결과(총점 = 4개 섹션 합) */}
      <section className="section">
        <ResultCard branchLabel={branchLabel} subcatLabel={subcatLabel} total={score} />
       
      </section>

      {/* 섹션 모달 */}
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
