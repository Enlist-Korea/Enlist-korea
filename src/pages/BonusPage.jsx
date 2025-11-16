// src/pages/BonusPage.jsx
import React, { useEffect, useMemo, useState } from "react";
import { fetchBonusRules, requestBranchScores } from "../api/bonus";
import BranchTabs from "../components/BranchTabs";
import SubcatChips from "../components/SubcatChips";
import ResultCard from "../components/ResultCard";
import SectionModal from "../components/SectionModal";

/* 1) 병과별 입력 규칙 */
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

/* 2) 페이지 컴포넌트 */
export default function BonusPage() {
  // URL 쿼리
  const params = useMemo(() => new URLSearchParams(window.location.search), []);

  // 군/세부 카테고리
  const [branches, setBranches] = useState([]);
  const [branch, setBranch] = useState("");
  const [subcat, setSubcat] = useState("");

  // 섹션 폼 (팝업에서 저장)
  const [qualForm, setQualForm]   = useState(null);
  const [majorForm, setMajorForm] = useState(null);
  const [attdForm, setAttdForm]   = useState(null);
  const [bonusForm, setBonusForm] = useState(null);

  // 섹션별 점수 & 총점(백엔드 응답으로 채움)
  const [qualScore, setQualScore]   = useState(0);
  const [majorScore, setMajorScore] = useState(0);
  const [attdScore, setAttdScore]   = useState(0);
  const [bonusScore, setBonusScore] = useState(0);
  const [total, setTotal] = useState(0);

  // 모달
  const [modalOpen, setModalOpen] = useState(false);
  const [modalSection, setModalSection] = useState(null); // "qual" | "major" | "attd" | "bonus"

  // 메시지
  const [message, setMessage] = useState("");

  // 초기 군/세부 세팅
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

  // 팝업 저장(점수는 백엔드로 받을 것이므로 여기서는 폼만 저장)
  const handleSave = (section, form) => {
    if (section === "qual")  setQualForm(form);
    if (section === "major") setMajorForm(form);
    if (section === "attd")  setAttdForm(form);
    if (section === "bonus") setBonusForm(form);
    setMessage(`${section} 입력 저장됨`);
    closeSec();
  };

  // Request Body 조립(명세대로)
  function assemblePayload() {
    // 출결: aAbsent(밴드) → 결석 일수 근사치로 변환
    const absences =
      attdForm?.aAbsent === "0"   ? 0 :
      attdForm?.aAbsent === "1-4" ? 4 :
      attdForm?.aAbsent === "5-8" ? 8 : 9;

    // 명세의 확장 출결 필드(현재 UI엔 없음 → 기본값으로 전송)
    const lateCount  = 0;
    const earlyLeave = 0;
    const resultCount = 0;
    const noRecord   = false;
    const specialAvg = false;

    // 가산점: 카테고리당 1개 선택을 {category, label}로 단순화
    const bonusSelected = [];
    const pushIf = (key, labelText) => {
      const v = bonusForm?.[key];
      if (v && v !== "") bonusSelected.push({ category: key, label: v || labelText });
    };
    ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"]
      .forEach(k => pushIf(k, k));

    return {
      // === ScoreRequest (최종 명세) ===
      qualificationLabel:  qualForm?.qGrade || "",     // 라벨 기반
      majorTrack:          majorForm?.mTrack || "전공",
      majorLevel:          majorForm?.mLevel || "",
      absences,
      lateCount,
      earlyLeave,
      resultCount,
      noRecord,
      specialAvg,
      bonusSelected,

      // 선택적으로 현재 선택 정보도 함께(백엔드 분기용)
      branchLabel,
      subcatLabel,

      // 참고 필드(자격 분류/관련도)
      qualificationCategory: qualForm?.qCategory || "",
      qualificationRelation: qualForm?.qRelation || "",
    };
  }

  // 응답에서 현재 선택과 가장 잘 맞는 항목 1개를 고른다
  function pickBestMatch(responseArray) {
    if (!Array.isArray(responseArray) || responseArray.length === 0) return null;
    // 1) branchName이 현재 subcatLabel을 포함하면 우선
    const hit = responseArray.find(r => String(r.branchName||"").includes(subcatLabel));
    if (hit) return hit;
    // 2) 그게 없으면 첫 번째
    return responseArray[0];
  }

  // 결과 확인(서버에 점수 계산 요청)
  async function handleRequestScore() {
    try {
      if (!qualForm || !majorForm || !attdForm) {
        setMessage("필수 항목(자격/전공/출결)을 먼저 저장하세요.");
        return;
      }
      const payload = assemblePayload();
      const data = await requestBranchScores(payload);

      // 응답 파싱(명세: BranchScoreResponse[])
      let picked = null;
      if (Array.isArray(data)) picked = pickBestMatch(data);
      else if (data && typeof data === "object") picked = data; // 단일 객체 응답도 허용
      else if (typeof data === "number") {
        // 숫자만 오면 총점만 세팅
        setTotal(Number(data));
        setMessage("점수 계산 완료");
        return;
      }

      if (!picked) {
        setMessage("점수 계산 결과가 없습니다.");
        return;
      }

      // 섹션별 점수 & 총점 반영(없으면 0으로)
      setQualScore(Number(picked.qualScore ?? 0));
      setMajorScore(Number(picked.majorScore ?? 0));
      setAttdScore(Number(picked.attendanceScore ?? 0));
      setBonusScore(Number(picked.bonusScore ?? 0));
      setTotal(Number(picked.totalDocumentScore ?? 0));
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

      {/* 4개 입력 카드(각 카드 상단에 점수 표시) */}
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

      {/* 결과 카드(총점 + 내부 버튼으로 계산) */}
      <section className="section">
        <ResultCard
          branchLabel={branchLabel}
          subcatLabel={subcatLabel}
          total={total}
          onCompute={handleRequestScore}
          disabled={!qualForm || !majorForm || !attdForm}
        />
      </section>

      {/* 섹션 모달: 현재 선택한 군/세부 규칙 적용 */}
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
