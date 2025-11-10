// src/components/ResultCard.jsx
import React from "react";

const fmt = (n) => (Number(n) || 0).toFixed(2);

export default function ResultCard({
  branchLabel,
  subcatLabel,
  total = 0,
  onRequestScore,       // ← 추가
  disabled = false,     // ← 추가
  loading = false,      // ← 추가
}) {
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

      {/* 버튼을 결과 카드 내부에 배치 */}
      <div style={{ marginTop: 12, display: "flex", justifyContent: "flex-end" }}>
        <button
          className="btn primary"
          type="button"
          onClick={onRequestScore}
          disabled={disabled || loading}
          aria-busy={loading ? "true" : "false"}
        >
          {loading ? "계산 중…" : "결과 확인"}
        </button>
      </div>
    </aside>
  );
}
