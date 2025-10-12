// --- import ---
import { XMLParser } from "fast-xml-parser"; // XML 문자열을 JS 객체로 변환해주는 라이브러리
import { mockRecuritments } from "../data/MockData";

// 현재 API로부터 데이터를 직접 받지만 추후 DB에서 JSON으로 받는 기능 구현 예정

/*
 * 외부 API와의 통신을 담당
 * 여러 페이지로 나뉜 데이터를 순회하며 모두 가져오는 로직을 처리
 * XML 형식의 데이터를 JavaScript 객체로 파싱
 */

// API 요청을 보낼 기본 URL과 개인 인증키
// 관리 용이하게 하기 위하여 상수로 선언
const API_BASE_URL = "/api/1300000/MJBGJWJeopSuHH4/list";
const SERVICE_KEY =
  "f74462b64e89c4a27846d3e86dec2bfcc0d6f99428fc89523a70ba5aee0fbe60";

// 비동기 함수로 호출하여 API에서 데이터를 가져오는 작업 시작
export const fetchRecruitments = async () => {
  //const MAX_PAGES_TO_SEARCH = 200; // 최대 200페이지까지 조회 시도
  //let allItems = []; // 모든 페이지에서 가져온 데이터를 합칠 배열

  // 공공데이터 화재로 인한 mockData 사용
  /*
  // 1페이지부터 최대 페이지까지 반복
  for (
    let pageNo = 1;
    pageNo <= MAX_PAGES_TO_SEARCH;
    pageNo++
  ) {
    console.log(`🔍 ${pageNo} 페이지의 공고 탐색 중...`);
    const url = `${API_BASE_URL}?serviceKey=${SERVICE_KEY}&pageNo=${pageNo}&numOfRows=300`;

    try {
      // fetch 함수로 해당 URL에 GET 요청을 보냄
      const response = await fetch(url);

      // 응답이 성공적이지 않으면 에러를 출력하고 다음 페이지로 넘어감
      if (!response.ok) {
        console.error(
          `HTTP 에러! 상태: ${response.status}`,
        );
        continue;
      }

      // 응답 본문을 XML 텍스트로 변환
      const xmlText = await response.text();
      const parser = new XMLParser();
      const jsonObj = parser.parse(xmlText); // XML을 JSON 객체로 파싱
      const items = jsonObj.response?.body?.items?.item; // 실제 데이터가 있는 위치

      // 해당 페이지에 데이터가 없으면 반복을 중단
      if (!items) {
        console.warn(
          `${pageNo} 페이지에 데이터가 없습니다. 탐색을 종료합니다.`,
        );
        break;
      }

      // API 응답에서 item이 하나일 경우 객체로, 여러 개일 경우 배열로 오기 때문에
      // 항상 배열로 처리하기 위해 Array.isArray로 확인 후 처리
      const itemsArray = Array.isArray(items)
        ? items
        : [items];

      // 현재 페이지의 데이터를 전체 데이터 배열에 추가
      allItems = [...allItems, ...itemsArray];

      // API 응답에 포함된 전체 데이터 수를 확인해서 마지막 페이지인지 검사
      const totalCount = jsonObj.response?.body?.totalCount;
      const numOfRows = jsonObj.response?.body?.numOfRows;
      if (
        totalCount &&
        numOfRows &&
        pageNo * numOfRows >= totalCount
      ) {
        console.log("모든 페이지 탐색 완료.");
        break; // 마지막 페이지까지 다 읽었으면 반복 중단
      }
    } catch (error) {
      console.error(
        `${pageNo} 페이지를 가져오는 데 실패했습니다:`,
        error,
      );
      continue; // 에러가 발생하면 다음 페이지로 넘어감
    }
      
  }

  // 최종적으로 수집된 데이터가 하나도 없으면 경고 메시지 출력
  if (allItems.length === 0) {
    console.warn(
      "최대 페이지까지 탐색했지만 공고를 찾지 못했습니다.",
    );
  }
  */

  console.log("로컬 Mock 데이터에서 탐색중...");

  // 각 아이템에 고유한 id 값을 부여 (rnum 값을 id로 사용)
  // 나중에 React가 목록을 효율적으로 관리하기 위해 필요 (key prop)
  // return allItems.map((item) => ({
  //   ...item,
  //   id: item.rnum,
  // }));

  return mockRecuritments;
};
