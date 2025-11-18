// src/components/SectionModal.jsx
import React, { useEffect, useRef, useState } from "react";

/* 자격 분류 → 등급 목록 */
const GRADES = {
  nat:   ["기사이상", "산업기사", "기능사"],
  ws:    ["L6, L5", "L4, L3", "L2"],
  gen:   ["공인", "일반"],
  drive: ["1종 대형/특수", "기타(차량운전분야 특기 한함)"],
  none:  ["미소지"],
};

/* 섹션별 기본 폼 초기값 */
const DEFAULT_FORMS = {
  qual:  { qCategory: "nat", qGrade: "기사이상", qRelation: "direct" },
  major: { mTrack: "전공", mLevel: "4학년 수료이상" },
  attd:  { aAbsent: "0" },
  bonus: {
    bExp: "", bOnline: "", bVeteran: "", bMulti: "", bBenefit: "",
    bBlood: "", bVol: "", bDispo: "", bImmig: "", bDrivequal: "",
  },
};

export default function SectionModal({
  open,
  section,       // "qual" | "major" | "attd" | "bonus"
  onClose,
  onSave,        // (section, form) => void
  initial,       // 기존 저장값
  rule = {       // 부모에서 계산해 내려주는 규칙
    qualification: { use: true, allowCategories: ["nat","ws","gen","drive","none"], showRelation: true },
    major:        { use: true },
    attendance:   { use: true },
    bonus:        { use: true, show: ["bExp","bOnline","bVeteran","bMulti","bBenefit","bBlood","bVol","bDispo","bImmig","bDrivequal"] }
  },
}) {
  const backdropRef = useRef(null);

  // 1) 폼 상태
  const [form, setForm] = useState(DEFAULT_FORMS[section] || {});

  // 2) 모달 열림/섹션 변경 시 폼 초기화
  useEffect(() => {
    const base = DEFAULT_FORMS[section] || {};
    setForm({ ...base, ...(initial || {}) });
  }, [open, section, initial]);

  // 3) body 스크롤 잠금
  useEffect(() => {
    document.body.classList.toggle("no-scroll", !!open);
    return () => document.body.classList.remove("no-scroll");
  }, [open]);

  // 4) 자격 분류 변경 시 등급 동기화
  useEffect(() => {
    if (section !== "qual") return;
    const list = GRADES[form.qCategory] || [];
    if (list.length && !list.includes(form.qGrade)) {
      setForm((s) => ({ ...s, qGrade: list[0] }));
    }
  }, [section, form.qCategory, form.qGrade]);

  if (!open) return null;

  const close = () => onClose?.();
  const onBackdrop = (e) => { if (e.target === backdropRef.current) close(); };
  const setVal = (k) => (e) => setForm((s) => ({ ...s, [k]: e.target.value }));
  const handleSave = () => onSave?.(section, form);

  const qualRule   = rule.qualification || {};
  const allowedCats = qualRule.allowCategories || ["nat","ws","gen","drive","none"];
  const showRelation = !!qualRule.showRelation;

  const bonusRule = rule.bonus || {};
  const bonusShow = bonusRule.show || [
    "bExp","bOnline","bVeteran","bMulti","bBenefit",
    "bBlood","bVol","bDispo","bImmig","bDrivequal",
  ];

  // 섹션 UI (필요한 섹션만 조건부로 렌더)
  const renderQual = () => (!qualRule.use ? null : (
    <section className="subcard">
      <h4 style={{ margin: "0 0 10px" }}>
        기술자격/면허 <span className="badge"><strong>배점</strong> 50</span>
      </h4>
      <div className="form">
        <label className="field">
          <span className="label">분류</span>
          <select className="select" value={form.qCategory} onChange={setVal("qCategory")}>
            {allowedCats.includes("nat")   && <option value="nat">국가기술자격증</option>}
            {allowedCats.includes("ws")    && <option value="ws">일학습병행자격증</option>}
            {allowedCats.includes("gen")   && <option value="gen">일반자격증</option>}
            {allowedCats.includes("drive") && <option value="drive">운전면허(차량운전분야 특기 한함)</option>}
            {allowedCats.includes("none")  && <option value="none">자격증 미소지</option>}
          </select>
        </label>

        <label className="field">
          <span className="label">자격등급</span>
          <select className="select" value={form.qGrade} onChange={setVal("qGrade")}>
            {(GRADES[form.qCategory] || []).map((g) => (
              <option key={g} value={g}>{g}</option>
            ))}
          </select>
        </label>

        {showRelation && (
          <label className="field">
            <span className="label">관련도</span>
            <select className="select" value={form.qRelation} onChange={setVal("qRelation")}>
              <option value="direct">직접관련</option>
              <option value="indirect">간접관련</option>
            </select>
          </label>
        )}
      </div>
      <p className="help">* 분류를 바꾸면 자격등급 목록이 바뀝니다.</p>
    </section>
  ));

  const renderMajor = () => (!(rule.major?.use) ? null : (
    <section className="subcard">
      <h4 style={{ margin: "0 0 10px" }}>
        전공 <span className="badge"><strong>배점</strong> 40</span>
      </h4>
      <div className="form">
        <label className="field">
          <span className="label">트랙</span>
          <select className="select" value={form.mTrack} onChange={setVal("mTrack")}>
            <option value="전공">전공</option>
            <option value="비전공">비전공</option>
          </select>
        </label>

        <label className="field">
          <span className="label">학력/상태</span>
          <select className="select" value={form.mLevel} onChange={setVal("mLevel")}>
            <optgroup label="대학교">
              {["4학년 수료이상","4학년 재학","3학년 수료","3학년 재학",
                "2학년 수료","2학년 재학","1학년 수료","1학년 재학"]
                .map(v => <option key={v} value={v}>{v}</option>)}
            </optgroup>
            <optgroup label="전문대">
              {["3년 수료","3년 재학","2년 수료","2년 재학"]
                .map(v => <option key={v} value={v}>{v}</option>)}
            </optgroup>
            <optgroup label="기타">
              {["고졸","한국폴리텍대학 2년 이상 수료",
                "한국폴리텍대학 1년 이상 수료","직업능력개발원 6개월 이상 수료"]
                .map(v => <option key={v} value={v}>{v}</option>)}
            </optgroup>
          </select>
        </label>
      </div>
      <p className="help">* 점수 계산은 서버에서 처리됩니다(선택값만 전달).</p>
    </section>
  ));

  const renderAttd = () => (!(rule.attendance?.use) ? null : (
    <section className="subcard">
      <h4 style={{ margin: "0 0 10px" }}>
        출결상황 <span className="badge"><strong>배점</strong> 5</span>
      </h4>
      <div className="form">
        <label className="field">
          <span className="label">결석 일수 구간</span>
          <select className="select" value={form.aAbsent} onChange={setVal("aAbsent")}>
            <option value="0">0일 (5점)</option>
            <option value="1-4">1~4일 (4점)</option>
            <option value="5-8">5~8일 (3점)</option>
            <option value="9+">9일 이상 (2점)</option>
          </select>
        </label>
      </div>
      <p className="help">* 중·고교 3년 누계 기준.</p>
    </section>
  ));

  const renderBonus = () => {
    if (!rule.bonus?.use) return null;
    const fields = [
      ["bExp","모집특기 경력",["","6개월 이상~1년 미만 (1점)","1년 이상 (2점)"]],
      ["bOnline","온라인서비스 군 추천특기 지원자",["","해당 (1점)"]],
      ["bVeteran","국가유공자 자녀·독립유공자 손자녀",["","해당 (3점)"]],
      ["bMulti","다자녀 가정 자녀",["","2인 (1점)","3인 (2점)","4인 이상 (3점)"]],
      ["bBenefit","생계급여 수급권자",["","해당 (3점)"]],
      ["bBlood","헌혈(최근 3년)",["","1회 (1점)","2회 (2점)","3회 (3점)"]],
      ["bVol","봉사(최근 3년)",["","8~15시간 (1점)","16~23시간 (2점)","24시간 이상 (3점)"]],
      ["bDispo","병역처분변경으로 현역입영대상 판정",["","해당 (3점)"]],
      ["bImmig","국외이주자 중 현역병무지원자",["","해당 (3점)"]],
      ["bDrivequal","군 운전적성검정 합격자",["","해당 (2점)"]],
    ].filter(([key]) => (rule.bonus.show || []).includes(key));

    return (
      <section className="subcard">
        <h4 style={{ margin: "0 0 10px" }}>
          가산점 <span className="badge"><strong>배점</strong> 최대 10</span>
        </h4>
        <div className="form">
          {fields.map(([key, label, opts]) => (
            <label key={key} className="field">
              <span className="label">{label}</span>
              <select className="select" value={form[key]} onChange={setVal(key)}>
                {opts.map((o) => <option key={o} value={o}>{o || "없음"}</option>)}
              </select>
            </label>
          ))}
        </div>
        <p className="help">* 헌혈/봉사는 통합 최대 3점, 가산점 총합 최대 10점.</p>
      </section>
    );
  };

  return (
    <div
      ref={backdropRef}
      className="modal-backdrop is-open"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modalTitle"
      onClick={onBackdrop}
    >
      <div className="modal">
        <div className="modal-header">
          <div id="modalTitle" className="modal-title">
            {section === "qual"  && "기술자격/면허 입력"}
            {section === "major" && "전공 입력"}
            {section === "attd"  && "출결상황 입력"}
            {section === "bonus" && "가산점 입력"}
          </div>
          <button className="modal-close" type="button" aria-label="닫기" onClick={close}>×</button>
        </div>

        <div className="modal-body">
          {section === "qual"  && renderQual()}
          {section === "major" && renderMajor()}
          {section === "attd"  && renderAttd()}
          {section === "bonus" && renderBonus()}
        </div>

        <div className="modal-footer">
          <button className="btn" type="button" onClick={close}>취소</button>
          <button className="btn primary" type="button" onClick={handleSave}>저장</button>
        </div>
      </div>
    </div>
  );
}
