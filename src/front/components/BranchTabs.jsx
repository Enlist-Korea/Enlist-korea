// src/components/BranchTabs.jsx
// [역할] 상단의 군 분류 탭(육/해/해병/공)을 버튼 리스트로 렌더링
import React from 'react';

// props:
// - branches: [{id, label, subcats:[...]}, ...]
// - value: 현재 선택된 branch id
// - onChange: branch id 변경 콜백
export default function BranchTabs({ branches = [], value, onChange }) {
  return (
    <div className="grid grid-4" role="tablist" aria-label="군 분류">
      {branches.map((b) => (
        <button
          key={b.id}
          role="tab"
          aria-selected={value === b.id}
          type="button"
          className={'tab' + (value === b.id ? ' is-active' : '')}
          onClick={() => onChange?.(b.id)}
        >
          {b.label}
        </button>
      ))}
    </div>
  );
}
