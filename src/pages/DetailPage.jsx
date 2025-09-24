// --- import ---
import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { Loader } from "../components/Loader";
import {
  formatDate,
  formatYearMonth,
} from "../utils/dateUtils";

// TODO: DetailPage 컴포넌트는 현재 임시 목업 데이터 사용중!!
// 추후 병과 별 간단 정보를 AI API를 사용하여 출력 예정
const mockAllItems = [
  {
    id: "1",
    rnum: "1",
    gunGbnm: "육군",
    mojipGbnm: "기술행정병",
    gsteukgiNm: "포병레이더",
    jeopsuSjdtm: "20250529",
    jeopsuJrdtm: "20250604",
    seonbalPcnt: "2",
    jeopsuPcnt: "2",
    rate: "1",
    iyyjsijakYm: "202509",
    ipyeongDe: "*",
  },
  {
    id: "2",
    rnum: "2",
    gunGbnm: "육군",
    mojipGbnm: "기술행정병",
    gsteukgiNm: "군사정보",
    jeopsuSjdtm: "20250529",
    jeopsuJrdtm: "20250604",
    seonbalPcnt: "14",
    jeopsuPcnt: "32",
    rate: "2.3",
    iyyjsijakYm: "202509",
    ipyeongDe: "*",
  },
];

export const DetailPage = () => {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const findItem = () => {
      const foundItem =
        mockAllItems.find((d) => d.id === id) || null;
      setTimeout(() => {
        setItem(foundItem);
        setIsLoading(false);
      }, 500);
    };
    findItem();
  }, [id]);

  if (isLoading) {
    return (
      <div
        style={{
          minHeight: "80vh",
          display: "grid",
          placeContent: "center",
        }}
      >
        <Loader />
      </div>
    );
  }

  if (!item) {
    return (
      <div className="page-container">
        <p>해당 모집병 정보를 찾을 수 없습니다.</p>
        <Link
          to="/"
          className="apply-button"
          style={{ maxWidth: "200px", marginTop: "1rem" }}
        >
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  const enlistmentMonth =
    item.ipyeongDe && item.ipyeongDe !== "*"
      ? item.ipyeongDe
      : item.iyyjsijakYm;

  return (
    <div className="page-container">
      <header
        className="page-header"
        style={{
          textAlign: "left",
          marginBottom: "1.5rem",
        }}
      >
        <h1>모집병 상세 정보</h1>
      </header>
      <div
        className="card details-card"
        style={{
          maxWidth: "1200px",
          backgroundColor: "var(--card-bg-color)",
        }}
      >
        <div className="card-header">
          <div className="card-title">
            <h3>
              {item.gunGbnm} {item.gsteukgiNm}
            </h3>
            <p>{item.mojipGbnm}</p>
          </div>
        </div>
        <div className="card-body">
          <div className="info-grid">
            <InfoItem
              icon="👤"
              label="선발인원"
              value={`${item.seonbalPcnt}명`}
            />
            <InfoItem
              icon="✅"
              label="접수인원"
              value={`${item.jeopsuPcnt}명`}
            />
            <InfoItem
              icon="📊"
              label="경쟁률"
              value={item.rate}
            />
            <InfoItem
              icon="📅"
              label="입영예정"
              value={formatYearMonth(enlistmentMonth)}
            />
            <div className="info-item full-width">
              <span className="icon">🕒</span>
              <div>
                <div className="label">접수기간</div>
                <div className="value">
                  {formatDate(item.jeopsuSjdtm)} ~{" "}
                  {formatDate(item.jeopsuJrdtm)}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          className="card-footer"
          style={{ backgroundColor: "transparent" }}
        >
          <Link to="/" className="apply-button">
            목록으로 돌아가기
          </Link>
        </div>
      </div>
    </div>
  );
};

const InfoItem = ({
  icon,
  label,
  value,
  className = "",
}) => (
  <div className={`info-item ${className}`}>
    <span className="icon">{icon}</span>
    <div>
      <div className="label">{label}</div>
      <div className="value">{value}</div>
    </div>
  </div>
);
