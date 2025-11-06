import { Link } from "react-router-dom";
import {
  formatDate,
  getRecruitmentStatus,
  formatYearMonth,
} from "../utils/dateUtils";

// 텍스트를 코드로 번역해줄 명단(MAP) 객체 생성
// "육군" 같은 한글 텍스트를 "1"과 같은 코드로 변환하기 위해 사용합니다.
// 이 코드는 '자세히 보기' 링크를 통해 DetailPage로 전달됩니다.
const GUN_CODE_MAP = {
  육군: "1",
  해군: "2",
  공군: "3",
  해병대: "4",
};

// "기술행정병"을 "1"과 같은 코드로 변환합니다.
// API 데이터에 '전문특기병' 같은 다른 모집 구분이 있다면 여기에 추가
const MOJIP_CODE_MAP = {
  기술행정병: "1",
  // 예: '전문특기병': '2'
  전문기술병: "1", // MockData에 '전문기술병'이 있어서 추가했습니다.
};

/**
 * 경쟁률 문자열(예: '2.0:1' 또는 '2')을 숫자(예: 2.0)로 변환합니다.
 * @param {string | number | undefined} rateStr - API에서 받은 경쟁률 문자열
 * @returns {number} 소수점을 포함한 경쟁률 숫자. 유효하지 않으면 0을 반환.
 */
const parseRate = (rateStr) => {
  if (!rateStr) return 0;
  // "2.0:1" 같은 형식일 경우 ":1" 부분을 제거하기 위해 split 사용
  const rate = parseFloat(String(rateStr).split(":")[0]);
  // 숫자로 변환 실패 시(NaN) 0을 반환
  return isNaN(rate) ? 0 : rate;
};

/**
 * 모집 공고 하나를 표시하는 카드 UI 컴포넌트
 * @param {{item: Object}} props - ListPage로부터 전달받은 props
 * @param {Object} props.item - 단일 모집 공고 데이터 객체
 * @returns {JSX.Element} 카드 UI를 나타내는 React 엘리먼트
 */
export const Card = ({ item }) => {
  // 모집 상태 및 남은 기간 계산
  // dateUtils.js의 함수를 사용해 "모집중", "3일 남음" 등의 텍스트를 가져옴
  const { statusText, daysRemainingText } =
    getRecruitmentStatus(
      item.jeopsuSjdtm, // 접수시작일
      item.jeopsuJrdtm, // 접수종료일
    );

  // 경쟁률 계산
  const competitionRate = parseRate(item.rate); // '2.0:1' -> 2.0
  // 경쟁률을 기반으로 ProgressBar의 너비를 계산 (최대 10:1을 100%로 설정)
  const progressBarWidth = Math.min(
    (competitionRate / 10) * 100,
    100, // 100%를 넘지 않도록 최댓값 설정
  );

  // 모집 상태(statusText)에 따라 CSS 클래스 이름을 반환
  const getTagClassName = () => {
    if (statusText === "모집중") return "recruiting";
    if (statusText === "모집마감") return "finished";
    return "upcoming"; // "모집예정" 등 그 외
  };

  // 입영월 데이터 처리
  // item.ipyeongDe ('*') 값이 우선이지만, 없거나 '*'이면 item.iyyjsijakYm 사용
  const enlistmentMonth =
    item.ipyeongDe && item.ipyeongDe !== "*"
      ? item.ipyeongDe
      : item.iyyjsijakYm;

  // 카드 UI 렌더링
  return (
    <div className="modern-card-wrapper">
      <div className="modern-card">
        {/* 카드 헤더: 군종, 모집구분, 모집상태 */}
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

        {/* 카드 제목: 특기명 */}
        <h3 className="card-title">{item.gsteukgiNm}</h3>

        {/* 카드 본문: 핵심 정보 및 상세 정보 */}
        <div className="card-body">
          {/* 핵심 스탯: 남은 기간, 경쟁률 */}
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

          {/* 경쟁률 프로그레스 바 */}
          <div className="competition-bar">
            <div
              className="competition-bar-inner"
              style={{ width: `${progressBarWidth}%` }}
            ></div>
          </div>

          {/* 상세 정보 그리드 */}
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

        {/* 카드 푸터: 자세히 보기 버튼 */}
        <div className="card-footer">
          {/* '자세히 보기' 버튼. react-router-dom의 Link 컴포넌트를 사용해 페이지 이동 */}
          <Link
            className="apply-button"
            // 클릭 시 이동할 URL 경로를 동적으로 생성
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

/**
 * 카드 내부에 '라벨: 값' 쌍을 표시하는 작은 헬퍼 컴포넌트
 * @param {{label: string, value: string | number}} props - 표시할 라벨과 값
 * @param {string} props.label - 라벨 텍스트 (예: "선발")
 * @param {string | number} props.value - 값 텍스트 (예: "25명")
 * @returns {JSX.Element}
 */
const DetailItem = ({ label, value }) => (
  <div className="detail-item">
    <span className="detail-label">{label}</span>
    <span className="detail-value">{value}</span>
  </div>
);
