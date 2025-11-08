// src/pages/BonusPage.jsx
// [역할]
// - 상단 탭/칩 조합 + 입력 모달 + 결과 카드로 화면을 구성
// - URL 쿼리(branch/subcat)가 있으면 그 값으로 초기 선택 상태 세팅
// - 모달 성공(onSuccess) 시 메시지/점수 상태 갱신

import React, { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchBonusRules } from "../api/bonus";
import BranchTabs from "../components/BranchTabs";
import SubcatChips from "../components/SubcatChips";
import BonusModal from "../components/BonusModal";
import ResultCard from "../components/ResultCard";

export default function BonusPage() {
  const [params] = useSearchParams();

  const [branches, setBranches] = useState([]);
  const [branch, setBranch] = useState("");
  const [subcat, setSubcat] = useState("");

  const [open, setOpen] = useState(false); // 모달
  const [message, setMessage] = useState(""); // 상단 안내
  const [score, setScore] = useState(0); // 서버 계산 결과(필요 시 반영)

  // 진입 시 군/세부 초기화 (쿼리 있으면 우선)
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
    () => branches.find((b) => b.id === branch) || { subcats: [] },
    [branches, branch]
  );

  // 군이 바뀌면 세부 카테고리를 해당 군의 첫 항목으로 리셋
  useEffect(() => {
    if (currentBranch.subcats?.length) setSubcat(currentBranch.subcats[0].id);
  }, [branch]);

  const subcatLabel =
    currentBranch.subcats.find((s) => s.id === subcat)?.label || "";

  return (
    <div className="page container">
      {/* 헤더/초기화 */}
      <div className="header">
        <div>
          <h1 className="title">군 가산점 계산</h1>
          {message && <p className="desc" style={{ color: "#cbd6ff" }}>{message}</p>}
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
        <SubcatChips items={currentBranch.subcats} value={subcat} onChange={setSubcat} />
      </section>

      {/* 입력 카드 */}
      <section className="section">
        <div className="card" style={{ gridColumn: "span 3" }}>
          <h3 className="section-title" style={{ marginTop: 0 }}>가산 항목 입력</h3>
          <p className="desc" style={{ margin: "6px 0 14px" }}>
            버튼을 눌러 팝업에서 항목을 입력하고 점수 계산을 요청하세요.
          </p>
          <button className="btn-primary" style={{ width: "100%" }} onClick={() => setOpen(true)}>
            가산 항목 입력하기
          </button>
        </div>
      </section>

      {/* 결과 카드 (현재 score 상태를 표시) */}
      <section className="section">
        <ResultCard branchLabel={currentBranch.label} subcatLabel={subcatLabel} total={score} />
      </section>

      {/* 모달: 성공 시 메시지/점수 반영 */}
      <BonusModal
        open={open}
        onClose={() => setOpen(false)}
        onSuccess={(data) => {
          // TODO: 백엔드 응답 구조에 맞게 매핑
          
          setMessage(`계산 요청 성공 (${Array.isArray(data) ? data.length : 1}개 결과)`);
          setOpen(false);
        }}
        onError={(e) => setMessage(`계산 요청 실패: ${e?.message || "알 수 없는 오류"}`)}
      />
    </div>
  );
}
