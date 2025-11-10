// import { mockRecuritments } from '../data/MockData';
import { getRecruitmentStatus } from '../utils/dateUtils';

// 백엔드 실행 시 백엔드 주소 수정 !!!!!!!!!!
const BACKEND_API_URL =
  'https://oppositional-atonalistic-shawn.ngrok-free.dev/api/recruitments/status';

/**
 * DB에서 가져온 원본 데이터를 프론트에서 사용할 수 있도록 가공하는 함수
 * @returns {Promise<object[]>} - '모집 중', '모집예정' 공고 객체만 담긴 배열을 이행하는 Promise
 */
export default async function fetchRecruitments() {
  try {
    // --- 백엔드 실행 시 아래 변수 2개 주석 풀고 실행 ---
    const response = await fetch(BACKEND_API_URL, {
      method: 'GET',
      headers: new Headers({
        'ngrok-skip-browser-warning': 'true',
      }),
    });
    const originalItems = await response.json();
    // ---------------------------------------------------

    // Mock 데이터를 가져와서 현재 날짜 기준 'status' 필드를 동적으로 추가
    // const processedItems = mockRecuritments.map((item) => {
    // --- 백엔드 실행 시 아래 1줄 주석 풀고 실행, 위에 한줄은 주석 처리 해야함 ---
    const processedItems = originalItems.map((item) => {
      // --------------------------------------------------

      // 각 item의 모집 시작일과 모집 마감일 값을 getRecruitmentStatus 함수에 전달
      // getRecruitmentStatus 함수는 계산 후 객체 반환
      const { statusText, daysRemainingText } = getRecruitmentStatus(
        item.applyStart,
        item.applyEnd,
      );

      return {
        ...item,
        status: statusText, // DB 스키마에 status 필드가 있지만, 동적 계산값으로 덮어쓰기
        daysRemainingText, // Card에서 사용하기 편하도록 추가
      };
    });

    console.log(processedItems);

    // '모집마감' 상태인 공고는 제외
    // 추후 DB 에서는 모집마감 상태인 공고는 아예 안올것
    // 임시로 모집마감 상태 허용
    // const availableNotice = processedItems.filter(
    //   (item) => item.status !== '모집마감',
    // );

    return processedItems;
  } catch (error) {
    console.error('Failed to fetch recruitments', error);
    return [];
  }
}
