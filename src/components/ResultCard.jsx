// src/components/ResultCard.jsx
// [역할] 현재 선택된 군/세부 카테고리와 총점(서버 응답 반영)을 요약해서 보여줌
import React from "react";


const fmt = (n) => (Number(n) || 0).toFixed(2);

export default function ResultCard({ branchLabel, subcatLabel, total = 0 }) {
  return (
    <aside className="card result-card">
      <h3 className="section-title" style={{ marginTop: 0 }}>결과</h3>

      <div className="kpi">
        <div className="value">{fmt(total)}</div>
        <div className="unit">점</div>
      </div>

      <div className="row">
        <span>군</span>
        <strong>{branchLabel || "-"}</strong>
      </div>
      <div className="row">
        <span>카테고리</span>
        <strong>{subcatLabel || "-"}</strong>
      </div>
    </aside>
  );
}
