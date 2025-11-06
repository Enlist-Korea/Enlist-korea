import { XMLParser } from "fast-xml-parser"; // XML 문자열을 JS 객체로 변환해주는 라이브러리
import { mockRecuritments } from "../data/MockData";

// 비동기 함수로 호출하여 API에서 데이터를 가져오는 작업 시작
export const fetchRecruitments = async () => {
  // 공공데이터 화재로 인한 mockData 사용
  // API 호출 코드 삭제

  console.log("로컬 Mock 데이터에서 탐색중...");

  // 각 아이템에 고유한 id 값을 부여 (rnum 값을 id로 사용)
  // 나중에 React가 목록을 효율적으로 관리하기 위해 필요 (key prop)
  // return allItems.map((item) => ({
  //   ...item,
  //   id: item.rnum,
  // }));

  return mockRecuritments;
};
