// src/pages/BonusPage.jsx
import React, { useEffect, useMemo, useState } from "react";
import {
  fetchBonusRules,
  requestBranchScores,
  requestScore,
  USE_MOCK,
} from "../api/bonus";
import BranchTabs from "../components/BranchTabs";
import SubcatChips from "../components/SubcatChips";
import ResultCard from "../components/ResultCard";
import SectionModal from "../components/SectionModal";

/* ─────────────────────────────────────────────
 * 1) 병과별 입력 규칙
 * ───────────────────────────────────────────── */
const FORM_RULES = {
  육군: {
    기술행정병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "ws", "gen", "none"],
        showRelation: true,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: {
        use: true,
        show: [
          "bExp",
          "bOnline",
          "bVeteran",
          "bMulti",
          "bBenefit",
          "bBlood",
          "bVol",
          "bDispo",
          "bImmig",
          "bDrivequal",
        ],
      },
    },
    "기술행정병(연단위)": {
      qualification: {
        use: true,
        allowCategories: ["nat", "gen", "none"],
        showRelation: true,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: {
        use: true,
        show: ["bExp", "bVeteran", "bMulti", "bBenefit", "bBlood", "bVol"],
      },
    },
    전문특기병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "drive", "none"],
        showRelation: false,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: { use: true, show: ["bExp", "bVeteran", "bDrivequal"] },
    },
  },
  해군: {
    기술병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "gen", "none"],
        showRelation: true,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: {
        use: true,
        show: ["bExp", "bVeteran", "bMulti", "bBlood", "bVol"],
      },
    },
    전문특기병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "drive", "none"],
        showRelation: false,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: { use: true, show: ["bExp", "bDrivequal", "bVeteran"] },
    },
  },
  해병대: {
    기술병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "gen", "none"],
        showRelation: true,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: { use: true, show: ["bExp", "bVeteran", "bBlood", "bVol"] },
    },
    전문특기병: {
      qualification: {
        use: true,
        allowCategories: ["nat", "drive", "none"],
        showRelation: false,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: { use: true, show: ["bExp", "bDrivequal", "bVeteran"] },
    },
  },
  공군: {
    "기술병/전문특기병": {
      qualification: {
        use: true,
        allowCategories: ["nat", "gen", "none"],
        showRelation: true,
      },
      major: { use: true },
      attendance: { use: true },
      bonus: {
        use: true,
        show: ["bExp", "bVeteran", "bMulti", "bBlood", "bVol"],
      },
    },
  },
};
const getRuleFor = (branchLabel, subcatLabel) =>
  FORM_RULES?.[branchLabel]?.[subcatLabel] ?? {
    qualification: {
      use: true,
      allowCategories: ["nat", "ws", "gen", "drive", "none"],
      showRelation: true,
    },
    major: { use: true },
    attendance: { use: true },
    bonus: {
      use: true,
      show: [
        "bExp",
        "bOnline",
        "bVeteran",
        "bMulti",
        "bBenefit",
        "bBlood",
        "bVol",
        "bDispo",
        "bImmig",
        "bDrivequal",
      ],
    },
  };

/* ─────────────────────────────────────────────
 * 2) 테스트용 계산기 (팝업 저장 → 카드 점수 즉시 반영)
 *    - 백엔드 붙일 때는 USE_MOCK=false로 끄면 됨
 * ───────────────────────────────────────────── */

// (임시) 기술자격/면허 점수
const calcQualMock = (f) => {
  if (!f) return 0;
  const baseByGrade = {
    기사이상: 50,
    산업기사: 45,
    기능사: 40,
    "L6, L5": 50,
    "L4, L3": 45,
    L2: 40,
    공인: 30,
    일반: 26,

    "대형/특수": 90,
    "1종보통(수동)": 87,
    미소지: 0,
  };

  const base = baseByGrade[f.qGrade] ?? 0;

  // 운전면허일 때는 관련도에 따른 감점 없음
  const relAdj =
    f.qCategory === "drive" ? 0 : f.qRelation === "indirect" ? -5 : 0;

  return Math.max(0, Math.min(100, base + relAdj)); // 섹션 만점 100 기준(임시)
};

// (임시) 전공 점수
const calcMajorMock = (f) => {
  if (!f) return 0;
  const level =
    {
      "4학년 수료이상": 40,
      "4학년 재학": 38,
      "3학년 수료": 36,
      "3학년 재학": 34,
      "2학년 수료": 32,
      "2학년 재학": 30,
      "1학년 수료": 28,
      "1학년 재학": 26,
      "3년 수료": 40,
      "3년 재학": 38,
      "2년 수료": 36,
      "2년 재학": 34,
      고졸: 20,
      "한국폴리텍대학 2년 이상 수료": 32,
      "한국폴리텍대학 1년 이상 수료": 30,
      "직업능력개발원 6개월 이상 수료": 26,
    }[f.mLevel] ?? 0;
  const trackAdj = f.mTrack === "비전공" ? -12 : 0;
  return Math.max(0, Math.min(40, level + trackAdj)); // 섹션 만점 40
};

// (임시) 출결 점수
const calcAttdMock = (f) => {
  if (!f) return 0;
  return { 0: 5, "1-4": 4, "5-8": 3, "9+": 2 }[f.aAbsent] ?? 0; // 만점 5
};

// (임시) 가산점 점수
const calcBonusMock = (f) => {
  if (!f) return 0;
  const pick = (v) =>
    /\((\d)점/.exec(v || "")?.[1] ? Number(/\((\d)점/.exec(v)[1]) : 0;
  const sum = [
    pick(f.bExp),
    pick(f.bOnline),
    pick(f.bVeteran),
    pick(f.bMulti),
    pick(f.bBenefit),
    pick(f.bDispo),
    pick(f.bImmig),
    pick(f.bDrivequal),
    Math.min(3, pick(f.bBlood) + pick(f.bVol)), // 헌혈+봉사 합산 최대 3
  ].reduce((a, b) => a + b, 0);
  return Math.min(10, sum); // 섹션 만점 10
};

// 프론트 자격증 카테고리
const QUAL_CATEGORY_MAP = {
  nat: "NATIONAL_TECHNICAL", // 국가기술자격증
  ws: "WORK_STUDY", // 일학습병행자격증도 같은 Enum이면 이렇게
  gen: "GENERAL", // 일반자격증
  drive: "DRIVERS", // 운전면허증
  none: "NONE", // 자격 미소지
};

// 프론트 자격증 카테고리
const QUAL_TYPE_MAP = {
  nat: "국가기술자격증",
  ws: "일학습병행자격증",
  gen: "일반",
  drive: "운전면허증",
  none: "",
};

// qRelation 값
const QUAL_RELATION_LABEL = {
  direct: "직접관련",
  indirect: "간접관련",
};

function buildQualDto(form) {
  const category = QUAL_CATEGORY_MAP[form.qCategory] || "NONE";
  const typeCondition = QUAL_TYPE_MAP[form.qCategory] || "";

  // 운전면허(분류=drive)일 때: mainCondition = 자격등급, subCondition = ""
  if (form.qCategory === "drive") {
    return {
      queryGroup: "QUALIFICATION",
      category, // "DRIVERS"
      typeCondition, // "운전면허증"
      mainCondition: form.qGrade, // "대형/특수" 또는 "1종보통(수동)"
      subCondition: "",
    };
  }

  // 그 외 자격증: mainCondition = 관련도, subCondition = 자격등급
  return {
    queryGroup: "QUALIFICATION",
    category, // "NATIONAL_TECHNICAL", "GENERAL" 등
    typeCondition, // "국가기술자격증", "일반" ...
    mainCondition: QUAL_RELATION_LABEL[form.qRelation] || "직접관련",
    subCondition: form.qGrade, // "기사이상", "산업기사", "기능사" 등
  };
}

/* 학력 */

const MAJOR_CATEGORY_MAP = {
  univ4: "UNIVERSITY",
  univ3: "JUNIOR_COLLEGE_3_YEAR",
  univ2: "JUNIOR_COLLEGE_2_YEAR",
  hs: "HIGH_SCHOOL",
  kp: "KP_SCHOOL",
  credit: "CREDIT_BANK",
};

// "4학년 수료이상" → main="4학년", sub="수료"
function splitAcademicLevel(levelStr) {
  const parts = levelStr.split(" ");
  return {
    main: parts[0], // "4학년"
    sub: parts[1]?.replace("이상", "") || "", // "수료"
  };
}

function buildAcademicDto(form) {
  const category = MAJOR_CATEGORY_MAP[form.mBranch] || "UNIVERSITY";
  const { main, sub } = splitAcademicLevel(form.mLevel);

  return {
    queryGroup: "ACADEMIC",
    category, // UNIVERSITY, JUNIOR_COLLEGE_3_YEAR ...
    mainCondition: main, // "4학년"
    subCondition: sub, // "수료"
  };
}

/*출결*/

const ATTEND_MAP = {
  0: "0일",
  "1-4": "1~4일",
  "5-8": "5~8일",
  "9+": "9일 이상",
};

function buildAttendanceDto(form) {
  const label = ATTEND_MAP[form.aAbsent] || "0일";

  return {
    queryGroup: "ATTENDANCE",
    attendanceCount: label,
  };
}

// src/pages/BonusPage.jsx 파일의 수정된 DTO 빌더 함수들

/* 1) 헌혈 (BLOOD_DONATION) */
function buildBonusBloodDto(form) {
  if (!form.bBlood) return null;

  // 예: "1회 (1점)" → "1회"
  const count = form.bBlood.split(" (")[0];

  return {
    queryGroup: "BONUS",
    category: "BLOOD_DONATION",
    mainCondition: "헌혈(횟수)",
    subCondition: count,          // "1회", "2회", "3회"
  };
}

/* 2) 봉사(시간) (VOLUNTEER) */
function buildBonusVolunteerDto(form) {
  if (!form.bVol) return null;

  // 예: "8~15시간 (1점)" → "8~15시간"
  const label = form.bVol.split(" (")[0];

  return {
    queryGroup: "BONUS",
    category: "VOLUNTEER",
    mainCondition: "봉사(시간)",
    subCondition: label,          // "8~15시간", "16~23시간", "24시간 이상"
  };
}

/* 3) 다자녀 가정 자녀 (MULTIPLE_CHILDREN) */
function buildBonusMultiChildDto(form) {
  // form.bMulti: "2인 (1점)", "3인 (2점)", "4인 이상 (3점)" 중 하나
  if (!form.bMulti) return null;

  const mainCond = "다자녀 가정 자녀";

  // "4인 이상 (3점)" → "4인 이상"
  const label = form.bMulti.split(" (")[0];

  // "4인 이상" → "4인이상" → "(4인이상)"  (DB: "(4인이상)" 형태)
  const subCond = `(${label.replace(/ /g, "")})`;

  return {
    queryGroup: "BONUS",
    category: "MULTIPLE_CHILDREN",
    mainCondition: mainCond,
    subCondition: subCond,        // "(2인)", "(3인)", "(4인이상)"
  };
}

/* 4) 모집특기 경력 (SPECIALTY_EXPERIENCE) */
function buildBonusSpecialtyExperience(form) {
  if (!form.bExp) return null;

  // form.bExp: "6개월~1년 미만 (1점)", "1년 이상 (2점)" 등
  const subCond = form.bExp.split(" (")[0];  // "6개월~1년 미만", "1년 이상"

  return {
    queryGroup: "BONUS",
    category: "SPECIALTY_EXPERIENCE",
    mainCondition: "모집특기 경력",
    subCondition: subCond,
  };
}

/* 5) 군 추천특기 지원자 (RECOMMEND_MILITARY) */
function buildBonusRecommendMilitary(form) {
  if (!form.bOnline) return null;

  return {
    queryGroup: "BONUS",
    category: "RECOMMEND_MILITARY",
    mainCondition: "병역진로설계 온라인서비스 군 추천특기 지원자",
    subCondition: "",
  };
}

/* 6) 국가유공자 자녀 / 독립유공자 손·자녀 (CHILDREN_OF_NATIONAL) */
function buildBonusChildrenOfNational(form) {
  if (!form.bVeteran) return null;

  return {
    queryGroup: "BONUS",
    category: "CHILDREN_OF_NATIONAL",
    mainCondition: "국가유공자 자녀 또는 독립유공자 손·자녀",
    subCondition: "",
  };
}

/* 7) 수급권자 (BENEFICIARY) */
function buildBonusBeneficiary(form) {
  if (!form.bBenefit) return null;

  return {
    queryGroup: "BONUS",
    category: "BENEFICIARY",
    mainCondition: "국민기초생활보장법 제7조제1항제1호에 따른 생계급여 수급권자",
    subCondition: "",
  };
}

/* 8) 현역병입영대상 판정자 (ELIGIBLE_ACTIVE_DUTY) */
function buildBonusEligibleActiveDuty(form) {
  if (!form.bDispo) return null;

  return {
    queryGroup: "BONUS",
    category: "ELIGIBLE_ACTIVE_DUTY",
    mainCondition: "질병치료에 따른 병역처분변경으로 현역병입영대상 판정자",
    subCondition: "",
  };
}

/* 9) 국외이주자 중 현역병복무지원자 (IMMIGRANTS_ACTIVE_DUTY) */
function buildBonusImmigrantsActiveDuty(form) {
  if (!form.bImmig) return null;   // ← 오타 주의: bImming 아님!

  return {
    queryGroup: "BONUS",
    category: "IMMIGRANTS_ACTIVE_DUTY",
    mainCondition: "국외이주자 중 현역병복무지원자",
    subCondition: "",
  };
}

/* 10) 군운전적성정밀검사 합격자 (DRIVING_APTITUDE_TEST) */
function buildBonusDrivingAptitudeTest(form) {
  if (!form.bDrivequal) return null;

  return {
    queryGroup: "BONUS",
    category: "DRIVING_APTITUDE_TEST",
    mainCondition: "군운전적성정밀검사 합격자",
    subCondition: "",
  };
}


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
  const [qualForm, setQualForm] = useState(null);
  const [majorForm, setMajorForm] = useState(null);
  const [attdForm, setAttdForm] = useState(null);
  const [bonusForm, setBonusForm] = useState(null);

  const [qualScore, setQualScore] = useState(0);
  const [majorScore, setMajorScore] = useState(0);
  const [attdScore, setAttdScore] = useState(0);
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
      const b = data.branches.find((x) => x.id === qBranch) || first;
      setBranch(b?.id || "");
      const sList = b?.subcats || [];
      const s = sList.find((x) => x.id === qSubcat) || sList[0];
      setSubcat(s?.id || "");
    });
  }, []);

  const currentBranch = useMemo(
    () => branches.find((b) => b.id === branch) || { label: "", subcats: [] },
    [branches, branch],
  );
  useEffect(() => {
    if (currentBranch.subcats?.length) setSubcat(currentBranch.subcats[0].id);
  }, [branch]);
  const majorDisabled = qualForm?.qCategory === "drive";

  const subcatLabel =
    currentBranch.subcats.find((s) => s.id === subcat)?.label || "";
  const branchLabel = currentBranch.label || "";

  // 모달 제어
  const openSec = (sec) => {
    setModalSection(sec);
    setModalOpen(true);
  };
  const closeSec = () => {
    setModalOpen(false);
    setModalSection(null);
  };

  // 팝업 저장 → 해당 카드 점수 즉시 반영
  // 팝업 저장 → 해당 카드 점수 즉시 반영
const handleSave = async (section, form) => {
  // 🔹 기술자격/면허
  if (section === "qual") {
    setQualForm(form);

    if (USE_MOCK) {
      setQualScore(calcQualMock(form));
    } else {
      try {
        const dto   = buildQualDto(form);
        const score = await requestScore(dto);
        setQualScore(Number(score) || 0);
      } catch (e) {
        console.error(e);
        setMessage("자격증 점수 조회 중 오류가 발생했습니다.");
      }
    }
  }

  // 🔹 전공
  if (section === "major") {
    setMajorForm(form);

    if (USE_MOCK) {
      setMajorScore(calcMajorMock(form));
    } else {
      try {
        const dto   = buildAcademicDto(form);
        const score = await requestScore(dto);
        setMajorScore(Number(score) || 0);
      } catch (e) {
        console.error(e);
      }
    }
  }

  // 🔹 출결
  if (section === "attd") {
    setAttdForm(form);

    if (USE_MOCK) {
      setAttdScore(calcAttdMock(form));
    } else {
      try {
        const dto   = buildAttendanceDto(form);
        const score = await requestScore(dto);
        setAttdScore(Number(score) || 0);
      } catch (e) {
        console.error(e);
      }
    }
  }

  // 🔹 가산점
  if (section === "bonus") {
    setBonusForm(form);

    if (USE_MOCK) {
      // 프론트 목 계산기 (이미 헌혈+봉사 3점 / 총 10점 캡 포함)
      setBonusScore(calcBonusMock(form));
    } else {
      // DTO 들 생성
      const dtoList = [
        buildBonusBloodDto(form),
        buildBonusVolunteerDto(form),
        buildBonusSpecialtyExperience(form),
        buildBonusRecommendMilitary(form),
        buildBonusChildrenOfNational(form),
        buildBonusMultiChildDto(form),
        buildBonusBeneficiary(form),
        buildBonusEligibleActiveDuty(form),
        buildBonusImmigrantsActiveDuty(form),
        buildBonusDrivingAptitudeTest(form),
      ].filter(Boolean);

      let bloodVolTotal = 0; // 헌혈 + 봉사 합
      let otherTotal    = 0; // 나머지 가산점 합

      for (const dto of dtoList) {
        try {
          const s = Number(await requestScore(dto)) || 0;

          if (dto.category === "BLOOD_DONATION" || dto.category === "VOLUNTEER") {
            bloodVolTotal += s;
          } else {
            otherTotal += s;
          }
        } catch (e) {
          console.error("가산점 조회 오류:", e, dto);
        }
      }

      // 헌혈+봉사 최대 3점
      const cappedBloodVol = Math.min(3, bloodVolTotal);
      // 전체 가산점 최대 10점
      const totalBonus = Math.min(10, otherTotal + cappedBloodVol);

      setBonusScore(totalBonus);
    }
  }

  closeSec();
};


  // 결과 확인(총점 계산)
  async function handleRequestScore() {
    // 백엔드 준비 전: 테스트용 합산(소계 합)
    if (USE_MOCK) {
      setTotal(
        (Number(qualScore) || 0) +
          (Number(majorScore) || 0) +
          (Number(attdScore) || 0) +
          (Number(bonusScore) || 0),
      );
      setMessage("테스트 합산 완료");
      return;
    }

    // === 백엔드 호출(준비 완료 시) ===
    try {
      // 출결 구간을 백엔드 기대값으로 변환(예시)
      const absences =
        attdForm?.aAbsent === "0"
          ? 0
          : attdForm?.aAbsent === "1-4"
          ? 4
          : attdForm?.aAbsent === "5-8"
          ? 8
          : 9;

      // 가산 선택 단순화
      const bonusSelected = [];
      const pushIf = (key, labelText) => {
        const v = bonusForm?.[key];
        if (v && v !== "")
          bonusSelected.push({ category: key, label: v || labelText });
      };
      [
        "bExp",
        "bOnline",
        "bVeteran",
        "bMulti",
        "bBenefit",
        "bBlood",
        "bVol",
        "bDispo",
        "bImmig",
        "bDrivequal",
      ].forEach((k) => pushIf(k, k));

      const payload = {
        qualificationLabel: qualForm?.qGrade || "",
        majorTrack: majorForm?.mTrack || "전공",
        majorLevel: majorForm?.mLevel || "",
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
      else if (Array.isArray(data))
        next = Number(data[0]?.total ?? data[0]?.score ?? 0);
      else if (data && typeof data === "object")
        next = Number(data.total ?? data.score ?? 0);
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
          {message && (
            <p className="desc" style={{ color: "#cbd6ff" }}>
              {message}
            </p>
          )}
        </div>
        <button className="btn-reset" onClick={() => window.location.reload()}>
          초기화
        </button>
      </div>

      {/* 군 분류 */}
      <section className="section">
        <h3 className="section-title">군 분류</h3>
        <BranchTabs branches={branches} value={branch} onChange={setBranch} />
      </section>

      {/* 세부 카테고리 */}
      <section className="section">
        <h3 className="section-title">세부 카테고리</h3>
        <SubcatChips
          items={currentBranch.subcats}
          value={subcat}
          onChange={setSubcat}
        />
      </section>

      {/* 4개 섹션 카드 (각 카드 상단에 소계 점수) */}
      <section className="section grid cards-2x2">
        <div className="card">
          <h3
            className="section-title"
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span>기술자격/면허</span>
            <div className="kpi">
              <div className="value">{qualScore.toFixed(2)}</div>
              <div className="unit">점</div>
            </div>
          </h3>
          <p className="desc">자격 분류/등급/관련도를 입력하세요.</p>
          <button
            className="btn-primary"
            style={{ width: "100%" }}
            onClick={() => openSec("qual")}
          >
            입력하기
          </button>
        </div>

        <div className="card">
          <h3
            className="section-title"
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span>전공</span>
            <div className="kpi">
              <div className="value">{majorScore.toFixed(2)}</div>
              <div className="unit">점</div>
            </div>
          </h3>
          <p className="desc">전공/학력 상태를 선택하세요.</p>
          <button
            className="btn-primary"
            style={{ width: "100%" }}
            onClick={() => openSec("major")}
            disabled={qualForm?.qCategory === "drive"}
          >
            입력하기
          </button>
        </div>

        <div className="card">
          <h3
            className="section-title"
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span>출결사항</span>
            <div className="kpi">
              <div className="value">{attdScore.toFixed(2)}</div>
              <div className="unit">점</div>
            </div>
          </h3>
          <p className="desc">결석 일수 구간을 선택하세요.</p>
          <button
            className="btn-primary"
            style={{ width: "100%" }}
            onClick={() => openSec("attd")}
          >
            입력하기
          </button>
        </div>

        <div className="card">
          <h3
            className="section-title"
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span>가산점</span>
            <div className="kpi">
              <div className="value">{bonusScore.toFixed(2)}</div>
              <div className="unit">점</div>
            </div>
          </h3>
          <p className="desc">해당되는 가산 항목을 선택하세요.</p>
          <button
            className="btn-primary"
            style={{ width: "100%" }}
            onClick={() => openSec("bonus")}
          >
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
          modalSection === "qual"
            ? qualForm
            : modalSection === "major"
            ? majorForm
            : modalSection === "attd"
            ? attdForm
            : modalSection === "bonus"
            ? bonusForm
            : null
        }
        branchLabel={branchLabel}
        subcatLabel={subcatLabel}
      />
    </div>
  );
}
