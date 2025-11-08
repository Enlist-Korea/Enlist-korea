// src/components/BonusModal.jsx
// [역할]
// - 모달 열릴 때 /api/score/options 로 선택지 로드
// - 폼 입력 후 '점수 계산 요청' 클릭 시 /api/score/branches 로 제출
// - 성공하면 onSuccess(data) 호출 → 부모(BonusPage)에서 메시지/점수 갱신

import React, { useEffect, useRef, useState } from "react";
import { getScoreOptions, requestBranchScores } from "../api/bonus";

export default function BonusModal({ open, onClose, onSuccess, onError }) {
  const backdropRef = useRef(null);

  // 로딩/에러/옵션 상태
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");
  const [options, setOptions] = useState({ qualifications: [], majors: [] });

  // 폼 입력값 상태
  const [form, setForm] = useState({
    qualificationLabel: "",
    majorTrack: "전공",
    majorLevel: "",
    absences: 0,
    bonusSelected: [], // 추후 체크박스/셀렉트로 확장
  });

  // 모달 오픈/닫힘 시 바디 스크롤 제어
  useEffect(() => {
    document.body.classList.toggle("no-scroll", !!open);
    return () => document.body.classList.remove("no-scroll");
  }, [open]);

  // 모달 열릴 때 옵션 로드
  useEffect(() => {
    if (!open) return;
    setLoading(true);
    setErr("");
    getScoreOptions()
      .then((data) => setOptions({
        qualifications: data.qualifications ?? [],
        majors: data.majors ?? [],
      }))
      .catch((e) => { setErr("선택지 로드 실패"); onError?.(e); })
      .finally(() => setLoading(false));
  }, [open]);

  if (!open) return null;

  const close = () => onClose?.();
  const onBackdrop = (e) => { if (e.target === backdropRef.current) close(); };

  const setVal = (k) => (e) => {
    const v = e.target.type === "number" ? Number(e.target.value || 0) : e.target.value;
    setForm((s) => ({ ...s, [k]: v }));
  };

  // 제출 → 백엔드 계산 요청
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        qualificationLabel: form.qualificationLabel,
        majorTrack: form.majorTrack,
        majorLevel: form.majorLevel,
        absences: Number(form.absences) || 0,
        bonusSelected: form.bonusSelected,
      };
      const data = await requestBranchScores(payload);
      onSuccess?.(data); // 부모에서 점수/메시지 반영
      close();
    } catch (e2) {
      setErr("점수 계산 요청 실패");
      onError?.(e2);
    }
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
          <div id="modalTitle" className="modal-title">가산 항목 입력</div>
          <button className="modal-close" type="button" aria-label="닫기" onClick={close}>×</button>
        </div>

        <div className="modal-body">
          {loading ? (
            <div>불러오는 중…</div>
          ) : err ? (
            <div style={{ color: "salmon" }}>{err}</div>
          ) : (
            <form className="form" onSubmit={handleSubmit}>
              {/* 자격증/면허 */}
              <label className="field">
                <span className="label">자격증 선택</span>
                <select className="select" value={form.qualificationLabel} onChange={setVal("qualificationLabel")}>
                  <option value="">선택하세요</option>
                  {options.qualifications.map((q) => <option key={q} value={q}>{q}</option>)}
                </select>
              </label>

              {/* 전공/경력 */}
              <label className="field">
                <span className="label">전공 트랙</span>
                <select className="select" value={form.majorTrack} onChange={setVal("majorTrack")}>
                  <option value="전공">전공</option>
                  <option value="비전공">비전공</option>
                </select>
              </label>
              <label className="field">
                <span className="label">전공/학력 선택</span>
                <select className="select" value={form.majorLevel} onChange={setVal("majorLevel")}>
                  <option value="">선택하세요</option>
                  {options.majors.map((m) => <option key={m} value={m}>{m}</option>)}
                </select>
              </label>

              {/* 출결 */}
              <label className="field">
                <span className="label">결석 일수</span>
                <input className="input" type="number" min="0" value={form.absences} onChange={setVal("absences")} />
              </label>

              {/* 제출 버튼 */}
              <div style={{ gridColumn: "1 / -1", display:"flex", gap:8, justifyContent:"flex-end" }}>
                <button className="btn" type="button" onClick={close}>취소</button>
                <button className="btn primary" type="submit" disabled={!form.qualificationLabel || !form.majorLevel}>
                  점수 계산 요청
                </button>
              </div>
            </form>
          )}
        </div>

        <div className="modal-footer">
          <span className="desc">제출 시 서버에 계산 요청이 전달됩니다.</span>
        </div>
      </div>
    </div>
  );
}
