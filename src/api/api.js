import { XMLParser } from 'fast-xml-parser'; // XML 문자열을 JS 객체로 변환해주는 라이브러리
import { mockRecuritments } from '../data/MockData';

// 비동기 함수로 호출하여 API에서 데이터를 가져오는 작업 시작
export const fetchRecruitments = async () => {
  // 공공데이터 화재로 인한 mockData 사용
  // API 호출 코드 삭제

  console.log('로컬 Mock 데이터에서 탐색중...');

  return mockRecuritments;
};
