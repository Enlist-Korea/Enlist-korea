// src/components/ResultCard.jsx
import React from "react";

const fmt = (n) => (Number(n) || 0).toFixed(2);

export default function ResultCard({ branchLabel, subcatLabel, total = 0, onCompute, disabled }) {
  return (
    <aside className="card result-card">
      <h3 className="section-title" style={{ marginTop: 0 }}>결과</h3>

      {/* 총점만 표기 */}
      <div className="kpi">
        <div className="value">{fmt(total)}</div>
        <div className="unit">점</div>
      </div>

      {/* 군/카테고리 표시는 유지(원하면 지워도 됨) */}
      <div className="row">
        <span>군</span>
        <strong>{branchLabel || "-"}</strong>
      </div>
      <div className="row">
        <span>카테고리</span>
        <strong>{subcatLabel || "-"}</strong>
      </div>

      {/* 결과 확인 버튼을 결과 카드 안으로 */}
      <div style={{ marginTop: 12 }}>
        <button
          className="btn-primary"
          type="button"
          onClick={onCompute}
          disabled={disabled}
          style={{ width: "100%" }}
        >
          결과 확인
        </button>
      </div>
    </aside>
  );
}
