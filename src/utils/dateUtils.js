/**
 * 날짜 데이터의 포맷팅(예: 20250917 -> 2025.09.17).
 * 모집 시작일과 종료일을 기준으로 현재 모집 상태(모집중, 모집마감, 모집예정)를 계산.
 */

/**
 * 날짜 값이 유효한 8자리 YYYYMMDD 형식의 문자열인지 확인하고 반환합
 * @param {string | number | null | undefined} dateValue - 변환할 날짜 값 (예: 20251031)
 * @returns {string | null} 유효한 8자리 날짜 문자열 또는 유효하지 않은 경우 null
 */
const getDataString = (dateValue) => {
  if (!dateValue) return null;
  const dateStr = String(dateValue);
  if (dateStr.length !== 8) return null;

  return dateStr;
};

/**
 * YYYYMMDD 형식의 날짜 값을 'YYYY.MM.DD' 형식의 문자열로 변환
 * @param {string | number | null | undefined} dateValue - YYYYMMDD 형식의 날짜 값
 * @returns {string} 'YYYY.MM.DD' 형식의 문자열. 유효하지 않은 경우 "날짜 정보 없음".
 */
export const formatDate = (dateValue) => {
  const dateStr = getDataString(dateValue);
  if (!dateStr) return "날짜 정보 없음";

  const year = dateStr.substring(0, 4);
  const month = dateStr.substring(4, 6);
  const day = dateStr.substring(6, 8);
  return `${year}.${month}.${day}`;
};

/**
 * YYYYMM 형식의 값을 'YYYY년 MM월' 형식의 문자열로 변환
 * @param {string | number | null | undefined} yearMonthValue - YYYYMM 형식의 값 (예: 202512)
 * @returns {string} 'YYYY년 MM월' 형식의 문자열. 유효하지 않은 경우 "정보 없음".
 */
export const formatYearMonth = (yearMonthValue) => {
  if (!yearMonthValue || String(yearMonthValue) === "*") return "정보 없음";
  const yearMonthStr = String(yearMonthValue);
  if (yearMonthStr.length !== 6) return "정보 없음";

  const year = yearMonthStr.substring(0, 4);
  const month = yearMonthStr.substring(4, 6);
  return `${year}년 ${month}월`;
};

/**
 * YYYYMMDD 형식의 날짜 값을 JavaScript의 Date 객체로 변환
 * 시간은 비교를 위해 자정(00:00:00)으로 설정
 * @param {string | number | null | undefined} dateValue - YYYYMMDD 형식의 날짜 값
 * @returns {Date | null} 변환된 Date 객체 또는 유효하지 않은 경우 null
 */
const parseDate = (dateValue) => {
  if (!dateValue) return null; // 비어있는 값인지 먼저 확인 후, 비어있으면 null 반환
  const dateStr = String(dateValue);
  if (dateStr.length !== 8) return null;

  const year = parseInt(dateStr.substring(0, 4), 10);
  const month = parseInt(dateStr.substring(4, 6), 10) - 1; // JS 월은 0부터 시작
  const day = parseInt(dateStr.substring(6, 8), 10);

  const date = new Date(year, month, day);
  // 유효하지 않은 날짜(예: 20250230)인지 확인
  if (isNaN(date.getTime())) return null;

  return date;
};

/**
 * 현재 날짜를 기준으로 모집 상태(모집중, 모집예정, 모집마감)와
 * 남은 기간 텍스트를 계산하여 객체로 반환
 * @param {string | number | null | undefined} startDateValue - 모집 시작일 (YYYYMMDD)
 * @param {string | number | null | undefined} endDateValue - 모집 종료일 (YYYYMMDD)
 * @returns {{statusText: string, daysRemainingText: string}}
 * - statusText: "모집중", "모집예정", "모집마감"
 * - daysRemainingText: "X일 남음", "오늘 마감", "모집 예정", "모집 마감", "정보 없음"
 */
export const getRecruitmentStatus = (startDateValue, endDateValue) => {
  const now = new Date(); // 새로운 Date 객체 생성
  now.setHours(0, 0, 0, 0); // 시간은 무시하고 날짜만 비교하기 위해 자정으로 설정

  const startDate = parseDate(startDateValue);
  const endDate = parseDate(endDateValue);

  // 날짜 정보가 유효하지 않으면 '모집마감'으로 처리
  if (!startDate || !endDate) {
    return {
      statusText: "모집마감",
      daysRemainingText: "정보 없음",
    };
  }

  // 현재가 시작일보다 전이면 '모집예정'
  if (now < startDate) {
    return {
      statusText: "모집예정",
      daysRemainingText: "모집 예정",
    };
    // 현재가 종료일보다 후면 '모집마감'
  } else if (now > endDate) {
    return {
      statusText: "모집마감",
      daysRemainingText: "모집 마감",
    };

    // 그 외의 경우 (시작일과 종료일 사이)는 '모집중'
  } else {
    // 남은 날짜 계산
    const diffTime = endDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays < 0)
      return {
        statusText: "모집마감",
        daysRemainingText: "모집 마감",
      };
    if (diffDays === 0)
      return {
        statusText: "모집중",
        daysRemainingText: "오늘 마감",
      };
    return {
      statusText: "모집중",
      daysRemainingText: `${diffDays}일 남음`,
    };
  }
};
