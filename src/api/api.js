// --- import ---
import { XMLParser } from "fast-xml-parser";

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
  const MAX_PAGES_TO_SEARCH = 200;
  let allItems = [];

  for (
    let pageNo = 1;
    pageNo <= MAX_PAGES_TO_SEARCH;
    pageNo++
  ) {
    console.log(`🔍 ${pageNo} 페이지의 공고 탐색 중...`);
    const url = `${API_BASE_URL}?serviceKey=${SERVICE_KEY}&pageNo=${pageNo}&numOfRows=300`;

    try {
      const response = await fetch(url);

      if (!response.ok) {
        console.error(
          `HTTP 에러! 상태: ${response.status}`,
        );
        continue;
      }

      const xmlText = await response.text();
      const parser = new XMLParser();
      const jsonObj = parser.parse(xmlText);
      const items = jsonObj.response?.body?.items?.item;

      if (!items) {
        console.warn(
          `${pageNo} 페이지에 데이터가 없습니다. 탐색을 종료합니다.`,
        );
        break;
      }

      const itemsArray = Array.isArray(items)
        ? items
        : [items];

      allItems = [...allItems, ...itemsArray];

      const totalCount = jsonObj.response?.body?.totalCount;
      const numOfRows = jsonObj.response?.body?.numOfRows;
      if (
        totalCount &&
        numOfRows &&
        pageNo * numOfRows >= totalCount
      ) {
        console.log("모든 페이지 탐색 완료.");
        break;
      }
    } catch (error) {
      console.error(
        `${pageNo} 페이지를 가져오는 데 실패했습니다:`,
        error,
      );
      continue;
    }
  }

  if (allItems.length === 0) {
    console.warn(
      "최대 페이지까지 탐색했지만 공고를 찾지 못했습니다.",
    );
  }

  return allItems.map((item) => ({
    ...item,
    id: item.rnum,
  }));
};
