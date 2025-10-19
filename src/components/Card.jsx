import { Link } from "react-router-dom";
import {
  formatDate,
  getRecruitmentStatus,
  formatYearMonth,
} from "../utils/dateUtils";

// 텍스트를 코드로 번역해줄 명단(MAP) 객체 생성
const GUN_CODE_MAP = {
  육군: "1",
  해군: "2",
  공군: "3",
  해병대: "4",
};

// 만약 API 데이터에 '전문특기병' 같은 다른 모집 구분이 있다면 여기에 추가
const MOJIP_CODE_MAP = {
  기술행정병: "1",
  // 예: '전문특기병': '2'
};

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
    if (statusText === "모집중") return "recruiting";
    if (statusText === "모집마감") return "finished";
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
          {/* 조회한 코드를 URL에 담아서 전달 */}
          <Link
            className="apply-button"
            to={`/details/${item.gsteukgiCd}?name=${
              item.gsteukgiNm
            }&gun=${GUN_CODE_MAP[item.gunGbnm]}&mojip=${
              MOJIP_CODE_MAP[item.mojipGbnm]
            }`}
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
