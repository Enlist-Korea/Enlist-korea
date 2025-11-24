// src/components/SubcatChips.jsx
// [역할] 선택된 군(branch)에 속하는 세부 카테고리 칩 리스트
import React from 'react';

// props:
// - items: [{id, label}, ...] (현재 branch의 세부 카테고리)
// - value: 현재 선택된 subcat id
// - onChange: subcat id 변경 콜백
export default function SubcatChips({ items = [], value, onChange }) {
  return (
    <div className="chips" role="tablist" aria-label="세부 카테고리">
      {items.map((s) => (
        <button
          key={s.id}
          role="tab"
          aria-selected={value === s.id}
          type="button"
          className={'chip' + (value === s.id ? ' is-active' : '')}
          onClick={() => onChange?.(s.id)}
        >
          {s.label}
        </button>
      ))}
    </div>
  );
}
