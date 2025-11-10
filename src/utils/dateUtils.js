/**
 * ISO 형식의 날짜 값을 'YYYY.MM.DD' 형식의 문자열로 변환
 * @param {string | null | undefined} dateValue - ISO 형식 날짜 문자열
 * @returns {string} 'YYYY.MM.DD' 형식의 문자열. 유효하지 않은 경우 "날짜 정보 없음".
 */
export const formatDate = (dateValue) => {
  if (!dateValue) return '날짜 정보 없음';
  try {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return '날짜 정보 없음';

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
  } catch (e) {
    return '날짜 정보 없음';
  }
};

/**
 * ISO 형식의 날짜 값을 'YYYY년 MM월' 형식의 문자열로 변환
 * @param {string | null | undefined} dateValue - ISO 형식 날짜 문자열
 * @returns {string} 'YYYY년 MM월' 형식의 문자열. 유효하지 않은 경우 "정보 없음".
 */
export const formatYearMonth = (dateValue) => {
  if (!dateValue || String(dateValue) === '*') return '정보 없음';
  try {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return '정보 없음';

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${year}년 ${month}월`;
  } catch (e) {
    return '정보 없음';
  }
};

/**
 * ISO 형식의 날짜 값을 JavaScript의 Date 객체로 변환
 * 시간은 비교를 위해 자정(00:00:00)으로 설정
 * @param {string | null | undefined} dateValue - ISO 형식 날짜 문자열
 * @returns {Date | null} 변환된 Date 객체 또는 유효하지 않은 경우 null
 */
const parseDate = (dateValue) => {
  if (!dateValue) return null;

  try {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return null;

    // 시간, 분, 초, 밀리초를 0으로 설정하여 날짜만 비교
    date.setHours(0, 0, 0, 0);
    return date;
  } catch (e) {
    return null;
  }
};

/**
 * 현재 날짜를 기준으로 모집 상태(모집중, 모집예정, 모집마감)와
 * 남은 기간 텍스트를 계산하여 객체로 반환
 * @param {string | null | undefined} startDateValue - 모집 시작일 (ISO String)
 * @param {string | null | undefined} endDateValue - 모집 종료일 (ISO String)
 * @returns {{statusText: string, daysRemainingText: string}}
 */
export function getRecruitmentStatus(startDateValue, endDateValue) {
  const now = new Date();
  now.setHours(0, 0, 0, 0); // 시간은 무시하고 날짜만 비교

  const startDate = parseDate(startDateValue);
  const endDate = parseDate(endDateValue);

  if (!startDate || !endDate) {
    return {
      statusText: '모집마감',
      daysRemainingText: '정보 없음',
    };
  }

  // !! 중요: endDate는 18:00 등에 마감하므로, 날짜 비교 시 +1일 또는 시간 포함 비교 필요
  // 여기서는 parseDate가 시간을 00:00으로 맞추므로, endDate 당일도 '모집중'이 되도록
  // endDate.getTime() 대신 endDate의 날짜 기준으로 비교

  // (예: 마감일이 11월 10일이면, 11월 10일 23:59:59까지는 모집중이어야 함)
  // parseDate에서 이미 시간을 00:00:00으로 맞췄으므로 로직은 동일하게 작동
  // now (오늘 00시) < startDate (시작일 00시) -> 모집예정
  // now (오늘 00시) > endDate (마감일 00시) -> 모집마감

  if (now < startDate) {
    // 시작일 전
    return {
      statusText: '모집예정',
      daysRemainingText: '모집 예정',
    };
  }
  if (now > endDate) {
    // 마감일 지남
    return {
      statusText: '모집마감',
      daysRemainingText: '모집 마감',
    };
  }
  if (now >= startDate && now <= endDate) {
    // 모집 기간 중 (시작일 <= 오늘 <= 마감일)
    const diffTime = endDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); // 남은 날짜 계산

    if (diffDays === 0) {
      return {
        statusText: '모집중',
        daysRemainingText: '오늘 마감',
      };
    }
    return {
      statusText: '모집중',
      daysRemainingText: `${diffDays}일 남음`,
    };
  }
  return 0; // 아무것도 하지 않을 시 0 리턴
}
