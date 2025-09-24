// --- import ---
import { Link } from "react-router-dom";
import {
  formatDate,
  getRecruitmentStatus,
  formatYearMonth,
} from "../utils/dateUtils";

/*
 * 단일 모집 공고 데이터를 시작적으로 표현하는 컴포넌트
 * props로 받은 item 데이터를 dateUtils 함수를 이용하여 가공 후 보여줌
 * LInk 컴포넌트를 통해 사용자가 클릭하면 상세 페이지로 이동하는 네비게이션 기능 제공
 */

const parseRate = (rateStr) => {
  if (!rateStr) return 0;
  const rate = parseFloat(String(rateStr).split(":")[0]);
  return isNaN(rate) ? 0 : rate;
};

export const Card = ({ item }) => {
  const { statusText, daysRemainingText } =
    getRecruitmentStatus(
      item.jeopsuSjdtm,
      item.jeopsuJrdtm,
    );
  const competitionRate = parseRate(item.rate);
  const progressBarWidth = Math.min(
    (competitionRate / 10) * 100,
    100,
  );

  const getTagClassName = () => {
    if (statusText === "모집중") {
      return "recruiting";
    }
    if (statusText === "모집마감") {
      return "finished";
    }
    return "upcoming";
  };

  const enlistmentMonth =
    item.ipyeongDe && item.ipyeongDe !== "*"
      ? item.ipyeongDe
      : item.iyyjsijakYm;

  return (
    <div className="modern-card-wrapper">
      <div className="modern-card">
        <div className="card-header">
          <div className="card-category">
            <span>{item.gunGbnm}</span>
            <span>|</span>
            <span>{item.mojipGbnm}</span>
          </div>
          <span
            className={`status-tag ${getTagClassName()}`}
          >
            {statusText}
          </span>
        </div>

        <h3 className="card-title">{item.gsteukgiNm}</h3>

        <div className="card-body">
          <div className="key-stats">
            <div className="stat-item">
              <span className="stat-value">
                {daysRemainingText}
              </span>
              <span className="stat-label">남은 기간</span>
            </div>
            <div className="stat-item">
              <span className="stat-value">
                {item.rate}
              </span>
              <span className="stat-label">경쟁률</span>
            </div>
          </div>
          <div className="competition-bar">
            <div
              className="competition-bar-inner"
              style={{ width: `${progressBarWidth}%` }}
            ></div>
          </div>
          <div className="details-grid">
            <DetailItem
              label="선발"
              value={`${item.seonbalPcnt}명`}
            />
            <DetailItem
              label="접수"
              value={`${item.jeopsuPcnt}명`}
            />
            <DetailItem
              label="접수기간"
              value={`${formatDate(
                item.jeopsuSjdtm,
              )} ~ ${formatDate(item.jeopsuJrdtm)}`}
            />
            <DetailItem
              label="입영예정"
              value={formatYearMonth(enlistmentMonth)}
            />
          </div>
        </div>

        <div className="card-footer">
          <Link
            to={`/details/${item.id}`}
            className="apply-button"
          >
            자세히 보기
          </Link>
        </div>
      </div>
    </div>
  );
};

const DetailItem = ({ label, value }) => (
  <div className="detail-item">
    <span className="detail-label">{label}</span>
    <span className="detail-value">{value}</span>
  </div>
);
